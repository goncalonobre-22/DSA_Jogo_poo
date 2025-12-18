package jogo.gameobject.character;

import jogo.framework.math.Vec3;
import jogo.util.inventory.Inventory;
import jogo.util.inventory.Stacks;

import java.util.LinkedList;
import java.util.Queue;

public class Player extends Character {
    // Inventário
    private final Inventory inventory;
    private final Stacks[] craftingGrid = new Stacks[9];
    private int selectedCraftSlot = 0;

    // Fome
    private int hunger = 100;
    private float hungerTimer = 0.0f;
    private float starvationTimer = 0.0f;

    // Constantes do sistema de fome
    private static final float BASE_DECAY_TIME = 120.0f; // 2 minutos = 120 segundos
    private static final float STARVATION_DAMAGE_TIME = 2.0f;

    private int score = 0;

    private final Queue<Integer> scoreQueue = new LinkedList<>();

    private boolean justTookDamage = false;



    public Player() {
        super("Player");
        this.inventory = new Inventory(40);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Stacks[] getCraftingGrid() {
        return craftingGrid;
    }

    public int getSelectedCraftSlot() {
        return selectedCraftSlot;
    }

    public void setSelectedCraftSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            this.selectedCraftSlot = slot;
        }
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    public void updateHunger(float tpf, boolean isSprinting) {
        float decayMultiplier = isSprinting ? 4.0f : 1.0f;

        hungerTimer += tpf * decayMultiplier;

        if (hungerTimer >= BASE_DECAY_TIME) {
            int decayAmount = 2;
            setHunger(hunger - decayAmount);
            hungerTimer = 0.0f;
        }

        if (hunger <= 0) {
            starvationTimer += tpf;
            if (starvationTimer >= STARVATION_DAMAGE_TIME) {
                takeDamage(5);
                starvationTimer = 0.0f;
            }
        } else {
            starvationTimer = 0.0f;
        }
    }

    public void resetStats() {
        setHealth(100);
        setHunger(100);
        this.hungerTimer = 0.0f;
        this.starvationTimer = 0.0f;
    }

    @Override
    public void takeDamage(int damage) {
        int oldHealth = getHealth();
        super.takeDamage(damage);
        int newHealth = getHealth();

        if (newHealth < oldHealth && newHealth > 0) {
            this.justTookDamage = true; // Aciona o flag para o AppState ler
            System.out.println("Player sofreu dano. Vida: " + newHealth);
        }
    }

    // Para o AppState verificar se o jogador sofreu dano.
    public boolean consumeDamageFlag() {
        if (justTookDamage) {
            justTookDamage = false;
            return true;
        }
        return false;
    }

    public int getScore() {
        return score;
    }

    // Adiciona a quantidade de pontos ao score
    public void incrementScore(int amount) {
        this.score += amount;
    }

    // Adiciona um evento de incremento de pontuação à fila
    public void addScoreIncrement(int amount) {
        if (amount > 0) {
            scoreQueue.offer(amount);
        }
    }

    // Processa todos os incrementos de pontuação pendentes na fila
    public void processScoreQueue() {
        while (!scoreQueue.isEmpty()) {
            int amount = scoreQueue.poll();
            incrementScore(amount);
        }
    }

}
