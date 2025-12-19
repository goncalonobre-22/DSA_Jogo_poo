package jogo.util.inventory;

import jogo.gameobject.item.*;
import jogo.gameobject.item.placeableitems.*;
import jogo.voxel.VoxelPalette;
import java.util.*;

/**
 * Gestor central para o mapeamento entre IDs de blocos do mundo e as suas respetivas classes de itens.
 * Permite a criação dinâmica de objetos de item a partir de IDs de voxels.
 */
public class ItemRegistry {
    /** Mapa que associa o ID de um bloco (byte) à classe Java do item correspondente que pode ser colocado. */
    private static final Map<Byte, Class<? extends PlaceableItem>> blockToItem = new HashMap<>();

    static {
        // Regista todos os mapeamentos iniciais de blocos para itens.
        registerBlockItem(VoxelPalette.STONE_ID, StoneBlockItem.class);
        registerBlockItem(VoxelPalette.DIRT_ID, DirtBlockItem.class);
        registerBlockItem(VoxelPalette.WOOD_ID, WoodBlockItem.class);
        registerBlockItem(VoxelPalette.METALORE_ID, MetalOreBlockItem.class);
        registerBlockItem(VoxelPalette.SAND_ID, SandBlockItem.class);
        registerBlockItem(VoxelPalette.SOULSAND_ID, SoulSandBlockItem.class);
        registerBlockItem(VoxelPalette.PLANK_ID, PlankBlockItem.class);
        registerBlockItem(VoxelPalette.GRASS_ID, GrassBlockItem.class);
    }

    /**
     * Regista um novo mapeamento entre um ID de bloco e uma classe de item.
     * @param blockId O identificador numérico do bloco.
     * @param itemClass A classe Java que estende PlaceableItem associada ao bloco.
     */
    private static void registerBlockItem(byte blockId, Class<? extends PlaceableItem> itemClass) {
        blockToItem.put(blockId, itemClass);
    }

    /**
     * Cria uma nova instância de um item com base no ID do bloco fornecido.
     * Utiliza reflexão para instanciar o objeto através do seu construtor padrão.
     * @param blockId O ID do bloco do qual se quer gerar um item.
     * @return Uma nova instância de {@link PlaceableItem}, ou null se não houver mapeamento ou ocorrer erro.
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
}