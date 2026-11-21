package view;

import controller.ProdutoController;
import model.Estoque;
import model.Lote;
import model.LotesPorValidade;
import model.Produto;
import util.Funcoes;
import util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class CRUDProduto extends JFrame {
    private JTable tabelaProdutos;
    private DefaultTableModel model;
    private JButton btnNovo, btnEditar, btnExcluir, btnLotes, btnSaida;
    private final ProdutoController controller;

    public CRUDProduto(Estoque estoque) {
        controller = new ProdutoController(estoque);

        // Enquanto esta tela estiver aberta, os alertas também aparecem em janela (Observer).
        // Ao fechar, o observer é removido do Estoque.
        AlertaPopup popup = new AlertaPopup(this);
        estoque.adicionarObserver(popup);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                estoque.removerObserver(popup);
            }
        });

        setTitle("Gerenciamento de Produtos");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(
            new String[]{"ID", "Nome", "Marca", "Tipo", "Preço", "Qtd.", "Mínimo", "Próx. validade"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaProdutos = new JTable(model);
        Tema.estilizarTabela(tabelaProdutos);
        tabelaProdutos.getColumnModel().getColumn(0).setMaxWidth(60);
        tabelaProdutos.getColumnModel().getColumn(5).setMaxWidth(90);
        tabelaProdutos.getColumnModel().getColumn(6).setMaxWidth(90);
        RenderizadorProduto renderizador = new RenderizadorProduto();
        tabelaProdutos.getColumnModel().getColumn(5).setCellRenderer(renderizador);
        tabelaProdutos.getColumnModel().getColumn(7).setCellRenderer(renderizador);

        btnNovo = Tema.botaoPrimario("+ Novo produto");
        btnEditar = Tema.botaoSecundario("Editar");
        btnLotes = Tema.botaoSecundario("Lotes");
        btnSaida = Tema.botaoSecundario("Registrar saída");
        btnExcluir = Tema.botaoPerigo("Excluir");

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Produtos"));
        textos.add(Tema.subtitulo("Estoque, lotes e validades"));
        cabecalho.add(textos, BorderLayout.WEST);
        cabecalho.add(Tema.centralizar(btnNovo), BorderLayout.EAST);

        JPanel acoes = new JPanel(new BorderLayout());
        acoes.setOpaque(false);
        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        esquerda.setOpaque(false);
        esquerda.add(btnEditar);
        esquerda.add(btnLotes);
        esquerda.add(btnSaida);
        acoes.add(esquerda, BorderLayout.WEST);
        acoes.add(btnExcluir, BorderLayout.EAST);

        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(Tema.FUNDO);
        raiz.setBorder(new EmptyBorder(26, 30, 24, 30));
        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(Tema.cartaoTabela(Tema.rolagem(tabelaProdutos)), BorderLayout.CENTER);
        raiz.add(acoes, BorderLayout.SOUTH);
        setContentPane(raiz);

        btnNovo.addActionListener(e -> abrirFormularioProduto(null));
        btnEditar.addActionListener(e -> editarProduto());
        btnExcluir.addActionListener(e -> excluirProduto());
        btnLotes.addActionListener(e -> abrirLotes());
        btnSaida.addActionListener(e -> registrarSaida());

        atualizarTabela();
    }

    // Público porque o FormularioProduto e a TelaLotes chamam este método após salvar
    public void atualizarTabela() {
        model.setRowCount(0);

        List<Produto> produtos = controller.listarProdutos();

        for (Produto p : produtos) {
            model.addRow(new Object[]{
                p.getId(),
                p.getNome(),
                p.getMarca(),
                p.exigeValidade() ? "Perecível" : "Não perecível",
                String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", p.getPreco()),
                p.getQuantidade(),
                p.getEstoqueMinimo(),
                proximaValidade(p)
            });
        }
    }

    /** Usa o Iterator para pegar o lote (com saldo) que vence primeiro. */
    private String proximaValidade(Produto p) {
        if (!p.exigeValidade()) {
            return "-";
        }
        for (Lote lote : new LotesPorValidade(p.getLotes())) {
            if (lote.getQuantidade() > 0) {
                return Funcoes.formatarData(lote.getValidade());
            }
        }
        return "-";
    }

    private void abrirFormularioProduto(Produto produto) {
        FormularioProduto form = new FormularioProduto(this, controller, produto);
        form.setVisible(true);
    }

    /** Devolve o produto da linha selecionada (recarregado do banco) ou null, avisando o usuário. */
    private Produto produtoSelecionado(String avisoSemSelecao) {
        int linha = tabelaProdutos.getSelectedRow();

        if (linha < 0) {
            JOptionPane.showMessageDialog(this, avisoSemSelecao);
            return null;
        }

        int id = (int) model.getValueAt(linha, 0);
        Produto produto = controller.buscarPorId(id);

        if (produto == null) {
            JOptionPane.showMessageDialog(this, "Produto não encontrado. A lista será atualizada.");
            atualizarTabela();
        }
        return produto;
    }

    private void editarProduto() {
        Produto produto = produtoSelecionado("Selecione um produto para editar.");
        if (produto != null) {
            abrirFormularioProduto(produto);
        }
    }

    private void excluirProduto() {
        int linha = tabelaProdutos.getSelectedRow();

        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir.");
            return;
        }

        int id = (int) model.getValueAt(linha, 0);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Tem certeza que deseja excluir este produto? Os lotes dele também serão excluídos.",
            "Confirmação",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.excluirProduto(id)) {
                atualizarTabela();
                JOptionPane.showMessageDialog(this, "Produto excluído com sucesso.");
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir produto.");
            }
        }
    }

    private void abrirLotes() {
        Produto produto = produtoSelecionado("Selecione um produto para ver os lotes.");
        if (produto == null) {
            return;
        }
        if (!produto.exigeValidade()) {
            JOptionPane.showMessageDialog(this, "Produto não perecível não possui lotes.");
            return;
        }
        new TelaLotes(this, controller, produto).setVisible(true);
    }

    private void registrarSaida() {
        Produto produto = produtoSelecionado("Selecione um produto para registrar a saída.");
        if (produto == null) {
            return;
        }

        String texto = JOptionPane.showInputDialog(this,
                "Quantidade de saída de \"" + produto.getNome() + "\" (em estoque: " + produto.getQuantidade() + "):");
        if (texto == null) {
            return;
        }

        try {
            int quantidade = Integer.parseInt(texto.trim());
            controller.registrarSaida(produto, quantidade);
            JOptionPane.showMessageDialog(this, "Saída registrada com sucesso.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Informe uma quantidade válida!");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }

        atualizarTabela();
    }

    /** Quantidade em vermelho quando está no mínimo ou abaixo; validade em vermelho (vencida) ou âmbar (perto). */
    private static class RenderizadorProduto extends Tema.RenderizadorCelula {
        RenderizadorProduto() {
            super(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, foco, linha, coluna);

            if (coluna == 5) {
                int quantidade = (int) tabela.getValueAt(linha, 5);
                int minimo = (int) tabela.getValueAt(linha, 6);
                if (quantidade <= minimo) {
                    setForeground(Tema.PERIGO);
                    setFont(Tema.fonte(Font.BOLD, 14));
                }
            } else if (coluna == 7) {
                LocalDate validade = Funcoes.lerData(String.valueOf(valor));
                if (validade != null) {
                    long dias = ChronoUnit.DAYS.between(LocalDate.now(), validade);
                    if (dias < 0) {
                        setForeground(Tema.PERIGO);
                        setFont(Tema.fonte(Font.BOLD, 14));
                    } else if (dias <= Estoque.DIAS_AVISO_VALIDADE) {
                        setForeground(Tema.AVISO);
                        setFont(Tema.fonte(Font.BOLD, 14));
                    }
                }
            }
            return this;
        }
    }
}
