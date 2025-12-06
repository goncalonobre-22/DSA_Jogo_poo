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

public class FurnaceRegistry {
    private static final List<FurnaceRecipe> recipes = new ArrayList<>();
    // Nome do Item -> Eficiência de Combustível (Melt Units)
    private static final Map<String, Float> fuelEfficiency = new HashMap<>();

    static {
        registerRecipes();
        registerFuel();
    }

    private static void registerRecipes() {
        // Requerimento: Metal Ore -> Metal Bar
        recipes.add(new FurnaceRecipe(new MetalOreBlockItem(), new MetalBar()));

        // Requerimento: Wood Block -> Charcoal
        recipes.add(new FurnaceRecipe(new WoodBlockItem(), new Charcoal()));
    }

    private static void registerFuel() {
        // Requerimento: Carvão (Charcoal) = 2.0 melts
        fuelEfficiency.put(new Charcoal().getName(), 2.0f);

        // Requerimento: Bloco de Madeira (Wood) = 1.5 melts
        fuelEfficiency.put(new WoodBlockItem().getName(), 1.5f);

        // Requerimento: Stick = 0.5 melts
        fuelEfficiency.put(new WoodStick().getName(), 0.5f);
    }

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

    public static float getFuelEfficiency(Item fuelItem) {
        if (fuelItem == null) return 0.0f;
        return fuelEfficiency.getOrDefault(fuelItem.getName(), 0.0f);
    }
}
