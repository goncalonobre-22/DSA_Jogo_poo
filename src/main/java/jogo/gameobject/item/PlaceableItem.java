package jogo.gameobject.item;

public abstract class PlaceableItem extends Item{
    private final byte blockId;

    protected PlaceableItem(String name, byte blockId) {
        super(name);
        this.blockId = blockId;
    }

    public byte getBlockId() {
        return blockId;
    }
}
