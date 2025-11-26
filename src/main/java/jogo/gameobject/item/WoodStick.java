package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.util.Stacks;

public class WoodStick extends Item {

    public static final String NAME = "Stick";

    // Construtor sem argumentos para o GameRegistry
    public WoodStick() {
        super(NAME);
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        // Crie e adicione Textures/sticks.png ao seu projeto
        return assetManager.loadTexture("Textures/stick.png");
    }
}
