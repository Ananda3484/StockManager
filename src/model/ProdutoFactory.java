package model;

/** Padrão: Factory Method - cada subclasse decide qual tipo de produto instanciar. */
public abstract class ProdutoFactory {

    // Factory Method
    public abstract Produto criarProduto(String nome, int qtd, int estoqueMinimo);

    public Produto cadastrar(String nome, int qtd, int estoqueMinimo) {
        if (qtd < 0 || estoqueMinimo < 0) {
            throw new IllegalArgumentException("Quantidade e estoque mínimo não podem ser negativos!");
        }
        return criarProduto(nome, qtd, estoqueMinimo);
    }
}
