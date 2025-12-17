package jogo.gameobject.npc.hostil;

import jogo.framework.math.Vec3;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;
import jogo.gameobject.character.Player; // Se precisar de player para o takeDamage

public class Zombie extends NPC {

    private Vec3 targetPos;
    private float speed = 1.5f; // Mais lento que o Slime
    private final VoxelWorld world;
    private final Player player;

    // --- VARIÁVEIS DE FÍSICA E MOVIMENTO ---
    private static final float GRAVITY = 24.0f;
    private float verticalVelocity = 0;
    private final float STEP_HEIGHT = 1.05f; // Altura máxima que o Zumbi consegue subir (ligeiramente acima de 1 bloco)

    // --- VARIÁVEIS DE ATAQUE ---
    private static final float ATTACK_RANGE = 1.5f;
    private static final float ATTACK_COOLDOWN = 2.0f; // Ataque mais lento
    private float attackCooldownTimer = 0.0f;
    private final int ATTACK_DAMAGE = 8; // Mais dano que o Slime

    private static final float PERCEPTION_RANGE = 25.0f;

    public Zombie(String name, Vec3 spawnPos, VoxelWorld world, Player player) {
        super(name);
        this.position = new Vec3(spawnPos.x, spawnPos.y, spawnPos.z);
        this.world = world;
        this.player = player;
        setHealth(15); // Mais vida que o Slime
    }

    public void setTarget(Vec3 target) {
        this.targetPos = target;
    }

    @Override
    public void updateAI(float tpf) {

        if (targetPos == null) return;

        // Decrementa temporizador
        attackCooldownTimer -= tpf;

        float dx = targetPos.x - position.x;
        float dz = targetPos.z - position.z;

        float distSq = dx * dx + dz * dz;
        float dist = (float)Math.sqrt(distSq);

        // --- 1. Lógica de Ataque (PRIORIDADE) ---
        if (dist < ATTACK_RANGE && attackCooldownTimer <= 0) {

            // LÓGICA DE DANO CONSOLIDADA AQUI (APLICADA AO ZUMBI)
            player.takeDamage(ATTACK_DAMAGE);
            System.out.println(this.getName() + " ATAQUE ATIVADO! DIST: " + dist +
                    " | Player sofreu " + ATTACK_DAMAGE + " de dano. Vida atual: " + player.getHealth());
            // FIM LÓGICA DE DANO CONSOLIDADA

            attackCooldownTimer = ATTACK_COOLDOWN;
            return; // Pára o movimento e AI para atacar.
        }

        // Se estiver muito longe ou inválido, apenas aplica gravidade
        if (Float.isNaN(dist) || dist > PERCEPTION_RANGE || dist < 0.001f) {
            // Vai para a secção 3 (Gravidade)
        } else {
            // --- 2. Lógica de Perseguição Horizontal com Subida de Degraus ---

            float safeDist = dist;

            float nx = dx / safeDist;
            float nz = dz / safeDist;

            float moveX = nx * speed * tpf;
            float moveZ = nz * speed * tpf;

            float newX = position.x + moveX;
            float newZ = position.z + moveZ;

            int currentY = (int) Math.floor(position.y);
            int targetXBlock = (int) Math.floor(newX);
            int targetZBlock = (int) Math.floor(newZ);

            // Colisão e Degrau
            boolean collisionX = world.isSolid(targetXBlock, currentY, (int)Math.floor(position.z));
            boolean collisionZ = world.isSolid((int)Math.floor(position.x), currentY, targetZBlock);

            boolean stepBlockX = world.isSolid(targetXBlock, currentY + 1, (int)Math.floor(position.z));
            boolean stepBlockZ = world.isSolid((int)Math.floor(position.x), currentY + 1, targetZBlock);


            // Se houver colisão (X ou Z) e o bloco acima NÃO for sólido (degrau livre)
            if ((collisionX && !stepBlockX) || (collisionZ && !stepBlockZ)) {
                // Tenta subir
                if (position.y < currentY + STEP_HEIGHT) {
                    position.y += speed * tpf; // Levanta o Zumbi
                }
            }

            // Move horizontalmente (se o bloco acima da cabeça NÃO for sólido)
            if (!stepBlockX) position.x = newX;
            if (!stepBlockZ) position.z = newZ;
        }


        // --- 3. Gravidade (Queda Livre Suave) ---

        int blockBelow = (int) Math.floor(position.y - 0.1f);
        boolean onGround = world.isSolid((int)Math.floor(position.x), blockBelow, (int)Math.floor(position.z));

        if (!onGround) {
            verticalVelocity -= GRAVITY * tpf;
            float moveY = verticalVelocity * tpf;
            float newY = position.y + moveY;

            if (world.isSolid((int)Math.floor(position.x), (int)Math.floor(newY), (int)Math.floor(position.z)) && moveY < 0) {
                // Colidiu com o chão ao descer
                position.y = (int)Math.floor(newY) + 1.0f;
                verticalVelocity = 0;
            } else {
                position.y = newY;
            }
        } else {
            // Corrige a altura
            verticalVelocity = 0;
            if (position.y < blockBelow + 1.0f) {
                position.y = blockBelow + 1.0f;
            }
        }
    }
}