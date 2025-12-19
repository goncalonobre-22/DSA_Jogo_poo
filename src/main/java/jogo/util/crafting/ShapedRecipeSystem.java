package jogo.util.crafting;

import jogo.gameobject.item.Item;
import jogo.util.inventory.Stacks;
import java.util.Map;

/**
 * Implementação de um sistema de receitas com formato definido (Shaped Recipe).
 * Esta classe valida se os itens colocados na grelha de crafting correspondem exatamente
 * a um padrão visual de 3x3 definido por caracteres.
 */
public class ShapedRecipeSystem extends RecipeSystem {

    /** O padrão visual da receita, representado por um array de Strings (3x3). */
    private final String[] pattern;

    /** Mapeamento que associa cada caractere do padrão a um objeto {@link Item} específico. */
    private final Map<Character, Item> ingredientsMap;

    /**
     * Construtor da classe ShapedRecipeSystem.
     * Utiliza um mapa de caracteres para identificar itens, permitindo flexibilidade na definição do padrão
     * sem conflitos de nomes.
     * @param result O item produzido pela receita.
     * @param resultAmount A quantidade do item a ser produzida.
     * @param pattern O esquema visual de 3x3 (ex: " W ", " W ", "   ").
     * @param ingredientsMap Mapa que traduz os caracteres do padrão nos itens necessários.
     */
    public ShapedRecipeSystem(Item result, int resultAmount, String[] pattern, Map<Character, Item> ingredientsMap) {
        super(result, resultAmount);
        this.pattern = pattern;
        this.ingredientsMap = ingredientsMap;
    }

    /**
     * Verifica se a disposição dos itens na grelha de 9 slots corresponde ao padrão definido.
     * Valida espaços vazios e compara o nome dos itens nos slots com os itens exigidos pelo padrão.
     * @param grid Array de {@link Stacks} representando a mesa de trabalho atual.
     * @return true se a grelha for uma cópia exata do padrão; false caso contrário.
     */
    @Override
    public boolean matches(Stacks[] grid) {
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;

            char patternChar = pattern[row].charAt(col);
            Stacks stack = grid[i];

            if (patternChar == ' ') {
                // Se o padrão pede vazio, o slot deve estar nulo ou ter quantidade zero
                if (stack != null && stack.getAmount() > 0) {
                    return false;
                }
            } else {
                Item requiredItem = getItemForChar(patternChar);
                // Valida se o item no slot corresponde ao item exigido pelo caractere do mapa
                if (requiredItem == null || stack == null || !stack.getItem().getName().equals(requiredItem.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Consome uma unidade de cada ingrediente presente na grelha de acordo com o padrão.
     * Se a quantidade de uma pilha chegar a zero, o slot da grelha é limpo (definido como null).
     * @param grid A grelha de onde os itens serão removidos.
     */
    @Override
    public void consumeIngredients(Stacks[] grid) {
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;

            char patternChar = pattern[row].charAt(col);
            if (patternChar != ' ') {
                if (grid[i] != null) {
                    grid[i].removeAmount(1);
                    if (grid[i].getAmount() <= 0) {
                        grid[i] = null;
                    }
                }
            }
        }
    }

    /**
     * Traduz um caractere do padrão no item correspondente através do mapa de ingredientes.
     * @param c O caractere a procurar.
     * @return O {@link Item} associado, ou null se não for encontrado.
     */
    private Item getItemForChar(char c) {
        return ingredientsMap.get(c);
    }
}