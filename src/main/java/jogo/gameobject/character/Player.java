package jogo.gameobject.character;

import jogo.framework.math.Vec3;
import jogo.util.inventory.Inventory;
import jogo.util.inventory.Stacks;

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
        float decayMultiplier = isSprinting ? 4.0f : 1.0f; // Corrigido para 4.0f

        // 1. Atualiza o timer de fome (decay)
        hungerTimer += tpf * decayMultiplier;

        if (hungerTimer >= BASE_DECAY_TIME) {
            int decayAmount = 2;
            setHunger(hunger - decayAmount);
            hungerTimer = 0.0f; // Reseta o timer
        }

        // 2. Lógica de dano por inanição (starvation)
        if (hunger <= 0) {
            starvationTimer += tpf;
            if (starvationTimer >= STARVATION_DAMAGE_TIME) {
                // NOVO: Chama takeDamage para acionar o som e a lógica centralizada
                takeDamage(5);
                starvationTimer = 0.0f; // Reseta o timer de dano
            }
        } else {
            // Garante que o timer é resetado se a fome subir
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

    @Override
    public void takeDamage(int damage) {
        int oldHealth = getHealth();
        super.takeDamage(damage); // Chama o takeDamage de Character, que aplica Math.max(0, health - damage)
        int newHealth = getHealth();

        // Se a vida diminuiu e o jogador não morreu
        if (newHealth < oldHealth && newHealth > 0) {
            this.justTookDamage = true; // Aciona o flag para o AppState ler
            System.out.println("Player sofreu dano. Vida: " + newHealth);
        }
        // Nota: A lógica de dano por inanição em updateHunger() deve ser alterada
        // para chamar takeDamage() em vez de takeDamage(2) diretamente.
    }

    /** NOVO: Para o AppState verificar se o jogador sofreu dano. */
    public boolean consumeDamageFlag() {
        if (justTookDamage) {
            justTookDamage = false;
            return true;
        }
        return false;
    }

    public void setPosition(Vec3 newPosition) {
        // Assume-se que 'position' é um campo herdado/existente na classe Character
        this.position = newPosition;
    }
}
