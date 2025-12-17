package jogo.util.inventory;

import jogo.gameobject.item.Item;

public class Stacks {
    public static final int MAX_STACK_SIZE = 64;
    private int amount;
    private Item item;

    public Stacks(Item item, int amount) {
        this.amount = amount;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void addAmount(int add) {
        this.amount += add;
    }

    public void removeAmount(int remove) {
        this.amount -= remove;
    }

    public boolean isFull() {
        return amount < MAX_STACK_SIZE;
    }

    public boolean isSameItem(Item other) {
        if (other == null) return false;
        return item.getName().equals(other.getName());
    }
}
