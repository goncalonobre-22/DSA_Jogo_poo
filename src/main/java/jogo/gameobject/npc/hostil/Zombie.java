package jogo.gameobject.npc.hostil;

import jogo.framework.math.Vec3;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;
import jogo.gameobject.character.Player; // Se precisar de player para o takeDamage

public class Zombie extends NPC {

    private Vec3 targetPos;
    private float speed = 1.5f;
    private final VoxelWorld world;
    private final Player player;

    // Física e movimento
    private static final float GRAVITY = 24.0f;
    private float verticalVelocity = 0;
    private final float STEP_HEIGHT = 1.05f;

    // Ataque
    private static final float ATTACK_RANGE = 1.5f;
    private static final float ATTACK_COOLDOWN = 2.0f;
    private float attackCooldownTimer = 0.0f;
    private final int ATTACK_DAMAGE = 8;

    private static final float PERCEPTION_RANGE = 25.0f;

    public Zombie(String name, Vec3 spawnPos, VoxelWorld world, Player player) {
        super(name);
        this.position = new Vec3(spawnPos.x, spawnPos.y, spawnPos.z);
        this.world = world;
        this.player = player;
        setHealth(15);
    }

    public void setTarget(Vec3 target) {
        this.targetPos = target;
    }

    @Override
    public void updateAI(float tpf) {

        if (targetPos == null) return;


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
            return;
        }

        if (Float.isNaN(dist) || dist > PERCEPTION_RANGE || dist < 0.001f) {
        } else {
            // Lógica de perseguição
            float safeDist = dist;

            float nx = dx / safeDist;
            float nz = dz / safeDist;

            float moveX = nx * speed * tpf;
            float moveZ = nz * speed * tpf;

            float newX = position.x + moveX;
            float newZ = position.z + moveZ;

            int currentY = (int) Math.floor(position.y);

            // Converter a posição futura para ver se há paredes
            int targetXBlock = (int) Math.floor(newX);
            int targetZBlock = (int) Math.floor(newZ);

            // Colisão e Degrau
            boolean collisionX = world.isSolid(targetXBlock, currentY, (int)Math.floor(position.z));
            boolean collisionZ = world.isSolid((int)Math.floor(position.x), currentY, targetZBlock);

            boolean stepBlockX = world.isSolid(targetXBlock, currentY + 1, (int)Math.floor(position.z));
            boolean stepBlockZ = world.isSolid((int)Math.floor(position.x), currentY + 1, targetZBlock);


            if ((collisionX && !stepBlockX) || (collisionZ && !stepBlockZ)) {
                if (position.y < currentY + STEP_HEIGHT) {
                    position.y += speed * tpf; // Levanta o Zumbi
                }
            }

            if (!stepBlockX) position.x = newX;
            if (!stepBlockZ) position.z = newZ;
        }


        // Gravidade

        int blockBelow = (int) Math.floor(position.y - 0.1f);
        boolean onGround = world.isSolid((int)Math.floor(position.x), blockBelow, (int)Math.floor(position.z));

        if (!onGround) {
            verticalVelocity -= GRAVITY * tpf;
            float moveY = verticalVelocity * tpf;
            float newY = position.y + moveY;

            if (world.isSolid((int)Math.floor(position.x), (int)Math.floor(newY), (int)Math.floor(position.z)) && moveY < 0) {
                position.y = (int)Math.floor(newY) + 1.0f;
                verticalVelocity = 0;
            } else {
                position.y = newY;
            }
        } else {
            verticalVelocity = 0;
            if (position.y < blockBelow + 1.0f) {
                position.y = blockBelow + 1.0f;
            }
        }
    }
}