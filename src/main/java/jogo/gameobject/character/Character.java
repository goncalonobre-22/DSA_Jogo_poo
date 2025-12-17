package jogo.gameobject.character;

import jogo.gameobject.GameObject;
import jogo.framework.math.Vec3; // Se Vec3 for necessário para a posição (Assumimos que sim)

public abstract class Character extends GameObject {

    // --- CAMPOS DE SAÚDE ATUALIZADOS ---
    protected int health;
    protected int maxHealth = 100; // Definimos a vida máxima por defeito

    // O campo 'position' é herdado ou deve ser adicionado aqui se não estiver em GameObject
    protected Vec3 position;

    protected Character(String name) {
        super(name);
        this.health = maxHealth; // Inicializar a vida cheia
    }

    // --- MÉTODOS DE SAÚDE ATUALIZADOS ---

    // [NOVO] Método para obter a vida máxima (necessário para o Healer)
    public int getMaxHealth() {
        return maxHealth;
    }

    // Método para obter a vida atual
    public int getHealth() {
        return health;
    }

    // Método para definir a vida (garante que não ultrapassa o máximo)
    public void setHealth(int newHealth) {
        this.health = Math.min(newHealth, maxHealth);
        this.health = Math.max(0, this.health); // Garante que não é negativo
    }

    // Método para levar dano
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    // [NOVO] Método para curar (necessário para o Healer)
    public void heal(int amount) {
        this.health = Math.min(this.health + amount, maxHealth);
    }

    // --- MÉTODOS DE POSIÇÃO ---

    // Para ser usado pelo NPCAppState para dar o alvo à Slime/Zombie
    public Vec3 getPosition() {
        return position;
    }

    // Para ser usado pelo PlayerAppState para sincronizar a posição real
    public void setPosition(Vec3 position) {
        this.position = position;
    }
}