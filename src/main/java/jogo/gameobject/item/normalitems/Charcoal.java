package jogo.gameobject.item.normalitems;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.gameobject.item.Item;

public class Charcoal extends Item {
    public Charcoal() {
        super("Charcoal");
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/charcoal.png");
    }
}
