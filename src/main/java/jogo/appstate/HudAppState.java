package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

// Coisas adicionadas 1

import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.Item;
import jogo.gameobject.item.PlaceableItem;
import jogo.gameobject.item.SandBlockItem;
import jogo.util.Inventory;
import jogo.util.RecipeRegistry;
import jogo.util.RecipeSystem;
import jogo.util.Stacks;
import jogo.voxel.VoxelPalette;

import java.util.Objects;

public class HudAppState extends BaseAppState {

    private final Node guiNode;
    private final AssetManager assetManager;
    private BitmapText crosshair;

    // Coisas adicionadas 2

    private Player player;  // Referência ao jogador
    private Node hotbarNode;
    private Node inventoryNode;
    private boolean inventoryOpen = false;
    private BitmapFont font;

    private static final int SLOT_SIZE = 40;
    private static final int SLOT_SPACING = 45;
    private static final int HOTBAR_SLOTS = 10;
    private static final int INVENTORY_COLS = 10;
    private static final int INVENTORY_ROWS = 4; // 40 slots total

    private boolean craftMenuOpen = false;
    private Item selectedItem = null; // Item selecionado com T
    private Node craftingNode;

    public HudAppState(Node guiNode, AssetManager assetManager) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }


    @Override
    protected void initialize(Application app) {
        font = assetManager.loadFont("Interface/Fonts/Default.fnt");


        crosshair = new BitmapText(font, false);
        crosshair.setText("+");
        crosshair.setSize(font.getCharSet().getRenderedSize() * 2f);
        guiNode.attachChild(crosshair);
        centerCrosshair();
        System.out.println("HudAppState initialized: crosshair attached");

        // Hotbar
        hotbarNode = new Node("Hotbar");
        guiNode.attachChild(hotbarNode);
        System.out.println("HudAppState initialized: hotbarNode attached");

        // Inventário
        inventoryNode = new Node("Inventory");
        System.out.println("HudAppState initialized: inventoryNode created");

        //Sistema de Crafting
        craftingNode = new Node("Crafting");
        System.out.println("HudAppState initialized: craftingNode created");
    }

    private void centerCrosshair() {
        SimpleApplication sapp = (SimpleApplication) getApplication();
        int w = sapp.getCamera().getWidth();
        int h = sapp.getCamera().getHeight();
        float x = (w - crosshair.getLineWidth()) / 2f;
        float y = (h + crosshair.getLineHeight()) / 2f;
        crosshair.setLocalTranslation(x, y, 0);
    }

    @Override
    public void update(float tpf) {
        // keep centered (cheap)
        centerCrosshair();
        if (font == null || player == null) return; // Espera pelo player

        InputAppState input = getStateManager().getState(InputAppState.class);

        if (inventoryOpen) {
            // A interface é sempre a combinada, mas a lógica de input é que muda.
            if (craftMenuOpen) {
                updateCraftingInterface(input);
            } else{
                updateInventoryAndCrafting();
            }
        } else {
            updateHotbar();
        }

        // Se inventário está aberto → teclas movem seleção / acionam transição
        if (inventoryOpen) {
            // Navegação nas setas do Inventário (só funciona se o Crafting não estiver aberto)
            if (!craftMenuOpen) {
                if (input.consumeInventoryLeft())  moveInventorySelection("Left");
                if (input.consumeInventoryRight()) moveInventorySelection("Right");
                if (input.consumeInventoryUp())    moveInventorySelection("Up");
                if (input.consumeInventoryDown())  moveInventorySelection("Down");
            }

            // **Transição para Crafting (T e ENTER)**
            if (!craftMenuOpen) {
                // 1. T (Take): Tenta selecionar item e transiciona (implementa a transição e a seleção)
                if (input.consumeTakeRequested()) handleTakeFromInventory();
                // 2. ENTER: Transiciona diretamente para o craft sem precisar de item (satisfaz a regra)
                if (input.consumeCraftMenuRequested()) enterCraftMenu();
            }

        } else {
            // Hotbar numbers (só funciona se o inventário NÃO está aberto)
            int num = input.consumeHotbarNumber();
            if (num > 0 && num <= HOTBAR_SLOTS) {
                player.getInventory().setSelectedSlot(num - 1);
            }
        }

    }

    //Coisas novas 3

    private void drawInventorySlot(Node parentNode, Stacks stack, int slotIndex, int x, int y, ColorRGBA bgColor, boolean isSelected) {

        // 1. Draw Selection Border
        if (isSelected) {
            int border = 3;
            Quad borderQuad = new Quad(SLOT_SIZE + border * 2, SLOT_SIZE + border * 2);
            Geometry borderGeom = new Geometry("InvBorder" + slotIndex, borderQuad);

            Material borderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            // A cor do contorno é DarkGray se o slot for de inventário/crafting, mas Gray se for da Hotbar
            borderMat.setColor("Color", ColorRGBA.Black);

            // Z=-1: Behind the slot
            borderGeom.setMaterial(borderMat);
            borderGeom.setLocalTranslation(x - border, y - border, -2);
            parentNode.attachChild(borderGeom);
        }

        // Background dos Slots
        Quad quad = new Quad(SLOT_SIZE, SLOT_SIZE);
        Geometry slot = new Geometry("InvSlot" + slotIndex, quad);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", bgColor);

        slot.setMaterial(mat);
        slot.setLocalTranslation(x, y, -1);
        parentNode.attachChild(slot);

        // Ícone e quantidade do item
        if (stack != null && stack.getAmount() > 0) {
            Item item = stack.getItem();
            Texture iconTex = item.getIcon(assetManager);

            // Desenhar ícone
            if (iconTex != null) {
                Quad iconQuad = new Quad(SLOT_SIZE - 8, SLOT_SIZE - 8);
                Geometry iconGeom = new Geometry("Icon" + slotIndex, iconQuad);

                Material iconMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                iconMat.setTexture("ColorMap", iconTex);

                iconMat.setTransparent(true);
                iconMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

                iconGeom.setMaterial(iconMat);

                // Centrar o ícone no slot
                if (stack.getAmount() > 1) {
                    iconGeom.setLocalTranslation(x + 3, y + 7, 1);
                } else {
                    iconGeom.setLocalTranslation(x + 4, y + 4, 1);
                }
                parentNode.attachChild(iconGeom);
            }

            // Texto da quantidade
            if (stack.getAmount() > 1) {
                BitmapText amountText = new BitmapText(font, false);
                amountText.setText(String.valueOf(stack.getAmount()));
                amountText.setSize(font.getCharSet().getRenderedSize() * 0.8f);
                ColorRGBA textColor = ColorRGBA.Black;

                amountText.setColor(textColor);

                // Ajustar posição quando a quantidade passa de 10
                if (stack.getAmount() < 10) {
                    amountText.setLocalTranslation(x + 30, y + 15, 2);
                } else {
                    amountText.setLocalTranslation(x + 20, y + 15, 2);
                }

                parentNode.attachChild(amountText);
            }
        }
    }


    private void updateHotbar() {
        hotbarNode.detachAllChildren();

        Inventory inv = player.getInventory();
        int screenWidth = getApplication().getCamera().getWidth();
        int startX = (screenWidth - (HOTBAR_SLOTS * SLOT_SPACING)) / 2;
        int startY = 50;

        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            int x = startX + i * SLOT_SPACING;
            int y = startY;
            // Hotbar usa o slot selecionado (0-9) do inventário completo.
            boolean selected = (i == player.getInventory().getSelectedSlot());
            Stacks stack = inv.getSlot(i);

            // Desenha Slot, Borda, Ícone e Quantidade
            drawInventorySlot(hotbarNode, stack, i, x, y, ColorRGBA.Gray, selected);
        }
    }


    // NOVO MÉTODO: Combina a visualização do Inventário e do Crafting (lateral)
    private void updateInventoryAndCrafting() {
        inventoryNode.detachAllChildren();

        Inventory inv = player.getInventory();
        int screenWidth = getApplication().getCamera().getWidth();
        int screenHeight = getApplication().getCamera().getHeight();

        int invCols = INVENTORY_COLS;
        int invRows = INVENTORY_ROWS;
        int craftGridCols = 3;

        // Espaçamentos
        int innerSpacing = 40; // Espaço entre inventário e grid de craft
        int craftResultSpace = SLOT_SPACING * 3; // Espaço para a seta e o slot de resultado

        // Largura total
        int totalDisplayWidth = invCols * SLOT_SPACING + innerSpacing + craftGridCols * SLOT_SPACING + craftResultSpace + 20;

        // Posição inicial da área do inventário/crafting
        int invStartX = (screenWidth - totalDisplayWidth) / 2 + 10;
        int invStartY = 250;

        int bgWidth = totalDisplayWidth - 20;
        int bgHeight = invRows * SLOT_SPACING + 50;

        Quad bgQuad = new Quad(bgWidth, bgHeight);
        Geometry bg = new Geometry("InvBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", ColorRGBA.LightGray);
        bg.setMaterial(bgMat);
        bg.setLocalTranslation(invStartX - 10, invStartY - invRows * SLOT_SPACING + 25, -2); // Z=-2 for BG
        inventoryNode.attachChild(bg);

        //Slots do inventário
        for (int i = 0; i < inv.getSize(); i++) { // Tamanho do inventário (40 slots)
            int col = i % invCols;
            int row = i / invCols;

            int x = invStartX + col * SLOT_SPACING;
            int y = invStartY - row * SLOT_SPACING;

            // Highlight no modo Inventário.
            boolean selected = (!craftMenuOpen && i == inv.getSelectedSlot());
            Stacks stack = inv.getSlot(i);

            // Desenha Slot, Borda, Ícone e Quantidade
            drawInventorySlot(inventoryNode, stack, i, x, y, ColorRGBA.Gray, selected);
        }

        // Informação dos botões
        BitmapText title = new BitmapText(font, false);
        // O título reflete o modo atual
        String titleText = craftMenuOpen
                ? "CRAFTING MODE (ALT: Sair | T: Seleciona/Cancela | P: Coloca | ENTER: Craft)"
                : "INVENTARIO (TAB: Fechar | T: Entrar no craft com bloco | ENTER: Entrar no craft sem bloco)";

        title.setText(titleText);
        title.setSize(font.getCharSet().getRenderedSize() * 0.9f);
        title.setColor(ColorRGBA.Black);
        title.setLocalTranslation(invStartX, invStartY + 65, 0);
        inventoryNode.attachChild(title);


        // Grid de crafting
        int craftGridStartX = invStartX + invCols * SLOT_SPACING + innerSpacing;
        int craftGridStartY = invStartY - 20;

        Stacks[] grid = player.getCraftingGrid();

        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;

            int x = craftGridStartX + col * SLOT_SPACING;
            int y = craftGridStartY - row * SLOT_SPACING;

            // Highlight só se o Craft Menu estiver aberto E o slot estiver selecionado
            boolean selected = (craftMenuOpen && i == player.getSelectedCraftSlot());

            drawInventorySlot(inventoryNode, grid[i], i + 100, x, y,
                    ColorRGBA.Gray, selected); // Z=0
        }

        // Seta e resultado
        BitmapText arrow = new BitmapText(font, false);
        arrow.setText("=>");
        arrow.setSize(font.getCharSet().getRenderedSize() * 2f);
        arrow.setColor(ColorRGBA.Black);
        arrow.setLocalTranslation(craftGridStartX + craftGridCols * SLOT_SPACING + 10, craftGridStartY - SLOT_SPACING + 40, -1);
        inventoryNode.attachChild(arrow);

        // Slot de resultado
        RecipeSystem recipe = RecipeRegistry.findRecipe(grid);
        if (recipe != null) {
            int resultX = craftGridStartX + craftGridCols * SLOT_SPACING + 40;
            int resultY = craftGridStartY - SLOT_SPACING;

            Stacks resultStack = new Stacks(recipe.getResult(), recipe.getResultAmount());
            drawInventorySlot(inventoryNode, resultStack, 200, resultX, resultY,
                    ColorRGBA.Gray, true);
        }

        // Item selecionado no modo crafting
        if (craftMenuOpen && selectedItem != null) {
            SimpleApplication sapp = (SimpleApplication) getApplication();
            float mouseX = sapp.getInputManager().getCursorPosition().x;
            float mouseY = sapp.getInputManager().getCursorPosition().y;

            Stacks tempStack = new Stacks(selectedItem,
                    player.getInventory().countItem(selectedItem));

            drawInventorySlot(inventoryNode, tempStack, -1,
                    (int)mouseX - SLOT_SIZE / 2, (int)mouseY - SLOT_SIZE / 2, // Centrado no cursor
                    ColorRGBA.White.mult(0.7f), false); // Z=10 para ficar por cima de tudo
        }
    }


    private void updateCraftingInterface(InputAppState input) {
        // ALT → Sai do menu de craft
        if (input.consumeExitCraftRequested()) {
            exitCraftMenu();
            updateInventoryAndCrafting();
            return;
        }

        // T → Seleciona/Desseleciona item (agora significa cancelar a seleção de tipo)
        if (input.consumeTakeRequested()) {
            handleTakeForCrafting();
        }

        // P → Coloca item na grid
        if (input.consumePutRequested()) {
            handlePutInCraftGrid();
        }

        // ENTER → Realiza craft
        if (input.consumeCraftMenuRequested()) {
            handleCrafting();
        }

        // Navegação na grid com setas (A navegação Y é invertida para simular a grelha)
        if (input.consumeCraftArrowUp()) moveCraftSelection(0, -1);
        if (input.consumeCraftArrowDown()) moveCraftSelection(0, 1);
        if (input.consumeCraftArrowLeft()) moveCraftSelection(-1, 0);
        if (input.consumeCraftArrowRight()) moveCraftSelection(1, 0);

        // Apenas chamamos a função de desenho combinada.
        updateInventoryAndCrafting();
    }

    public void toggleInventory() {
        inventoryOpen = !inventoryOpen;

        if (inventoryOpen) {
            guiNode.attachChild(inventoryNode);
            craftMenuOpen = false;
            selectedItem = null;
            player.getInventory().setSelectedSlot(0);
            player.setSelectedCraftSlot(0); // Começa no slot 0
        } else {
            inventoryNode.removeFromParent();
            // NUNCA DESTRUIR ITEM: Se selectedItem != null, ele deve ser adicionado de volta
            if (selectedItem != null) {
                System.out.println("Inventário fechado com item selecionado. Item não será adicionado de volta (deve estar no inventário para transferir).");
            }
            craftMenuOpen = false;
            selectedItem = null;
        }
    }

    public boolean isInventoryOpen() {
        return inventoryOpen;
    }

    @Override
    protected void cleanup(Application app) {
        if (crosshair != null) crosshair.removeFromParent();
        if (hotbarNode != null) {
            hotbarNode.removeFromParent();
        }
        if (inventoryNode != null) {
            inventoryNode.removeFromParent();
        }
    }

    public void moveInventorySelection(String dir) {
        Inventory inv = player.getInventory();
        int selected = inv.getSelectedSlot();

        int cols = INVENTORY_COLS;
        int rows = INVENTORY_ROWS;

        int col = selected % cols;
        int row = selected / cols;

        switch (dir) {
            case "Left":
                if (col > 0) selected--;
                break;
            case "Right":
                if (col < cols - 1) selected++;
                break;
            case "Up":
                if (row > 0) selected -= cols;
                break;
            case "Down":
                if (row < rows - 1) selected += cols;
                break;
        }

        inv.setSelectedSlot(selected);
    }

    private void enterCraftMenu() {
        craftMenuOpen = true;
        player.setSelectedCraftSlot(0); // Começa no slot 0
        System.out.println("Entrou no menu de crafting");
    }

    private void exitCraftMenu() {
        craftMenuOpen = false;
        selectedItem = null; // Limpa seleção
        player.setSelectedCraftSlot(-1);
        System.out.println("Saiu do menu de crafting (ALT)");
    }

    // MODIFICADO: Lógica de transição e seleção para 'T'
    private void handleTakeFromInventory() {
        if (selectedItem != null) {
            // Se já tem um item selecionado para colocar, deseleciona.
            selectedItem = null;
            System.out.println("Item desselecionado (T)");
            return;
        }

        // Tenta selecionar item do slot atual
        int slot = player.getInventory().getSelectedSlot();
        Stacks stack = player.getInventory().getSlot(slot);

        if (stack != null && stack.getAmount() > 0) {
            //Seleciona o ite
            selectedItem = stack.getItem();
            //Transiciona para o modo de crafting
            enterCraftMenu(); // Sets craftMenuOpen = true
            System.out.println("Entrou no crafting e selecionou item: " + selectedItem.getName() + " (T)");
        } else {
            // Slot vazio: Transiciona para o modo de crafting
            enterCraftMenu(); // Sets craftMenuOpen = true
            System.out.println("Entrou no crafting (T no slot vazio).");
        }
    }

    private void handleTakeForCrafting() {
        if (selectedItem != null) {
            // Clica em T novamente para cancelar a seleção de tipo.
            selectedItem = null;
            System.out.println("Seleção de Item cancelada (T)");
        } else {
            // Se selectedItem é null, o jogador pode estar a tentar pegar um item da grid.
            System.out.println("Nenhum item selecionado. Use T no inventário para selecionar o tipo de item a colocar.");
        }
    }

    // Lõgica do P:
    private void handlePutInCraftGrid() {
        if (selectedItem == null) {
            System.out.println("Nenhum item selecionado para colocar!");
            return;
        }

        // Verifica se tem o item no inventário
        if (player.getInventory().countItem(selectedItem) == 0) {
            System.out.println("Não tens esse item no inventário!");
            selectedItem = null; // Limpa a seleção de tipo
            return;
        }

        // Remove 1 do inventário
        boolean removed = player.getInventory().removeItem(selectedItem, 1);
        if (!removed) {
            System.out.println("Erro ao remover item!");
            return;
        }

        // Coloca na grid
        int slot = player.getSelectedCraftSlot();
        Stacks[] grid = player.getCraftingGrid();

        if (grid[slot] == null) {
            grid[slot] = new Stacks(selectedItem, 1);
            System.out.println("Colocado 1x " + selectedItem.getName() + " no slot " + slot);
        } else if (grid[slot].getItem().getName().equals(selectedItem.getName())) {
            if (grid[slot].getAmount() < Stacks.MAX_STACK_SIZE) {
                grid[slot].addAmount(1);
                System.out.println("Empilhado 1x " + selectedItem.getName());
            } else {
                System.out.println("Stack cheio!");
                // Devolve ao inventário
                player.getInventory().addItem(selectedItem, 1);
            }
        } else {
            System.out.println("Slot ocupado com item diferente!");
            // Devolve ao inventário
            player.getInventory().addItem(selectedItem, 1);
        }

        // Verifica se ficou sem items, e limpa a seleção de tipo
        if (player.getInventory().countItem(selectedItem) == 0) {
            selectedItem = null;
            System.out.println("Ficaste sem esse item! Seleção de tipo limpa.");
        }
    }

    // Lógica de crafting:
    private void handleCrafting() {
        RecipeSystem recipe = RecipeRegistry.findRecipe(player.getCraftingGrid());

        if (recipe == null) {
            System.out.println("Receita inválida!");
            return;
        }

        Item result = recipe.getResult();
        int amount = recipe.getResultAmount();

        boolean added = player.getInventory().addItem(result, amount);

        if (added) {
            // Consome ingredientes e limpa os slots vazios da grid
            recipe.consumeIngredients(player.getCraftingGrid());
            System.out.println("Crafting realizado: " + amount + "x " + result.getName());
        } else {
            System.out.println("Inventário cheio! Não foi possível adicionar o resultado do craft.");
        }
    }

    // Navegação na grid:
    private void moveCraftSelection(int deltaX, int deltaY) {
        int current = player.getSelectedCraftSlot();
        int col = current % 3;
        int row = current / 3;

        int newCol = col + deltaX;
        int newRow = row + deltaY;

        // Limita aos bounds da grid 3x3 (0 a 2)
        if (newCol < 0) newCol = 0;
        if (newCol > 2) newCol = 2;
        // A navegação Y é invertida (Row 0 é topo, Row 2 é fundo)
        if (newRow < 0) newRow = 0;
        if (newRow > 2) newRow = 2;

        int newSlot = newRow * 3 + newCol;
        player.setSelectedCraftSlot(newSlot);
    }


    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }
}