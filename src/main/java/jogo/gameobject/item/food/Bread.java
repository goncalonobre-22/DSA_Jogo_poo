package jogo.gameobject.item.food;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.Item;

public class Bread extends Item {
    public Player player;
    public Bread() {
        super("Bread");
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/bread.png");
    }

    @Override
    public void onInteract() {
        if (player != null && player.getInventory().removeItem(this, 1)) {
            player.setHunger(player.getHunger() + 30);
            System.out.println("Consumiu Pão, Fome +30. Fome atual: " + player.getHunger());
        }
    }
}
