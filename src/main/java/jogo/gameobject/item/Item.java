package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import jogo.gameobject.GameObject;

public abstract class Item extends GameObject {

    protected Item(String name) {
        super(name);
    }

    public abstract Texture getIcon(AssetManager assetManager);

    public void onInteract() {
        // Hook for interaction logic (engine will route interactions)
    }


}
