package jogo.util.inventory;

import jogo.gameobject.item.Item;

/**
 * Representa uma pilha (stack) de itens no inventário.
 * Contém uma referência a um tipo de item e a quantidade desse item presente na pilha,
 * respeitando um limite máximo definido.
 */
public class Stacks {
    /** Tamanho máximo permitido para cada pilha de itens (padrão: 64). */
    public static final int MAX_STACK_SIZE = 64;

    /** Quantidade atual de itens na pilha. */
    private int amount;

    /** O objeto {@link Item} que define o tipo de item desta pilha. */
    private Item item;

    /**
     * Construtor da classe Stacks.
     * @param item O tipo de item a ser armazenado.
     * @param amount A quantidade inicial de itens.
     */
    public Stacks(Item item, int amount) {
        this.amount = amount;
        this.item = item;
    }

    /**
     * Retorna o item armazenado nesta pilha.
     * @return O objeto Item.
     */
    public Item getItem() {
        return item;
    }

    /**
     * Retorna a quantidade de itens presentes nesta pilha.
     * @return O valor inteiro da quantidade.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Incrementa a quantidade de itens na pilha.
     * @param add A quantidade a somar.
     */
    public void addAmount(int add) {
        this.amount += add;
    }

    /**
     * Decrementa a quantidade de itens na pilha.
     * @param remove A quantidade a subtrair.
     */
    public void removeAmount(int remove) {
        this.amount -= remove;
    }

    /**
     * Verifica se a pilha ainda tem espaço para mais itens.
     * @return true se a quantidade for inferior a MAX_STACK_SIZE; false caso contrário.
     */
    public boolean isFull() {
        return amount < MAX_STACK_SIZE;
    }

    /**
     * Verifica se um determinado item é do mesmo tipo que o item desta pilha, comparando os seus nomes.
     * @param other O item a comparar.
     * @return true se tiverem o mesmo nome; false caso contrário.
     */
    public boolean isSameItem(Item other) {
        if (other == null) return false;
        return item.getName().equals(other.getName());
    }
}