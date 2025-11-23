package jogo.util;

public class Stacks {
    public static final int MAX_STACK_SIZE = 64;
    private int amount;
    private byte blockId;

    public Stacks(byte blockId, int amount) {
        this.amount = Math.min(amount, MAX_STACK_SIZE);
        this.blockId = blockId;
    }

    public byte getBlockId() {
        return blockId;
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

    public Stacks copy() {
        return new Stacks(blockId, amount);
    }
}
