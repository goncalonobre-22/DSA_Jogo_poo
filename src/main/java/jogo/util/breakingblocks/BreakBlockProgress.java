package jogo.util.breakingblocks;

/** Armazena o progresso de quebra de um único bloco. */
public class BreakBlockProgress {
    public static final float RESET_TIME = 2.0f; // Tempo em segundos para resetar o progresso
    private final float maxHardness; // Dureza total (float)
    private float currentDamage = 0.0f; // Dano total recebido
    private float timeSinceLastHit = 0f;

    public BreakBlockProgress(float maxHardness) { // <--- AGORA RECEBE float
        this.maxHardness = maxHardness;
    }

    /** Adiciona dano e verifica se o bloco deve quebrar. */
    public boolean addDamage(float damage) {
        currentDamage += damage;
        timeSinceLastHit = 0f;
        return currentDamage >= maxHardness;
    }


    /** * Atualiza o temporizador e retorna true se o progresso deve ser resetado (timeout). */
    public boolean update(float tpf) {
        timeSinceLastHit += tpf;
        if (timeSinceLastHit >= RESET_TIME) {
            return true;
        }
        return false;
    }

    public float getMaxHardness() { return maxHardness; }
    public float getCurrentDamage() { return currentDamage; }
}
