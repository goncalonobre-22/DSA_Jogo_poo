package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import jogo.engine.GameRegistry;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.Item;
import jogo.gameobject.item.PlaceableItem;
import jogo.util.BreakingBlockSystem;
import jogo.util.ItemRegistry;
import jogo.voxel.VoxelBlockType;
import jogo.voxel.VoxelPalette;
import jogo.voxel.VoxelWorld;

public class WorldAppState extends BaseAppState {

    private final Node rootNode;
    private final AssetManager assetManager;
    private final PhysicsSpace physicsSpace;
    private final Camera cam;
    private final InputAppState input;
    private PlayerAppState playerAppState;

    // Adicionado do inventário
    private Player player;

    private BreakingBlockSystem breakingBlockSystem;

    private static final int GRAVITY_RADIUS = 24;


    // world root for easy cleanup
    private Node worldNode;
    private VoxelWorld voxelWorld;
    private com.jme3.math.Vector3f spawnPosition;

    public WorldAppState(Node rootNode, AssetManager assetManager, PhysicsSpace physicsSpace, Camera cam, InputAppState input, Player player) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.physicsSpace = physicsSpace;
        this.cam = cam;
        this.input = input;
        // Adicionado do inventário
        this.player = player;
    }

    public void registerPlayerAppState(PlayerAppState playerAppState) {
        this.playerAppState = playerAppState;
    }


    @Override
    protected void initialize(Application app) {
        worldNode = new Node("World");
        rootNode.attachChild(worldNode);

        // Lighting
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.20f)); // slightly increased ambient
        worldNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.35f, -1.3f, -0.25f).normalizeLocal()); // more top-down to reduce harsh contrast
        sun.setColor(ColorRGBA.White.mult(0.85f)); // slightly dimmer sun
        worldNode.addLight(sun);

        // Voxel world 16x16x16 (reduced size for simplicity)
        voxelWorld = new VoxelWorld(assetManager, 320, 32, 320);
        voxelWorld.generateLayers();
        voxelWorld.buildMeshes();
        voxelWorld.clearAllDirtyFlags();
        worldNode.attachChild(voxelWorld.getNode());
        voxelWorld.buildPhysics(physicsSpace);

        breakingBlockSystem = new BreakingBlockSystem(voxelWorld);

        // compute recommended spawn
        spawnPosition = voxelWorld.getRecommendedSpawn();
    }

    public com.jme3.math.Vector3f getRecommendedSpawnPosition() {
        return spawnPosition != null ? spawnPosition.clone() : new com.jme3.math.Vector3f(25.5f, 12f, 25.5f);
    }

    public VoxelWorld getVoxelWorld() {
        return voxelWorld;
    }

    @Override
    public void update(float tpf) {
        if (breakingBlockSystem != null) {
            breakingBlockSystem.update(tpf);
        }

        checkFallingBlocks();

        if (input != null && input.isMouseCaptured() && input.consumeBreakRequested()) {
            var pick = voxelWorld.pickFirstSolid(cam, 6f);
            pick.ifPresent(hit -> {
                VoxelWorld.Vector3i cell = hit.cell;
                byte blockId = voxelWorld.getBlock(cell.x, cell.y, cell.z);
                Item heldItem = null;
                if (player != null) {
                    var selectedStack = player.getInventory().getSelectedItem();
                    if (selectedStack != null && selectedStack.getAmount() > 0) {
                        heldItem = selectedStack.getItem();
                    }
                }

                boolean shouldBreak = breakingBlockSystem.hitBlock(cell.x, cell.y, cell.z, heldItem);

                if (shouldBreak) {
                    if (voxelWorld.breakAt(cell.x, cell.y, cell.z)) {
                        // Cria o item correspondente ao bloco
                        if (player != null && blockId != VoxelPalette.AIR_ID) {
                            PlaceableItem item = ItemRegistry.createItemFromBlock(blockId);
                            if (item != null) { // ITEM CRIADO COM SUCESSO
                                boolean added = player.getInventory().addItem(item, 1);
                                if (added) {
                                    System.out.println(item.getName() + " adicionado ao inventário!");
                                } else {
                                    System.out.println("Inventário cheio!");
                                }
                            } else {
                                System.out.println("Falha na criação do item para bloco ID: " + blockId);
                            }
                        }

                        voxelWorld.rebuildDirtyChunks(physicsSpace);
                        playerAppState.refreshPhysics();
                    }

                }
            });
        }

        // Adicionado por causa do inventário

        if (input != null && input.isMouseCaptured() && input.consumePlaceRequested()) {
            var pick = voxelWorld.pickFirstSolid(cam, 6f);
            pick.ifPresent(hit -> {
                VoxelWorld.Vector3i cell = hit.cell;
                Vector3f normal = hit.normal;

                int placeX = cell.x + (int)normal.x;
                int placeY = cell.y + (int)normal.y;
                int placeZ = cell.z + (int)normal.z;

                if (player != null) {
                    var selectedStack = player.getInventory().getSelectedItem();

                    // 1. Verificar se existe item selecionado
                    if (selectedStack == null || selectedStack.getAmount() <= 0) {
                        System.out.println("Nenhum item selecionado ou pilha vazia!");
                        return;
                    }

                    Item item = selectedStack.getItem();

                    // 2. Verificar se o item é colocável
                    if (!(item instanceof PlaceableItem placeableItem)) {
                        System.out.println(item.getName() + "' não é um bloco colocável.");
                        return;
                    }

                    byte blockId = placeableItem.getBlockId();

                    // 3. Verificar se o local de colocação está ocupado
                    if (voxelWorld.getBlock(placeX, placeY, placeZ) != VoxelPalette.AIR_ID) {
                        System.out.println("Não é possível colocar: Local já ocupado.");
                        return;
                    }

                    // SUCESSO: Colocar o Bloco e remover do inventário
                    voxelWorld.setBlock(placeX, placeY, placeZ, blockId);

                    player.getInventory().removeItem(item, 1);
                    System.out.println("Bloco '" + item.getName() + "' colocado!");

                    voxelWorld.rebuildDirtyChunks(physicsSpace);
                    playerAppState.refreshPhysics();
                }
            });

        }

        if (input != null && input.consumeToggleShadingRequested()) {
            voxelWorld.toggleRenderDebug();
        }


    }

    @Override
    protected void cleanup(Application app) {

        if (worldNode != null) {
            // Remove all physics controls under worldNode
            worldNode.depthFirstTraversal(spatial -> {
                RigidBodyControl rbc = spatial.getControl(RigidBodyControl.class);
                if (rbc != null) {
                    physicsSpace.remove(rbc);
                    spatial.removeControl(rbc);
                }
            });
            worldNode.removeFromParent();
            worldNode = null;
        }

        if (breakingBlockSystem != null) {
            breakingBlockSystem.cleanup();
            breakingBlockSystem = null;
        }
    }

    public Player getPlayer() {
        return player;
    }

//    private void checkFallingBlocks() {
//        if (voxelWorld == null || physicsSpace == null) return;
//
//        boolean worldChanged = false;
//
//        // Obtém os tamanhos para iterar sobre todos os blocos (ou sobre uma área relevante)
//        int sizeX = 170; // Assumindo o tamanho do mundo (aqui 320)
//        int sizeY = 32;  // Assumindo o tamanho do mundo (aqui 32)
//        int sizeZ = 170; // Assumindo o tamanho do mundo (aqui 320)
//
//        // Itera de Y=1 para cima, para garantir que blocos de cima caem primeiro.
//        for (int y = 1; y < sizeY; y++) {
//            for (int x = 0; x < sizeX; x++) {
//                for (int z = 0; z < sizeZ; z++) {
//                    byte currentId = voxelWorld.getBlock(x, y, z);
//                    if (currentId == VoxelPalette.AIR_ID) continue;
//
//                    VoxelBlockType currentType = voxelWorld.getPalette().get(currentId);
//
//                    // 1. Verifica se o bloco é afetado pela gravidade
//                    if (currentType.isAffectedByGravity()) {
//                        // 2. Verifica o bloco abaixo (y-1)
//                        byte belowId = voxelWorld.getBlock(x, y - 1, z);
//                        VoxelBlockType belowType = voxelWorld.getPalette().get(belowId);
//
//                        // 3. Verifica se o bloco abaixo não é sólido
//                        if (!belowType.isSolid()) {
//                            // Faz o bloco cair 1 unidade (troca com o bloco abaixo)
//                            voxelWorld.setBlock(x, y, z, VoxelPalette.AIR_ID);
//                            voxelWorld.setBlock(x, y - 1, z, currentId);
//                            worldChanged = true;
//                            // Decrementa y em 1 para garantir que o bloco recém-movido é verificado
//                            // na próxima iteração do ciclo (para propagação da queda no mesmo frame)
//                            y--;
//                        }
//                    }
//                }
//            }
//        }
//
//        if (worldChanged) {
//            // Rebuild the necessary chunks and update physics
//            voxelWorld.rebuildDirtyChunks(physicsSpace);
//            if (playerAppState != null) {
//                playerAppState.refreshPhysics();
//            }
//        }
//    }

    private void checkFallingBlocks() {
        if (voxelWorld == null || physicsSpace == null) return;

        boolean worldChanged = false;

        // Posição do jogador
        Vector3f playerPos = playerAppState.getPlayerPosition();

        // Converte para coords de bloco
        int px = (int) playerPos.x;
        int py = (int) playerPos.y;
        int pz = (int) playerPos.z;

        // Limites da área a processar
        int minX = Math.max(0, px - GRAVITY_RADIUS);
        int maxX = Math.min(voxelWorld.getSizeX() - 1, px + GRAVITY_RADIUS);

        int minY = Math.max(1, py - GRAVITY_RADIUS);
        int maxY = Math.min(voxelWorld.getSizeY() - 1, py + GRAVITY_RADIUS);

        int minZ = Math.max(0, pz - GRAVITY_RADIUS);
        int maxZ = Math.min(voxelWorld.getSizeZ() - 1, pz + GRAVITY_RADIUS);

        // Itera somente nos blocos perto do jogador
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {

                    byte id = voxelWorld.getBlock(x, y, z);
                    if (id == VoxelPalette.AIR_ID) continue;

                    VoxelBlockType type = voxelWorld.getPalette().get(id);

                    if (type.isAffectedByGravity()) {
                        byte belowId = voxelWorld.getBlock(x, y - 1, z);
                        VoxelBlockType belowType = voxelWorld.getPalette().get(belowId);

                        if (!belowType.isSolid()) {
                            // Faz o bloco cair
                            voxelWorld.setBlock(x, y, z, VoxelPalette.AIR_ID);
                            voxelWorld.setBlock(x, y - 1, z, id);
                            worldChanged = true;
                        }
                    }
                }
            }
        }

        // Só reconstrói se realmente mudou algo
        if (worldChanged) {
            voxelWorld.rebuildDirtyChunks(physicsSpace);
            if (playerAppState != null) {
                playerAppState.refreshPhysics();
            }
        }
    }


    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }
}
