package jogo.util.furnace;

import jogo.gameobject.item.Item;
import jogo.util.inventory.Stacks;

/**
 * Representa o estado interno e a lógica de processamento de uma fornalha específica.
 * Controla os slots de entrada, combustível e saída, bem como o progresso da fundição
 * e o consumo de energia calorífica.
 */
public class FurnaceState {
    /** Slot que contém o item a ser processado. */
    public Stacks inputStack = null;

    /** Slot que contém o combustível atual. */
    public Stacks fuelStack = null;

    /** Slot que contém o resultado final do processamento. */
    public Stacks outputStack = null;

    /** Progresso atual da fundição (de 0.0f até MELT_TIME_TOTAL). */
    public float meltProgress = 0.0f;

    /** Quantidade de energia de combustível restante na câmara de combustão. */
    public float fuelLeft = 0.0f;

    /** Tempo total (em segundos) necessário para processar um item. */
    public static final float MELT_TIME_TOTAL = 8.0f;

    /** Duração de cada fase visual da fundição (utilizado para animações ou ícones). */
    public static final float PHASE_DURATION = 2.0f;

    /**
     * Calcula a fase atual do progresso de fundição (0 a 4).
     * @return O índice da fase baseado no progresso atual.
     */
    public int getPhase() {
        if (inputStack == null || fuelLeft <= 0.0f) return 0;
        return (int) (meltProgress / PHASE_DURATION);
    }

    /**
     * Tenta colocar um item no slot de entrada.
     * Só aceita o item se houver uma receita válida registada para ele.
     * @param item O item a ser fundido.
     * @return true se o item foi aceite; false caso contrário.
     */
    public boolean setInput(Item item) {
        if (inputStack == null) {
            if (FurnaceRegistry.findRecipe(item) != null) {
                inputStack = new Stacks(item, 1);
                meltProgress = 0.0f;
                return true;
            }
        }
        return false;
    }

    /**
     * Tenta abastecer a fornalha com combustível.
     * @param item O item a ser usado como combustível.
     * @return true se o item for um combustível válido e o slot estiver livre; false caso contrário.
     */
    public boolean setFuel(Item item) {
        if (fuelStack == null && FurnaceRegistry.getFuelEfficiency(item) > 0.0f) {
            fuelStack = new Stacks(item, 1);
            return true;
        }
        return false;
    }

    /**
     * Executa a lógica de atualização da fornalha.
     * Consome combustível, avança o progresso da fundição e gera o item de saída
     * quando o processo termina, desde que o slot de saída esteja disponível.
     * @param tpf Tempo por frame (Time Per Frame).
     */
    public void updateMelt(float tpf) {
        if (inputStack == null) {
            meltProgress = 0.0f;
            return;
        }

        // Se a fundição terminou, aguarda que o slot de output seja libertado.
        if (meltProgress >= MELT_TIME_TOTAL) {
            return;
        }

        // Tenta consumir uma nova unidade de combustível se a energia acabou.
        if (fuelLeft <= 0.0f) {
            if (fuelStack != null && fuelStack.getAmount() > 0) {
                fuelLeft = FurnaceRegistry.getFuelEfficiency(fuelStack.getItem());
                fuelStack.removeAmount(1);
                if (fuelStack.getAmount() == 0) fuelStack = null;
            } else {
                return; // Para o progresso se não houver combustível disponível.
            }
        }

        // Avança o progresso e consome a energia do combustível proporcionalmente ao tempo.
        float fuelCost = tpf / MELT_TIME_TOTAL;
        meltProgress += tpf;
        fuelLeft -= fuelCost;

        // Finaliza o processamento e move o resultado para o slot de saída.
        if (meltProgress >= MELT_TIME_TOTAL) {
            FurnaceRecipe recipe = FurnaceRegistry.findRecipe(inputStack.getItem());
            if (recipe != null) {
                Item outputItem = recipe.getOutput();

                if (outputStack == null) {
                    outputStack = new Stacks(outputItem, 1);
                    inputStack.removeAmount(1);
                    meltProgress = 0.0f;
                } else if (outputStack.isSameItem(outputItem) && !outputStack.isFull()) {
                    outputStack.addAmount(1);
                    inputStack.removeAmount(1);
                    if (inputStack.getAmount() == 0) inputStack = null;
                    meltProgress = 0.0f;
                }
            }
        }

        if (fuelLeft < 0.0f) fuelLeft = 0.0f;
    }
}