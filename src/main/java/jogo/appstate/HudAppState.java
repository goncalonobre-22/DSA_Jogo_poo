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
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

// Coisas adicionadas 1

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.shape.Quad;
import jogo.gameobject.character.Player;
import jogo.util.Inventory;
import jogo.util.Stacks;
import jogo.voxel.VoxelPalette;

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

        if (inventoryOpen) {
            updateFullInventory();
        } else {
            updateHotbar();
        }

        InputAppState input = getStateManager().getState(InputAppState.class);

// Se inventário está aberto → teclas movem seleção
        if (inventoryOpen) {
            if (input.consumeInventoryLeft())  moveInventorySelection("Left");
            if (input.consumeInventoryRight()) moveInventorySelection("Right");
            if (input.consumeInventoryUp())    moveInventorySelection("Up");
            if (input.consumeInventoryDown())  moveInventorySelection("Down");
        } else {
            // Hotbar numbers
            int num = input.consumeHotbarNumber();
            if (num > 0 && num <= 10) {
                player.getInventory().setSelectedSlot(num - 1);
            }
        }

    }

    //Coisas novas 3


    private Geometry createSlot(int x, int y, boolean selected) {
        Quad quad = new Quad(SLOT_SIZE, SLOT_SIZE);
        Geometry geom = new Geometry("Slot", quad);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        if (selected) {
            mat.setColor("Color", ColorRGBA.Yellow);  // Amarelo sólido
        } else {
            mat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.4f, 1f));  // Cinza sólido
        }

        geom.setMaterial(mat);
        geom.setLocalTranslation(x, y, 0);

        return geom;
    }


    private BitmapText createItemText(Stacks stack, int x, int y) {
        String blockName = getBlockName(stack.getBlockId());
        String text = blockName + "\nx" + stack.getAmount();

        BitmapText bt = new BitmapText(font);
        bt.setText(text);
        bt.setSize(font.getCharSet().getRenderedSize() * 0.7f);
        bt.setColor(ColorRGBA.White);
        bt.setLocalTranslation(x + 3, y + 20, 0);

        return bt;
    }

    private void updateHotbar() {
        hotbarNode.detachAllChildren();

        Inventory inv = player.getInventory();
        int screenWidth = getApplication().getCamera().getWidth();
        int startX = (screenWidth - (HOTBAR_SLOTS * SLOT_SPACING)) / 2;
        int startY = 50;

        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            int x = startX + i * SLOT_SPACING;

            // Slot
            boolean selected = (i == inv.getSelectedSlot());
            Quad quad = new Quad(SLOT_SIZE, SLOT_SIZE);
            Geometry slot = new Geometry("Slot" + i, quad);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.LightGray);
            // Contorno apenas quando selecionado
            if (selected) {
                int border = 3; // espessura do contorno

                Quad borderQuad = new Quad(SLOT_SIZE + border * 2, SLOT_SIZE + border * 2);
                Geometry borderGeom = new Geometry("Border" + i, borderQuad);

                Material borderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                borderMat.setColor("Color", ColorRGBA.Gray);

                borderGeom.setMaterial(borderMat);
                borderGeom.setLocalTranslation(x - border, startY - border, -1);

                hotbarNode.attachChild(borderGeom);
            }

            slot.setMaterial(mat);
            slot.setLocalTranslation(x, startY, 0);
            hotbarNode.attachChild(slot);

            // Item
            Stacks stack = inv.getSlot(i);
            if (stack != null) {
                String blockName = getBlockName(stack.getBlockId());
                BitmapText text = new BitmapText(font, false);
                text.setText(blockName + "\nx" + stack.getAmount());
                text.setSize(font.getCharSet().getRenderedSize() * 0.6f);
                text.setColor(ColorRGBA.White);
                text.setLocalTranslation(x + 3, startY + 25, 0);
                hotbarNode.attachChild(text);
            }

            // Número
            BitmapText number = new BitmapText(font, false);
            number.setText(String.valueOf(i + 1));
            number.setSize(font.getCharSet().getRenderedSize()- 3);
            number.setColor(ColorRGBA.DarkGray);
            number.setLocalTranslation(x + 3, startY + SLOT_SIZE - 2, 0);
            hotbarNode.attachChild(number);
        }
    }

    // MODIFICAR o updateFullInventory() para garantir Z correto:
    private void updateFullInventory() {
        inventoryNode.detachAllChildren();

        Inventory inv = player.getInventory();
        int screenWidth = getApplication().getCamera().getWidth();
        int screenHeight = getApplication().getCamera().getHeight();

        int cols = 10;
        int rows = 3;
        int startX = (screenWidth - (cols * SLOT_SPACING)) / 2;
        int startY = 250;

        // Background
        Quad bgQuad = new Quad(cols * SLOT_SPACING+ 20, rows * SLOT_SPACING + 80);
        Geometry bg = new Geometry("InvBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", ColorRGBA.LightGray);
        bg.setMaterial(bgMat);
        bg.setLocalTranslation(startX - 10, startY - rows * SLOT_SPACING - 15, -1);
        inventoryNode.attachChild(bg);

        // Slots
        for (int i = 0; i < inv.getSize(); i++) {
            int col = i % cols;
            int row = i / cols;

            int x = startX + col * SLOT_SPACING;
            int y = startY - row * SLOT_SPACING;

            boolean selected = (i == inv.getSelectedSlot());

            int border = 3; // espessura da borda

            // Se estiver selecionado, desenhar contorno atrás do slot
            if (selected) {
                Quad borderQuad = new Quad(SLOT_SIZE + border * 2, SLOT_SIZE + border * 2);
                Geometry borderGeom = new Geometry("InvBorder" + i, borderQuad);

                Material borderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                borderMat.setColor("Color", ColorRGBA.DarkGray); // cor do contorno

                // Nota o -1 no z para garantir que fica atrás
                borderGeom.setMaterial(borderMat);
                borderGeom.setLocalTranslation(x - border, y - border, -1);

                inventoryNode.attachChild(borderGeom);
            }

            // Slot normal (cinzento)
            Quad quad = new Quad(SLOT_SIZE, SLOT_SIZE);
            Geometry slot = new Geometry("InvSlot" + i, quad);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Gray);

            slot.setMaterial(mat);
            slot.setLocalTranslation(x, y, 0);
            inventoryNode.attachChild(slot);

            Stacks stack = inv.getSlot(i);
            if (stack != null) {
                BitmapText text = new BitmapText(font, false);
                text.setText(getBlockName(stack.getBlockId()) + "\nx" + stack.getAmount());
                text.setSize(font.getCharSet().getRenderedSize() * 0.6f);
                text.setColor(ColorRGBA.White);
                text.setLocalTranslation(x + 3, y + 25, 0);
                inventoryNode.attachChild(text);
            }
        }

        // Título
        BitmapText title = new BitmapText(font, false);
        title.setText("Inventario - Pressiona I para fechar");
        title.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(startX, screenHeight - 100, 0);
        inventoryNode.attachChild(title);
    }

    private String getBlockName(byte blockId) {
        if (blockId == VoxelPalette.STONE_ID) return "stone";
        if (blockId == VoxelPalette.DIRT_ID) return "dirt";
        if (blockId == VoxelPalette.SAND_ID) return "sand";
        if (blockId == VoxelPalette.METALORE_ID) return "metal ore";
        if (blockId == VoxelPalette.WOOD_ID) return "wood";
        return "Block " + blockId;
    }

    public void toggleInventory() {
        inventoryOpen = !inventoryOpen;

        if (inventoryOpen) {
            guiNode.attachChild(inventoryNode);
        } else {
            inventoryNode.removeFromParent();
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

        int cols = 10;
        int rows = 4;

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


    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }
}

