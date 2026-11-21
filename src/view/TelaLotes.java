package view;

import controller.ProdutoController;
import model.Estoque;
import model.Lote;
import model.LotesPorValidade;
import model.Produto;
import util.Funcoes;
import util.ResultadoCadastro;
import util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class TelaLotes extends JDialog {

    private final DefaultTableModel model;
    private final CRUDProduto parent;
    private final ProdutoController controller;
    private Produto produto;

    public TelaLotes(CRUDProduto parent, ProdutoController controller, Produto produto) {
        super(parent, true);
        this.parent = parent;
        this.controller = controller;
        this.produto = produto;

        setTitle("Lotes de " + produto.getNome());
        setSize(680, 460);
        setLocationRelativeTo(parent);

        model = new DefaultTableModel(new String[]{"Lote", "Validade", "Quantidade", "Situação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabela = new JTable(model);
        Tema.estilizarTabela(tabela);
        tabela.getColumnModel().getColumn(3).setCellRenderer(new RenderizadorSituacao());

        JButton btnNovo = Tema.botaoPrimario("+ Novo lote");
        JButton btnFechar = Tema.botaoSecundario("Fechar");

        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Lotes de " + produto.getNome()));
        textos.add(Tema.subtitulo("Ordenados pela menor validade (o primeiro a vencer sai primeiro)"));

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.add(btnNovo, BorderLayout.WEST);
        rodape.add(btnFechar, BorderLayout.EAST);

        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(Tema.FUNDO);
        raiz.setBorder(new EmptyBorder(24, 28, 22, 28));
        raiz.add(textos, BorderLayout.NORTH);
        raiz.add(Tema.cartaoTabela(Tema.rolagem(tabela)), BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);
        setContentPane(raiz);

        btnNovo.addActionListener(e -> novoLote());
        btnFechar.addActionListener(e -> dispose());

        preencherTabela();
    }

    /** Os lotes aparecem na ordem de validade (FEFO), graças ao Iterator. */
    private void preencherTabela() {
        model.setRowCount(0);
        for (Lote lote : new LotesPorValidade(produto.getLotes())) {
            model.addRow(new Object[]{
                lote.getNumero(),
                Funcoes.formatarData(lote.getValidade()),
                lote.getQuantidade(),
                situacao(lote)
            });
        }
    }

    private String situacao(Lote lote) {
        if (lote.getQuantidade() == 0) {
            return "Esgotado";
        }
        long dias = lote.diasParaVencer();
        if (dias < 0) {
            return "Vencido";
        }
        if (dias <= Estoque.DIAS_AVISO_VALIDADE) {
            return "Vence em " + dias + " dia(s)";
        }
        return "OK";
    }

    private void novoLote() {
        JTextField txtNumero = Tema.campoTexto();
        JTextField txtValidade = Tema.campoTexto();
        JTextField txtQuantidade = Tema.campoTexto();

        JPanel painel = Tema.coluna(340,
                Tema.campo("Número do lote", txtNumero),
                Tema.linha(Tema.campo("Validade (dd/mm/aaaa)", txtValidade), Tema.campo("Quantidade", txtQuantidade)));

        int opcao = JOptionPane.showConfirmDialog(this, painel, "Novo lote",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }

        String numero = txtNumero.getText().trim();
        LocalDate validade = Funcoes.lerData(txtValidade.getText());
        int quantidade;

        try {
            quantidade = Integer.parseInt(txtQuantidade.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Informe uma quantidade válida!");
            return;
        }

        if (numero.isEmpty() || validade == null || quantidade <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Informe o número do lote, a validade (dd/mm/aaaa) e uma quantidade maior que zero!");
            return;
        }

        ResultadoCadastro resultado = controller.adicionarLote(produto, numero, validade, quantidade);

        switch (resultado) {
            case SUCESSO -> JOptionPane.showMessageDialog(this, "Lote cadastrado com sucesso!");
            case JA_EXISTE -> JOptionPane.showMessageDialog(this, "Já existe um lote com esse número neste produto!");
            case ERRO_BANCO -> JOptionPane.showMessageDialog(this, "Erro ao salvar lote no banco de dados!");
        }

        // Recarrega do banco: mantém a tela igual ao que foi realmente gravado
        Produto atualizado = controller.buscarPorId(produto.getId());
        if (atualizado != null) {
            produto = atualizado;
        }
        preencherTabela();
        parent.atualizarTabela();
    }

    /** Situação do lote com cor: verde (OK), âmbar (perto de vencer), vermelho (vencido), cinza (esgotado). */
    private static class RenderizadorSituacao extends Tema.RenderizadorCelula {
        RenderizadorSituacao() {
            super(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, foco, linha, coluna);
            String texto = String.valueOf(valor);
            Color cor = Tema.SUCESSO;
            if (texto.startsWith("Vencido")) {
                cor = Tema.PERIGO;
            } else if (texto.startsWith("Vence")) {
                cor = Tema.AVISO;
            } else if (texto.startsWith("Esgotado")) {
                cor = Tema.TEXTO_SUAVE;
            }
            setForeground(cor);
            setFont(Tema.fonte(Font.BOLD, 13));
            return this;
        }
    }
}
