package model;

public class ProdutoNaoPerecivel extends Produto {

    public ProdutoNaoPerecivel(String nome, int quantidade, int estoqueMinimo) {
        super(nome, quantidade, estoqueMinimo);
    }

    @Override
    public boolean exigeValidade() {
        return false;
    }

    @Override
    public String getTipo() {
        return "N";
    }
}
