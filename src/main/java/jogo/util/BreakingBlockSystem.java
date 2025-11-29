package jogo.util;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import jogo.voxel.VoxelWorld;
import jogo.voxel.VoxelWorld.Vector3i;
import jogo.voxel.VoxelBlockType;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class BreakingBlockSystem {

    private final VoxelWorld world;
    // Usar String como chave para garantir unicidade e corrigir o crash
    private final Map<String, BreakBlockProgress> breakingBlocks = new HashMap<>();

    public BreakingBlockSystem(VoxelWorld world) {
        this.world = world;
        // AssetManager e Node não são mais necessários neste sistema.
    }

    /**
     * Regista um hit num bloco e determina se deve quebrar.
     * @return true se o bloco deve quebrar, false caso contrário.
     */
    public boolean hitBlock(int x, int y, int z) {
        String key = x + "," + y + "," + z; // Chave única e estável para a posição
        VoxelBlockType blockType = world.getPalette().get(world.getBlock(x, y, z));
        int hardness = blockType.getHardness();

        if (!blockType.isSolid()) {
            return false;
        }

        if (hardness <= 1) {
            breakingBlocks.remove(key);
            return true;
        }

        BreakBlockProgress progress = breakingBlocks.get(key);
        if (progress == null) {
            progress = new BreakBlockProgress(new Vector3i(x, y, z), hardness);
            breakingBlocks.put(key, progress);
        }

        boolean shouldBreak = progress.addHit();
        System.out.println("Hit no bloco '" + blockType.getName() + "'. Hits: "
                + progress.getHitCount() + "/" + hardness);

        if (shouldBreak) {
            breakingBlocks.remove(key);
            return true;
        } else {
            return false;
        }
    }

    /** Chamado em cada frame para atualizar o timeout dos blocos em quebra. */
    public boolean update(float tpf) {
        Iterator<Map.Entry<String, BreakBlockProgress>> it = breakingBlocks.entrySet().iterator();
        boolean reset = false;

        while (it.hasNext()) {
            Map.Entry<String, BreakBlockProgress> entry = it.next();
            BreakBlockProgress progress = entry.getValue();

            if (progress.update(tpf)) {
                System.out.println("Progresso de quebra resetado (timeout)");
                it.remove();
                reset = true;
            }
        }
        return reset;
    }

    public void cleanup() {
        breakingBlocks.clear();
    }
}
