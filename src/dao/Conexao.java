package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Padrão: Singleton - garante uma única conexão com o banco para todo o sistema. */
public class Conexao {

    private static final String ARQUIVO_CONFIG = "db.properties";

    private static Conexao instancia;

    private final String url;
    private final String usuario;
    private final String senha;
    private Connection conexao;

    // Construtor privado: ninguém de fora consegue dar "new Conexao()"
    private Conexao() {
        Properties config = new Properties();
        try (FileInputStream in = new FileInputStream(ARQUIVO_CONFIG)) {
            config.load(in);
        } catch (IOException e) {
            System.out.println("Arquivo " + ARQUIVO_CONFIG + " não encontrado. Usando valores padrão.");
        }
        this.url = config.getProperty("db.url",
                "jdbc:mysql://localhost:3306/estoque?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        this.usuario = config.getProperty("db.user", "root");
        this.senha = config.getProperty("db.password", "123");
    }

    // synchronized evita que duas threads criem duas instâncias ao mesmo tempo
    public static synchronized Conexao getInstancia() {
        if (instancia == null) {
            instancia = new Conexao();
        }
        return instancia;
    }

    public synchronized Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed() || !conexao.isValid(2)) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver JDBC do MySQL não encontrado. Verifique a pasta lib/.", e);
            }
            conexao = DriverManager.getConnection(url, usuario, senha);
            System.out.println("Conectado com sucesso ao MySQL!");
        }
        return conexao;
    }

    public synchronized void fechar() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar a conexão: " + e.getMessage());
        }
    }
}
