package jogo.util;

import jogo.gameobject.item.*;

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
        // Receita 1: WoodStick
        recipes.add(new ShapedRecipeSystem(
                new WoodStick(),
                4,
                new String[]{
                        " W ",
                        " W ",
                        "   "
                },
                // W corresponde ao bloco de madeira
                Map.of('W', new WoodBlockItem())
        ));

        // Receita 2: Pickaxe de madeira
        recipes.add(new ShapedRecipeSystem(
                new WoodPickaxe(),
                1,
                new String[]{
                        "WWW",
                        " S ",
                        " S "
                },
                // S corresponde à pedra e W corresponde ao Stick
                Map.of(
                        'W', new WoodBlockItem(),
                        'S', new WoodStick()
                )
        ));

        // Receita 3: Pickaxe de pedra
        recipes.add(new ShapedRecipeSystem(
                new StonePickaxe(),
                1,
                new String[]{
                        "SSS",
                        " W ",
                        " W "
                },
                // S corresponde à pedra e W corresponde ao Stick
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
}