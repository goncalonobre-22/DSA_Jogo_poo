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
import jogo.gameobject.character.Player;
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
        this.player = this.player;
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
                    // Adiciona o bloco ao inventário
                    if (player != null && blockId != VoxelPalette.AIR_ID) {
                        boolean added = player.getInventory().addItem(blockId, 1);
                        if (added) {
                            System.out.println("Bloco adicionado ao inventário!");
                        } else {
                            System.out.println("Inventário cheio!");
                        }
                    }

                    voxelWorld.rebuildDirtyChunks(physicsSpace);
                    playerAppState.refreshPhysics();
                }
            });
        }

        // Adicionado por causa do inventário
        // COLOCAR BLOCO (clique direito)
        if (input != null && input.isMouseCaptured() && input.consumePlaceRequested()) {
            var pick = voxelWorld.pickFirstSolid(cam, 6f);
            pick.ifPresent(hit -> {
                // Coloca o bloco na face do bloco atingido
                VoxelWorld.Vector3i cell = hit.cell;
                Vector3f normal = hit.normal;

                int placeX = cell.x + (int)normal.x;
                int placeY = cell.y + (int)normal.y;
                int placeZ = cell.z + (int)normal.z;

                // Verifica se tem bloco selecionado no inventário
                if (player != null) {
                    var selectedItem = player.getInventory().getSelectedItem();
                    if (selectedItem != null && selectedItem.getAmount() > 0) {
                        byte blockId = selectedItem.getBlockId();

                        // Verifica se a posição está vazia
                        if (voxelWorld.getBlock(placeX, placeY, placeZ) == VoxelPalette.AIR_ID) {
                            voxelWorld.setBlock(placeX, placeY, placeZ, blockId);

                            // Remove 1 item do inventário
                            player.getInventory().removeItem(blockId, 1);
                            System.out.println("Bloco colocado!");

                            voxelWorld.rebuildDirtyChunks(physicsSpace);
                            playerAppState.refreshPhysics();
                        }
                    } else {
                        System.out.println("Nenhum bloco selecionado!");
                    }
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
