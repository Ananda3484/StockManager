package model;

public class PerecivelFactory extends ProdutoFactory {
    @Override
    public Produto criarProduto(String nome, int qtd, int estoqueMinimo) {
        return new ProdutoPerecivel(nome, qtd, estoqueMinimo);
    }
}
