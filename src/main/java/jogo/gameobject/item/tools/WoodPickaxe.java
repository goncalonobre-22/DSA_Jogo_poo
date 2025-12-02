package jogo.gameobject.item.tools;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.gameobject.item.Tool;
import jogo.voxel.VoxelBlockType;

public class WoodPickaxe extends Tool {
    public WoodPickaxe(){
        super("Wood Pickaxe");
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/woodPickaxe.png");
    }

    @Override
    public float getMiningSpeed(VoxelBlockType type) {
        String category = type.getMiningCategory();
        if (category.equals("GRANULAR")) {
            return 1f;
        }
        if (category.equals("COMPACT")) {
            return 1.5f;
        }
        return 1.25f;
    }
}