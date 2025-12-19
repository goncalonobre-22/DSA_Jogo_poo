package jogo.gameobject.npc.pacifico;

import jogo.framework.math.Vec3;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;
import jogo.gameobject.character.Player;

public class Healer extends NPC {

    /** Referência ao mundo de voxels para cálculos de colisão e posicionamento. */
    private final VoxelWorld world;

    /** Referência ao jogador que o Healer deve proteger e curar. */
    private final Player player;

    /** Posição alvo para o movimento (geralmente a posição do jogador). */
    private Vec3 targetPos;

    /** Velocidade de movimento base ao vaguear. */
    private float speed = 1.0f;

    /** Velocidade acelerada utilizada quando o Healer corre para ajudar o jogador. */
    private float followSpeed = 2.5f;

    /** Temporizador para controlar a mudança de direção no modo de vaguear. */
    private float wanderTimer = 0;

    /** Intervalo de tempo entre as mudanças de alvo no modo de vaguear. */
    private static final float WANDER_INTERVAL = 8.0f;

    /** Coordenadas do ponto para onde o Healer se desloca quando não está a seguir o jogador. */
    private Vec3 wanderTarget;

    /** Distância máxima que o Healer percorre em cada ciclo de movimento aleatório. */
    private static final float WANDER_DISTANCE = 10.0f;

    /** Raio de alcance (em metros) dentro do qual o Healer consegue aplicar a cura. */
    private static final float HEAL_RANGE = 5.0f;

    /** Distância máxima a que o Healer começará a perseguir o jogador para o curar. */
    private static final float HEAL_FOLLOW_RANGE = 10.0f;

    /** Percentagem de vida (1.0 = 100%) abaixo da qual o jogador é considerado elegível para cura. */
    private static final float LOW_HEALTH_PERCENT = 1f;

    /** Quantidade de pontos de vida restaurados por cada pulso de cura. */
    private static final int HEAL_AMOUNT = 5;

    /** Tempo de espera (em segundos) entre cada ação de cura. */
    private static final float HEAL_COOLDOWN = 3.0f;

    /** Temporizador interno para gerir o cooldown da habilidade de cura. */
    private float healCooldownTimer = 0;

    /**
     * Construtor da classe Healer.
     * Inicializa o NPC com 20 pontos de vida e define a sua posição inicial.
     * @param name Nome do NPC.
     * @param spawnPos Posição inicial no mundo.
     * @param world Referência ao sistema de voxels.
     * @param player Referência ao jogador alvo.
     */
    public Healer(String name, Vec3 spawnPos, VoxelWorld world, Player player) {
        super(name);
        this.position = new Vec3(spawnPos.x, spawnPos.y, spawnPos.z);
        this.world = world;
        this.player = player;
        setHealth(20);
        this.wanderTarget = this.position;
    }

    public void setTarget(Vec3 target) {
        this.targetPos = target;
    }

    /**
     * Atualiza a lógica de decisão, cura, movimento e física do Healer.
     * O Healer prioriza aproximar-se do jogador se este precisar de cura e estiver dentro do raio de 10m.
     * Caso contrário, vagueia aleatoriamente pelo cenário.
     * @param tpf Tempo por frame (Time Per Frame).
     */
    @Override
    public void updateAI(float tpf) {

        if (targetPos == null) return;

        healCooldownTimer -= tpf;
        wanderTimer -= tpf;

        // cálculos condicionais
        float dist = this.position.distance(targetPos);
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        // O jogador precisa de cura e está fora do alcance de cura (5m)
        boolean playerNeedsHealAndIsFar = (maxHealth > 0 && currentHealth / maxHealth <= LOW_HEALTH_PERCENT && dist > HEAL_RANGE);

        // Distância máxima para o Healer começar a seguir (10m)
        boolean isInFollowRange = dist < HEAL_FOLLOW_RANGE;

        // Lógica de Cura (Só cura se estiver no raio E for necessário)
        if (healCooldownTimer <= 0) {

            // O jogador precisa de cura E está dentro do raio
            if (!playerNeedsHealAndIsFar && dist < HEAL_RANGE && maxHealth > 0 && currentHealth / maxHealth <= LOW_HEALTH_PERCENT) {

                player.heal(HEAL_AMOUNT);
                System.out.println("Healer curou o jogador em " + HEAL_AMOUNT + " HP. Vida atual: " + player.getHealth());
                healCooldownTimer = HEAL_COOLDOWN;
            }
        }

        // Lógica de Movimento (Seguir OU Vaguear)
        Vec3 moveTarget = wanderTarget;
        float currentSpeed = speed;
        boolean isFollowing = false;

        // Se a vida está baixa e o jogador está dentro do raio de seguimento (10m)
        if (playerNeedsHealAndIsFar && isInFollowRange) {
            // CONDIÇÃO: RUSH TO THE PLAYER
            moveTarget = targetPos;
            currentSpeed = followSpeed;
            isFollowing = true;
            wanderTimer = WANDER_INTERVAL; // Congela o timer de vaguear para não interromper a perseguição
        }
        // Se não estiver a seguir (não precisa de cura ou está longe demais), vagueia
        else if (wanderTimer <= 0 || this.position.distance(wanderTarget) < 0.6f) {
            // CONDIÇÃO: Vaguear (Wander)
            float angle = (float) Math.random() * 360;
            float targetX = position.x + (float) Math.cos(angle) * WANDER_DISTANCE;
            float targetZ = position.z + (float) Math.sin(angle) * WANDER_DISTANCE;

            this.wanderTarget = new Vec3(targetX, position.y, targetZ);
            wanderTimer = WANDER_INTERVAL + (float) Math.random() * 2.0f;
            moveTarget = this.wanderTarget;
        }

        // Executar o movimento horizontal com colisão
        float dx = moveTarget.x - position.x;
        float dz = moveTarget.z - position.z;
        float distToTarget = (float) Math.sqrt(dx * dx + dz * dz);

        if (distToTarget > 0.1f) {
            float safeDist = distToTarget;
            float nx = dx / safeDist;
            float nz = dz / safeDist;

            float moveX = nx * currentSpeed * tpf;
            float moveZ = nz * currentSpeed * tpf;

            // --- COLISÃO HORIZONTAL (Herança de código de colisão) ---
            float newX = position.x + moveX;
            float newZ = position.z + moveZ;
            int currentY = (int) Math.floor(position.y);

            if (!world.isSolid((int) Math.floor(newX), currentY, (int) Math.floor(position.z))) {
                position.x = newX;
            }

            if (!world.isSolid((int) Math.floor(position.x), currentY, (int) Math.floor(newZ))) {
                position.z = newZ;
            }
            // FIM COLISÃO

            // Se está a seguir E chegou ao alcance de cura, para e espera pelo cooldown
            if (isFollowing && dist < HEAL_RANGE) {
                wanderTarget = this.position;
            }
        }

        // Gravidade

        int blockBelow = (int) Math.floor(position.y - 0.1f);
        boolean onGround = world.isSolid((int) Math.floor(position.x), blockBelow, (int) Math.floor(position.z));

        if (!onGround) {
            verticalVelocity -= GRAVITY * tpf;
            position.y += verticalVelocity * tpf;
        } else {
            verticalVelocity = 0;
            if (position.y < blockBelow + 1.0f) {
                position.y = blockBelow + 1.0f;
            }
        }
    }
}
