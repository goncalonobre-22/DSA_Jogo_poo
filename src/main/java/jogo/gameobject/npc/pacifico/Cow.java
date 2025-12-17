package jogo.gameobject.npc.pacifico;

import jogo.framework.math.Vec3;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;
import jogo.gameobject.character.Player;

public class Cow extends NPC {

    private final VoxelWorld world;
    private final Player player;

    // --- VARIÁVEIS DE ALVO ---
    private Vec3 targetPos;

    // --- VARIÁVEIS DE MOVIMENTO E FÍSICA ---
    private float speed = 0.8f;
    private float wanderTimer = 0;
    private static final float WANDER_INTERVAL = 5.0f;
    private Vec3 wanderTarget;
    private static final float WANDER_DISTANCE = 5.0f;

    // Usamos os campos 'verticalVelocity' e 'GRAVITY' herdados de NPC.java

    public Cow(String name, Vec3 spawnPos, VoxelWorld world, Player player) {
        super(name);
        this.position = new Vec3(spawnPos.x, spawnPos.y, spawnPos.z);
        this.world = world;
        this.player = player;
        setHealth(10);
        this.wanderTarget = this.position;
    }

    public void setTarget(Vec3 target) {
        this.targetPos = target;
    }

    @Override
    public void updateAI(float tpf) {

        if (targetPos == null) return;

        wanderTimer -= tpf;

        // --- 1. Lógica de Vaguear (Wander) ---
        if (wanderTimer <= 0) {
            float angle = (float) Math.random() * 360;
            float targetX = position.x + (float) Math.cos(angle) * WANDER_DISTANCE;
            float targetZ = position.z + (float) Math.sin(angle) * WANDER_DISTANCE;

            this.wanderTarget = new Vec3(targetX, position.y, targetZ);
            wanderTimer = WANDER_INTERVAL + (float) Math.random() * 2.0f;
        }

        // Perseguir o wanderTarget com Colisão
        float dx = wanderTarget.x - position.x;
        float dz = wanderTarget.z - position.z;
        float distSq = dx * dx + dz * dz;
        float dist = (float) Math.sqrt(distSq);

        if (dist > 0.5f) {
            float safeDist = dist;
            float nx = dx / safeDist;
            float nz = dz / safeDist;

            float moveX = nx * speed * tpf;
            float moveZ = nz * speed * tpf;

            // --- COLISÃO HORIZONTAL (CORRIGIDO) ---
            float newX = position.x + moveX;
            float newZ = position.z + moveZ;
            int currentY = (int) Math.floor(position.y);

            // Tenta mover-se para X (Impede atravessar paredes)
            if (!world.isSolid((int)Math.floor(newX), currentY, (int)Math.floor(position.z))) {
                position.x = newX;
            }

            // Tenta mover-se para Z (Impede atravessar paredes)
            if (!world.isSolid((int)Math.floor(position.x), currentY, (int)Math.floor(newZ))) {
                position.z = newZ;
            }
            // FIM COLISÃO

            // Se atingir o alvo, redefine o wanderTarget para o local atual para parar
            if (this.position.distance(wanderTarget) < 0.6f) {
                wanderTarget = this.position;
            }
        }

        // --- 2. Gravidade ---

        int blockBelow = (int) Math.floor(position.y - 0.1f);
        boolean onGround = world.isSolid((int)Math.floor(position.x), blockBelow, (int)Math.floor(position.z));

        if (!onGround) {
            // Usa as variáveis herdadas de NPC.java
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
