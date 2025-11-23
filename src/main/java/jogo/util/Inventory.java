package jogo.util;

import jogo.gameobject.item.Item;

import java.util.ArrayList;

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



    public boolean addItem(Byte blockId, int amount) {
        for (int i = 0; i < this.inventorySize; i++) {
            Stacks inv = inventoryArray[i];
            if (inv != null && inv.getBlockId() == blockId){
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
            inventoryArray[emptySlot] = new Stacks(blockId, stackAmount);
            amount -= stackAmount;
        } return true;
    }

    public boolean removeItem(Byte blockId, int amount) {
        int remainingAmount = amount;
        for (int i = 0; i < this.inventorySize; i++) {
            Stacks stacks = inventoryArray[i];
            if (stacks != null && stacks.getBlockId() == blockId){
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

    public int countItem(byte blockId) {
        int count = 0;
        for (Stacks stack : inventoryArray) {
            if (stack != null && stack.getBlockId() == blockId) {
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

    public void setSlot(int index, Stacks stack) {
        if (index >= 0 && index < inventorySize) {
            inventoryArray[index] = stack;
        }
    }

    public void clearSlot(int index) {
        if (index >= 0 && index < inventorySize) {
            inventoryArray[index] = null;
        }
    }

    public boolean isFull() {
        return findEmptySlot() == -1;
    }

    public boolean isEmpty() {
        for (Stacks stack : inventoryArray) {
            if (stack != null) return false;
        }
        return true;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < 9) {  // Hotbar tem 9 slots (0-8)
            this.selectedSlot = slot;
        }
    }

    public Stacks getSelectedItem() {
        return getSlot(selectedSlot);
    }


}
