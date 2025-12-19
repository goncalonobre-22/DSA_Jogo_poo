package jogo.util.crafting;

import jogo.gameobject.item.Item;
import jogo.util.inventory.Stacks;

/**
 * Classe base abstrata para o sistema de receitas do jogo.
 * Define a estrutura para validar combinações de itens numa grelha de crafting,
 * bem como a lógica para consumir ingredientes e gerar o item resultante.
 */
public abstract class RecipeSystem {

    /** O item que é produzido como resultado desta receita. */
    private final Item result;

    /** A quantidade do item resultante que é produzida em cada ciclo de crafting. */
    private final int resultAmount;

    /**
     * Construtor da classe RecipeSystem.
     * @param result O objeto {@link Item} que será gerado.
     * @param resultAmount A quantidade de unidades do item a serem criadas.
     */
    protected RecipeSystem(Item result, int resultAmount) {
        this.result = result;
        this.resultAmount = resultAmount;
    }

    /**
     * Verifica se a disposição atual dos itens na grelha de crafting corresponde aos requisitos desta receita.
     * @param grid Array de {@link Stacks} representando os slots da bancada de trabalho.
     * @return true se os itens na grelha formarem uma combinação válida para esta receita; false caso contrário.
     */
    public abstract boolean matches(Stacks[] grid);

    /**
     * Remove ou decrementa os itens utilizados como ingredientes da grelha de crafting após a conclusão do processo.
     * @param grid Array de {@link Stacks} de onde os ingredientes serão consumidos.
     */
    public abstract void consumeIngredients(Stacks[] grid);

    /**
     * Retorna a instância do item resultante desta receita.
     * @return O {@link Item} produzido.
     */
    public Item getResult() {
        return result;
    }

    /**
     * Retorna a quantidade produzida pelo resultado desta receita.
     * @return O valor de resultAmount.
     */
    public int getResultAmount() {
        return resultAmount;
    }
}