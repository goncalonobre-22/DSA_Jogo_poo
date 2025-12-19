package jogo.gameobject.npc.hostil;

import jogo.framework.math.Vec3;
import jogo.gameobject.character.Player;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;

public class Slime extends NPC {

    /** Posição atual do alvo que o Slime está a tentar alcançar. */
    private Vec3 targetPos;

    /** Velocidade de movimento horizontal do Slime. */
    private float speed = 3.0f;

    /** Referência ao mundo de voxels para verificação de colisões e terreno. */
    private final VoxelWorld world;

    /** Referência ao jogador para cálculo de perseguição e aplicação de dano. */
    private final Player player;

    /** Velocidade vertical atual (para gravidade e saltos). */
    private float verticalVelocity = 0;

    /** Força da gravidade aplicada ao Slime. */
    private static final float GRAVITY = 24.0f;

    /** Força aplicada no momento do salto. */
    private static final float JUMP_POWER = 8.0f;

    /** Temporizador para controlar o intervalo entre saltos. */
    private float jumpTimer = 0.0f;

    /** Intervalo de tempo (em segundos) entre cada salto. */
    private static final float JUMP_INTERVAL = 1.0f;

    /** Distância máxima para o Slime conseguir desferir um ataque ao jogador. */
    private static final float ATTACK_RANGE = 1.8f;

    /** Tempo de espera entre ataques consecutivos. */
    private static final float ATTACK_COOLDOWN = 1.5f;

    /** Temporizador interno para gerir o cooldown do ataque. */
    public float attackCooldownTimer = 0.0f;

    /** Quantidade de dano infligida ao jogador por cada ataque. */
    private final int ATTACK_DAMAGE = 5;

    /** Distância máxima a que o Slime consegue detetar o jogador. */
    private static final float PERCEPTION_RANGE = 30.0f;

    /** Vida inicial atribuída ao Slime. */
    private final int health = 5;

    /**
     * Construtor da classe Slime.
     * @param name Nome identificador do NPC.
     * @param spawnPos Posição inicial onde o Slime será criado.
     * @param world O mundo de voxels onde o Slime habita.
     * @param player O jogador que o Slime irá perseguir.
     */
    public Slime(String name, Vec3 spawnPos, VoxelWorld world, Player player) {
        super(name);
        this.position = new Vec3(spawnPos.x, spawnPos.y, spawnPos.z);
        this.world = world;
        this.player = player;
        setHealth(health);
    }

    /**
     * Define a posição alvo para a qual o Slime se deve dirigir.
     * @param target Coordenadas do destino (geralmente a posição do jogador).
     */
    public void setTarget(Vec3 target) {
        this.targetPos = target;
    }

    /**
     * Atualiza a IA e a física do Slime a cada frame.
     * Gere a lógica de ataque se o jogador estiver perto, calcula o movimento horizontal
     * em direção ao alvo, verifica colisões e aplica a física de saltos e gravidade.
     * @param tpf Tempo por frame (Time Per Frame).
     */
    @Override
    public void updateAI(float tpf) {

        if (targetPos == null) return;

        jumpTimer -= tpf;
        attackCooldownTimer -= tpf;

        //Distância entre nós e o NPC
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

            // Para o NPC não corre super rápido
            float nx = dx / safeDist;
            float nz = dz / safeDist;

            // Quanto tem que andar nesse milissegundo
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


        // Gravidade e Movimento Vertical (Salto)

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