package jogo.gameobject.item.placeableitems;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.gameobject.item.PlaceableItem;
import jogo.voxel.VoxelPalette;

public class MetalOreBlockItem extends PlaceableItem {
    public MetalOreBlockItem() {
        super("Iron Ore", VoxelPalette.METALORE_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/ironOreItem.png");
    }
}
