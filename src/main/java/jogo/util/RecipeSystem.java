package jogo.util;

import jogo.gameobject.item.Item;

public abstract class RecipeSystem {

    private final Item result;
    private final int resultAmount;

    protected RecipeSystem(Item result, int resultAmount) {
        this.result = result;
        this.resultAmount = resultAmount;
    }

    /**
     * Verifica se a grid corresponde a esta receita.
     */
    public abstract boolean matches(Stacks[] grid);

    /**
     * Consome os ingredientes da grid após crafting.
     */
    public abstract void consumeIngredients(Stacks[] grid);

    public Item getResult() {
        return result;
    }

    public int getResultAmount() {
        return resultAmount;
    }
}
