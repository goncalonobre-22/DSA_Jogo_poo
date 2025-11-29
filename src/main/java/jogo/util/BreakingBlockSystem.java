package jogo.util;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import jogo.gameobject.item.Item;
import jogo.gameobject.item.Tool;
import jogo.voxel.VoxelWorld;
import jogo.voxel.VoxelWorld.Vector3i;
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

        // Dureza do bloco, convertida para float
        float hardness = (float) blockType.getHardness();

        // 1. Determinar o multiplicador (1.0x é o padrão da "mão")
        float multiplier = 1.0f;
        if (heldItem instanceof Tool tool) {
            multiplier = tool.getMiningSpeed(blockType);
        }

        // Dano efetivo: 1.0f de dano base por clique * multiplicador da ferramenta
        float damage = 1.0f * multiplier;

        // Se o dano for maior ou igual à dureza (quebra no 1º hit com bónus)
        if (damage >= hardness) {
            breakingBlocks.remove(key);
            return true;
        }

        // 2. Inicializar ou atualizar progresso
        BreakBlockProgress progress = breakingBlocks.get(key);
        if (progress == null) {
            // Cria um novo registo com a dureza original do bloco
            progress = new BreakBlockProgress(hardness);
            breakingBlocks.put(key, progress);
        }

        // 3. Aplicar o dano float
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
