package jogo.engine;

import jogo.gameobject.GameObject;
import jogo.gameobject.item.*;
import jogo.voxel.VoxelPalette;

import java.util.*;

public class GameRegistry {
    private final List<GameObject> objects = new ArrayList<>();
    private static final Map<Byte, Class<? extends PlaceableItem>> blockToItem = new HashMap<>();
    private static final Map<String, Class<? extends Item>> nameToItem = new HashMap<>();
    private static final List<Class<? extends Item>> recipeClasses = new ArrayList<>();

    public synchronized void add(GameObject obj) {
        if (obj != null && !objects.contains(obj)) {
            objects.add(obj);
        }
    }

    public synchronized void remove(GameObject obj) {
        objects.remove(obj);
    }

    public synchronized List<GameObject> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(objects));
    }

    static {
        // Regista todos os items de blocos
        registerBlockItem(VoxelPalette.STONE_ID, StoneBlockItem.class);
        registerBlockItem(VoxelPalette.DIRT_ID, DirtBlockItem.class);
        registerBlockItem(VoxelPalette.WOOD_ID, WoodBlockItem.class);
        registerBlockItem(VoxelPalette.METALORE_ID, MetalOreBlockItem.class);
        registerBlockItem(VoxelPalette.SAND_ID, SandBlockItem.class);

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

    /**
     * Cria um item a partir de um block ID.
     */
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

    /**
     * Verifica se existe um item para este bloco.
     */
    public static boolean hasItemForBlock(byte blockId) {
        return blockToItem.containsKey(blockId);
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

