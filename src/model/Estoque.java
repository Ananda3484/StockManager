package model;

import java.util.ArrayList;
import java.util.List;

/** Padrão: Observer (Subject) - avisa todos os observers quando há alerta, sem conhecê-los. */
public class Estoque {
    public static final int DIAS_AVISO_VALIDADE = 7;

    private final List<EstoqueObserver> observers = new ArrayList<>();

    public void adicionarObserver(EstoqueObserver o) {
        observers.add(o);
    }

    public void removerObserver(EstoqueObserver o) {
        observers.remove(o);
    }

    private void notificar(String mensagem) {
        for (EstoqueObserver o : new ArrayList<>(observers)) {
            o.atualizar(mensagem);
        }
    }

    public void registrarSaida(Produto p, int qtd) {
        p.retirar(qtd);
        verificarEstoqueBaixo(p);
    }

    public void verificarEstoqueBaixo(Produto p) {
        if (p.estoqueBaixo()) {
            notificar("ESTOQUE BAIXO: " + p.getNome() + " (" + p.getQuantidade()
                    + " un., mínimo " + p.getEstoqueMinimo() + ")");
        }
    }

    public void verificarValidades(Produto p, int diasAviso) {
        if (!p.exigeValidade()) {
            return;
        }
        for (Lote l : new LotesPorValidade(p.getLotes())) {
            if (l.getQuantidade() <= 0) {
                continue;
            }
            long dias = l.diasParaVencer();
            if (dias < 0) {
                notificar("VENCIDO: " + p.getNome() + " - lote " + l.getNumero());
            } else if (dias <= diasAviso) {
                notificar("VENCE EM " + dias + " DIA(S): " + p.getNome() + " - lote " + l.getNumero());
            }
        }
    }
}
