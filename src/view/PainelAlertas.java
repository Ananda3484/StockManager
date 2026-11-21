package view;

import javax.swing.DefaultListModel;
import model.EstoqueObserver;

/** Observer concreto: escreve cada alerta na lista exibida no menu principal. */
public class PainelAlertas implements EstoqueObserver {
    private final DefaultListModel<String> modelo;

    public PainelAlertas(DefaultListModel<String> modelo) {
        this.modelo = modelo;
    }

    @Override
    public void atualizar(String mensagem) {
        modelo.addElement(mensagem);
    }
}
