package jogo.util.breakingblocks;

/**
 * Gere o progresso de destruição de um bloco específico no mundo.
 * Esta classe armazena a dureza máxima do bloco, o dano acumulado e controla
 * o tempo decorrido desde o último impacto para permitir o reset do progresso.
 */
public class BreakBlockProgress {

    /** Tempo de inatividade (em segundos) necessário para que o progresso de quebra seja reiniciado. */
    public static final float RESET_TIME = 2.0f;

    /** Resistência total (dureza) necessária para destruir o bloco. */
    private final float maxHardness;

    /** Quantidade de dano acumulada no bloco até ao momento. */
    private float currentDamage = 0.0f;

    /** Temporizador que contabiliza o tempo passado desde a última vez que o bloco foi atingido. */
    private float timeSinceLastHit = 0f;

    /**
     * Construtor da classe BreakBlockProgress.
     * @param maxHardness O valor de dureza máxima do bloco a ser destruído.
     */
    public BreakBlockProgress(float maxHardness) {
        this.maxHardness = maxHardness;
    }

    /**
     * Adiciona uma quantidade de dano ao progresso atual e reinicia o temporizador de inatividade.
     * @param damage Quantidade de dano a aplicar ao bloco.
     * @return true se o dano acumulado for igual ou superior à dureza máxima (bloco destruído); false caso contrário.
     */
    public boolean addDamage(float damage) {
        currentDamage += damage;
        timeSinceLastHit = 0f;
        return currentDamage >= maxHardness;
    }

    /**
     * Atualiza o temporizador de inatividade com base no tempo decorrido entre frames.
     * @param tpf Tempo por frame (Time Per Frame).
     * @return true se o tempo desde o último hit exceder o RESET_TIME, indicando que o progresso deve ser limpo; false caso contrário.
     */
    public boolean update(float tpf) {
        timeSinceLastHit += tpf;
        if (timeSinceLastHit >= RESET_TIME) {
            return true;
        }
        return false;
    }

    /**
     * Retorna a dureza máxima definida para este bloco.
     * @return O valor de maxHardness.
     */
    public float getMaxHardness() {
        return maxHardness;
    }

    /**
     * Retorna o total de dano acumulado no bloco no momento atual.
     * @return O valor de currentDamage.
     */
    public float getCurrentDamage() {
        return currentDamage;
    }
}