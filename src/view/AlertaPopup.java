package view;

import java.awt.Component;
import javax.swing.JOptionPane;
import model.EstoqueObserver;

/** Observer concreto: abre uma janela de aviso na hora em que o alerta acontece. */
public class AlertaPopup implements EstoqueObserver {
    private final Component pai;

    public AlertaPopup(Component pai) {
        this.pai = pai;
    }

    @Override
    public void atualizar(String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Alerta de Estoque", JOptionPane.WARNING_MESSAGE);
    }
}
