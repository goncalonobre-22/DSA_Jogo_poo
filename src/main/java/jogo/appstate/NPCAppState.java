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

    //    private final Node worldNode;
    private final Player player;
    private final RenderIndex renderIndex;
    private final GameRegistry registry; // Não usado para NPC, mas bom para consistência
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

    @Override
    protected void initialize(Application app) {
        Node worldNode = worldAppState.getWorldNode();
        for (NPC npc : npcList) {
            Node model = create3DModel(app, npc);
            models.put(npc, model);
            modelToNpc.put(model, npc);
            worldNode.attachChild(model);

            renderIndex.register(model, npc);

            // [NOVO] Injetar o AppState hook no NPC
            npc.appStateHook = this;
        }
    }

    private com.jme3.scene.Node create3DModel(com.jme3.app.Application app, jogo.gameobject.npc.NPC npc) {

        com.jme3.scene.Node parent = new com.jme3.scene.Node(npc.getName());
        com.jme3.scene.Geometry geo;

        // --- 1. LÓGICA DA GEOMETRIA ---
        if (npc instanceof jogo.gameobject.npc.hostil.Slime) {
            com.jme3.scene.shape.Sphere sphere = new com.jme3.scene.shape.Sphere(16, 16, 0.5f);
            geo = new com.jme3.scene.Geometry("slime", sphere);
            geo.setLocalScale(1f, 0.6f, 1f);
            geo.setLocalTranslation(0, 0.3f, 0);
        }
        else if (npc instanceof jogo.gameobject.npc.hostil.Zombie) {
            // Caixa de 0.6m de largura/profundidade e 1.8m de altura
            com.jme3.scene.shape.Box box = new com.jme3.scene.shape.Box(0.3f, 0.9f, 0.3f);
            geo = new com.jme3.scene.Geometry("zombie", box);
            // Desloca para assentar no chão (Y=0).
            geo.setLocalTranslation(0, 0.9f, 0);
        }
        // --- NOVO: VACA (Cow) ---
        else if (npc instanceof Cow) {
            // Modelo maior e mais gordo: 1.6m x 1.2m x 0.8m
            com.jme3.scene.shape.Box body = new com.jme3.scene.shape.Box(0.8f, 0.6f, 0.4f);
            geo = new com.jme3.scene.Geometry("cow", body);
            // Altura total 1.2m, desloca o centro 0.6f
            geo.setLocalTranslation(0, 0.6f, 0);
        }
        // --- NOVO: CURANDEIRO (Healer) ---
        else if (npc instanceof Healer) {
            // Modelo humanoide simples: 0.4m x 1.6m x 0.4m
            com.jme3.scene.shape.Box body = new com.jme3.scene.shape.Box(0.2f, 0.8f, 0.2f);
            geo = new com.jme3.scene.Geometry("healer", body);
            // Altura total 1.6m, desloca o centro 0.8f
            geo.setLocalTranslation(0, 0.8f, 0);
        }
        // --- FALLBACK ---
        else {
            geo = new com.jme3.scene.Geometry("default", new com.jme3.scene.shape.Sphere(8, 8, 0.1f));
        }


        // --- 2. MATERIAL (Aplicação Universal) ---
        com.jme3.material.Material mat = new com.jme3.material.Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");

        // --- SLIME: MATERIAL E TEXTURA ---
        if (npc instanceof jogo.gameobject.npc.hostil.Slime) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Green);
            try {
                mat.setTexture("ColorMap", app.getAssetManager().loadTexture("Textures/NPC/slime.png"));
                mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            } catch (Exception e) {
                System.err.println("ERRO: Falha ao carregar textura Slime. Usa VERDE.");
            }
        }

        // --- ZUMBI: MATERIAL E TEXTURA ---
        else if (npc instanceof jogo.gameobject.npc.hostil.Zombie) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Blue); // Cor de fallback
            try {
                mat.setTexture("ColorMap", app.getAssetManager().loadTexture("Textures/NPC/zombie.png"));
            } catch (Exception e) {
                System.err.println("ERRO: Falha ao carregar textura Zumbi. Usa AZUL SÓLIDO.");
            }
        }

        // --- NOVO: VACA (Cow) ---
        else if (npc instanceof Cow) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Brown); // Cor castanha sólida
        }

        // --- NOVO: CURANDEIRO (Healer) ---
        else if (npc instanceof Healer) {
            mat.setColor("Color", com.jme3.math.ColorRGBA.Yellow); // Cor amarela sólida
        }

        // --- FALLBACK GERAL ---
        else {
            mat.setColor("Color", com.jme3.math.ColorRGBA.White);
        }

        geo.setMaterial(mat);
        parent.attachChild(geo);

        // Posição Final da Node (que é o ponto de spawn lógico)
        parent.setLocalTranslation(
                npc.getPosition().x,
                npc.getPosition().y,
                npc.getPosition().z
        );

        return parent;
    }

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

            // --- NOVO: Cow Target ---
            else if (npc instanceof Cow cow) {
                cow.setTarget(player.getPosition());
            }

            // --- NOVO: Healer Target ---
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

    // Adicionar método de remoção:
    public void removeNPC(NPC npc) {
        Node model = models.get(npc);
        if (model != null) {
            // 1. Remover da cena
            model.removeFromParent();

            // 2. Limpar o RenderIndex
            renderIndex.unregister(model);

            // 3. Remover dos mapas e da lista de NPCs a serem atualizados (AI)
            models.remove(npc);
            modelToNpc.remove(model);
            npcList.remove(npc);
            // Nota: Se houver controlo de física no NPC (RigidBodyControl), também deve ser removido aqui,
            // mas a Slime usa lógica de movimento manual por padrão (NPC.setPhysicsControl retorna null).

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