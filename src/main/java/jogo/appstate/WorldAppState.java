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

    private VoxelWorld.Vector3i targetBlock = null; // As coordenadas do bloco atualmente debaixo da mira.
    private float currentDurability = 0;           // A durabilidade/vida restante do bloco.
    private static final float STONE_DURABILITY = 3.0f; // Exemplo: 3 acertos para a Pedra


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
        // Adicionado do inventário (Experimentar tirar depois)
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

        if (input != null && input.isMouseCaptured() && input.consumeBreakRequested()) {
            var pick = voxelWorld.pickFirstSolid(cam, 6f);
            pick.ifPresent(hit -> {
                VoxelWorld.Vector3i cell = hit.cell;
                byte blockId = voxelWorld.getBlock(cell.x, cell.y, cell.z);

                if (voxelWorld.breakAt(cell.x, cell.y, cell.z)) {
                    // Cria o item correspondente ao bloco
                    if (player != null && blockId != VoxelPalette.AIR_ID) {
                        PlaceableItem item = GameRegistry.createItemFromBlock(blockId);
                        if (item != null) { // ITEM CRIADO COM SUCESSO
                            boolean added = player.getInventory().addItem(item, 1);
                            if (added) {
                                System.out.println("✅ " + item.getName() + " adicionado ao inventário!");
                            } else {
                                System.out.println("❌ Inventário cheio!");
                            }
                        } else {
                            System.out.println("❌ Falha na criação do item para bloco ID: " + blockId); // 🛑 NOVO LOG
                        }
                    }

                    voxelWorld.rebuildDirtyChunks(physicsSpace);
                    playerAppState.refreshPhysics();
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
                        return; // 🛑 Falha
                    }

                    Item item = selectedStack.getItem();

                    // 2. Verificar se o item é colocável
                    if (!(item instanceof PlaceableItem placeableItem)) {
                        System.out.println("❌ Item selecionado '" + item.getName() + "' não é um bloco colocável.");
                        return; // 🛑 Falha
                    }

                    byte blockId = placeableItem.getBlockId();

                    // 3. Verificar se o local de colocação está ocupado
                    if (voxelWorld.getBlock(placeX, placeY, placeZ) != VoxelPalette.AIR_ID) {
                        System.out.println("❌ Não é possível colocar: Local já ocupado.");
                        return; // 🛑 Falha
                    }

                    // SUCESSO: Colocar o Bloco e remover do inventário
                    voxelWorld.setBlock(placeX, placeY, placeZ, blockId);

                    player.getInventory().removeItem(item, 1);
                    System.out.println("✅ Bloco '" + item.getName() + "' colocado!");

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
    }



    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }
}
