package view;

import controller.ProdutoController;
import model.AlertaLog;
import model.Estoque;
import model.Usuario;
import util.Tema;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;

public class TelaMenu extends JFrame {

    private final Usuario usuario;

    // Subject do Observer: os alertas de todo o sistema passam por aqui
    private final Estoque estoque = new Estoque();
    private final DefaultListModel<String> modeloAlertas = new DefaultListModel<>();
    private final ProdutoController produtoController = new ProdutoController(estoque);

    private final JLabel lblBaixo = new JLabel("0");
    private final JLabel lblVencidos = new JLabel("0");
    private final JLabel lblVencendo = new JLabel("0");
    private final JPanel areaAlertas = new JPanel(new CardLayout());

    public TelaMenu(Usuario usuario) {
        this.usuario = usuario;
        boolean administrador = "A".equals(usuario.getTipo());

        setTitle("Controle de Estoque");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Observers cadastrados: painel de alertas (tela) e log (console)
        estoque.adicionarObserver(new PainelAlertas(modeloAlertas));
        estoque.adicionarObserver(new AlertaLog());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(criarMenuLateral(administrador), BorderLayout.WEST);
        raiz.add(criarConteudo(), BorderLayout.CENTER);
        setContentPane(raiz);

        // Recalcula os totais sempre que um alerta entra ou sai da lista
        modeloAlertas.addListDataListener(new ListDataListener() {
            @Override public void intervalAdded(ListDataEvent e) { atualizarResumo(); }
            @Override public void intervalRemoved(ListDataEvent e) { atualizarResumo(); }
            @Override public void contentsChanged(ListDataEvent e) { atualizarResumo(); }
        });

        atualizarAlertas();
    }

    private JPanel criarMenuLateral(boolean administrador) {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Tema.MENU_LATERAL);
        menu.setBorder(new EmptyBorder(24, 16, 20, 16));
        menu.setPreferredSize(new Dimension(240, 0));

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        marca.setOpaque(false);
        marca.add(new Tema.Logo(36));
        JLabel nome = new JLabel("Estoque");
        nome.setFont(Tema.fonte(Font.BOLD, 18));
        nome.setForeground(Color.WHITE);
        marca.add(nome);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        marca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        menu.add(marca);
        menu.add(Box.createVerticalStrut(28));

        JButton btnProdutos = Tema.botaoMenu("Produtos");
        JButton btnUsuarios = Tema.botaoMenu("Usuários");
        JButton btnAlertas = Tema.botaoMenu("Atualizar alertas");
        btnUsuarios.setEnabled(administrador); // só administrador gerencia usuários

        for (JButton b : new JButton[]{btnProdutos, btnUsuarios, btnAlertas}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            menu.add(b);
            menu.add(Box.createVerticalStrut(4));
        }

        menu.add(Box.createVerticalGlue());

        JLabel lblUsuario = new JLabel(usuario.getLogin());
        lblUsuario.setFont(Tema.fonte(Font.BOLD, 14));
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setBorder(new EmptyBorder(0, 16, 0, 0));
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTipo = new JLabel(administrador ? "Administrador" : "Usuário");
        lblTipo.setFont(Tema.fonte(Font.PLAIN, 12));
        lblTipo.setForeground(new Color(0x9CA3AF));
        lblTipo.setBorder(new EmptyBorder(2, 16, 12, 0));
        lblTipo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSair = Tema.botaoMenu("Sair");
        btnSair.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSair.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        menu.add(lblUsuario);
        menu.add(lblTipo);
        menu.add(btnSair);

        btnProdutos.addActionListener(e -> new CRUDProduto(estoque).setVisible(true));
        btnUsuarios.addActionListener(e -> new CRUDUsuario().setVisible(true));
        btnAlertas.addActionListener(e -> atualizarAlertas());
        btnSair.addActionListener(e -> {
            dispose();
            new TelaLogin().setVisible(true);
        });

        return menu;
    }

    private JPanel criarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout(0, 20));
        conteudo.setBackground(Tema.FUNDO);
        conteudo.setBorder(new EmptyBorder(28, 32, 28, 32));

        // Cabeçalho + cartões de resumo
        JPanel cabecalho = new JPanel(new GridLayout(0, 1, 0, 4));
        cabecalho.setOpaque(false);
        cabecalho.add(Tema.titulo("Painel"));
        cabecalho.add(Tema.subtitulo("Visão geral do estoque e dos vencimentos"));

        JPanel resumo = new JPanel(new GridLayout(1, 3, 16, 0));
        resumo.setOpaque(false);
        resumo.add(criarCartaoResumo("Estoque baixo", lblBaixo, Tema.PERIGO));
        resumo.add(criarCartaoResumo("Lotes vencidos", lblVencidos, Tema.PERIGO));
        resumo.add(criarCartaoResumo("Vencendo em " + Estoque.DIAS_AVISO_VALIDADE + " dias", lblVencendo, Tema.AVISO));

        JPanel topo = new JPanel(new BorderLayout(0, 20));
        topo.setOpaque(false);
        topo.add(cabecalho, BorderLayout.NORTH);
        topo.add(resumo, BorderLayout.CENTER);

        // Lista de alertas
        JList<String> lista = new JList<>(modeloAlertas);
        lista.setCellRenderer(new AlertaRenderer());
        lista.setSelectionBackground(Tema.SELECAO);
        JScrollPane rolagem = Tema.rolagem(lista);

        JLabel vazio = new JLabel("Nenhum alerta no momento. Tudo em dia!", SwingConstants.CENTER);
        vazio.setFont(Tema.fonte(Font.PLAIN, 14));
        vazio.setForeground(Tema.TEXTO_SUAVE);
        vazio.setOpaque(true);
        vazio.setBackground(Color.WHITE);

        areaAlertas.add(rolagem, "lista");
        areaAlertas.add(vazio, "vazio");

        JLabel tituloLista = Tema.secao("Alertas de estoque e validade");
        tituloLista.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel blocoLista = new JPanel(new BorderLayout());
        blocoLista.setOpaque(false);
        blocoLista.add(tituloLista, BorderLayout.NORTH);
        blocoLista.add(Tema.cartaoTabela(areaAlertas), BorderLayout.CENTER);

        conteudo.add(topo, BorderLayout.NORTH);
        conteudo.add(blocoLista, BorderLayout.CENTER);
        return conteudo;
    }

    private Tema.Cartao criarCartaoResumo(String titulo, JLabel valor, Color cor) {
        Tema.Cartao cartao = new Tema.Cartao(14, 20);
        cartao.setLayout(new GridLayout(0, 1, 0, 6));

        JLabel lblTitulo = Tema.subtitulo(titulo);
        valor.setFont(Tema.fonte(Font.BOLD, 34));
        valor.setForeground(cor);

        cartao.add(lblTitulo);
        cartao.add(valor);
        return cartao;
    }

    private void atualizarAlertas() {
        modeloAlertas.clear();
        produtoController.verificarAlertas();
        atualizarResumo();
    }

    private void atualizarResumo() {
        int baixo = 0, vencidos = 0, vencendo = 0;
        for (int i = 0; i < modeloAlertas.size(); i++) {
            String alerta = modeloAlertas.get(i);
            if (alerta.startsWith("ESTOQUE BAIXO")) {
                baixo++;
            } else if (alerta.startsWith("VENCIDO")) {
                vencidos++;
            } else if (alerta.startsWith("VENCE EM")) {
                vencendo++;
            }
        }
        lblBaixo.setText(String.valueOf(baixo));
        lblVencidos.setText(String.valueOf(vencidos));
        lblVencendo.setText(String.valueOf(vencendo));

        ((CardLayout) areaAlertas.getLayout()).show(areaAlertas, modeloAlertas.isEmpty() ? "vazio" : "lista");
    }

    public Usuario getUsuario() {
        return usuario;
    }

    /** Desenha cada alerta com uma faixa colorida: âmbar para vencimento próximo, vermelho para o resto. */
    private static class AlertaRenderer extends JLabel implements ListCellRenderer<String> {
        @Override
        public Component getListCellRendererComponent(JList<? extends String> lista, String valor,
                                                      int indice, boolean selecionado, boolean foco) {
            Color cor = valor.startsWith("VENCE EM") ? Tema.AVISO : Tema.PERIGO;
            setText(valor);
            setFont(Tema.fonte(Font.PLAIN, 14));
            setForeground(Tema.TEXTO);
            setOpaque(true);
            setBackground(selecionado ? Tema.SELECAO : Color.WHITE);
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, Tema.LINHA),
                    new CompoundBorder(new MatteBorder(0, 4, 0, 0, cor), new EmptyBorder(12, 14, 12, 14))));
            return this;
        }
    }
}
