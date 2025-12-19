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

    /**
     * Define se o bloco deve ser afetado pela gravidade (ex: Areia).
     * @return false por padrão.
     */
    public boolean isAffectedByGravity() {
        return false;
    }

    /**
     * Retorna a dureza (resistência) do bloco para efeitos de mineração.
     * @return Valor inteiro da dureza (padrão é 1).
     */
    public int getHardness() { return 1; }

    /**
     * Define a categoria de mineração (ex: "GRANULAR", "COMPACT") para determinar a eficácia de ferramentas.
     * @return String com a categoria (padrão é "DEFAULT").
     */
    public String getMiningCategory() { return "DEFAULT"; }

    /**
     * Retorna um multiplicador de velocidade de movimento para entidades que caminham sobre este bloco.
     * @return Multiplicador de velocidade (padrão é 1.0f).
     */
    public float getSpeedMultiplier() { return 1f; }

    /**
     * Indica se este bloco causa dano a entidades que entrem em contacto com ele (ex: Lava ou Espinhos).
     * @return false por padrão.
     */
    public boolean doesDamage() {
        return false; // Falso por padrão (não causa dano)
    }

    /**
     * Retorna a quantidade de pontos de vida a subtrair se o bloco causar dano.
     * @return Valor do dano (padrão é 2).
     */
    public int getDamageAmount() {
        return 2;
    }

    /**
     * Indica se o bloco requer atualizações lógicas periódicas (ticks).
     * @return false por padrão; deve ser true para blocos com lógica ativa (ex: crescimento de plantas).
     */
    public boolean isTickable() {
        return false; // Falso por padrão (a maioria dos blocos não precisa de update)
    }

    /**
     * Executa a lógica associada ao tick do bloco.
     * @param x Coordenada X do bloco.
     * @param y Coordenada Y do bloco.
     * @param z Coordenada Z do bloco.
     * @param world Referência ao mundo de voxels para manipulação de blocos.
     * @param tpf Tempo por frame (Time Per Frame).
     * @return true se o estado do bloco foi alterado durante o tick; false caso contrário.
     */
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
