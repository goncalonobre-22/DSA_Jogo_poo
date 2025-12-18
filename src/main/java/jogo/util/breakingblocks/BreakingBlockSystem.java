package jogo.util.breakingblocks;

import jogo.gameobject.item.Item;
import jogo.gameobject.item.Tool;
import jogo.voxel.VoxelWorld;
import jogo.voxel.VoxelBlockType;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class BreakingBlockSystem {

    private final VoxelWorld world;
    private final Map<String, BreakBlockProgress> breakingBlocks = new HashMap<>();

    public BreakingBlockSystem(VoxelWorld world) {
        this.world = world;
    }

    public boolean hitBlock(int x, int y, int z, Item heldItem) {
        String key = x + "," + y + "," + z;
        VoxelBlockType blockType = world.getPalette().get(world.getBlock(x, y, z));

        if (!blockType.isSolid()) {
            return false;
        }

        float hardness = (float) blockType.getHardness();

        float multiplier = 1.0f;
        if (heldItem instanceof Tool tool) {
            multiplier = tool.getMiningSpeed(blockType);
        }

        float damage = multiplier;

        if (damage >= hardness) {
            breakingBlocks.remove(key);
            return true;
        }

        BreakBlockProgress progress = breakingBlocks.get(key);
        if (progress == null) {
            progress = new BreakBlockProgress(hardness);
            breakingBlocks.put(key, progress);
        }

        boolean shouldBreak = progress.addDamage(damage);

        System.out.println("Hit no bloco '" + blockType.getName() +
                "' (Categoria: " + blockType.getMiningCategory() +
                "). Dano/Clique: " + String.format("%.2f", damage) +
                ". Progresso: " + String.format("%.2f", progress.getCurrentDamage()) +
                " / " + String.format("%.2f", progress.getMaxHardness()));

        if (shouldBreak) {
            breakingBlocks.remove(key);
            return true;
        } else {
            return false;
        }

    }

    // atualiza o timeout dos blocos em quebra.
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
