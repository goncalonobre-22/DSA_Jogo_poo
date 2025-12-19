package jogo.gameobject.item;

import jogo.voxel.VoxelBlockType;

/**
 * Classe base abstrata para ferramentas no jogo.
 * Estende {@link Item} e introduz capacidades de mineração e combate,
 * permitindo que diferentes ferramentas tenham eficiências distintas conforme o material.
 */
public abstract class Tool extends Item {

    /**
     * Construtor da classe Tool.
     * @param nome O nome identificador da ferramenta.
     */
    public Tool(String nome) {
        super(nome);
    }

    /**
     * Retorna a velocidade base de mineração da ferramenta.
     * Por defeito, ferramentas têm uma velocidade de 1.0f.
     * @return O valor da velocidade de mineração base.
     */
    public float getMiningSpeed() {
        return 1.0f;
    }

    /**
     * Retorna o multiplicador de dano de ataque desta ferramenta.
     * Utilizado para calcular o dano causado a entidades (NPCs) quando a ferramenta é usada como arma.
     * @return O multiplicador de ataque (padrão é 1.0f).
     */
    public float getAttackMultiplier() {
        return 1.0f; // Multiplicador padrão (1.0x para a Mão/Item básico)
    }

    /**
     * Calcula a velocidade de mineração específica para um determinado tipo de bloco.
     * Este método deve ser implementado por ferramentas específicas para definir se são
     * mais eficientes contra certos materiais (ex: picareta contra pedra).
     * @param type O tipo de bloco ({@link VoxelBlockType}) que está a ser minerado.
     * @return A velocidade de mineração ajustada para o tipo de bloco.
     */
    public abstract float getMiningSpeed(VoxelBlockType type);
}