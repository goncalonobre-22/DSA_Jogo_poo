package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.voxel.VoxelPalette;

public class GrassBlockItem extends PlaceableItem {
    public GrassBlockItem() {
        super("Grass", VoxelPalette.GRASS_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/grassBlockItem.png");
    }
}
