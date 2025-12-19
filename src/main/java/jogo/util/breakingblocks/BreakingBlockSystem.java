package jogo.util.breakingblocks;

import jogo.gameobject.item.Item;
import jogo.gameobject.item.Tool;
import jogo.voxel.VoxelWorld;
import jogo.voxel.VoxelBlockType;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Sistema responsável por gerir a destruição progressiva de blocos no mundo.
 * Controla o dano acumulado em cada bloco, aplica multiplicadores de mineração baseados
 * no item segurado e remove blocos do mundo quando a sua resistência chega a zero.
 */
public class BreakingBlockSystem {

    /** Referência ao mundo de voxels para aceder aos dados dos blocos. */
    private final VoxelWorld world;

    /** Mapa que armazena o progresso de quebra de cada bloco, usando as coordenadas "x,y,z" como chave. */
    private final Map<String, BreakBlockProgress> breakingBlocks = new HashMap<>();

    /**
     * Construtor do sistema de quebra de blocos.
     * @param world O mundo de voxels onde os blocos serão manipulados.
     */
    public BreakingBlockSystem(VoxelWorld world) {
        this.world = world;
    }

    /**
     * Processa um impacto (hit) num bloco específico.
     * Calcula o dano a aplicar com base na dureza do bloco e na eficiência do item utilizado.
     * * @param x Coordenada X do bloco.
     * @param y Coordenada Y do bloco.
     * @param z Coordenada Z do bloco.
     * @param heldItem O item que o jogador está a segurar no momento do impacto.
     * @return true se o bloco foi completamente destruído após este impacto; false caso contrário.
     */
    public boolean hitBlock(int x, int y, int z, Item heldItem) {
        String key = x + "," + y + "," + z;
        VoxelBlockType blockType = world.getPalette().get(world.getBlock(x, y, z));

        // Apenas blocos sólidos podem sofrer danos de quebra.
        if (!blockType.isSolid()) {
            return false;
        }

        float hardness = (float) blockType.getHardness();
        float multiplier = 1.0f;

        // Aplica o multiplicador de velocidade se o item for uma ferramenta adequada.
        if (heldItem instanceof Tool tool) {
            multiplier = tool.getMiningSpeed(blockType);
        }

        float damage = multiplier;

        // Se o dano de um único clique for superior à dureza, quebra imediatamente.
        if (damage >= hardness) {
            breakingBlocks.remove(key);
            return true;
        }

        // Obtém ou cria um novo registo de progresso para as coordenadas atingidas.
        BreakBlockProgress progress = breakingBlocks.get(key);
        if (progress == null) {
            progress = new BreakBlockProgress(hardness);
            breakingBlocks.put(key, progress);
        }

        boolean shouldBreak = progress.addDamage(damage);

        // Log de depuração do progresso de quebra.
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

    /**
     * Atualiza o estado de todos os blocos em processo de quebra.
     * Verifica se algum bloco excedeu o tempo limite (timeout) sem receber novos impactos.
     * * @param tpf Tempo por frame (Time Per Frame).
     * @return true se pelo menos um bloco teve o seu progresso reiniciado (reset) nesta atualização.
     */
    public boolean update(float tpf) {
        Iterator<Map.Entry<String, BreakBlockProgress>> it = breakingBlocks.entrySet().iterator();
        boolean reset = false;

        while (it.hasNext()) {
            Map.Entry<String, BreakBlockProgress> entry = it.next();
            BreakBlockProgress progress = entry.getValue();

            // Se o progresso expirou por inatividade, remove o bloco do mapa de quebra.
            if (progress.update(tpf)) {
                System.out.println("Progresso de quebra resetado (timeout)");
                it.remove();
                reset = true;
            }
        }
        return reset;
    }

    /**
     * Limpa todos os registos de progresso de quebra pendentes.
     * Utilizado para reiniciar o estado do sistema (ex: ao mudar de mundo ou nível).
     */
    public void cleanup() {
        breakingBlocks.clear();
    }
}