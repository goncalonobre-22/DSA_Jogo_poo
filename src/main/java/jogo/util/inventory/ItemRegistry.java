package jogo.util.inventory;

import jogo.gameobject.item.*;
import jogo.gameobject.item.placeableitems.*;
import jogo.voxel.VoxelPalette;

import java.util.*;

public class ItemRegistry {
    private static final Map<Byte, Class<? extends PlaceableItem>> blockToItem = new HashMap<>();


    static {
        // Regista todos os items de blocos
        registerBlockItem(VoxelPalette.STONE_ID, StoneBlockItem.class);
        registerBlockItem(VoxelPalette.DIRT_ID, DirtBlockItem.class);
        registerBlockItem(VoxelPalette.WOOD_ID, WoodBlockItem.class);
        registerBlockItem(VoxelPalette.METALORE_ID, MetalOreBlockItem.class);
        registerBlockItem(VoxelPalette.SAND_ID, SandBlockItem.class);
        registerBlockItem(VoxelPalette.SOULSAND_ID, SoulSandBlockItem.class);
        registerBlockItem(VoxelPalette.PLANK_ID, PlankBlockItem.class);
        registerBlockItem(VoxelPalette.GRASS_ID, GrassBlockItem.class);
    }

    private static void registerBlockItem(byte blockId, Class<? extends PlaceableItem> itemClass) {
        blockToItem.put(blockId, itemClass);
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
}
