package jogo.util.furnace;

import jogo.gameobject.item.Item;

/**
 * Representa uma receita individual de fundição na fornalha.
 * Associa um item de entrada (input) a um item resultante (output) após o processo de queima.
 */
public class FurnaceRecipe {
    /** O item necessário para iniciar o processo de fundição. */
    private final Item input;

    /** O item que será produzido após a conclusão da fundição. */
    private final Item output;

    /**
     * Construtor da classe FurnaceRecipe.
     * @param input O {@link Item} de entrada.
     * @param output O {@link Item} de saída.
     */
    public FurnaceRecipe(Item input, Item output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Obtém o item de entrada da receita.
     * @return O item de input.
     */
    public Item getInput() {
        return input;
    }

    /**
     * Obtém o item resultante da receita.
     * @return O item de output.
     */
    public Item getOutput() {
        return output;
    }
}