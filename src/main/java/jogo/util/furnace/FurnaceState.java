package jogo.util.furnace;

import jogo.gameobject.item.Item;
import jogo.util.inventory.Stacks;

public class FurnaceState {
    public Stacks inputStack = null;
    public Stacks fuelStack = null;
    public Stacks outputStack = null;

    public float meltProgress = 0.0f; // 0.0f a 8.0f (tempo total de fusão)
    public float fuelLeft = 0.0f; // Combustível restante (em unidades de 'melts')

    public static final float MELT_TIME_TOTAL = 8.0f;
    public static final float PHASE_DURATION = 2.0f; // 8.0 / 4 fases

    /**
     * Retorna o índice da imagem de fase (0 a 4, onde 4 é o progresso total).
     */
    public int getPhase() {
        if (inputStack == null || fuelLeft <= 0.0f) return 0;
        // Fase 1 a 4
        return (int) (meltProgress / PHASE_DURATION);
    }

    /**
     * Coloca um item no slot de Input (se for uma receita válida).
     */
    public boolean setInput(Item item) {
        if (inputStack == null) {
            if (FurnaceRegistry.findRecipe(item) != null) {
                inputStack = new Stacks(item, 1);
                meltProgress = 0.0f;
                return true;
            }
        }
        // 2. Se o slot já tiver o mesmo item e não estiver cheio (MAX_STACK_SIZE)
        else if (inputStack.isSameItem(item) && !inputStack.isFull()) {
            inputStack.addAmount(1);
            return true;
        }
        return false;
    }

    /**
     * Coloca combustível (se for um item combustível).
     */
    public boolean setFuel(Item item) {
        if (fuelStack == null && FurnaceRegistry.getFuelEfficiency(item) > 0.0f) {
            fuelStack = new Stacks(item, 1);
            return true;
        }
        return false;
    }

    /**
     * O coração da lógica da fornalha, chamado pelo WorldAppState.
     */
    public void updateMelt(float tpf) {
        if (inputStack == null) {
            meltProgress = 0.0f;
            return;
        }

        // 1. Fundição terminada, mas output bloqueado
        if (meltProgress >= MELT_TIME_TOTAL) {
            // Espera até o jogador retirar o item (o outputStack está cheio/ocupado)
            return;
        }

        // 2. Tentar consumir combustível se acabou e há mais para usar
        if (fuelLeft <= 0.0f) {
            if (fuelStack != null && fuelStack.getAmount() > 0) {
                // Consome 1 de combustível
                fuelLeft = FurnaceRegistry.getFuelEfficiency(fuelStack.getItem());
                fuelStack.removeAmount(1);
                if (fuelStack.getAmount() == 0) fuelStack = null;
            } else {
                // Sem combustível, o progresso para.
                return;
            }
        }

        // 3. Fundir e gastar combustível (1 Melt Unit em 8.0s)
        float fuelCost = tpf / MELT_TIME_TOTAL;

        meltProgress += tpf;
        fuelLeft -= fuelCost;

        // 4. Finalizar Fundição (Item Consumido -> Output Adicionado)
        if (meltProgress >= MELT_TIME_TOTAL) {
            FurnaceRecipe recipe = FurnaceRegistry.findRecipe(inputStack.getItem());
            if (recipe != null) {
                Item outputItem = recipe.getOutput();

                // Tentar adicionar ao output
                if (outputStack == null) {
                    outputStack = new Stacks(outputItem, 1);
                    inputStack = null; // Item de input consumido
                    meltProgress = 0.0f; // Prepara para o próximo item
                } else if (outputStack.isSameItem(outputItem) && !outputStack.isFull()) {
                    outputStack.addAmount(1);
                    inputStack = null;
                    meltProgress = 0.0f;
                }
                // Se o output estiver bloqueado, o meltProgress fica em 8.0f (estado 'done').
            }
        }

        // 5. Garante que o combustível não é negativo
        if (fuelLeft < 0.0f) {
            fuelLeft = 0.0f;
        }
    }
}
