import dao.Conexao;
import util.Tema;
import view.TelaLogin;

public class Main {
    public static void main(String[] args) {
        // Fecha a conexão tecnica (Singleton) quando o programa termina
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Conexao.getInstancia().fechar()));

        Tema.aplicar();

        javax.swing.SwingUtilities.invokeLater(() -> {
            new TelaLogin().setVisible(true);
        });
    }
}

