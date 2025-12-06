package jogo.gameobject.item.normalitems;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.gameobject.item.Item;

public class MetalBar extends Item {
    public MetalBar() {
        super("Metal Bar");
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/metalBar.png");
    }
}
