package jogo.util;

import jogo.voxel.VoxelWorld.Vector3i;

/** Armazena o progresso de quebra de um único bloco. */
public class BreakBlockProgress {
    public static final float RESET_TIME = 2.0f; // Tempo em segundos para resetar o progresso
    private final int requiredHits;
    private int hitCount = 0;
    private float timeSinceLastHit = 0f;

    public BreakBlockProgress(Vector3i position, int requiredHits) {
        // A posição é apenas armazenada no BreakingBlockSystem (como chave)
        this.requiredHits = requiredHits;
    }

    /** Adiciona um hit e verifica se o bloco deve quebrar. */
    public boolean addHit() {
        hitCount++;
        timeSinceLastHit = 0f; // Resetar o temporizador após o hit
        return hitCount >= requiredHits;
    }

    /** * Atualiza o temporizador e retorna true se o progresso deve ser resetado (timeout).
     * @param tpf Tempo por frame.
     */
    public boolean update(float tpf) {
        timeSinceLastHit += tpf;
        if (timeSinceLastHit >= RESET_TIME) {
            return true;
        }
        return false;
    }

    public int getRequiredHits() { return requiredHits; }
    public int getHitCount() { return hitCount; }
}
