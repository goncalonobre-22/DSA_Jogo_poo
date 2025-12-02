package jogo.gameobject.item.placeableitems;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.gameobject.item.PlaceableItem;
import jogo.voxel.VoxelPalette;

public class StoneBlockItem extends PlaceableItem {

    public StoneBlockItem() {
        super("stone", VoxelPalette.STONE_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/stoneBlockItem.png");
    }
}
