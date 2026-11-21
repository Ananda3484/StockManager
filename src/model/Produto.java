package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Produto {
    protected int id;
    protected String nome;
    protected String marca;
    protected double preco;
    protected int quantidade;
    protected int estoqueMinimo;
    protected List<Lote> lotes = new ArrayList<>();

    public Produto(String nome, int quantidade, int estoqueMinimo) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
    }

    /** Perecíveis exigem lote e validade; não perecíveis não. */
    public abstract boolean exigeValidade();

    /** Código gravado na coluna "tipo" do banco: P = perecível, N = não perecível. */
    public abstract String getTipo();

    public void adicionarLote(Lote lote) {
        lotes.add(lote);
    }

    /** Dá baixa no estoque. Lança IllegalArgumentException se a quantidade for inválida. */
    public void retirar(int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade maior que zero!");
        }
        if (qtd > quantidade) {
            throw new IllegalArgumentException("Quantidade maior que o estoque!");
        }
        quantidade -= qtd;
    }

    public boolean estoqueBaixo() {
        return quantidade <= estoqueMinimo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public int getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public List<Lote> getLotes() { return lotes; }
}
