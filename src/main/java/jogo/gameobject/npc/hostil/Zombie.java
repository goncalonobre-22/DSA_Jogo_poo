package jogo.gameobject.npc.hostil;

import jogo.framework.math.Vec3;
import jogo.gameobject.npc.NPC;
import jogo.voxel.VoxelWorld;
import jogo.gameobject.character.Player; // Se precisar de player para o takeDamage

public class Zombie extends NPC {

    /** Posição do alvo que o Zombie está a tentar alcançar. */
    private Vec3 targetPos;

    /** Velocidade de movimento horizontal do Zombie. */
    private float speed = 1.5f;

    /** Referência ao mundo de voxels para cálculos de colisão e terreno. */
    private final VoxelWorld world;

    /** Referência ao jogador para aplicar dano e calcular perseguição. */
    private final Player player;

    /** Força da gravidade aplicada ao NPC. */
    private static final float GRAVITY = 24.0f;

    /** Velocidade vertical atual para o sistema de gravidade. */
    private float verticalVelocity = 0;

    /** Altura máxima que o Zombie consegue subir automaticamente (degrau). */
    private final float STEP_HEIGHT = 1.05f;

    /** Distância máxima para conseguir atacar o jogador. */
    private static final float ATTACK_RANGE = 1.5f;

    /** Tempo de espera (em segundos) entre ataques. */
    private static final float ATTACK_COOLDOWN = 2.0f;

    /** Temporizador para controlar o intervalo entre ataques. */
    private float attackCooldownTimer = 0.0f;

    /** Quantidade de dano infligida ao jogador por cada ataque bem-sucedido. */
    private final int ATTACK_DAMAGE = 8;

    /** Raio de distância dentro do qual o Zombie deteta e começa a perseguir o jogador. */
    private static final float PERCEPTION_RANGE = 25.0f;

    /**
     * Construtor da classe Zombie.
     * Inicializa o Zombie com um nome, posição inicial e define a sua vida para 15 pontos.
     * @param name Nome do NPC.
     * @param spawnPos Posição onde o Zombie será instanciado.
     * @param world Referência ao mundo de jogo.
     * @param player Referência ao jogador alvo.
     */
    public Zombie(String name, Vec3 spawnPos, VoxelWorld world, Player player) {
        super(name);
        this.position = new Vec3(spawnPos.x, spawnPos.y, spawnPos.z);
        this.world = world;
        this.player = player;
        setHealth(15);
    }

    /**
     * Define o alvo atual para o movimento da IA.
     * @param target Coordenadas do alvo (geralmente a posição do jogador).
     */
    public void setTarget(Vec3 target) {
        this.targetPos = target;
    }

    /**
     * Atualiza o comportamento do Zombie a cada frame.
     * Gere a lógica de ataque, a perseguição do alvo, a subida de degraus e a aplicação da gravidade.
     * @param tpf Tempo por frame (Time Per Frame).
     */
    @Override
    public void updateAI(float tpf) {
        if (targetPos == null) return;

        attackCooldownTimer -= tpf;

        float dx = targetPos.x - position.x;
        float dz = targetPos.z - position.z;
        float distSq = dx * dx + dz * dz;
        float dist = (float)Math.sqrt(distSq);

        // Lógica de ataque: Ataca se estiver no alcance e o cooldown tiver terminado.
        if (dist < ATTACK_RANGE && attackCooldownTimer <= 0) {
            player.takeDamage(ATTACK_DAMAGE);
            System.out.println(this.getName() + " ATAQUE ATIVADO! DIST: " + dist +
                    " | Player sofreu " + ATTACK_DAMAGE + " de dano. Vida atual: " + player.getHealth());
            attackCooldownTimer = ATTACK_COOLDOWN;
            return;
        }

        // Lógica de perseguição e movimento com detecção de degraus.
        if (Float.isNaN(dist) || dist > PERCEPTION_RANGE || dist < 0.001f) {
            // Fora do alcance ou distância inválida.
        } else {
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

            boolean collisionX = world.isSolid(targetXBlock, currentY, (int)Math.floor(position.z));
            boolean collisionZ = world.isSolid((int)Math.floor(position.x), currentY, targetZBlock);

            boolean stepBlockX = world.isSolid(targetXBlock, currentY + 1, (int)Math.floor(position.z));
            boolean stepBlockZ = world.isSolid((int)Math.floor(position.x), currentY + 1, targetZBlock);

            // Mecânica de degrau: Se houver colisão mas o bloco acima estiver livre, o Zombie "sobe".
            if ((collisionX && !stepBlockX) || (collisionZ && !stepBlockZ)) {
                if (position.y < currentY + STEP_HEIGHT) {
                    position.y += speed * tpf;
                }
            }

            if (!stepBlockX) position.x = newX;
            if (!stepBlockZ) position.z = newZ;
        }

        // Lógica de Gravidade: Garante que o Zombie caia se não houver chão sólido por baixo.
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