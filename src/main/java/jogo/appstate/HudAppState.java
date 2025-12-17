package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

// Coisas adicionadas 1

import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.Item;
import jogo.util.furnace.FurnaceRegistry;
import jogo.util.furnace.FurnaceState;
import jogo.util.inventory.Inventory;
import jogo.util.crafting.RecipeRegistry;
import jogo.util.crafting.RecipeSystem;
import jogo.util.inventory.Stacks;
import jogo.voxel.VoxelWorld;


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

    private Node heartNode;
    private Node hungerNode;
    private Node scoreNode;

    private static final int SLOT_SIZE = 35;
    private static final int SLOT_SPACING = 38;
    private static final int HOTBAR_SLOTS = 10;
    private static final int INVENTORY_COLS = 10;
    private static final int INVENTORY_ROWS = 4; // 40 slots total

    private static final int MAX_HEARTS = 10;
    private static final int HEART_SIZE = 15;
    private static final int HEART_SPACING = 17;
    private static final int HEALTH_PER_HEART = 10;

    private static final int MAX_FOODS = 10;
    private static final int FOOD_SIZE = 15;
    private static final int FOOD_SPACING = 17;
    private static final int HUNGER_PER_FOOD = 10;

    private static final int FURNACE_SLOT_INPUT = 1;
    private static final int FURNACE_SLOT_FUEL = 2;
    private static final int FURNACE_SLOT_OUTPUT = 3;

    private int selectedFurnaceSlot = FURNACE_SLOT_INPUT;


    private boolean craftMenuOpen = false;
    private Item selectedItem = null; // Item selecionado com T
    private Node craftingNode;

    private VoxelWorld.Vector3i currentFurnaceCell = null; // <--- NOVO
    private FurnaceState currentFurnaceState = null; // <--- NOVO
    private boolean furnaceMenuOpen = false;
    private Item selectedItemForFurnace = null;

    private boolean gameOverOpen = false;
    private Node gameOverNode;
    private PlayerAppState playerAppState;

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

        this.playerAppState = getStateManager().getState(PlayerAppState.class);


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

        heartNode = new Node("HeartDisplay");
        guiNode.attachChild(heartNode);
        System.out.println("HudAppState initialized: heartNode attached");

        hungerNode = new Node("HungerDisplay");
        guiNode.attachChild(hungerNode);
        System.out.println("HudAppState initialized: hungerNode attached");

        // Inventário
        inventoryNode = new Node("Inventory");
        System.out.println("HudAppState initialized: inventoryNode created");

        //Sistema de Crafting
        craftingNode = new Node("Crafting");
        System.out.println("HudAppState initialized: craftingNode created");

        scoreNode = new Node("ScoreDisplay");
        guiNode.attachChild(scoreNode);
        System.out.println("HudAppState initialized: scoreNode attached");

        gameOverNode = new Node("GameOverScreen");
        System.out.println("HudAppState initialized: gameOverNode created");
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

        if (player.getHealth() <= 0 && !gameOverOpen) {
            // JOGADOR MORREU: Trava o jogo e mostra a tela de Game Over
            showGameOver(true);
            return;
        } else if (gameOverOpen) {
            // JOGADOR MORTO: Verifica o Respawn pela tecla 'R' e retorna
            updateGameOverScreen(input);
            return;
        }

        boolean clearCraft = input.consumeClearCraftRequested();
        if (clearCraft && inventoryOpen) {
            handleClearCrafting();
            // Não é mais necessário consumir input.consumeToggleShadingRequested();
        }

        updateHealthDisplay();

        updateHungerDisplay();

        updateScoreDisplay();

        if (inventoryOpen) {
            // A interface é sempre a combinada, mas a lógica de input é que muda.
            if (craftMenuOpen) {
                updateCraftingInterface(input);
            } else if (furnaceMenuOpen) { // <--- NOVO: Prioridade à Fornalha
                updateFurnaceInterface(input);
            } else{
                updateInventoryAndCrafting();
            }
        } else {
            updateHotbar();
        }

        // Se inventário está aberto → teclas movem seleção / acionam transição
        if (inventoryOpen) {
            // Navegação nas setas do Inventário (só funciona se o Crafting NÃO estiver aberto E Fornalha NÃO estiver aberta)
            if (!craftMenuOpen && !furnaceMenuOpen) { // <--- ALTERADO
                if (input.consumeInventoryLeft())  moveInventorySelection("Left");
                if (input.consumeInventoryRight()) moveInventorySelection("Right");
                if (input.consumeInventoryUp())    moveInventorySelection("Up");
                if (input.consumeInventoryDown())  moveInventorySelection("Down");
            }

            // **Transição para Crafting (T e ENTER)**
            if (inventoryOpen && !craftMenuOpen && !furnaceMenuOpen) {
                // 1. T (Take): Tenta selecionar item e transiciona (implementa a transição e a seleção)
                if (input.consumeTakeRequested()) handleTakeFromInventory();
                // 2. ENTER: Transiciona diretamente para o craft sem precisar de item (satisfaz a regra)
                if (input.consumeCraftMenuRequested()) enterCraftMenu();

                //if (input.consumePutInFurnaceRequested()) handleTakeForFurnace(input);
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
                    amountText.setLocalTranslation(x + 25, y + 15, 2);
                } else {
                    amountText.setLocalTranslation(x + 15, y + 15, 2);
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
        arrow.setSize(font.getCharSet().getRenderedSize() * 1.6f);
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
            furnaceMenuOpen = false;
            selectedItemForFurnace = null;
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
            furnaceMenuOpen = false;
            selectedItemForFurnace = null;
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

        if (craftingNode != null) {
            craftingNode.removeFromParent();
        }

        if (heartNode != null) { // [NOVO] Limpar o nó de vida
            heartNode.removeFromParent();
        }

        if (hungerNode != null) { // [NOVO] Limpar o nó de fome
            hungerNode.removeFromParent();
        }

        if (scoreNode != null) { // [NOVO] Limpar o nó de pontuação
            scoreNode.removeFromParent();
        }

        if (gameOverNode != null) { // Limpar o nó de Game Over
            gameOverNode.removeFromParent();
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

    private void drawHeartIcon(Node parentNode, String texturePath, int x, int y) {
        Quad quad = new Quad(HEART_SIZE, HEART_SIZE);
        Geometry heartGeom = new Geometry("HeartIcon", quad);

        Material heartMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture iconTex = assetManager.loadTexture(texturePath);

        if (iconTex != null) {
            heartMat.setTexture("ColorMap", iconTex);
            heartMat.setTransparent(true);
            heartMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        } else {
            // Fallback: usar uma cor sólida se a textura falhar
            heartMat.setColor("Color", ColorRGBA.Red.mult(0.5f));
        }

        heartGeom.setMaterial(heartMat);
        heartGeom.setLocalTranslation(x - 105, y - 8, 0); // Z=0
        parentNode.attachChild(heartGeom);
    }

    // [NOVO MÉTODO] Atualiza e desenha o display de vida
    private void updateHealthDisplay() {
        heartNode.detachAllChildren();

        int currentHealth = player.getHealth(); // Assume 100 de vida máxima (10 corações * 10/coração)

        int screenWidth = getApplication().getCamera().getWidth();
        // Centrar os corações
        int startX = (screenWidth - (MAX_HEARTS * HEART_SPACING)) / 2;
        // Posicionar acima da Hotbar (que está em y=50, com slots de 40px)
        int startY = 100;

        for (int i = 0; i < MAX_HEARTS; i++) {
            int x = startX + i * HEART_SPACING;
            int y = startY;

            // O limite de vida que este coração representa (e.g., 10, 20, 30, ...)
            int heartValue = (i + 1) * HEALTH_PER_HEART;

            String texturePath;

            if (currentHealth >= heartValue) {
                // Coração Cheio: vida atual é maior ou igual ao limite superior do coração (ex: vida 90, coração 9)
                texturePath = "Interface/full_heart.png";
            } else if (currentHealth >= (heartValue - 5)) {
                // Meio Coração: vida atual está no intervalo de 5 (ex: vida 85, coração 9)
                texturePath = "Interface/half_heart.png";
            } else {
                // Coração Vazio: vida atual abaixo do intervalo de 5 (ex: vida 84, coração 9)
                texturePath = "Interface/empty_heart.png";
            }

            drawHeartIcon(heartNode, texturePath, x, y);
        }
    }

    private void drawHungerIcon(Node parentNode, String texturePath, int x, int y) {
        Quad quad = new Quad(FOOD_SIZE, FOOD_SIZE);
        Geometry hungerGeom = new Geometry("FoodIcon", quad);

        Material hungerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture iconTex = assetManager.loadTexture(texturePath);

        if (iconTex != null) {
            hungerMat.setTexture("ColorMap", iconTex);
            hungerMat.setTransparent(true);
            hungerMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        } else {
            // Fallback: usar uma cor sólida se a textura falhar (laranja para fome)
            hungerMat.setColor("Color", ColorRGBA.Orange.mult(0.5f));
        }

        hungerGeom.setMaterial(hungerMat);
        hungerGeom.setLocalTranslation(x - 435, y - 8, 0); // Z=0
        parentNode.attachChild(hungerGeom);
    }

    private void updateHungerDisplay() {
        hungerNode.detachAllChildren();

        int currentHunger = player.getHunger(); // 100 de fome máxima

        int screenWidth = getApplication().getCamera().getWidth();
        // Colocar na mesma altura dos corações (y=100) mas alinhados à direita.
        // O startX é calculado da direita para a esquerda.
        int totalWidth = MAX_FOODS * FOOD_SPACING;
        int startX = screenWidth - totalWidth - 20; // 20px de margem à direita
        int startY = 100; // Mesma altura da barra de vida

        for (int i = 0; i < MAX_FOODS; i++) {
            // Desenho da esquerda para a direita (ícone 1 ao 10)
            int x = startX + i * FOOD_SPACING;
            int y = startY;

            // O limite de fome que este ícone representa (e.g., 10, 20, 30, ...)
            int foodValue = (i + 1) * HUNGER_PER_FOOD;

            String texturePath;

            if (currentHunger >= foodValue) {
                // Ícone Cheio
                texturePath = "Interface/full_food.png";
            } else if (currentHunger >= (foodValue - 5)) {
                // Meio Ícone
                texturePath = "Interface/half_food.png";
            } else {
                // Ícone Vazio
                texturePath = "Interface/empty_food.png";
            }

            drawHungerIcon(hungerNode, texturePath, x, y); // Reutiliza a função de desenho
        }
    }

    private void updateGameOverScreen(InputAppState input) {
        // Consumir 'R' (RespawnRequested) que foi deixado passar pelo InputAppState
        if (input.consumeRespawnRequested()) {
            handleRespawn();
            return;
        }
    }

    public void showGameOver(boolean show) {
        if (show == gameOverOpen) return;

        gameOverOpen = show;

        if (show) {
            // 1. DESATIVA CONTROLO DO JOGADOR
            playerAppState.setControlEnabled(false);
            // 2. FORÇA O RATO A SER LIBERTADO
            getStateManager().getState(InputAppState.class).setMouseCaptured(false);

            // 3. ESCONDE HUDs NORMAIS
            if(hotbarNode.getParent() != null) hotbarNode.removeFromParent();
            if(inventoryNode.getParent() != null) inventoryNode.removeFromParent();
            if(heartNode.getParent() != null) heartNode.removeFromParent();
            if(hungerNode.getParent() != null) hungerNode.removeFromParent();

            // 4. CONFIGURA A TELA DE GAME OVER
            gameOverNode.detachAllChildren();

            SimpleApplication sapp = (SimpleApplication) getApplication();
            int w = sapp.getCamera().getWidth();
            int h = sapp.getCamera().getHeight();

            // Overlay Vermelho
            Quad bgQuad = new Quad(w, h);
            Geometry bg = new Geometry("GameOverOverlay", bgQuad);
            Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            bgMat.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, 0.7f));
            bgMat.setTransparent(true);
            bgMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            bg.setMaterial(bgMat);
            bg.setLocalTranslation(0, 0, -10);
            gameOverNode.attachChild(bg);

            // Texto "Morreste"
            BitmapText title = new BitmapText(font, false);
            title.setText("MORRESTE");
            title.setSize(font.getCharSet().getRenderedSize() * 3f);
            title.setColor(ColorRGBA.White);
            float titleX = (w - title.getLineWidth()) / 2f;
            float titleY = h * 0.7f;
            title.setLocalTranslation(titleX, titleY, 0);
            gameOverNode.attachChild(title);

            // Botão "Respawn" [EDITADO]
            String buttonText = "Para dar Respawn clique na tecla R"; // Agora é o R
            BitmapText button = new BitmapText(font, false);
            button.setText(buttonText);
            button.setSize(font.getCharSet().getRenderedSize() * 1.5f);
            button.setColor(ColorRGBA.White.mult(0.9f));
            float buttonX = (w - button.getLineWidth()) / 2f;
            float buttonY = h * 0.4f;
            button.setLocalTranslation(buttonX, buttonY, 0);
            gameOverNode.attachChild(button);

            guiNode.attachChild(gameOverNode);

        } else {
            // Esconde a tela de morte e restaura HUDs normais
            guiNode.detachChild(gameOverNode);
            guiNode.attachChild(hotbarNode);
            guiNode.attachChild(heartNode);
            guiNode.attachChild(hungerNode);
        }
    }

    private void handleRespawn() {
        if (playerAppState == null) return;

        // Aciona o respawn completo (reset stats e teleport)
        playerAppState.triggerRespawn();

        // Esconde a tela de Game Over e reativa o jogo
        showGameOver(false);
    }

    public void enterFurnaceMode(VoxelWorld.Vector3i cell) {
        WorldAppState worldAppState = getStateManager().getState(WorldAppState.class);
        VoxelWorld vw = worldAppState.getVoxelWorld();

        currentFurnaceCell = cell;
        currentFurnaceState = vw.getFurnaceState(cell.x, cell.y, cell.z);
        furnaceMenuOpen = true;
        inventoryOpen = true;

        craftMenuOpen = false;
        selectedItem = null;
        selectedItemForFurnace = null;

        // CORREÇÃO: Não alterar o slot principal com um marcador. Apenas inicializar o slot da fornalha.
        this.selectedFurnaceSlot = FURNACE_SLOT_INPUT;
        player.getInventory().setSelectedSlot(0); // Garante que o slot do inventário principal é válido (0-39)

        guiNode.attachChild(inventoryNode);
    }

    private void exitFurnaceMode() {
        if (currentFurnaceCell != null) {
            // Limpar o target do InteractionAppState, para que a próxima interação "E" seja nova.
            getStateManager().getState(InteractionAppState.class).clearTargetFurnace();
        }
        currentFurnaceCell = null;
        currentFurnaceState = null;
        furnaceMenuOpen = false;
        selectedItemForFurnace = null;
        player.getInventory().setSelectedSlot(0);
    }

    private void handlePutInFurnace(int slotType) {
        if (currentFurnaceState == null || selectedItemForFurnace == null) return;

        Inventory inv = player.getInventory();
        Item itemToPut = selectedItemForFurnace;

        if (inv.countItem(itemToPut) == 0) {
            System.out.println("Não tens esse item no inventário!");
            selectedItemForFurnace = null;
            return;
        }

        boolean canPlace = false;
        String slotName = "";

        // Tenta colocar 1x no slot.
        if (slotType == FURNACE_SLOT_INPUT) {
            if (currentFurnaceState.inputStack == null && FurnaceRegistry.findRecipe(itemToPut) != null) {
                if (inv.removeItem(itemToPut, 1)) {
                    canPlace = currentFurnaceState.setInput(itemToPut); // Coloca e inicia melt (se não estiver a derreter)
                }
            } else if (currentFurnaceState.inputStack != null && currentFurnaceState.inputStack.isSameItem(itemToPut) && currentFurnaceState.inputStack.isFull()) {
                if (inv.removeItem(itemToPut, 1)) {
                    currentFurnaceState.inputStack.addAmount(1); // Empilha
                    canPlace = true;
                }
            }
            slotName = "Input";
        } else if (slotType == FURNACE_SLOT_FUEL) {
            if (currentFurnaceState.fuelStack == null && FurnaceRegistry.getFuelEfficiency(itemToPut) > 0.0f) {
                if (inv.removeItem(itemToPut, 1)) {
                    canPlace = currentFurnaceState.setFuel(itemToPut);
                }
            } else if (currentFurnaceState.fuelStack != null && currentFurnaceState.fuelStack.isSameItem(itemToPut) && currentFurnaceState.fuelStack.isFull()) {
                if (inv.removeItem(itemToPut, 1)) {
                    currentFurnaceState.fuelStack.addAmount(1); // Empilha
                    canPlace = true;
                }
            }
            slotName = "Combustível";
        }

        if (canPlace) {
            System.out.println("Colocado 1x " + itemToPut.getName() + " no slot " + slotName);
        } else {
            System.out.println("Não foi possível colocar. Slot ocupado ou item inválido.");
        }

        if (inv.countItem(selectedItemForFurnace) == 0) {
            selectedItemForFurnace = null;
        }
    }

    private void handleRetrieveOutput() {
        if (currentFurnaceState == null || currentFurnaceState.outputStack == null) return;

        Stacks outputStack = currentFurnaceState.outputStack;
        Item outputItem = outputStack.getItem();
        int amount = outputStack.getAmount();

        boolean added = player.getInventory().addItem(outputItem, amount);

        if (added) {
            currentFurnaceState.outputStack = null;
            System.out.println("Retirado " + amount + "x " + outputItem.getName() + " da fornalha.");
        } else {
            System.out.println("Inventário cheio! Não foi possível retirar o item.");
        }
    }

    private void updateFurnaceInterface(InputAppState input) {
        if (currentFurnaceState == null) {
            exitFurnaceMode();
            return;
        }

        // ALT → Sai do menu de fornalha (volta para a vista de inventário)
        if (input.consumeExitCraftRequested()) {
            exitFurnaceMode();
            return;
        }

        // --- Lógica de Seleção / Colocação (O) ---
        if (input.consumePutInFurnaceRequested()) {
            int selectedSlot = player.getInventory().getSelectedSlot();
            if (selectedItemForFurnace == null) {
                // MODO 1: Seleciona Item (a partir do slot do Inventário)
                Stacks stack = player.getInventory().getSlot(selectedSlot);

                if (stack != null && stack.getAmount() > 0 &&
                        (FurnaceRegistry.findRecipe(stack.getItem()) != null || FurnaceRegistry.getFuelEfficiency(stack.getItem()) > 0.0f)) {
                    selectedItemForFurnace = stack.getItem();
                    // Seleciona o slot de Input (1) para começar a colocação (CORRIGIDO)
                    this.selectedFurnaceSlot = FURNACE_SLOT_INPUT;
                    System.out.println("Item selecionado para fornalha: " + selectedItemForFurnace.getName());
                } else {
                    System.out.println("Nenhum item selecionado ou item não é fundível/combustível no slot selecionado.");
                }
            } else {
                // MODO 2: Coloca Item (no slot da Fornalha) ou Cancela Seleção (noutros slots)
                // Usa this.selectedFurnaceSlot para determinar onde colocar/cancelar (CORRIGIDO)
                if (this.selectedFurnaceSlot == FURNACE_SLOT_INPUT) handlePutInFurnace(FURNACE_SLOT_INPUT);
                else if (this.selectedFurnaceSlot == FURNACE_SLOT_FUEL) handlePutInFurnace(FURNACE_SLOT_FUEL);
                else {
                    // Cancelar seleção (se clicar O no Output, ou noutros slots que não Input/Fuel)
                    selectedItemForFurnace = null;
                    this.selectedFurnaceSlot = FURNACE_SLOT_INPUT;
                    System.out.println("Seleção de Item para fornalha cancelada (O)");
                }
            }
        }

        // ENTER → Retira o item cozinhado (Output)
        if (input.consumeCraftMenuRequested()) {
            handleRetrieveOutput();
        }

        if (selectedItemForFurnace != null) {
            // MODO 2: COLOCAÇÃO - Navega APENAS entre slots da fornalha (1, 2, 3) (CORRIGIDO)

            int currentFurnaceSlot = this.selectedFurnaceSlot;
            int newFurnaceSlot = currentFurnaceSlot;

            // Lógica de navegação entre slots da fornalha (Input: 1, Fuel: 2, Output: 3)
            if (input.consumeInventoryUp()) {
                if (currentFurnaceSlot == FURNACE_SLOT_INPUT) newFurnaceSlot = FURNACE_SLOT_FUEL;
                else if (currentFurnaceSlot == FURNACE_SLOT_FUEL) newFurnaceSlot = FURNACE_SLOT_INPUT;
                else if (currentFurnaceSlot == FURNACE_SLOT_OUTPUT) newFurnaceSlot = FURNACE_SLOT_INPUT;
            } else if (input.consumeInventoryDown()) {
                if (currentFurnaceSlot == FURNACE_SLOT_INPUT) newFurnaceSlot = FURNACE_SLOT_FUEL;
                else if (currentFurnaceSlot == FURNACE_SLOT_FUEL) newFurnaceSlot = FURNACE_SLOT_INPUT;
                else if (currentFurnaceSlot == FURNACE_SLOT_OUTPUT) newFurnaceSlot = FURNACE_SLOT_FUEL;
            }

            if (input.consumeInventoryRight()) {
                if (currentFurnaceSlot != FURNACE_SLOT_OUTPUT) newFurnaceSlot = FURNACE_SLOT_OUTPUT;
            } else if (input.consumeInventoryLeft()) {
                if (currentFurnaceSlot == FURNACE_SLOT_OUTPUT) newFurnaceSlot = FURNACE_SLOT_FUEL;
            }


            if (newFurnaceSlot != currentFurnaceSlot) {
                this.selectedFurnaceSlot = newFurnaceSlot;
            }

        } else {
            // MODO 1: SELEÇÃO DE ITEM (DEFAULT) - Navega no INVENTÁRIO PRINCIPAL (0-39) (CORRIGIDO)

            // Delegamos para a função de navegação de inventário que manipula os slots 0-39
            if (input.consumeInventoryLeft())  moveInventorySelection("Left");
            if (input.consumeInventoryRight()) moveInventorySelection("Right");
            if (input.consumeInventoryUp())    moveInventorySelection("Up");
            if (input.consumeInventoryDown())  moveInventorySelection("Down");
        }

        // Desenho (simplificado)
        drawInventoryAndFurnace();
    }


    private void drawInventoryAndFurnace() {
        inventoryNode.detachAllChildren();

        // === 1. Lógica de desenho do Inventário (Base) ===

        Inventory inv = player.getInventory();
        int screenWidth = getApplication().getCamera().getWidth();
        int screenHeight = getApplication().getCamera().getHeight();

        // Constantes do HUDAppState.java
        int SLOT_SIZE = 35; // Valor do campo da classe (assumido)
        int SLOT_SPACING = 38; // Valor do campo da classe (assumido)
        int INVENTORY_COLS = 10; // Valor do campo da classe (assumido)
        int INVENTORY_ROWS = 4; // Valor do campo da classe (assumido)

        // Ajustar largura para dar espaço ao HUD da fornalha (3 slots)
        int furnaceSlotCols = 3; // Input, Phase, Output
        int innerSpacing = 40;
        int slotWidth = SLOT_SPACING * furnaceSlotCols + 10; // Largura do display de fornalha (3 slots + margem)

        // Calcular o tamanho do BG para incluir o inventário + a fornalha lateral
        int totalDisplayWidth = INVENTORY_COLS * SLOT_SPACING + innerSpacing + slotWidth + SLOT_SPACING;
        int invStartX = (screenWidth - totalDisplayWidth) / 2 + 10;
        int invStartY = 250;
        int bgWidth = totalDisplayWidth - 20;
        int bgHeight = INVENTORY_ROWS * SLOT_SPACING + 50;

        // --- VARIÁVEIS DE CONTROLO DE DESTAQUE CORRIGIDAS ---
        int currentSelectedSlot = inv.getSelectedSlot();
        // O modo de colocação de item na fornalha desliga o destaque no inventário.
        boolean isPuttingItemInFurnace = furnaceMenuOpen && (selectedItemForFurnace != null);
        // --- FIM VARIÁVEIS DE CONTROLO DE DESTAQUE CORRIGIDAS ---


        // Desenhar Fundo (Fundo igual ao do crafting/inventário)
        Quad bgQuad = new Quad(bgWidth, bgHeight);
        Geometry bg = new Geometry("InvBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", ColorRGBA.LightGray);
        bg.setMaterial(bgMat);
        bg.setLocalTranslation(invStartX - 10, invStartY - INVENTORY_ROWS * SLOT_SPACING + 25, -2); // Z=-2 for BG
        inventoryNode.attachChild(bg);

        // Desenhar Slots do inventário (0-39)
        for (int i = 0; i < inv.getSize(); i++) {
            int col = i % INVENTORY_COLS;
            int row = i / INVENTORY_COLS;

            int x = invStartX + col * SLOT_SPACING;
            int y = invStartY - row * SLOT_SPACING;

            // CORRIGIDO: O highlight no inventário só deve aparecer se NÃO estivermos no modo de colocação de fornalha.
            boolean selected = (i == currentSelectedSlot && !isPuttingItemInFurnace);
            Stacks stack = inv.getSlot(i);

            // Desenha Slot, Borda, Ícone e Quantidade
            drawInventorySlot(inventoryNode, stack, i, x, y, ColorRGBA.Gray, selected);
        }

        // Desenhar título
        BitmapText title = new BitmapText(font, false);
        String titleText = "FORNALHA (ALT: Fechar | O: Seleciona Item / Coloca | ENTER: Retirar)";
        title.setText(titleText);
        title.setSize(font.getCharSet().getRenderedSize() * 0.9f);
        title.setColor(ColorRGBA.Black);
        title.setLocalTranslation(invStartX, invStartY + 65, 0);
        inventoryNode.attachChild(title);

        // === 2. Desenho da Fornalha (Lateral) ===

        if (furnaceMenuOpen && currentFurnaceState != null) {
            int furnaceStartX = invStartX + INVENTORY_COLS * SLOT_SPACING + innerSpacing;
            int furnaceStartY = invStartY - 20;

            // Posição central para o bloco de fornalha
            int centralX = furnaceStartX + SLOT_SPACING;
            int centralY = furnaceStartY - SLOT_SPACING;

            // Fundo da fornalha (para destacar a área)
            int furnaceBgWidth = 300;
            int furnaceBgHeight = SLOT_SIZE * 5;
            Quad furnaceBgQuad = new Quad(furnaceBgWidth, furnaceBgHeight);
            Geometry furnaceBg = new Geometry("FurnaceBg", furnaceBgQuad);
            Material furnaceBgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            furnaceBgMat.setColor("Color", ColorRGBA.LightGray.mult(0.9f));
            furnaceBgMat.setTransparent(true);
            furnaceBgMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            furnaceBg.setMaterial(furnaceBgMat);
            furnaceBg.setLocalTranslation(350, centralY + 150, -1);
            inventoryNode.attachChild(furnaceBg);

            // --- Slots da Fornalha ---

            // 1. Slot de INPUT (Topo)
            int inputX = 400;
            int inputY = centralY + 265;
            // CORRIGIDO: Destaque usa this.selectedFurnaceSlot
            boolean inputSelected = (this.selectedFurnaceSlot == FURNACE_SLOT_INPUT);
            drawInventorySlot(inventoryNode, currentFurnaceState.inputStack, 1000, inputX, inputY,
                    ColorRGBA.Gray, inputSelected);

            // 2. Slot de COMBUSTÍVEL (Baixo)
            int fuelX = inputX;
            int fuelY = inputY - 85;
            // CORRIGIDO: Destaque usa this.selectedFurnaceSlot
            boolean fuelSelected = (this.selectedFurnaceSlot == FURNACE_SLOT_FUEL);
            drawInventorySlot(inventoryNode, currentFurnaceState.fuelStack, 1001, fuelX, fuelY,
                    ColorRGBA.Gray, fuelSelected);

            // 3. Slot de OUTPUT (Lateral)
            int outputX = 500;
            int outputY = fuelY + 42;
            // CORRIGIDO: Destaque usa this.selectedFurnaceSlot
            boolean outputSelected = (this.selectedFurnaceSlot == FURNACE_SLOT_OUTPUT);
            drawInventorySlot(inventoryNode, currentFurnaceState.outputStack, 1002, outputX, outputY,
                    ColorRGBA.Gray, outputSelected);

            // 4. Imagem de Fase (Meio)
            int phase = currentFurnaceState.getPhase();

            // Lógica explícita para o caminho da imagem da fase
            if (phase > 0) {
                String phaseImagePath;
                if (phase == 1) {
                    phaseImagePath = "Interface/furnace_phase1.png";
                } else if (phase == 2) {
                    phaseImagePath = "Interface/furnace_phase2.png";
                } else if (phase == 3) {
                    phaseImagePath = "Interface/furnace_phase3.png";
                } else if (phase == 4) {
                    phaseImagePath = "Interface/furnace_phase4.png";
                } else {
                    // Fallback para a fase final ou se for maior que 4 (nunca deve acontecer)
                    phaseImagePath = "Interface/furnace_phase4.png";
                }

                drawPhaseImage(inventoryNode, phaseImagePath, fuelX, outputY);
            }

            // 5. Item de Seleção (Flutuante)
            if (selectedItemForFurnace != null) {
                SimpleApplication sapp = (SimpleApplication) getApplication();
                float mouseX = sapp.getInputManager().getCursorPosition().x;
                float mouseY = sapp.getInputManager().getCursorPosition().y;

                Stacks tempStack = new Stacks(selectedItemForFurnace,
                        player.getInventory().countItem(selectedItemForFurnace));

                drawInventorySlot(inventoryNode, tempStack, -1,
                        (int)mouseX - SLOT_SIZE / 2, (int)mouseY - SLOT_SIZE / 2, // Centrado no cursor
                        ColorRGBA.White.mult(0.7f), false); // Z=10 para ficar por cima de tudo
            }
        }
    }

    // Novo: Desenho da imagem de fase (seta)
    private void drawPhaseImage(Node parentNode, String texturePath, int x, int y) {
        Quad quad = new Quad(SLOT_SIZE, SLOT_SIZE);
        Geometry geom = new Geometry("FurnacePhase", quad);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture iconTex = assetManager.loadTexture(texturePath);

        if (iconTex != null) {
            mat.setTexture("ColorMap", iconTex);
            mat.setTransparent(true);
            mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        } else {
            mat.setColor("Color", ColorRGBA.Black);
        }

        geom.setMaterial(mat);
        geom.setLocalTranslation(x, y, 5); // Z=5 para ficar no topo
        parentNode.attachChild(geom);
    }

    private void handleClearCrafting() {
        Stacks[] grid = player.getCraftingGrid();
        boolean gridFullyCleared = true;

        System.out.println("A tentar limpar a grelha de crafting (DELETE)...");

        for (int i = 0; i < grid.length; i++) {
            Stacks stack = grid[i];
            if (stack != null && stack.getAmount() > 0) {
                int amountToReturn = stack.getAmount();
                int returnedCount = 0;

                // Tenta devolver item por item para o inventário
                for (int k = 0; k < amountToReturn; k++) {
                    if (player.getInventory().addItem(stack.getItem(), 1)) {
                        returnedCount++;
                    } else {
                        // Inventário cheio
                        break;
                    }
                }

                if (returnedCount > 0) {
                    System.out.println("Devolvido " + returnedCount + "x " + stack.getItem().getName() + " para o inventário.");
                }

                if (returnedCount == amountToReturn) {
                    // Todos os itens foram devolvidos com sucesso
                    grid[i] = null;
                } else {
                    // Apenas uma parte ou nenhum item foi devolvido
                    if (returnedCount > 0) {
                        stack.addAmount(-returnedCount);
                        gridFullyCleared = false;
                    } else {
                        // Não devolveu nada, mantém a stack intacta
                        gridFullyCleared = false;
                    }
                }
            }
        }

        if (gridFullyCleared) {
            System.out.println("Grelha de crafting limpa.");
            player.setSelectedCraftSlot(0); // Reinicia a seleção
            selectedItem = null; // Limpa a seleção de tipo
        } else {
            System.out.println("A grelha não pôde ser totalmente limpa (Inventário cheio).");
        }
    }

    private void updateScoreDisplay() {
        scoreNode.detachAllChildren();

        int currentScore = player.getScore(); // Obtém a pontuação do Player
        String playerName = "Player 1"; // Nome do jogador (fixo)

        SimpleApplication sapp = (SimpleApplication) getApplication();
        int screenHeight = sapp.getCamera().getHeight();

        // Posição: Canto superior esquerdo (margem de 20px)
        int marginX = 20;
        int marginY = screenHeight - 20;

        // Background
        Quad bgQuad = new Quad(140, 70);
        Geometry bg = new Geometry("ScoreBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", ColorRGBA.DarkGray);
        bg.setMaterial(bgMat);
        bg.setLocalTranslation(marginX - 10, marginY - 60, -1); // Z=-2 for BG
        scoreNode.attachChild(bg);


        // Título da Coluna (Nome)
        BitmapText nameLabel = new BitmapText(font, false);
        nameLabel.setText("Nome:");
        nameLabel.setSize(font.getCharSet().getRenderedSize() * 1.0f);
        nameLabel.setColor(ColorRGBA.White);
        nameLabel.setLocalTranslation(marginX, marginY, 0);
        scoreNode.attachChild(nameLabel);

        // Valor da Coluna (Nome do Jogador)
        BitmapText playerNameText = new BitmapText(font, false);
        playerNameText.setText(playerName);
        playerNameText.setSize(font.getCharSet().getRenderedSize() * 1.0f);
        playerNameText.setColor(ColorRGBA.White);
        // Posição à direita de "Nome:"
        playerNameText.setLocalTranslation(marginX + nameLabel.getLineWidth() + 10, marginY, 0);
        scoreNode.attachChild(playerNameText);

        // Título da Coluna (Pontuação)
        BitmapText scoreLabel = new BitmapText(font, false);
        scoreLabel.setText("Pontuacao:");
        scoreLabel.setSize(font.getCharSet().getRenderedSize() * 1.0f);
        scoreLabel.setColor(ColorRGBA.White);
        int scoreY = marginY - 25; // Abaixo do nome
        scoreLabel.setLocalTranslation(marginX, scoreY, 0);
        scoreNode.attachChild(scoreLabel);

        // Valor da Coluna (Score)
        BitmapText scoreValueText = new BitmapText(font, false);
        scoreValueText.setText(String.valueOf(currentScore));
        scoreValueText.setSize(font.getCharSet().getRenderedSize() * 1.0f);
        scoreValueText.setColor(ColorRGBA.White);
        // Posição à direita de "Pontuação:"
        scoreValueText.setLocalTranslation(marginX + scoreLabel.getLineWidth() + 10, scoreY, 0);
        scoreNode.attachChild(scoreValueText);
    }


    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }
}