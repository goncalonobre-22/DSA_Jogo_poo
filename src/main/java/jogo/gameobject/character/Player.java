package jogo.gameobject.character;

import jogo.util.Inventory;

public class Player extends Character {
    private final Inventory inventory;
    public Player() {
        super("Player");
        this.inventory = new Inventory(40);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
