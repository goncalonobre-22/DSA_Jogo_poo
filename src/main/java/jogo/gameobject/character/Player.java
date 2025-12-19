package jogo.gameobject.character;

import jogo.framework.math.Vec3;
import jogo.util.inventory.Inventory;
import jogo.util.inventory.Stacks;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Representa o jogador controlado pelo utilizador no jogo.
 * Gere o inventário, sistema de crafting, níveis de fome, pontuação e estados de dano.
 */
public class Player extends Character {

    /** Inventário principal do jogador. */
    private final Inventory inventory;

    /** Grelha de crafting de 3x3 (9 slots). */
    private final Stacks[] craftingGrid = new Stacks[9];

    /** Índice do slot atualmente selecionado na grelha de crafting. */
    private int selectedCraftSlot = 0;

    /** Nível atual de fome do jogador (0 a 100). */
    private int hunger = 100;

    /** Temporizador acumulado para a redução passiva de fome. */
    private float hungerTimer = 0.0f;

    /** Temporizador para aplicação de dano por inanição quando a fome chega a zero. */
    private float starvationTimer = 0.0f;

    /** Tempo base (em segundos) para ocorrer uma redução no nível de fome. */
    private static final float BASE_DECAY_TIME = 120.0f;

    /** Intervalo de tempo (em segundos) entre cada penalização de vida por fome zero. */
    private static final float STARVATION_DAMAGE_TIME = 2.0f;

    /** Pontuação total acumulada pelo jogador. */
    private int score = 0;

    /** Fila para gerir incrementos de pontuação pendentes de processamento. */
    private final Queue<Integer> scoreQueue = new LinkedList<>();

    /** Sinalizador (flag) que indica se o jogador acabou de sofrer dano. */
    private boolean justTookDamage = false;

    /**
     * Construtor da classe Player.
     * Inicializa o personagem com o nome "Player" e cria um inventário com 40 slots.
     */
    public Player() {
        super("Player");
        this.inventory = new Inventory(40);
    }

    /**
     * Retorna o inventário do jogador.
     * @return O objeto Inventory associado ao jogador.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Retorna a grelha de crafting atual.
     * @return Array de Stacks representando os itens na bancada de trabalho.
     */
    public Stacks[] getCraftingGrid() {
        return craftingGrid;
    }

    /**
     * Obtém o índice do slot de crafting selecionado.
     * @return O índice do slot.
     */
    public int getSelectedCraftSlot() {
        return selectedCraftSlot;
    }

    /**
     * Define o slot de crafting a ser selecionado, validando se o índice está dentro dos limites (0-8).
     * @param slot O índice do slot desejado.
     */
    public void setSelectedCraftSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            this.selectedCraftSlot = slot;
        }
    }

    /**
     * Retorna o nível de fome atual do jogador.
     * @return Valor inteiro da fome.
     */
    public int getHunger() {
        return hunger;
    }

    /**
     * Define o nível de fome do jogador, limitando o valor entre 0 e 100.
     * @param hunger Novo valor de fome.
     */
    public void setHunger(int hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    /**
     * Atualiza a lógica de fome com base no tempo decorrido.
     * Reduz a fome periodicamente (mais rápido ao correr) e aplica dano se o nível chegar a zero.
     * @param tpf Tempo por frame (Time Per Frame).
     * @param isSprinting Indica se o jogador está a correr para acelerar o consumo de energia.
     */
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

    /**
     * Restaura as estatísticas vitais do jogador para os valores iniciais.
     * Define vida e fome a 100 e limpa os temporizadores.
     */
    public void resetStats() {
        setHealth(100);
        setHunger(100);
        this.hungerTimer = 0.0f;
        this.starvationTimer = 0.0f;
    }

    /**
     * Aplica dano ao jogador e aciona o sinalizador de dano se a vida diminuir.
     * @param damage Quantidade de dano a subtrair.
     */
    @Override
    public void takeDamage(int damage) {
        int oldHealth = getHealth();
        super.takeDamage(damage);
        int newHealth = getHealth();

        if (newHealth < oldHealth && newHealth > 0) {
            this.justTookDamage = true;
            System.out.println("Player sofreu dano. Vida: " + newHealth);
        }
    }

    /**
     * Verifica e consome o sinalizador de dano.
     * Utilizado por sistemas externos (como HUD/Efeitos) para detetar impactos recentes.
     * @return true se o jogador sofreu dano desde a última verificação; false caso contrário.
     */
    public boolean consumeDamageFlag() {
        if (justTookDamage) {
            justTookDamage = false;
            return true;
        }
        return false;
    }

    /**
     * Obtém a pontuação atual do jogador.
     * @return Valor total do score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Adiciona diretamente uma quantidade de pontos à pontuação total.
     * @param amount Quantidade de pontos a somar.
     */
    public void incrementScore(int amount) {
        this.score += amount;
    }

    /**
     * Adiciona um incremento de pontuação à fila para processamento posterior.
     * @param amount Quantidade de pontos a adicionar à fila (deve ser > 0).
     */
    public void addScoreIncrement(int amount) {
        if (amount > 0) {
            scoreQueue.offer(amount);
        }
    }

    /**
     * Processa e esvazia a fila de pontuações pendentes, somando-as ao score total.
     */
    public void processScoreQueue() {
        while (!scoreQueue.isEmpty()) {
            int amount = scoreQueue.poll();
            incrementScore(amount);
        }
    }
}