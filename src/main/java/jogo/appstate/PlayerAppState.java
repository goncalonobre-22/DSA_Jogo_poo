package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.light.PointLight;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import jogo.gameobject.character.Player;
import jogo.voxel.VoxelBlockType;
import jogo.voxel.VoxelWorld;

public class PlayerAppState extends BaseAppState {

    private final Node rootNode;
    private final AssetManager assetManager;
    private final Camera cam;
    private final InputAppState input;
    private final PhysicsSpace physicsSpace;
    private final WorldAppState world;

    private Node playerNode;
    private BetterCharacterControl characterControl;
    private Player player;

    private float playerDamageTimer = 0.0f;
    private static final float DAMAGE_TICK_RATE = 1f;

    private float passiveScoreTimer = 0.0f; // NOVO: Timer para a pontuação passiva
    private static final float PASSIVE_SCORE_INTERVAL = 10.0f; // 10 segundos
    private static final int PASSIVE_SCORE_AMOUNT = 1;

    private AudioNode hurtSound;

    // view angles
    private float yaw = 0f;
    private float pitch = 0f;

    // tuning
    private float moveSpeed = 8.0f; // m/s
    private float sprintMultiplier = 1.7f; //1.7f
    private float mouseSensitivity = 30f; // degrees per mouse analog unit
    private float eyeHeight = 1.7f;

    private Vector3f spawnPosition = new Vector3f(25.5f, 12f, 25.5f);
    private PointLight playerLight;

    public PlayerAppState(Node rootNode, AssetManager assetManager, Camera cam, InputAppState input, PhysicsSpace physicsSpace, WorldAppState world, Player player) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.cam = cam;
        this.input = input;
        this.physicsSpace = physicsSpace;
        this.world = world;
        this.player = player;
        world.registerPlayerAppState(this);
    }

    @Override
    protected void initialize(Application app) {
        // query world for recommended spawn now that it should be initialized
        if (world != null) {
            spawnPosition = world.getRecommendedSpawnPosition();
        }

        playerNode = new Node("Player");
        rootNode.attachChild(playerNode);

        // Engine-neutral player entity (no engine visuals here)
        //player = new Player();

        // BetterCharacterControl(radius, height, mass)
        characterControl = new BetterCharacterControl(0.42f, 1.8f, 80f);
        characterControl.setGravity(new Vector3f(0, -24f, 0));
        characterControl.setJumpForce(new Vector3f(0, 400f, 0));
        playerNode.addControl(characterControl);
        physicsSpace.add(characterControl);

        // Local light source that follows the player's head
        playerLight = new PointLight();
        playerLight.setColor(new com.jme3.math.ColorRGBA(0.6f, 0.55f, 0.5f, 1f));
        playerLight.setRadius(70f); //12
        rootNode.addLight(playerLight);

        // Spawn at recommended location
        respawn();

        // initialize camera
        cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.05f, 500f);
        // Look slightly downward so ground is visible immediately
        this.pitch = -0.35f;
        applyViewToCamera();
    }

    /**
     * Ciclo principal de atualização do jogador. Gere a sincronização da posição física com a entidade,
     * o consumo de fome, efeitos de som de dano, menus de inventário, movimento baseado na
     * sensibilidade do rato e interação com blocos especiais (dano e modificadores de velocidade).
     * @param tpf Tempo desde o último frame (time per frame).
     */
    @Override
    public void update(float tpf) {
        if (player != null && playerNode != null) {

            com.jme3.math.Vector3f physicsPos = playerNode.getWorldTranslation();

            player.setPosition(new jogo.framework.math.Vec3(physicsPos.x, physicsPos.y, physicsPos.z));
        }

        handleInventoryInput();

        if (player != null) {
            player.updateHunger(tpf, input.isSprinting());
        }

        if (player != null && player.consumeDamageFlag()) {
            playHurtSound();
        }

        if (player != null && player.getHealth() <= 0) {
            setControlEnabled(false);

            characterControl.setWalkDirection(Vector3f.ZERO);
            if (playerLight != null) playerLight.setPosition(playerNode.getWorldTranslation().add(0, eyeHeight, 0));
            return;
        }

        if (!isEnabled()) {
            characterControl.setWalkDirection(Vector3f.ZERO);
            if (playerLight != null) playerLight.setPosition(playerNode.getWorldTranslation().add(0, eyeHeight, 0));
            return;
        }

        // respawn on request
        if (input.consumeRespawnRequested()) {
            // refresh spawn from world in case terrain changed
            if (world != null) spawnPosition = world.getRecommendedSpawnPosition();
            respawn();
        }

        // pause controls if mouse not captured
        if (!input.isMouseCaptured()) {
            characterControl.setWalkDirection(Vector3f.ZERO);
            // keep light with player even when paused
            if (playerLight != null) playerLight.setPosition(playerNode.getWorldTranslation().add(0, eyeHeight, 0));
            applyViewToCamera();
            return;
        }

        handlePassiveScore(tpf);

        // handle mouse look
        Vector2f md = input.consumeMouseDelta();
        if (md.lengthSquared() != 0f) {
            float degX = md.x * mouseSensitivity;
            float degY = md.y * mouseSensitivity;
            yaw -= degX * FastMath.DEG_TO_RAD;
            pitch -= degY * FastMath.DEG_TO_RAD;
            pitch = FastMath.clamp(pitch, -FastMath.HALF_PI * 0.99f, FastMath.HALF_PI * 0.99f);
        }

        // movement input in XZ plane based on camera yaw
        Vector3f wish = input.getMovementXZ();
        Vector3f dir = Vector3f.ZERO;
        if (wish.lengthSquared() > 0f) {
            dir = computeWorldMove(wish).normalizeLocal();
        }

        VoxelBlockType blockType = getVoxelBlockTypeUnderPlayer();


        playerDamageTimer += tpf;
        if (blockType != null && playerDamageTimer >= DAMAGE_TICK_RATE) {

            if (blockType.doesDamage()) {
                int damage = blockType.getDamageAmount();
                player.takeDamage(damage);
                System.out.println("Dano do bloco '" + blockType.getName() + "': -" + damage + " | Vida atual: " + player.getHealth());
            }

            playerDamageTimer = 0.0f;
        }


        float blockMultiplier = 1.0f;

        if (characterControl.isOnGround() && blockType != null) {
            blockMultiplier = blockType.getSpeedMultiplier(); // <- REUTILIZA O VALOR DO BLOCO
        }

        float speed = moveSpeed * (input.isSprinting() ? sprintMultiplier : 1f) * blockMultiplier;
        characterControl.setWalkDirection(dir.mult(speed));


        // jump
        if (input.consumeJumpRequested() && characterControl.isOnGround()) {
            characterControl.jump();
        }

        // place camera at eye height above physics location
        applyViewToCamera();

        // update light to follow head
        if (playerLight != null) playerLight.setPosition(playerNode.getWorldTranslation().add(0, eyeHeight, 0));
    }

    /**
     * Identifica o tipo de bloco (VoxelBlockType) que se encontra imediatamente abaixo dos pés do jogador.
     * @return O objeto VoxelBlockType correspondente ao bloco no chão ou null se não for detetado.
     */
    private VoxelBlockType getVoxelBlockTypeUnderPlayer() {
        if (world == null || world.getVoxelWorld() == null || playerNode == null) {
            return null;
        }


        VoxelWorld vw = world.getVoxelWorld();

        int blockX = (int) getPlayerPosition().x;
        int blockY = (int) (getPlayerPosition().y - 1f);
        int blockZ = (int) getPlayerPosition().z;

        byte blockId = vw.getBlock(blockX, blockY, blockZ);
        return vw.getPalette().get(blockId);
    }

    private void respawn() {
        characterControl.setWalkDirection(Vector3f.ZERO);
        characterControl.warp(spawnPosition);
        // Reset look
        this.pitch = -0.35f;
        applyViewToCamera();
    }

    private Vector3f computeWorldMove(Vector3f inputXZ) {
        // Build forward and left unit vectors from yaw
        float sinY = FastMath.sin(yaw);
        float cosY = FastMath.cos(yaw);
        Vector3f forward = new Vector3f(-sinY, 0, -cosY); // -Z when yaw=0
        Vector3f left = new Vector3f(-cosY, 0, sinY);     // -X when yaw=0
        return left.mult(inputXZ.x).addLocal(forward.mult(inputXZ.z));
    }

    private void applyViewToCamera() {
        // Character world location (spatial is synced by control)
        Vector3f loc = playerNode.getWorldTranslation().add(0, eyeHeight, 0);
        cam.setLocation(loc);
        cam.setRotation(new com.jme3.math.Quaternion().fromAngles(pitch, yaw, 0f));
    }

    @Override
    protected void cleanup(Application app) {
        if (playerNode != null) {
            if (characterControl != null) {
                physicsSpace.remove(characterControl);
                playerNode.removeControl(characterControl);
                characterControl = null;
            }
            playerNode.removeFromParent();
            playerNode = null;
        }
        if (playerLight != null) {
            rootNode.removeLight(playerLight);
            playerLight = null;
        }
    }

    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }

    public void refreshPhysics() {
        if (characterControl != null) {
            physicsSpace.remove(characterControl);
            physicsSpace.add(characterControl);
        }
    }

    /**
     * Processa a entrada da roda do rato (scroll) para alternar entre os slots
     * rápidos (0-8) do inventário do jogador.
     */
    private void handleInventoryInput() {
        int scroll = input.consumeScrollDelta();
        if (scroll != 0) {
            int currentSlot = player.getInventory().getSelectedSlot();
            currentSlot -= scroll;

            if (currentSlot < 0) currentSlot = 8;
            if (currentSlot > 8) currentSlot = 0;

            player.getInventory().setSelectedSlot(currentSlot);
            System.out.println("Slot selecionado: " + (currentSlot + 1));
        }
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Executa o processo completo de renascimento: restaura estatísticas (vida/fome),
     * reposiciona no spawn, reativa controlos e captura o rato.
     */
    public void triggerRespawn() {
        if (world != null) {
            spawnPosition = world.getRecommendedSpawnPosition();
        }
        player.resetStats();
        respawn();
        setControlEnabled(true);
        input.setMouseCaptured(true);
    }

    /**
     * Ativa ou desativa a capacidade de movimento do jogador e a sua presença no motor de física.
     * @param enabled true para permitir controlo, false para imobilizar o jogador.
     */
    public void setControlEnabled(boolean enabled) {
        setEnabled(enabled);

        if (characterControl != null) {
            characterControl.setWalkDirection(Vector3f.ZERO);
            characterControl.setEnabled(enabled);

            if (physicsSpace != null) {
                if (enabled) {
                    physicsSpace.add(characterControl);
                } else {
                    physicsSpace.remove(characterControl);
                }
            }
        }
        input.setMovementEnabled(enabled);
    }

    /**
     * Define o nó de áudio a ser utilizado quando o jogador sofre dano.
     * @param hurtSound O AudioNode com o som de ferimento.
     */
    public void setHurtSound(AudioNode hurtSound) {
        this.hurtSound = hurtSound;
    }

    /**
     * Reproduz uma instância do som de dano, se este estiver definido.
     */
    private void playHurtSound() {
        if (hurtSound != null) {
            hurtSound.playInstance();
        }
    }

    /**
     * Gere a atribuição passiva de pontuação ao jogador com base em intervalos de tempo fixos.
     * @param tpf Tempo desde o último frame.
     */
    private void handlePassiveScore(float tpf) {
        if (player == null) return;

        passiveScoreTimer += tpf;

        if (passiveScoreTimer >= PASSIVE_SCORE_INTERVAL) {
            player.addScoreIncrement(PASSIVE_SCORE_AMOUNT);
            passiveScoreTimer = 0.0f; // Reinicia o timer
        }

        player.processScoreQueue();
    }

    public Vector3f getPlayerPosition() {
        return playerNode.getWorldTranslation();
    }
}
