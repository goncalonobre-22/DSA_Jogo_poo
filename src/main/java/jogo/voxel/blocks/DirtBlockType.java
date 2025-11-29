package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
import jogo.util.ProcTextures;
import jogo.voxel.VoxelBlockType;

public class DirtBlockType extends VoxelBlockType {
    public DirtBlockType() {
        super("dirt");
    }

    @Override
    public int getHardness() {
        return 3;
    }

    @Override
    public String getMiningCategory() {
        return "GRANULAR";
    }



    // isSolid() inherits true from base

    @Override
    public Material getMaterial(AssetManager assetManager) {
        Texture2D tex = (Texture2D) assetManager.loadTexture("Textures/dirt.png");
        Material d = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        d.setTexture("DiffuseMap", tex);
        d.setBoolean("UseMaterialColors", true);
        d.setColor("Diffuse", ColorRGBA.White);
        d.setColor("Specular", ColorRGBA.White.mult(0.02f)); // reduced specular
        d.setFloat("Shininess", 100f); // tighter, less intense highlight
        return d;
    }
}
