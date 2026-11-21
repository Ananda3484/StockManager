package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Usuario;
import util.Funcoes;
import util.ResultadoCadastro;

public class UsuarioDAO {

    /** Devolve o usuário autenticado (com tipo e situação) ou null se login/senha não conferem. */
    public Usuario autenticar(Usuario usuario) {
        String sql = "SELECT id, login, tipo, ativo, email, data_criacao FROM usuario "
                   + "WHERE login = ? AND senha = ? AND ativo = 'T'";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setString(1, usuario.getLogin());
                stmt.setString(2, Funcoes.gerarHashSHA256(usuario.getSenha()));

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao autenticar usuário: " + e.getMessage());
            return null;
        }
    }

    public boolean existeUsuario(String nomeUsuario) {
        String sql = "SELECT 1 FROM usuario WHERE login = ?";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setString(1, nomeUsuario);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar existência de usuário: " + e.getMessage());
            return false;
        }
    }

    public ResultadoCadastro inserir(Usuario dadosInformados) {
        if (existeUsuario(dadosInformados.getLogin())) {
            return ResultadoCadastro.JA_EXISTE;
        }

        String sql = "INSERT INTO usuario "
                   + "(login, senha, data_criacao, tipo, ativo, email) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setString(1, dadosInformados.getLogin());
                stmt.setString(2, Funcoes.gerarHashSHA256(dadosInformados.getSenha()));
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(4, dadosInformados.getTipo() != null ? dadosInformados.getTipo() : "U");
                stmt.setString(5, dadosInformados.getAtivo() != null ? dadosInformados.getAtivo() : "T");
                stmt.setString(6, dadosInformados.getEmail() != null ? dadosInformados.getEmail() : "");
                stmt.executeUpdate();
                return ResultadoCadastro.SUCESSO;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir usuário: " + e.getMessage());
            return ResultadoCadastro.ERRO_BANCO;
        }
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, login, tipo, ativo, email, data_criacao FROM usuario ORDER BY id";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }
        return lista;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao excluir usuário: " + e.getMessage());
            return false;
        }
    }

    public Usuario buscarPorId(int id) {
        String sql = "SELECT id, login, tipo, ativo, email, data_criacao FROM usuario WHERE id = ?";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapear(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar usuário: " + e.getMessage());
        }
        return null;
    }

    public boolean gravar(Usuario u) {
        String sql = "UPDATE usuario SET login = ?, tipo = ?, ativo = ?, email = ? WHERE id = ?";

        Usuario atual = buscarPorId(u.getId());
        if (atual == null) {
            return false;
        }
        if (!atual.getLogin().equals(u.getLogin()) && existeUsuario(u.getLogin())) {
            return false; // login já usado por outro usuário
        }

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setString(1, u.getLogin());
                stmt.setString(2, u.getTipo());
                stmt.setString(3, u.getAtivo());
                stmt.setString(4, u.getEmail() != null ? u.getEmail() : atual.getEmail());
                stmt.setInt(5, u.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setLogin(rs.getString("login"));
        u.setTipo(rs.getString("tipo"));
        u.setAtivo(rs.getString("ativo"));
        u.setEmail(rs.getString("email"));
        u.setDataCriacao(rs.getString("data_criacao"));
        return u;
    }
}
