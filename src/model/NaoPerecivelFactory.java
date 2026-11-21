package model;

public class NaoPerecivelFactory extends ProdutoFactory {
    @Override
    public Produto criarProduto(String nome, int qtd, int estoqueMinimo) {
        return new ProdutoNaoPerecivel(nome, qtd, estoqueMinimo);
    }
}
