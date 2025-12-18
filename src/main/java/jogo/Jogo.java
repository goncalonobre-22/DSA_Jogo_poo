package jogo;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.AppSettings;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import jogo.appstate.*;
import jogo.engine.GameRegistry;
import jogo.engine.RenderIndex;
import jogo.gameobject.character.Player;

/**
 * Main application entry.
 */
public class Jogo extends SimpleApplication {

    public static void main(String[] args) {
        Jogo app = new Jogo();
        app.setShowSettings(true); // show settings dialog
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Test");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setGammaCorrection(true); // enable sRGB gamma-correct rendering
        app.setSettings(settings);
        app.start();
    }



    private BulletAppState bulletAppState;

    private AudioNode hurtSound;

    @Override
    public void simpleInitApp() {
        // disable flyCam, we manage camera ourselves
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.6f, 0.75f, 1f, 1f)); // sky-like

        // Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(false); // toggle off later
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

        // AppStates (order matters a bit: input -> world -> render -> interaction -> player)
        InputAppState input = new InputAppState();
        stateManager.attach(input);

        Player player_inv = new Player();

        WorldAppState world = new WorldAppState(rootNode, assetManager, physicsSpace, cam, input, player_inv);
        stateManager.attach(world);

        // Engine registry and render layers
        GameRegistry registry = new GameRegistry();
        RenderIndex renderIndex = new RenderIndex();
        stateManager.attach(new RenderAppState(rootNode, assetManager, registry, renderIndex));
        stateManager.attach(new InteractionAppState(rootNode, cam, input, renderIndex, world));

        try {
            hurtSound = new AudioNode(assetManager, "Sounds/hurtPlayer.ogg", false);
            hurtSound.setPositional(false);
            hurtSound.setLooping(false);
            hurtSound.setVolume(2);
        } catch (Exception e) {
            System.err.println("Erro ao carregar o som de dano (hurt.ogg): " + e.getMessage());
        }

        PlayerAppState player = new PlayerAppState(rootNode, assetManager, cam, input, physicsSpace, world, player_inv);
        stateManager.attach(player);

        if (hurtSound != null) {
            player.setHurtSound(hurtSound);
        }

        stateManager.attach(new NPCAppState(world.getNpcList(), world, player_inv, renderIndex, registry));

        // Post-processing: SSAO for subtle contact shadows
        try {
            FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
            Class<?> ssaoCls = Class.forName("com.jme3.post.ssao.SSAOFilter");
            Object ssao = ssaoCls.getConstructor(float.class, float.class, float.class, float.class)
                    .newInstance(2.1f, 0.6f, 0.5f, 0.02f); // radius, intensity, scale, bias
            // Add filter via reflection to avoid compile-time dependency
            java.lang.reflect.Method addFilter = FilterPostProcessor.class.getMethod("addFilter", Class.forName("com.jme3.post.Filter"));
            addFilter.invoke(fpp, ssao);
            viewPort.addProcessor(fpp);
        } catch (Exception e) {
            System.out.println("SSAO not available (effects module missing?): " + e.getMessage());
        }

        // HUD (just a crosshair for now)
        HudAppState hudAppState = new HudAppState(guiNode, assetManager);
        hudAppState.setPlayer(player_inv);
        stateManager.attach(hudAppState);

        // Handler para toggle do inventário
        stateManager.attach(new BaseAppState() {
            @Override
            protected void initialize(com.jme3.app.Application app) {}

            @Override
            public void update(float tpf) {
                if (input.consumeToggleInventoryRequested()) {
                    hudAppState.toggleInventory();
                    input.setMouseCaptured(!hudAppState.isInventoryOpen());
                }
            }

            @Override
            protected void cleanup(com.jme3.app.Application app) {}
            @Override
            protected void onEnable() {}
            @Override
            protected void onDisable() {}
        });
    }
}
