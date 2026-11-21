package view;

import controller.UsuarioController;
import model.Usuario;
import util.ResultadoCadastro;
import util.Tema;

import javax.swing.*;

public class FormularioUsuario extends JDialog {
    private JTextField txtNome;
    private JPasswordField txtSenha;
    private JComboBox<String> cbTipo, cbAtivo;
    private JButton btnSalvar, btnCancelar;
    private final UsuarioController controller;
    private Usuario usuario;
    private CRUDUsuario parent;

    public FormularioUsuario(CRUDUsuario parent, UsuarioController controller, Usuario usuario) {
        super(parent, true);
        this.parent = parent;
        this.controller = controller;
        this.usuario = usuario;

        setTitle(usuario == null ? "Cadastrar Usuário" : "Editar Usuário");

        txtNome = Tema.campoTexto();
        cbTipo = Tema.combo(new String[]{"Administrador", "Usuário"});
        cbAtivo = Tema.combo(new String[]{"Sim", "Não"});
        btnSalvar = Tema.botaoPrimario("Salvar");
        btnCancelar = Tema.botaoSecundario("Cancelar");

        JPanel corpo;
        if (usuario == null) {
            txtSenha = Tema.campoSenha();
            corpo = Tema.coluna(380,
                    Tema.campo("Nome", txtNome),
                    Tema.campo("Senha", txtSenha),
                    Tema.linha(Tema.campo("Tipo", cbTipo), Tema.campo("Ativo", cbAtivo)));
        } else {
            corpo = Tema.coluna(380,
                    Tema.campo("Nome", txtNome),
                    Tema.linha(Tema.campo("Tipo", cbTipo), Tema.campo("Ativo", cbAtivo)));

            txtNome.setText(usuario.getLogin());
            cbTipo.setSelectedItem("A".equals(usuario.getTipo()) ? "Administrador" : "Usuário");
            cbAtivo.setSelectedItem("T".equals(usuario.getAtivo()) ? "Sim" : "Não");
        }

        setContentPane(Tema.painelDialogo(
                usuario == null ? "Novo usuário" : "Editar usuário",
                usuario == null ? "Informe os dados de acesso" : "Altere os dados do usuário",
                corpo, btnCancelar, btnSalvar));
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(btnSalvar);

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarUsuario());
    }

    private void salvarUsuario() {
        try {
            String nome = txtNome.getText().trim();

            String tipoSelecionado = (String) cbTipo.getSelectedItem();
            String ativoSelecionado = (String) cbAtivo.getSelectedItem();

            String tipo = tipoSelecionado.equalsIgnoreCase("Administrador") ? "A" : "U";
            String ativo = ativoSelecionado.equalsIgnoreCase("Sim") ? "T" : "F";

            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "O campo Nome é obrigatório!");
                return;
            }

            if (usuario == null) {
                String senha = new String(txtSenha.getPassword()).trim();
                if (senha.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "O campo Senha é obrigatório!");
                    return;
                }

                // A senha vai em texto puro: o hash é gerado uma única vez, no DAO
                ResultadoCadastro resultado = controller.cadastrar(nome, senha, tipo, ativo);
                if (resultado == ResultadoCadastro.JA_EXISTE) {
                    JOptionPane.showMessageDialog(this, "Já existe um usuário com esse nome!");
                    return;
                }
                if (resultado == ResultadoCadastro.ERRO_BANCO) {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar usuário no banco de dados!");
                    return;
                }
            } else {
                usuario.setLogin(nome);
                usuario.setTipo(tipo);
                usuario.setAtivo(ativo);
                if (!controller.atualizarUsuario(usuario)) {
                    JOptionPane.showMessageDialog(this, "Erro ao atualizar usuário no banco de dados!");
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Usuário salvo com sucesso!");
            parent.atualizarTabela();
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar usuário: " + ex.getMessage());
        }
    }
}
