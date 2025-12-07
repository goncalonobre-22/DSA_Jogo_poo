package jogo.gameobject.item;

import jogo.voxel.VoxelBlockType;

public abstract class Tool  extends Item{
    public Tool(String nome){
        super(nome);
    }

    public float getMiningSpeed(){
        return 1.0f;
    }
    public float getAttackMultiplier() {
        return 1.0f; // Multiplicador padrão (1.0x para a Mão/Item básico)
    }

    public abstract float getMiningSpeed(VoxelBlockType type);
}
