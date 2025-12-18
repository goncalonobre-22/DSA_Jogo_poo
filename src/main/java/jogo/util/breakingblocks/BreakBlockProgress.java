package jogo.util.breakingblocks;

public class BreakBlockProgress {
    public static final float RESET_TIME = 2.0f;
    private final float maxHardness;
    private float currentDamage = 0.0f;
    private float timeSinceLastHit = 0f;

    public BreakBlockProgress(float maxHardness) { // <--- AGORA RECEBE float
        this.maxHardness = maxHardness;
    }

    // Adiciona dano
    public boolean addDamage(float damage) {
        currentDamage += damage;
        timeSinceLastHit = 0f;
        return currentDamage >= maxHardness;
    }


    //Atualiza o temporizador
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
