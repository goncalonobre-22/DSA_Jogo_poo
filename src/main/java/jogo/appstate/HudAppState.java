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

            // Contorno selecionado
            if (selected) {
                int border = 3;

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

            // ITEM DO INVENTÁRIO
            Stacks stack = inv.getSlot(i);
            if (stack != null) {

                Item item = stack.getItem();
                Texture iconTex = item.getIcon(assetManager);

                // Desenhar ícone
                if (iconTex != null) {
                    Quad iconQuad = new Quad(SLOT_SIZE - 8, SLOT_SIZE - 8);
                    Geometry iconGeom = new Geometry("Icon" + i, iconQuad);

                    Material iconMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    iconMat.setTexture("ColorMap", iconTex);

                    iconGeom.setMaterial(iconMat);

                    // Ícone centrado no slot
                    iconGeom.setLocalTranslation(x + 4, startY + 4, 0);

                    hotbarNode.attachChild(iconGeom);
                }

                // Texto da quantidade
                if (stack.getAmount() > 1) {
                    BitmapText amountText = new BitmapText(font, false);
                    amountText.setText(String.valueOf(stack.getAmount()));
                    amountText.setSize(font.getCharSet().getRenderedSize() - 4);
                    amountText.setColor(ColorRGBA.White);
                    if (stack.getAmount() < 10) {
                        amountText.setLocalTranslation(x + SLOT_SIZE - 15, startY + 20, 2);
                    } else {
                        amountText.setLocalTranslation(x + SLOT_SIZE - 20, startY + 20, 2);
                    }

                    if (Objects.equals(item.getName(), "Sand")){
                        amountText.setColor(ColorRGBA.Black);
                    }
                    hotbarNode.attachChild(amountText);
                }
            }

            // NÚMERO DA SLOT
            BitmapText number = new BitmapText(font, false);
            number.setText(String.valueOf(i + 1));
            number.setSize(font.getCharSet().getRenderedSize() - 3);
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
                Item item = stack.getItem();
                Texture iconTex = item.getIcon(assetManager);
                if (iconTex != null) {
                    Quad iconQuad = new Quad(SLOT_SIZE - 8, SLOT_SIZE - 8);
                    Geometry iconGeom = new Geometry("Icon" + i, iconQuad);

                    Material iconMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    iconMat.setTexture("ColorMap", iconTex);

                    iconGeom.setMaterial(iconMat);

                    // Ícone centrado no slot
                    iconGeom.setLocalTranslation(x + 4, startY + 4, 0);

                    inventoryNode.attachChild(iconGeom);
                }

                BitmapText number = new BitmapText(font, false);
                number.setText(String.valueOf(stack.getAmount()));
                number.setSize(font.getCharSet().getRenderedSize() * 0.7f);
                number.setColor(ColorRGBA.White);
                if (stack.getAmount() < 10) {
                    number.setLocalTranslation(x+24, y + 23, 0);
                } else {
                    number.setLocalTranslation(x+18, y + 23, 0);
                }

                if (item.getName() == "Sand"){
                    number.setColor(ColorRGBA.Black);
                }
                inventoryNode.attachChild(number);
            }
        }

        // Título
        BitmapText title = new BitmapText(font, false);
        title.setText("Inventario - Pressiona Tab para fechar");
        title.setSize(font.getCharSet().getRenderedSize());
        title.setColor(ColorRGBA.Black);
        title.setLocalTranslation(startX, screenHeight - 408, 0);
        inventoryNode.attachChild(title);
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

