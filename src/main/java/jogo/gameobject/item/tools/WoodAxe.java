package jogo.gameobject.item.tools;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.Camera;
import com.jme3.texture.Texture;
import jogo.appstate.WorldAppState;
import jogo.gameobject.item.Tool;
import jogo.util.Hit;
import jogo.voxel.VoxelBlockType;
import jogo.voxel.VoxelPalette;
import jogo.voxel.VoxelWorld;
import java.util.Optional;

/**
 * Representa um Machado de Madeira (Wood Axe).
 * Esta ferramenta é eficiente para minerar blocos naturais e possui uma habilidade
 * especial de transformar blocos de madeira bruta em tábuas ao interagir com eles.
 */
public class WoodAxe extends Tool {

    /** Referência ao estado do mundo para permitir modificações nos blocos. */
    public WorldAppState worldAppState;

    /** Referência à câmara do jogador para calcular a direção da interação (raycasting). */
    public Camera camera;

    /**
     * Construtor da classe WoodAxe.
     * Inicializa a ferramenta com o nome "Wood Axe".
     */
    public WoodAxe() {
        super("Wood Axe");
    }

    /**
     * Carrega e retorna o ícone de textura do machado de madeira.
     * @param assetManager O gestor de assets utilizado para carregar o ficheiro "Interface/woodAxe.png".
     * @return A {@link Texture} do ícone da ferramenta.
     */
    @Override
    public Texture getIcon(AssetManager assetManager) {
        return assetManager.loadTexture("Interface/woodAxe.png");
    }

    /**
     * Calcula a velocidade de mineração baseada na categoria do bloco.
     * Oferece um bónus de 1.75x para blocos que não sejam GRANULAR ou COMPACT (ex: MADEIRA).
     * @param type O tipo de bloco ({@link VoxelBlockType}) a ser minerado.
     * @return A velocidade de mineração: 1.25f para GRANULAR, 1.0f para COMPACT e 1.75f para os restantes.
     */
    @Override
    public float getMiningSpeed(VoxelBlockType type) {
        String category = type.getMiningCategory();
        if (category.equals("GRANULAR")) {
            return 1.25f;
        }
        if (category.equals("COMPACT")) {
            return 1f;
        }
        return 1.75f;
    }

    /**
     * Retorna o multiplicador de dano de ataque para esta ferramenta.
     * @return Um multiplicador de 3.0x, tornando-a mais forte que a mão vazia.
     */
    @Override
    public float getAttackMultiplier() {
        return 3f;
    }

    /**
     * Define a lógica de interação especial do machado.
     * Realiza um teste de colisão (pick) num raio de 6 unidades. Se o bloco atingido
     * for um bloco de madeira (WOOD_ID), transforma-o automaticamente em tábuas (PLANK_ID)
     * e atualiza a malha e a física do mundo.
     */
    @Override
    public void onInteract() {
        if (worldAppState == null || camera == null) {
            System.out.println("Erro: WoodAxe não tem contexto de mundo/câmera injetado.");
            return;
        }

        VoxelWorld vw = worldAppState.getVoxelWorld();
        // Usa um alcance de 6f
        Optional<Hit> pick = vw.pickFirstSolid(camera, 6f);

        pick.ifPresent(hit -> {
            VoxelWorld.Vector3i cell = hit.cell;
            byte blockId = vw.getBlock(cell.x, cell.y, cell.z);

            if (blockId == VoxelPalette.WOOD_ID) {
                // Transforma Wood Block em Plank Block
                vw.setBlock(cell.x, cell.y, cell.z, VoxelPalette.PLANK_ID);

                // Reconstroi o chunk e a física
                worldAppState.getVoxelWorld().rebuildDirtyChunks(worldAppState.getPhysicsSpace());

                System.out.println("Wood Block transformado em Plank Block em: " + cell.x + "," + cell.y + "," + cell.z);
            }
        });

        if (pick.isEmpty()) {
            System.out.println("Nenhum bloco sólido encontrado no alcance para interagir.");
        }
    }
}