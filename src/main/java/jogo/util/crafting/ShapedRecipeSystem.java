package jogo.util.crafting;

import jogo.gameobject.item.Item;
import jogo.util.inventory.Stacks;

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
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;

            char patternChar = pattern[row].charAt(col);
            Stacks stack = grid[i];

            if (patternChar == ' ') {
                if (stack != null && stack.getAmount() > 0) {
                    return false;
                }
            } else {
                Item requiredItem = getItemForChar(patternChar);
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
                if (grid[i] != null) {
                    grid[i].removeAmount(1);
                    if (grid[i].getAmount() <= 0) {
                        grid[i] = null;
                    }
                }
            }
        }
    }

    private Item getItemForChar(char c) {
        return ingredientsMap.get(c);
    }
}