package jogo.gameobject.item;

import jogo.voxel.VoxelBlockType;

public abstract class Tool  extends Item{
    public Tool(String nome){
        super(nome);
    }

    public float getMiningSpeed(){
        return 1.0f;
    }

    public abstract float getMiningSpeed(VoxelBlockType type);
}
