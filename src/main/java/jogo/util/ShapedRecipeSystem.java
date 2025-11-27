package jogo.util;

import jogo.gameobject.item.Item;

import java.util.Map;

public class ShapedRecipeSystem extends RecipeSystem {
    private final String[] pattern;
    private final Map<Character, Item> ingredientsMap;

    // O construtor recebe um map de caracteres a idetificar o item para não ter de alterar os nomes dos itens quando uso itens com a mesma letra inicial
    public ShapedRecipeSystem(Item result, int resultAmount, String[] pattern, Map<Character, Item> ingredientsMap) {
        super(result, resultAmount);
        this.pattern = pattern;
        this.ingredientsMap = ingredientsMap;
    }

    @Override
    public boolean matches(Stacks[] grid) {
        // Verifica se a grid corresponde ao pattern
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;

            char patternChar = pattern[row].charAt(col);
            Stacks stack = grid[i];

            if (patternChar == ' ') {
                // Deve estar vazio
                if (stack != null && stack.getAmount() > 0) {
                    return false;
                }
            } else {
                // Deve ter o item correspondente
                Item requiredItem = getItemForChar(patternChar);
                // Verifica se o item existe no mapa E se corresponde ao item na grelha.
                if (requiredItem == null || stack == null || !stack.getItem().getName().equals(requiredItem.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void consumeIngredients(Stacks[] grid) {
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;

            char patternChar = pattern[row].charAt(col);
            if (patternChar != ' ') {
                // Remove 1 item
                if (grid[i] != null) {
                    grid[i].removeAmount(1);
                    if (grid[i].getAmount() <= 0) {
                        grid[i] = null;
                    }
                }
            }
        }
    }

    // Procura o item diretamente pelo caractere no mapa.
    private Item getItemForChar(char c) {
        return ingredientsMap.get(c);
    }
}