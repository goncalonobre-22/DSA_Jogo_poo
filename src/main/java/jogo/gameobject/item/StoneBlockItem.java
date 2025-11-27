package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import jogo.voxel.VoxelPalette;

public class StoneBlockItem extends PlaceableItem{

    public StoneBlockItem() {
        super("stone", VoxelPalette.STONE_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Textures/stoneBlockItem.png");
    }
}
