package jogo.gameobject.item.tools;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.Camera;
import com.jme3.texture.Texture;
import jogo.appstate.WorldAppState;
import jogo.gameobject.item.Tool;
import jogo.util.Hit;
import jogo.voxel.VoxelBlockType;
import jogo.voxel.VoxelPalette;
import jogo.voxel.VoxelWorld;

import java.util.Optional;

public class WoodAxe extends Tool {
    public WorldAppState worldAppState;
    public Camera camera;
    public WoodAxe(){
        super("Wood Axe");
    }

    @Override
    public Texture getIcon(AssetManager assetManager) {
        // Crie e adicione Textures/sticks.png ao seu projeto
        return assetManager.loadTexture("Interface/woodAxe.png");
    }

    @Override
    public float getMiningSpeed(VoxelBlockType type) {
        String category = type.getMiningCategory();
        if (category.equals("GRANULAR")) {
            return 1.25f;
        }
        if (category.equals("COMPACT")) {
            return 1f;
        }
        return 1.75f;
    }

    @Override
    public void onInteract() {
        if (worldAppState == null || camera == null) {
            System.out.println("Erro: WoodAxe não tem contexto de mundo/câmera injetado.");
            return;
        }

        VoxelWorld vw = worldAppState.getVoxelWorld();
        // Usa um alcance de 6f
        Optional<Hit> pick = vw.pickFirstSolid(camera, 6f);

        pick.ifPresent(hit -> {
            VoxelWorld.Vector3i cell = hit.cell;
            byte blockId = vw.getBlock(cell.x, cell.y, cell.z);
            // ... (rest of the code omitted for brevity)

            if (blockId == VoxelPalette.WOOD_ID) {
                // Transforma Wood Block (ID 5) em Plank Block (ID 7)
                vw.setBlock(cell.x, cell.y, cell.z, VoxelPalette.PLANK_ID);

                // Reconstroi o chunk e a física
                worldAppState.getVoxelWorld().rebuildDirtyChunks(worldAppState.getPhysicsSpace());

                System.out.println("Wood Block transformado em Plank Block em: " + cell.x + "," + cell.y + "," + cell.z);
            } // ... (rest of the code omitted for brevity)
        });

        if (pick.isEmpty()) {
            System.out.println("Nenhum bloco sólido encontrado no alcance para interagir.");
        }
    }

}
