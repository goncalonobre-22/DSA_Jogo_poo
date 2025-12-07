package jogo.appstate;

// ... (imports) ...

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.material.RenderState;

import jogo.gameobject.npc.NPC;
import jogo.gameobject.character.Player;
import jogo.gameobject.npc.hostil.Slime;

import java.util.HashMap;
import java.util.List;

public class NPCAppState extends BaseAppState {

    private final Node worldNode;
    private final Player player;

    private List<NPC> npcList;
    private HashMap<NPC, Node> models = new HashMap<>();

    public NPCAppState(List<NPC> npcList, Node worldNode, Player player) {
        this.npcList = npcList;
        this.worldNode = worldNode;
        this.player = player;
    }

    @Override
    protected void initialize(Application app) {
        for (NPC npc : npcList) {
            Node model = create3DModel(app, npc);
            models.put(npc, model);
            worldNode.attachChild(model);
        }
    }

    private Node create3DModel(Application app, NPC npc) {

        Node parent = new Node(npc.getName());

        // ---- SHAPE DO SLIME 3D ----
        // Raio 0.5f (Original) - Agora é a altura lógica da colisão.
        Sphere sphere = new Sphere(16, 16, 0.5f);
        Geometry geo = new Geometry("slime", sphere);

        // ---- MATERIAL: UNWASHED.J3MD COM MAGENTA/VERDE ---
        Material mat = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");

        // Define a cor que você viu funcionar (Verde, neste caso)
        mat.setColor("Color", com.jme3.math.ColorRGBA.Green);

        // Tenta carregar a textura (e define transparência)
        try {
            mat.setTexture("ColorMap", app.getAssetManager().loadTexture("Textures/slime.png"));
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        } catch (Exception e) {
            System.err.println("ERRO: Falha ao carregar textura. A Slime aparece a VERDE.");
        }

        geo.setMaterial(mat);

        // Achatamento da slime
        geo.setLocalScale(1f, 0.6f, 1f);

        // CORREÇÃO CRÍTICA DE ALTURA: O centro do modelo deve estar 0.3f acima
        // da posição lógica (Y) para o modelo assentar perfeitamente no chão.
        // (Raio 0.5 * Escala 0.6 = 0.3 de altura da metade inferior).
        geo.setLocalTranslation(0, 0.3f, 0);

        parent.attachChild(geo);

        // Posição inicial (vem da posição lógica X, Y, Z da Slime)
        parent.setLocalTranslation(
                npc.getPosition().x,
                npc.getPosition().y,
                npc.getPosition().z
        );

        return parent;
    }

    @Override
    public void update(float tpf) {

        for (NPC npc : npcList) {

            if (npc instanceof Slime slime && player != null) {
                slime.setTarget(player.getPosition());
            }




            npc.updateAI(tpf);

            Node model = models.get(npc);
            if (model != null) {
                model.setLocalTranslation(
                        npc.getPosition().x,
                        npc.getPosition().y,
                        npc.getPosition().z
                );
            }
        }
    }

    @Override
    protected void cleanup(Application app) {
        for (Node model : models.values()) {
            model.removeFromParent();
        }
        models.clear();
    }

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}