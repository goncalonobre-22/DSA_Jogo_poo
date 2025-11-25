package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import jogo.voxel.VoxelPalette;

public class DirtBlockItem extends PlaceableItem {
    public DirtBlockItem() {
        super("dirt", VoxelPalette.DIRT_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Textures/dirt.png");
    }
}
