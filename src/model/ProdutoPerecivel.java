package model;

public class ProdutoPerecivel extends Produto {

    public ProdutoPerecivel(String nome, int quantidade, int estoqueMinimo) {
        super(nome, quantidade, estoqueMinimo);
    }

    @Override
    public boolean exigeValidade() {
        return true;
    }

    @Override
    public String getTipo() {
        return "P";
    }

    /** Além de baixar o total, consome os lotes que vencem primeiro (FEFO), usando o Iterator. */
    @Override
    public void retirar(int qtd) {
        super.retirar(qtd);

        int restante = qtd;
        for (Lote lote : new LotesPorValidade(lotes)) {
            if (restante == 0) {
                break;
            }
            int retirada = Math.min(lote.getQuantidade(), restante);
            lote.setQuantidade(lote.getQuantidade() - retirada);
            restante -= retirada;
        }
    }
}
