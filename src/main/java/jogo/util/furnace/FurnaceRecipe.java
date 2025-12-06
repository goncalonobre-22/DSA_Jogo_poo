package jogo.util.furnace;

import jogo.gameobject.item.Item;

public class FurnaceRecipe {
    private final Item input;
    private final Item output;

    public FurnaceRecipe(Item input, Item output) {
        this.input = input;
        this.output = output;
    }

    public Item getInput() {
        return input;
    }

    public Item getOutput() {
        return output;
    }
}
