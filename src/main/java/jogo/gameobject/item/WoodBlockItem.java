package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.voxel.VoxelPalette;

public class WoodBlockItem extends PlaceableItem {
    public WoodBlockItem() {
        super("Wood", VoxelPalette.WOOD_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Textures/woodItem.png");
    }
}
