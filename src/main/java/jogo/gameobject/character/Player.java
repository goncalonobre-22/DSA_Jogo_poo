package jogo.gameobject.character;

import jogo.util.Inventory;
import jogo.util.Stacks;

public class Player extends Character {
    private final Inventory inventory;
    private final Stacks[] craftingGrid = new Stacks[9]; // 3x3 grid
    private int selectedCraftSlot = 0; // Slot selecionado na grid

    // [NOVOS CAMPOS] Para o sistema de Fome
    private int hunger = 100;
    private float hungerTimer = 0.0f;
    private float starvationTimer = 0.0f;
    // Constantes do Sistema
    private static final float BASE_DECAY_TIME = 120.0f; // 2 minutos = 120 segundos
    private static final float STARVATION_DAMAGE_TIME = 2.0f;

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

    public void clearCraftingGrid() {
        for (int i = 0; i < craftingGrid.length; i++) {
            craftingGrid[i] = null;
        }
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        // Garante que a fome fica entre 0 e 100
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    public void updateHunger(float tpf, boolean isSprinting) {
        // Multiplicador de 4x se estiver a correr
        float decayMultiplier = isSprinting ? 400.0f : 1.0f;

        // 1. Atualiza o timer de fome (decay)
        hungerTimer += tpf * decayMultiplier;

        if (hungerTimer >= BASE_DECAY_TIME) {
            int decayAmount = 2;
            setHunger(hunger - decayAmount);
            // System.out.println("Fome diminuiu para: " + hunger);
            hungerTimer = 0.0f; // Reseta o timer
        }

        // 2. Lógica de dano por inanição (starvation)
        if (hunger <= 0) {
            starvationTimer += tpf;
            if (starvationTimer >= STARVATION_DAMAGE_TIME) {
                takeDamage(2); // Perde 2 de vida
                // System.out.println("Inanição! Vida atual: " + getHealth());
                starvationTimer = 0.0f; // Reseta o timer de dano
            }
        } else {
            // Garante que o timer de inanição é resetado se a fome subir
            starvationTimer = 0.0f;
        }
    }

    public void resetStats() {
        setHealth(100);
        setHunger(100);
        // Reseta timers para evitar ticks imediatos
        this.hungerTimer = 0.0f;
        this.starvationTimer = 0.0f;
    }
}
