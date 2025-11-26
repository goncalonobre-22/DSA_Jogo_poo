package jogo.util;

import jogo.gameobject.item.WoodBlockItem;
import jogo.gameobject.item.WoodStick;
import jogo.gameobject.item.StoneBlockItem; // Importação adicionada
import jogo.gameobject.item.StonePickaxe; // Importação adicionada

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Importação adicionada

public class RecipeRegistry {
    private static final List<RecipeSystem> recipes = new ArrayList<>();

    static {
        // Registar receitas aqui
        registerRecipes();
    }

    private static void registerRecipes() {
        // Receita 1: WoodStick (usa W=WoodBlockItem)
        recipes.add(new ShapedRecipeSystem(
                new WoodStick(),
                4,
                new String[]{
                        " W ",
                        " W ",
                        "   " // Usa 2 madeiras para 4 sticks
                },
                Map.of('W', new WoodBlockItem()) // Mapeamento: 'W' -> WoodBlockItem
        ));

        // Receita 2: Stone Pickaxe (Picareta de Pedra)
        // Padrão: S=Stone Block, D=Stick
        recipes.add(new ShapedRecipeSystem(
                new StonePickaxe(),
                1,
                new String[]{
                        "SSS", // 3 Blocos de Pedra
                        " W ", // 1 Stick
                        " W "  // 1 Stick
                },
                // Mapeamento explícito: 'S' mapeia para StoneBlockItem, 'D' mapeia para WoodStick (Stick)
                Map.of(
                        'S', new StoneBlockItem(),
                        'W', new WoodStick()
                )
        ));
    }

    /**
     * Encontra uma receita que corresponda à grid.
     */
    public static RecipeSystem findRecipe(Stacks[] grid) {
        for (RecipeSystem recipe : recipes) {
            if (recipe.matches(grid)) {
                return recipe;
            }
        }
        return null;
    }

    public static void registerRecipe(RecipeSystem recipe) {
        recipes.add(recipe);
    }
}