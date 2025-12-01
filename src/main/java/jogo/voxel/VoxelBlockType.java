package jogo.voxel;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;

public abstract class VoxelBlockType {
    private final String name;

    protected VoxelBlockType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Whether this block is physically solid (collides/occludes). */
    public boolean isSolid() { return true; }

    public boolean isAffectedByGravity() {
        return false;
    }

    public int getHardness() { return 1; }

    public String getMiningCategory() { return "DEFAULT"; }

    public float getSpeedMultiplier() { return 1f; }

    public boolean doesDamage() {
        return false; // Falso por padrão (não causa dano)
    }

    //Quantidade de dano que o bloco causa
    public int getDamageAmount() {
        return 2;
    }

    public boolean isTickable() {
        return false; // Falso por padrão (a maioria dos blocos não precisa de update)
    }

    public boolean onTick(int x, int y, int z, VoxelWorld world, float tpf) {
        return false;
    }

    /**
     * Returns the Material for this block type. Override in subclasses for custom materials.
     */
    public abstract Material getMaterial(AssetManager assetManager);

    /**
     * Returns the Material for this block type at a specific block position.
     * Default implementation ignores the position for backward compatibility.
     * Subclasses can override to use blockPos.
     */
    public Material getMaterial(AssetManager assetManager, jogo.framework.math.Vec3 blockPos) {
        return getMaterial(assetManager);
    }
}
