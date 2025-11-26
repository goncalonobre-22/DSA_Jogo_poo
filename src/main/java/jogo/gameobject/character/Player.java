package jogo.gameobject.character;

import jogo.util.Inventory;
import jogo.util.Stacks;

public class Player extends Character {
    private final Inventory inventory;
    private final Stacks[] craftingGrid = new Stacks[9]; // 3x3 grid
    private int selectedCraftSlot = 0; // Slot selecionado na grid

    public Player() {
        super("Player");
        this.inventory = new Inventory(40);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Stacks[] getCraftingGrid() {
        return craftingGrid;
    }

    public int getSelectedCraftSlot() {
        return selectedCraftSlot;
    }

    public void setSelectedCraftSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            this.selectedCraftSlot = slot;
        }
    }

    public void clearCraftingGrid() {
        for (int i = 0; i < craftingGrid.length; i++) {
            craftingGrid[i] = null;
        }
    }
}
