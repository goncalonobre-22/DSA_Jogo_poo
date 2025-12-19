package jogo.util.inventory;

import jogo.gameobject.item.Item;

/**
 * Representa o inventário de um personagem ou entidade no jogo.
 * Gere um conjunto de slots contendo pilhas de itens (Stacks), permitindo a adição,
 * remoção, contagem e seleção de itens através de uma interface de array.
 */
public class Inventory {
    /** Array que armazena as pilhas de itens em cada slot do inventário. */
    private final Stacks[] inventoryArray;

    /** Tamanho total (número de slots) do inventário. */
    private final int inventorySize;

    /** Índice do slot atualmente selecionado pelo jogador. */
    private int selectedSlot = 0;

    /**
     * Construtor da classe Inventory.
     * @param inventorySize O número total de slots que o inventário terá.
     */
    public Inventory(int inventorySize) {
        this.inventorySize = inventorySize;
        this.inventoryArray = new Stacks[inventorySize];
    }

    /**
     * Retorna o tamanho total do inventário.
     * @return O número de slots.
     */
    public int getSize() {
        return inventorySize;
    }

    /**
     * Tenta adicionar uma quantidade específica de um item ao inventário.
     * Primeiro tenta preencher slots que já contenham o mesmo item. Se sobrar quantidade,
     * ocupa novos slots vazios até ao limite de empilhamento.
     * @param item O item a adicionar.
     * @param amount A quantidade a ser adicionada.
     * @return true se toda a quantidade foi adicionada com sucesso; false se o inventário estiver cheio.
     */
    public boolean addItem(Item item, int amount) {
        if (item == null) return false;
        // Tenta preencher stacks existentes
        for (int i = 0; i < this.inventorySize; i++) {
            Stacks inv = inventoryArray[i];
            if (inv != null && inv.isSameItem(item)) {
                if (inv.getAmount() < Stacks.MAX_STACK_SIZE) {
                    int space  = Stacks.MAX_STACK_SIZE - inv.getAmount();
                    int toAdd = Math.min(space, amount);
                    inv.addAmount(toAdd);
                    amount -= toAdd;
                    if (amount <= 0) return true;
                }
            }
        }
        // Cria novas stacks em slots vazios
        while (amount > 0) {
            int emptySlot = findEmptySlot();
            if (emptySlot == -1) return false;
            int stackAmount = Math.min(amount, Stacks.MAX_STACK_SIZE);
            inventoryArray[emptySlot] = new Stacks(item, stackAmount);
            amount -= stackAmount;
        } return true;
    }

    /**
     * Remove uma quantidade específica de um item do inventário.
     * Percorre os slots e subtrai a quantidade pedida até que o requisito seja satisfeito.
     * @param item O item a remover.
     * @param amount A quantidade total a remover.
     * @return true se a quantidade total foi removida; false se não havia itens suficientes.
     */
    public boolean removeItem(Item item, int amount) {
        if (item == null) return false;
        int remainingAmount = amount;
        for (int i = 0; i < this.inventorySize; i++) {
            Stacks stacks = inventoryArray[i];
            if (stacks != null && stacks.isSameItem(item)) {
                if (stacks.getAmount() <= remainingAmount) {
                    remainingAmount -= stacks.getAmount();
                    inventoryArray[i] = null;
                } else {
                    stacks.removeAmount(remainingAmount);
                    remainingAmount = 0;
                }
                if (remainingAmount <= 0) return true;
            }
        }
        return remainingAmount == 0;
    }

    /**
     * Conta o número total de unidades de um determinado item presentes em todos os slots.
     * @param item O item a ser contado.
     * @return O somatório total das quantidades em todas as pilhas correspondentes.
     */
    public int countItem(Item item) {
        if (item == null) return 0;
        int count = 0;
        for (Stacks stack : inventoryArray) {
            if (stack != null && stack.isSameItem(item)) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    /**
     * Procura o primeiro slot disponível (vazio) no inventário.
     * @return O índice do slot vazio, ou -1 se o inventário estiver completamente cheio.
     */
    private int findEmptySlot() {
        for (int i = 0; i < inventorySize; i++) {
            if (inventoryArray[i] == null) return i;
        }
        return -1;
    }

    /**
     * Obtém a pilha de itens (Stacks) num índice específico.
     * @param index O índice do slot.
     * @return O objeto Stacks no slot, ou null se estiver vazio ou fora de limites.
     */
    public Stacks getSlot(int index) {
        if (index < 0 || index >= inventorySize) return null;
        return inventoryArray[index];
    }

    /**
     * Retorna o índice do slot atualmente selecionado.
     * @return O valor de selectedSlot.
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }

    /**
     * Define o índice do slot a ser selecionado, validando se está dentro dos limites do inventário.
     * @param slot O novo índice do slot.
     */
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < inventorySize) {
            this.selectedSlot = slot;
        }
    }

    /**
     * Retorna a pilha de itens que se encontra no slot atualmente selecionado.
     * @return O objeto Stacks selecionado.
     */
    public Stacks getSelectedItem() {
        return getSlot(selectedSlot);
    }
}