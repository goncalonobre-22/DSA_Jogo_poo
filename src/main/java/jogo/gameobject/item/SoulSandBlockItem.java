package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.voxel.VoxelBlockType;
import jogo.voxel.VoxelPalette;

public class SoulSandBlockItem extends PlaceableItem {
    public SoulSandBlockItem() {
        super("Soul Sand", VoxelPalette.SOULSAND_ID);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/soulSandItem.png");
    }
}
