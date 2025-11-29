package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
import jogo.util.ProcTextures;
import jogo.voxel.VoxelBlockType;
import jogo.voxel.VoxelWorld;

public class SandBlockType extends VoxelBlockType {
    public SandBlockType() {
        super("sand");
    }

    @Override
    public int getHardness() {
        return 3;
    }

    // isSolid() inherits true from base

    @Override
    public Material getMaterial(AssetManager assetManager) {
        Texture2D tex = (Texture2D) assetManager.loadTexture("Textures/sand.png");
        Material s = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        s.setTexture("DiffuseMap", tex);
        s.setBoolean("UseMaterialColors", true);
        s.setColor("Diffuse", ColorRGBA.White);
        s.setColor("Specular", ColorRGBA.White.mult(0.02f)); // reduced specular
        s.setFloat("Shininess", 26f); // tighter, less intense highlight
        return s;
    }


}
