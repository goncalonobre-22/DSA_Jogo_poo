package jogo.voxel.blocks.natural;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
import jogo.voxel.VoxelBlockType;

public class PlankBlockType extends VoxelBlockType {

    public  PlankBlockType() {
        super("Plank Block");
    }

    public int getHardness() {
        return 4;
    }

    @Override
    public String getMiningCategory() {
        return "NATURAL";
    }

    @Override
    public Material getMaterial(AssetManager assetManager) {
        Texture2D tex = (Texture2D) assetManager.loadTexture("Textures/plankBlock.png");
        Material s = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        s.setTexture("DiffuseMap", tex);
        s.setBoolean("UseMaterialColors", true);
        s.setColor("Diffuse", ColorRGBA.White);
        s.setColor("Specular", ColorRGBA.White.mult(0.02f)); // reduced specular
        s.setFloat("Shininess", 26f); // tighter, less intense highlight
        return s;
    }
}
