package jogo.gameobject.item;

public abstract class PlaceableItem extends Item{
    private final byte blockId;

    protected PlaceableItem(String name, byte blockId) {
        super(name);
        this.blockId = blockId;
    }

    /**
     * Obtém o ID do bloco que este item representa.
     */
    public byte getBlockId() {
        return blockId;
    }
}
