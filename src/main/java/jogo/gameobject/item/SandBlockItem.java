package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.voxel.VoxelPalette;

public class SandBlockItem extends PlaceableItem {
    public SandBlockItem() {
        super("Sand", VoxelPalette.SAND_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Textures/sand.png");
    }
}
