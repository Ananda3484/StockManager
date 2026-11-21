package view;

import controller.UsuarioController;
import model.Usuario;
import util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CRUDUsuario extends JFrame {
    private JTable tabelaUsuarios;
    private DefaultTableModel model;
    private JButton btnNovo, btnEditar, btnExcluir;
    private final UsuarioController controller = new UsuarioController();

    public CRUDUsuario() {
        setTitle("Gerenciamento de Usuários");
        setSize(900, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"ID", "Nome", "Tipo", "Situação", "Data Criação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaUsuarios = new JTable(model);
        Tema.estilizarTabela(tabelaUsuarios);
        tabelaUsuarios.getColumnModel().getColumn(0).setMaxWidth(70);
        tabelaUsuarios.getColumnModel().getColumn(3).setCellRenderer(new RenderizadorSituacao());

        btnNovo = Tema.botaoPrimario("+ Novo usuário");
        btnEditar = Tema.botaoSecundario("Editar");
        btnExcluir = Tema.botaoPerigo("Excluir");

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(Tema.titulo("Usuários"));
        textos.add(Tema.subtitulo("Gerencie quem tem acesso ao sistema"));
        cabecalho.add(textos, BorderLayout.WEST);
        cabecalho.add(Tema.centralizar(btnNovo), BorderLayout.EAST);

        JPanel acoes = new JPanel(new BorderLayout());
        acoes.setOpaque(false);
        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        esquerda.setOpaque(false);
        esquerda.add(btnEditar);
        acoes.add(esquerda, BorderLayout.WEST);
        acoes.add(btnExcluir, BorderLayout.EAST);

        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(Tema.FUNDO);
        raiz.setBorder(new EmptyBorder(26, 30, 24, 30));
        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(Tema.cartaoTabela(Tema.rolagem(tabelaUsuarios)), BorderLayout.CENTER);
        raiz.add(acoes, BorderLayout.SOUTH);
        setContentPane(raiz);

        btnNovo.addActionListener(e -> abrirFormularioUsuario(null));
        btnEditar.addActionListener(e -> editarUsuario());
        btnExcluir.addActionListener(e -> excluirUsuario());

        atualizarTabela();
    }

    // Público porque o FormularioUsuario chama este método após salvar
    public void atualizarTabela() {
        model.setRowCount(0);
        List<Usuario> usuarios = controller.listarUsuarios();
        for (Usuario u : usuarios) {
            String tipoLegivel = "A".equals(u.getTipo()) ? "Administrador" : "Usuário";
            String ativoLegivel = "T".equals(u.getAtivo()) ? "Ativo" : "Inativo";

            model.addRow(new Object[]{
                u.getId(),
                u.getLogin(),
                tipoLegivel,
                ativoLegivel,
                u.getDataCriacao()
            });
        }
    }

    private void abrirFormularioUsuario(Usuario usuario) {
        FormularioUsuario form = new FormularioUsuario(this, controller, usuario);
        form.setVisible(true);
    }

    private void editarUsuario() {
        int linha = tabelaUsuarios.getSelectedRow();
        if (linha >= 0) {
            int id = (int) model.getValueAt(linha, 0);
            Usuario usuario = controller.buscarPorId(id);
            abrirFormularioUsuario(usuario);
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para editar.");
        }
    }

    private void excluirUsuario() {
        int linha = tabelaUsuarios.getSelectedRow();
        if (linha >= 0) {
            int id = (int) model.getValueAt(linha, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir este usuário?", "Confirmação",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (controller.excluirUsuario(id)) {
                    atualizarTabela();
                    JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso.");
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir usuário.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir.");
        }
    }

    /** Situação em verde (ativo) ou cinza (inativo). */
    private static class RenderizadorSituacao extends Tema.RenderizadorCelula {
        RenderizadorSituacao() {
            super(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, foco, linha, coluna);
            boolean ativo = "Ativo".equals(valor);
            setForeground(ativo ? Tema.SUCESSO : Tema.TEXTO_SUAVE);
            setFont(Tema.fonte(Font.BOLD, 13));
            return this;
        }
    }
}
