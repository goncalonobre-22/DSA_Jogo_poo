package jogo.gameobject.item.tools;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.gameobject.item.Tool;
import jogo.voxel.VoxelBlockType;

public class StonePickaxe extends Tool {
    // Construtor sem argumentos para o GameRegistry
    public StonePickaxe() {
        super("Stone Pickaxe");
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        // Crie e adicione Textures/sticks.png ao seu projeto
        return assetManager.loadTexture("Interface/StonePickaxe.png");
    }

    @Override
    public float getMiningSpeed(VoxelBlockType type) {
        String category = type.getMiningCategory();
        if (category.equals("GRANULAR")) {
            return 1.25f;
        }
        if (category.equals("COMPACT")) {
            return 2f;
        }
        return 1.5f;
    }
}
