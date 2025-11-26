package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;

public class StonePickaxe extends Item{
    public static final String NAME = "Stone Pickaxe";

    // Construtor sem argumentos para o GameRegistry
    public StonePickaxe() {
        super(NAME);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        // Crie e adicione Textures/sticks.png ao seu projeto
        return assetManager.loadTexture("Textures/StonePickaxe.png");
    }
}
