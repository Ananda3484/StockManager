package model;

/** Padrão: Observer - quem quiser receber alertas do estoque implementa esta interface. */
public interface EstoqueObserver {
    void atualizar(String mensagem);
}
