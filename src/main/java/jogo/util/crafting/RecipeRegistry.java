package jogo.util.crafting;

import jogo.gameobject.item.food.Bread;
import jogo.gameobject.item.normalitems.WoodStick;
import jogo.gameobject.item.placeableitems.FurnaceBlockItem;
import jogo.gameobject.item.placeableitems.StoneBlockItem;
import jogo.gameobject.item.placeableitems.WoodBlockItem;
import jogo.gameobject.item.tools.StonePickaxe;
import jogo.gameobject.item.tools.WoodAxe;
import jogo.gameobject.item.tools.WoodPickaxe;
import jogo.util.inventory.Stacks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repositório central para todas as receitas de crafting do jogo.
 * Esta classe é responsável por registar as combinações de itens necessárias para criar novos objetos
 * e por verificar se uma disposição na grelha de crafting corresponde a alguma receita válida.
 */
public class RecipeRegistry {

    /** Lista estática que armazena todas as instâncias de receitas registadas no sistema. */
    private static final List<RecipeSystem> recipes = new ArrayList<>();

    static {
        // Inicializa o registo de receitas assim que a classe é carregada.
        registerRecipes();
    }

    /**
     * Define e adiciona todas as receitas disponíveis ao sistema.
     * Configura o formato (pattern) de 3x3 e os ingredientes correspondentes para itens como:
     * WoodStick, WoodPickaxe, StonePickaxe, Bread, WoodAxe e Furnace.
     */
    private static void registerRecipes() {
        // Receita 1: WoodStick - Criado com blocos de madeira na vertical.
        recipes.add(new ShapedRecipeSystem(
                new WoodStick(),
                4,
                new String[]{
                        " W ",
                        " W ",
                        "   "
                } ,
                Map.of('W', new WoodBlockItem())
        ));

        // Receita 2: Pickaxe de madeira - Três blocos de madeira e dois gravetos.
        recipes.add(new ShapedRecipeSystem(
                new WoodPickaxe(),
                1,
                new String[]{
                        "WWW",
                        " S ",
                        " S "
                },
                Map.of(
                        'W', new WoodBlockItem(),
                        'S', new WoodStick()
                )
        ));

        // Receita 3: Pickaxe de pedra - Três blocos de pedra e dois gravetos.
        recipes.add(new ShapedRecipeSystem(
                new StonePickaxe(),
                1,
                new String[]{
                        "SSS",
                        " W ",
                        " W "
                },
                Map.of(
                        'S', new StoneBlockItem(),
                        'W', new WoodStick()
                )
        ));

        // Receita 4: Pão - Três blocos de madeira na horizontal (conforme implementação atual).
        recipes.add(new ShapedRecipeSystem(
                new Bread(),
                1,
                new String[]{
                        "   ",
                        "   ",
                        "WWW"
                },
                Map.of(
                        'W', new WoodBlockItem()
                )
        ));

        // Receita 5: Machado de madeira - Disposição específica de madeira e gravetos.
        recipes.add(new ShapedRecipeSystem(
                new WoodAxe(),
                1,
                new String[]{
                        "WW ",
                        "WS ",
                        " S "
                },
                Map.of(
                        'W', new WoodBlockItem(),
                        'S', new WoodStick()
                )
        ));

        // Receita 6: Fornalha - Oito blocos de pedra em redor de um centro vazio.
        recipes.add(new ShapedRecipeSystem(
                new FurnaceBlockItem(),
                1,
                new String[]{
                        "SSS",
                        "S S",
                        "SSS"
                },
                Map.of(
                        'S', new StoneBlockItem()
                )
        ));
    }

    /**
     * Procura no registo uma receita que coincida com a disposição atual dos itens na grelha de crafting.
     * @param grid Array de Stacks representando os 9 slots (3x3) da bancada de trabalho.
     * @return A instância de {@link RecipeSystem} correspondente, ou null se não houver combinação válida.
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