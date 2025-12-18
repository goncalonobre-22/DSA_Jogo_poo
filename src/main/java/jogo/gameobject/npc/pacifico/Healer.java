package jogo.gameobject.npc.pacifico;

import jogo.framework.math.Vec3;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;
import jogo.gameobject.character.Player;

public class Healer extends NPC {

    private final VoxelWorld world;
    private final Player player;

    // Alvo
    private Vec3 targetPos;

    // Movimento e Física
    private float speed = 1.0f;
    private float followSpeed = 2.5f;
    private float wanderTimer = 0;
    private static final float WANDER_INTERVAL = 8.0f;
    private Vec3 wanderTarget;
    private static final float WANDER_DISTANCE = 10.0f;

    // Cura
    private static final float HEAL_RANGE = 5.0f;
    private static final float HEAL_FOLLOW_RANGE = 10.0f; // Distância máxima para começar a seguir
    private static final float LOW_HEALTH_PERCENT = 1f;
    private static final int HEAL_AMOUNT = 5;
    private static final float HEAL_COOLDOWN = 3.0f;
    private float healCooldownTimer = 0;

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
