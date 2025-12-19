package jogo.util.furnace;

import jogo.gameobject.item.Item;
import jogo.gameobject.item.normalitems.Charcoal;
import jogo.gameobject.item.normalitems.MetalBar;
import jogo.gameobject.item.normalitems.WoodStick;
import jogo.gameobject.item.placeableitems.MetalOreBlockItem;
import jogo.gameobject.item.placeableitems.WoodBlockItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositório central para o sistema de fundição.
 * Gere o catálogo de receitas de processamento e a tabela de eficiência de combustíveis.
 */
public class FurnaceRegistry {
    /** Lista de todas as receitas de fundição registadas. */
    private static final List<FurnaceRecipe> recipes = new ArrayList<>();

    /** Mapa que associa o nome de um item à sua capacidade de queima (medida em unidades de 'melts'). */
    private static final Map<String, Float> fuelEfficiency = new HashMap<>();

    static {
        // Inicializa as receitas e os combustíveis ao carregar a classe.
        registerRecipes();
        registerFuel();
    }

    /**
     * Regista as receitas de transformação do jogo.
     * Exemplo: Minério de Metal -> Barra de Metal; Bloco de Madeira -> Carvão.
     */
    private static void registerRecipes() {
        recipes.add(new FurnaceRecipe(new MetalOreBlockItem(), new MetalBar()));
        recipes.add(new FurnaceRecipe(new WoodBlockItem(), new Charcoal()));
    }

    /**
     * Regista os itens que podem ser usados como combustível e as suas respetivas durações.
     * Carvão (2.0), Madeira (1.5) e Gravetos (0.5).
     */
    private static void registerFuel() {
        fuelEfficiency.put(new Charcoal().getName(), 2.0f);
        fuelEfficiency.put(new WoodBlockItem().getName(), 1.5f);
        fuelEfficiency.put(new WoodStick().getName(), 0.5f);
    }

    /**
     * Procura uma receita que aceite o item fornecido como entrada.
     * @param input O item que se pretende fundir.
     * @return A {@link FurnaceRecipe} correspondente ou null se não for processável.
     */
    public static FurnaceRecipe findRecipe(Item input) {
        if (input == null) return null;
        String inputName = input.getName();
        for (FurnaceRecipe recipe : recipes) {
            if (recipe.getInput().getName().equals(inputName)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Retorna a eficiência de queima de um item.
     * @param fuelItem O item a ser verificado como combustível.
     * @return O valor de 'melts' que o item fornece; retorna 0.0f se não for um combustível válido.
     */
    public static float getFuelEfficiency(Item fuelItem) {
        if (fuelItem == null) return 0.0f;
        return fuelEfficiency.getOrDefault(fuelItem.getName(), 0.0f);
    }
}