package jogo.util.inventory;

import jogo.gameobject.item.Item;

public class Inventory {
    private final Stacks[] inventoryArray;
    private final int inventorySize;
    private int selectedSlot = 0;

    public Inventory(int inventorySize) {
        this.inventorySize = inventorySize;
        this.inventoryArray = new Stacks[inventorySize];
    }

    public int getSize() {
        return inventorySize;
    }



    public boolean addItem(Item item, int amount) {
        if (item == null) return false;
        for (int i = 0; i < this.inventorySize; i++) {
            Stacks inv = inventoryArray[i];
            if (inv != null && inv.isSameItem(item)) {
                if (inv.getAmount() < Stacks.MAX_STACK_SIZE) {
                    int space  = Stacks.MAX_STACK_SIZE - inv.getAmount();
                    int toAdd = Math.min(space, amount);
                    inv.addAmount(toAdd);
                    amount -= toAdd;
                    if (amount <= 0) return true;
                }
            }
        }
        while (amount > 0) {
            int emptySlot = findEmptySlot();
            if (emptySlot == -1) return false;
            int stackAmount = Math.min(amount, Stacks.MAX_STACK_SIZE);
            inventoryArray[emptySlot] = new Stacks(item, stackAmount);
            amount -= stackAmount;
        } return true;
    }

    public boolean removeItem(Item item, int amount) {
        if (item == null) return false;
        int remainingAmount = amount;
        for (int i = 0; i < this.inventorySize; i++) {
            Stacks stacks = inventoryArray[i];
            if (stacks != null && stacks.isSameItem(item)) {
                if (stacks.getAmount() <= remainingAmount) {
                    remainingAmount -= stacks.getAmount();
                    inventoryArray[i] = null;
                } else {
                    stacks.removeAmount(remainingAmount);
                    remainingAmount = 0;
                }
                if (remainingAmount <= 0) return true;
            }
        }
        return remainingAmount == 0;
    }

    public int countItem(Item item) {
        if (item == null) return 0;
        int count = 0;
        for (Stacks stack : inventoryArray) {
            if (stack != null && stack.isSameItem(item)) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private int findEmptySlot() {
        for (int i = 0; i < inventorySize; i++) {
            if (inventoryArray[i] == null) return i;
        }
        return -1;
    }

    public Stacks getSlot(int index) {
        if (index < 0 || index >= inventorySize) return null;
        return inventoryArray[index];
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < inventorySize) {  // Hotbar tem 10 slots (0-9)
            this.selectedSlot = slot;
        }
    }

    public Stacks getSelectedItem() {
        return getSlot(selectedSlot);
    }


}
