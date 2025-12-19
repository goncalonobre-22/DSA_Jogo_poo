package jogo.appstate;


import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;

import jogo.engine.GameRegistry;
import jogo.engine.RenderIndex;
import jogo.gameobject.npc.NPC;
import jogo.gameobject.character.Player;
import jogo.gameobject.npc.hostil.Slime;
import jogo.gameobject.npc.hostil.Zombie;
import jogo.gameobject.npc.pacifico.Cow;
import jogo.gameobject.npc.pacifico.Healer;

import java.util.HashMap;
import java.util.List;

public class NPCAppState extends BaseAppState {

    // private final Node worldNode;
    private final Player player;
    private final RenderIndex renderIndex;
    private final GameRegistry registry;
    private final WorldAppState worldAppState;
    private HashMap<Node, NPC> modelToNpc = new HashMap<>();

    private List<NPC> npcList;
    private HashMap<NPC, Node> models = new HashMap<>();


    public NPCAppState(List<NPC> npcList, WorldAppState worldAppState, Player player, RenderIndex renderIndex, GameRegistry registry) {
        this.npcList = npcList;
        this.worldAppState = worldAppState;
        this.player = player;
        this.renderIndex = renderIndex;
        this.registry = registry;
    }


    /**
     * Inicializa o estado dos NPCs. Para cada NPC na lista, cria o seu modelo 3D,
     * regista-o no RenderIndex para deteção de colisões e anexa-o ao nó do mundo.
     * @param app A instância da aplicação JMonkeyEngine.
     */
    @Override
    protected void initialize(Application app) {
        Node worldNode = worldAppState.getWorldNode();
        for (NPC npc : npcList) {
            Node model = create3DModel(app, npc);
            models.put(npc, model);
            modelToNpc.put(model, npc);
            worldNode.attachChild(model);

            renderIndex.register(model, npc);

            npc.appStateHook = this;
        }
    }

    /**
     * Cria a representação visual (modelo 3D) de um NPC com base no seu tipo (Slime, Zombie, Cow ou Healer).
     * Configura a geometria, escala, materiais e texturas específicas para cada entidade.
     * @param app A instância da aplicação para aceder ao AssetManager.
     * @param npc A instância lógica do NPC.
     * @return Um Node contendo a geometria e material do NPC.
     */
    private com.jme3.scene.Node create3DModel(com.jme3.app.Application app, jogo.gameobject.npc.NPC npc) {

        com.jme3.scene.Node parent = new com.jme3.scene.Node(npc.getName());
        com.jme3.scene.Geometry geo;

        if (npc instanceof jogo.gameobject.npc.hostil.Slime) {
            com.jme3.scene.shape.Sphere sphere = new com.jme3.scene.shape.Sphere(16, 16, 0.5f);
            geo = new com.jme3.scene.Geometry("slime", sphere);
            geo.setLocalScale(1f, 0.6f, 1f);
            geo.setLocalTranslation(0, 0.3f, 0);
        }
        else if (npc instanceof jogo.gameobject.npc.hostil.Zombie) {
            com.jme3.scene.shape.Box box = new com.jme3.scene.shape.Box(0.3f, 0.9f, 0.3f);
            geo = new com.jme3.scene.Geometry("zombie", box);
            geo.setLocalTranslation(0, 0.9f, 0);
        }
        else if (npc instanceof Cow) {
            com.jme3.scene.shape.Box body = new com.jme3.scene.shape.Box(0.8f, 0.6f, 0.4f);
            geo = new com.jme3.scene.Geometry("cow", body);
            geo.setLocalTranslation(0, 0.6f, 0);
        }
        else if (npc instanceof Healer) {
            com.jme3.scene.shape.Box body = new com.jme3.scene.shape.Box(0.2f, 0.8f, 0.2f);
            geo = new com.jme3.scene.Geometry("healer", body);
            geo.setLocalTranslation(0, 0.8f, 0);
        }
        else {
            geo = new com.jme3.scene.Geometry("default", new com.jme3.scene.shape.Sphere(8, 8, 0.1f));
        }


        com.jme3.material.Material mat = new com.jme3.material.Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");

        if (npc instanceof jogo.gameobject.npc.hostil.Slime) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Green);
            try {
                mat.setTexture("ColorMap", app.getAssetManager().loadTexture("Textures/NPC/slime.png"));
                mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            } catch (Exception e) {
                System.err.println("ERRO: Falha ao carregar textura Slime. Usa VERDE.");
            }
        }

        else if (npc instanceof jogo.gameobject.npc.hostil.Zombie) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Blue);
            try {
                mat.setTexture("ColorMap", app.getAssetManager().loadTexture("Textures/NPC/zombie.png"));
            } catch (Exception e) {
                System.err.println("ERRO: Falha ao carregar textura Zumbi. Usa AZUL SÓLIDO.");
            }
        }

        else if (npc instanceof Cow) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Brown);
        }

        else if (npc instanceof Healer) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Yellow); // Cor amarela sólida
        }

        else {
            mat.setColor("Color", com.jme3.math.ColorRGBA.White);
        }

        geo.setMaterial(mat);
        parent.attachChild(geo);

        parent.setLocalTranslation(
                npc.getPosition().x,
                npc.getPosition().y,
                npc.getPosition().z
        );

        return parent;
    }


    /**
     * Ciclo principal de atualização dos NPCs. Define o jogador como alvo para as IAs,
     * executa a lógica de decisão de cada NPC (updateAI) e sincroniza a posição dos
     * modelos 3D com as coordenadas lógicas.
     * @param tpf Tempo desde o último frame (time per frame).
     */
    @Override
    public void update(float tpf) {

        if (player == null) return;

        for (NPC npc : npcList) {

            if (npc instanceof Slime slime) {
                slime.setTarget(player.getPosition());
            }
            else if (npc instanceof Zombie zombie) {
                zombie.setTarget(player.getPosition());
            }

            else if (npc instanceof Cow cow) {
                cow.setTarget(player.getPosition());
            }

            else if (npc instanceof Healer healer) {
                healer.setTarget(player.getPosition());
            }

            npc.updateAI(tpf);

            com.jme3.scene.Node model = models.get(npc);
            if (model != null) {
                model.setLocalTranslation(
                        npc.getPosition().x,
                        npc.getPosition().y,
                        npc.getPosition().z
                );
            }
        }
    }

    /**
     * Remove um NPC do jogo de forma completa: retira o modelo da árvore de cena,
     * remove o registo de colisão no RenderIndex e elimina o NPC das listas de controlo.
     * @param npc O NPC a ser removido.
     */
    public void removeNPC(NPC npc) {
        Node model = models.get(npc);
        if (model != null) {
            model.removeFromParent();

            renderIndex.unregister(model);

            models.remove(npc);
            modelToNpc.remove(model);
            npcList.remove(npc);

            System.out.println("NPC " + npc.getName() + " removido da cena.");
        }
    }

    @Override
    protected void cleanup(Application app) {
        for (Node model : models.values()) {
            model.removeFromParent();
        }
        models.clear();
        modelToNpc.clear();
    }

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}