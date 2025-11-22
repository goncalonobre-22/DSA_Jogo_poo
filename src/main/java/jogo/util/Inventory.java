package jogo.util;

import jogo.gameobject.item.Item;

import java.util.ArrayList;

public class Inventory {
    private final Inventory[] inventory;
    private final int inventorySize;
    private int selectedSlot = 0;
    public Inventory(int inventorySize) {
        this.inventory = new Inventory[inventorySize];
        this.inventorySize = inventorySize;
    }
}
