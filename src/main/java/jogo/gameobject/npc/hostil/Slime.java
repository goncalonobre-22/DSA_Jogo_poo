package jogo.gameobject.npc.hostil;

import jogo.framework.math.Vec3;
import jogo.gameobject.character.Player;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;

public class Slime extends NPC {

    private Vec3 targetPos;
    private float speed = 3.0f;
    private final VoxelWorld world;
    private final Player player;

    // Física e movimento
    private float verticalVelocity = 0;
    private static final float GRAVITY = 24.0f;
    private static final float JUMP_POWER = 8.0f;
    private float jumpTimer = 0.0f;
    private static final float JUMP_INTERVAL = 1.0f;

    // Ataque
    private static final float ATTACK_RANGE = 1.8f;
    private static final float ATTACK_COOLDOWN = 1.5f;
    public float attackCooldownTimer = 0.0f;
    private final int ATTACK_DAMAGE = 5;

    private static final float PERCEPTION_RANGE = 30.0f;

    private final int health = 5;

    public Slime(String name, Vec3 spawnPos, VoxelWorld world, Player player) {
        super(name);
        this.position = new Vec3(spawnPos.x, spawnPos.y, spawnPos.z);
        this.world = world;
        this.player = player;
        setHealth(health);
    }

    public void setTarget(Vec3 target) {
        this.targetPos = target;
    }

    @Override
    public void updateAI(float tpf) {

        if (targetPos == null) return;

        jumpTimer -= tpf;
        attackCooldownTimer -= tpf;

        float dx = targetPos.x - position.x;
        float dz = targetPos.z - position.z;

        float distSq = dx * dx + dz * dz;
        float dist = (float)Math.sqrt(distSq);

        // Lógica de ataque
        if (dist < ATTACK_RANGE && attackCooldownTimer <= 0) {
            player.takeDamage(ATTACK_DAMAGE);
            System.out.println(this.getName() + " ATAQUE ATIVADO! DIST: " + dist +
                    " | Player sofreu " + ATTACK_DAMAGE + " de dano. Vida atual: " + player.getHealth());
            attackCooldownTimer = ATTACK_COOLDOWN;
            return; // Pára o movimento e AI para atacar.
        }




        if (dist > 0.1f && dist < PERCEPTION_RANGE) {

            float safeDist = dist;

            float nx = dx / safeDist;
            float nz = dz / safeDist;

            float moveX = nx * speed * tpf;
            float moveZ = nz * speed * tpf;

            // Colisão Horizontal
            float newX = position.x + moveX;
            float newZ = position.z + moveZ;

            int currentY = (int) Math.floor(position.y);

            // Tenta mover-se para X, se colidir, tenta saltar
            if (!world.isSolid((int)Math.floor(newX), currentY, (int)Math.floor(position.z))) {
                position.x = newX;
            } else if (verticalVelocity == 0 && jumpTimer <= 0) {
                jumpTimer = JUMP_INTERVAL;
            }

            // Tenta mover-se para Z, se colidir, tenta saltar
            if (!world.isSolid((int)Math.floor(position.x), currentY, (int)Math.floor(newZ))) {
                position.z = newZ;
            } else if (verticalVelocity == 0 && jumpTimer <= 0) {
                jumpTimer = JUMP_INTERVAL;
            }
        }


        // --- 3. Gravidade e Movimento Vertical (Salto) ---

        verticalVelocity -= GRAVITY * tpf;
        float moveY = verticalVelocity * tpf;
        float newY = position.y + moveY;

        int blockY = (int) Math.floor(position.y - 0.1f);

        boolean onGround = world.isSolid((int)Math.floor(position.x), blockY, (int)Math.floor(position.z));

        if (onGround && moveY < 0) { // A descer e colidiu com o chão
            position.y = blockY + 1.0f;
            verticalVelocity = 0;

            if (jumpTimer <= 0) {
                verticalVelocity = JUMP_POWER;
                jumpTimer = JUMP_INTERVAL;
            }

        } else {
            position.y = newY;
            if (!onGround) jumpTimer = JUMP_INTERVAL;
        }

        // Ajuste final para evitar entalar-se
        int blockUnderSlimeCenter = (int) Math.floor(position.y);
        if (world.isSolid((int)Math.floor(position.x), blockUnderSlimeCenter, (int)Math.floor(position.z))) {
            position.y = blockUnderSlimeCenter + 1.0f;
        }
    }
}