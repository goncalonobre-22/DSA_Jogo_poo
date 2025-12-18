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


    // Método para obter a vida máxima
    public int getMaxHealth() {
        return maxHealth;
    }

    // Método para obter a vida atual
    public int getHealth() {
        return health;
    }

    // Método para definir a vida
    public void setHealth(int newHealth) {
        this.health = Math.min(newHealth, maxHealth);
        this.health = Math.max(0, this.health);
    }

    // Método para levar dano
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    // Método para curar
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