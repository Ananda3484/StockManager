package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Lote {
    private int id;
    private String numero;
    private LocalDate validade;
    private int quantidade;

    public Lote(String numero, LocalDate validade, int quantidade) {
        this.numero = numero;
        this.validade = validade;
        this.quantidade = quantidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public LocalDate getValidade() { return validade; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public long diasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), validade);
    }

    public boolean estaVencido() {
        return diasParaVencer() < 0;
    }
}
