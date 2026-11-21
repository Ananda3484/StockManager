package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Lote;
import model.NaoPerecivelFactory;
import model.PerecivelFactory;
import model.Produto;
import model.ProdutoFactory;
import util.ResultadoCadastro;

public class ProdutoDAO {

    private static final String COLUNAS = "id, nome, marca, preco, tipo, quantidade, estoque_minimo";

    public ResultadoCadastro inserir(Produto produto) {
        try {
            if (existeProduto(produto.getNome(), 0)) {
                return ResultadoCadastro.JA_EXISTE;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar existência de produto: " + e.getMessage());
            return ResultadoCadastro.ERRO_BANCO;
        }

        String sql = "INSERT INTO produto (nome, marca, preco, tipo, quantidade, estoque_minimo) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        Connection c = null;
        try {
            c = Conexao.getInstancia().getConexao();
            c.setAutoCommit(false);

            try (PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, produto.getNome());
                stmt.setString(2, produto.getMarca());
                stmt.setDouble(3, produto.getPreco());
                stmt.setString(4, produto.getTipo());
                stmt.setInt(5, produto.getQuantidade());
                stmt.setInt(6, produto.getEstoqueMinimo());
                stmt.executeUpdate();

                try (ResultSet chaves = stmt.getGeneratedKeys()) {
                    if (chaves.next()) {
                        produto.setId(chaves.getInt(1));
                    }
                }
            }

            for (Lote lote : produto.getLotes()) {
                inserirLote(c, produto.getId(), lote);
            }

            c.commit();
            return ResultadoCadastro.SUCESSO;

        } catch (SQLException e) {
            desfazer(c);
            System.out.println("Erro ao inserir produto: " + e.getMessage());
            return ResultadoCadastro.ERRO_BANCO;
        } finally {
            restaurarAutoCommit(c);
        }
    }

    /** Grava o lote novo e a nova quantidade total do produto, na mesma transação. */
    public ResultadoCadastro adicionarLote(Produto produto, Lote lote) {
        Connection c = null;
        try {
            c = Conexao.getInstancia().getConexao();
            c.setAutoCommit(false);

            inserirLote(c, produto.getId(), lote);
            atualizarQuantidade(c, produto);

            c.commit();
            return ResultadoCadastro.SUCESSO;

        } catch (SQLException e) {
            desfazer(c);
            if (e.getErrorCode() == 1062) { // chave duplicada: produto + número do lote
                return ResultadoCadastro.JA_EXISTE;
            }
            System.out.println("Erro ao adicionar lote: " + e.getMessage());
            return ResultadoCadastro.ERRO_BANCO;
        } finally {
            restaurarAutoCommit(c);
        }
    }

    /** Grava a quantidade do produto e a de cada lote (usado depois de uma saída de estoque). */
    public boolean atualizarEstoque(Produto produto) {
        Connection c = null;
        try {
            c = Conexao.getInstancia().getConexao();
            c.setAutoCommit(false);

            atualizarQuantidade(c, produto);

            String sqlLote = "UPDATE lote SET quantidade = ? WHERE id = ?";
            try (PreparedStatement stmt = c.prepareStatement(sqlLote)) {
                for (Lote lote : produto.getLotes()) {
                    stmt.setInt(1, lote.getQuantidade());
                    stmt.setInt(2, lote.getId());
                    stmt.executeUpdate();
                }
            }

            c.commit();
            return true;

        } catch (SQLException e) {
            desfazer(c);
            System.out.println("Erro ao atualizar estoque: " + e.getMessage());
            return false;
        } finally {
            restaurarAutoCommit(c);
        }
    }

    /** Existe outro produto (id diferente de idIgnorado) com esse nome? */
    private boolean existeProduto(String nome, int idIgnorado) throws SQLException {
        String sql = "SELECT 1 FROM produto WHERE nome = ? AND id <> ?";
        Connection c = Conexao.getInstancia().getConexao();
        try (PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setInt(2, idIgnorado);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeProduto(String nomeProduto) {
        try {
            return existeProduto(nomeProduto, 0);
        } catch (SQLException e) {
            System.out.println("Erro ao verificar existência de produto: " + e.getMessage());
            return false;
        }
    }

    public List<Produto> listarTodos() {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT " + COLUNAS + " FROM produto ORDER BY id";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            for (Produto p : lista) {
                if (p.exigeValidade()) {
                    carregarLotes(c, p);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
        }
        return lista;
    }

    public Produto buscarPorId(int id) {
        String sql = "SELECT " + COLUNAS + " FROM produto WHERE id = ?";

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Produto p = mapear(rs);
                        if (p.exigeValidade()) {
                            carregarLotes(c, p);
                        }
                        return p;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar produto: " + e.getMessage());
        }
        return null;
    }

    public boolean atualizar(Produto p) {
        String sql = "UPDATE produto SET nome = ?, marca = ?, preco = ?, quantidade = ?, "
                   + "estoque_minimo = ? WHERE id = ?";

        try {
            if (existeProduto(p.getNome(), p.getId())) {
                return false; // outro produto já usa esse nome
            }

            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setString(1, p.getNome());
                stmt.setString(2, p.getMarca());
                stmt.setDouble(3, p.getPreco());
                stmt.setInt(4, p.getQuantidade());
                stmt.setInt(5, p.getEstoqueMinimo());
                stmt.setInt(6, p.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar produto: " + e.getMessage());
            return false;
        }
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM produto WHERE id = ?"; // os lotes saem junto (ON DELETE CASCADE)

        try {
            Connection c = Conexao.getInstancia().getConexao();
            try (PreparedStatement stmt = c.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao excluir produto: " + e.getMessage());
            return false;
        }
    }

    // ---------- métodos auxiliares ----------

    private void inserirLote(Connection c, int produtoId, Lote lote) throws SQLException {
        String sql = "INSERT INTO lote (produto_id, numero, validade, quantidade) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, produtoId);
            stmt.setString(2, lote.getNumero());
            stmt.setDate(3, Date.valueOf(lote.getValidade()));
            stmt.setInt(4, lote.getQuantidade());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    lote.setId(chaves.getInt(1));
                }
            }
        }
    }

    private void atualizarQuantidade(Connection c, Produto produto) throws SQLException {
        String sql = "UPDATE produto SET quantidade = ? WHERE id = ?";
        try (PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, produto.getQuantidade());
            stmt.setInt(2, produto.getId());
            stmt.executeUpdate();
        }
    }

    private void carregarLotes(Connection c, Produto produto) throws SQLException {
        String sql = "SELECT id, numero, validade, quantidade FROM lote WHERE produto_id = ? ORDER BY validade";
        try (PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, produto.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lote lote = new Lote(rs.getString("numero"),
                                         rs.getDate("validade").toLocalDate(),
                                         rs.getInt("quantidade"));
                    lote.setId(rs.getInt("id"));
                    produto.adicionarLote(lote);
                }
            }
        }
    }

    /** Usa a coluna "tipo" para escolher a Factory certa (Factory Method). */
    private Produto mapear(ResultSet rs) throws SQLException {
        ProdutoFactory fabrica = "P".equals(rs.getString("tipo"))
                ? new PerecivelFactory()
                : new NaoPerecivelFactory();

        Produto p = fabrica.criarProduto(rs.getString("nome"), rs.getInt("quantidade"), rs.getInt("estoque_minimo"));
        p.setId(rs.getInt("id"));
        p.setMarca(rs.getString("marca"));
        p.setPreco(rs.getDouble("preco"));
        return p;
    }

    private void desfazer(Connection c) {
        try {
            if (c != null) {
                c.rollback();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao desfazer transação: " + e.getMessage());
        }
    }

    private void restaurarAutoCommit(Connection c) {
        try {
            if (c != null && !c.isClosed()) {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao restaurar auto-commit: " + e.getMessage());
        }
    }
}
