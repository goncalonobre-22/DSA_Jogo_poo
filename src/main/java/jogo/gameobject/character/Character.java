package jogo.gameobject.character;

import jogo.gameobject.GameObject;
import jogo.framework.math.Vec3; // Se Vec3 for necessário para a posição (Assumimos que sim)

public abstract class Character extends GameObject {


    protected int health;
    protected int maxHealth = 100; // Definimos a vida máxima por defeito

    // O campo 'position' é herdado ou deve ser adicionado aqui se não estiver em GameObject
    protected Vec3 position;

    protected Character(String name) {
        super(name);
        this.health = maxHealth; // Inicializar a vida cheia
    }


    /**
     * Retorna o valor da vida máxima permitida para este personagem.
     * * @return O valor de maxHealth.
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Retorna a quantidade de vida atual do personagem.
     * * @return O valor de health.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Define a quantidade de pontos de vida do personagem, garantindo que o valor
     * não ultrapasse o limite máximo permitido nem seja inferior a zero.
     * * @param newHealth Novo valor de vida a ser atribuído.
     */
    public void setHealth(int newHealth) {
        this.health = Math.min(newHealth, maxHealth);
        this.health = Math.max(0, this.health);
    }

    /**
     * Aplica dano ao personagem, subtraindo o valor indicado à vida atual.
     * O valor final de vida é garantido como sendo, no mínimo, zero.
     * * @param damage Quantidade de dano a aplicar.
     */
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    /**
     * Cura o personagem ao adicionar uma quantidade específica de pontos à sua vida atual.
     * A vida resultante não pode exceder o valor definido em maxHealth.
     * * @param amount Quantidade de pontos de vida a restaurar.
     */
    public void heal(int amount) {
        this.health = Math.min(this.health + amount, maxHealth);
    }


    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }
}