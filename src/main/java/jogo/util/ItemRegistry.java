package jogo.util;

import jogo.gameobject.GameObject;
import jogo.gameobject.item.*;
import jogo.voxel.VoxelPalette;

import java.util.*;

public class ItemRegistry {
    private static final Map<Byte, Class<? extends PlaceableItem>> blockToItem = new HashMap<>();
    private static final Map<String, Class<? extends Item>> nameToItem = new HashMap<>();
    private static final List<Class<? extends Item>> recipeClasses = new ArrayList<>();


    static {
        // Regista todos os items de blocos
        registerBlockItem(VoxelPalette.STONE_ID, StoneBlockItem.class);
        registerBlockItem(VoxelPalette.DIRT_ID, DirtBlockItem.class);
        registerBlockItem(VoxelPalette.WOOD_ID, WoodBlockItem.class);
        registerBlockItem(VoxelPalette.METALORE_ID, MetalOreBlockItem.class);
        registerBlockItem(VoxelPalette.SAND_ID, SandBlockItem.class);
        registerBlockItem(VoxelPalette.SOULSAND_ID, SoulSandBlockItem.class);

        registerNonBlockItem(WoodStick.class);
    }

    private static void registerBlockItem(byte blockId, Class<? extends PlaceableItem> itemClass) {
        blockToItem.put(blockId, itemClass);
        try {
            Item instance = itemClass.getDeclaredConstructor().newInstance();
            nameToItem.put(instance.getName(), itemClass);
        } catch (Exception e) {
            System.err.println("Erro ao registar item: " + itemClass.getName());
            e.printStackTrace();
        }
    }

    // Cria o item correspondente ao blockId
    public static PlaceableItem createItemFromBlock(byte blockId) {
        Class<? extends PlaceableItem> itemClass = blockToItem.get(blockId);
        if (itemClass == null) {
            System.err.println("Nenhum item registado para bloco ID: " + blockId);
            return null;
        }

        try {
            return itemClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Erro ao criar item para bloco " + blockId);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cria um item pelo nome.
     */
    public static Item createItemByName(String name) {
        Class<? extends Item> itemClass = nameToItem.get(name);
        if (itemClass == null) {
            System.err.println("Nenhum item com nome: " + name);
            return null;
        }

        try {
            return itemClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Erro ao criar item " + name);
            e.printStackTrace();
            return null;
        }
    }

    private static void registerNonBlockItem(Class<? extends Item> itemClass) { // <-- NOVO MÉTODO
        try {
            // Cria uma instância para registar o nome
            Item instance = itemClass.getDeclaredConstructor().newInstance();
            nameToItem.put(instance.getName(), itemClass);

            // Adiciona à lista de receitas
            recipeClasses.add(itemClass);
        } catch (Exception e) {
            System.err.println("Erro ao registar item não-bloco: " + itemClass.getName());
            e.printStackTrace();
        }
    }
}
