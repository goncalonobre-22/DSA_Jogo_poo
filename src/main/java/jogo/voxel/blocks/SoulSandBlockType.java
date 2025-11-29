package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
import jogo.voxel.VoxelBlockType;

public class SoulSandBlockType extends VoxelBlockType {
    public SoulSandBlockType() {
        super("Soul Sand");
    }

    @Override
    public int getHardness() {
        return 3;
    }

    @Override
    public String getMiningCategory() {
        return "GRANULAR";
    }

    @Override
    public float getSpeedMultiplier() {
        return 0.5f;
    }

    // isSolid() inherits true from base

    @Override
    public Material getMaterial(AssetManager assetManager) {
        Texture2D tex = (Texture2D) assetManager.loadTexture("Textures/soulSandBlock.png");
        Material s = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        s.setTexture("DiffuseMap", tex);
        s.setBoolean("UseMaterialColors", true);
        s.setColor("Diffuse", ColorRGBA.White);
        s.setColor("Specular", ColorRGBA.White.mult(0.02f)); // reduced specular
        s.setFloat("Shininess", 26f); // tighter, less intense highlight
        return s;
    }
}
