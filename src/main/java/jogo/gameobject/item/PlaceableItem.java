package jogo.gameobject.item;

/**
 * Classe abstrata que representa um item que pode ser colocado no mundo como um bloco.
 * Estende a funcionalidade de {@link Item} ao associar o item a um identificador de bloco específico.
 */
public abstract class PlaceableItem extends Item {

    /** Identificador único do tipo de bloco que este item coloca no mundo. */
    private final byte blockId;

    /**
     * Construtor da classe PlaceableItem.
     * @param name O nome do item.
     * @param blockId O ID do bloco correspondente na palete de voxels.
     */
    protected PlaceableItem(String name, byte blockId) {
        super(name);
        this.blockId = blockId;
    }

    /**
     * Retorna o identificador do bloco associado a este item.
     * Este ID é utilizado pelo motor para determinar que tipo de bloco deve ser criado quando o jogador interage com o mundo.
     * @return O ID do bloco em formato byte.
     */
    public byte getBlockId() {
        return blockId;
    }
}