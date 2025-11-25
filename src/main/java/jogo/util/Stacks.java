package jogo.util;

import jogo.gameobject.item.Item;

public class Stacks {
    public static final int MAX_STACK_SIZE = 64;
    private int amount;
    private Item item;

    public Stacks(Item item, int amount) {
        this.amount = Math.min(amount, MAX_STACK_SIZE);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, Math.min(amount, MAX_STACK_SIZE));
    }

    public void addAmount(int add) {
        this.amount = Math.min(amount + add, MAX_STACK_SIZE);
    }

    public void removeAmount(int remove) {
        this.amount = Math.max(0, amount - remove);
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public boolean isFull() {
        return amount >= MAX_STACK_SIZE;
    }

    public boolean isSameItem(Item other) {
        if (other == null) return false;
        return item.getName().equals(other.getName());
    }

    public Stacks copy() {
        return new Stacks(item, amount);
    }
}
