package model;

/** Observer concreto: grava os alertas no console. */
public class AlertaLog implements EstoqueObserver {
    @Override
    public void atualizar(String mensagem) {
        System.out.println("[ALERTA] " + mensagem);
    }
}
