package view;

import controller.UsuarioController;
import util.Mensagem;
import util.ResultadoCadastro;
import util.Tema;

import javax.swing.*;

public class TelaCadastroUsuario extends JFrame {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnSalvar, btnCancelar;

    private final UsuarioController controller = new UsuarioController();

    public TelaCadastroUsuario() {
        setTitle("Criar conta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        txtLogin = Tema.campoTexto();
        txtSenha = Tema.campoSenha();
        btnSalvar = Tema.botaoPrimario("Salvar");
        btnCancelar = Tema.botaoSecundario("Cancelar");

        JPanel corpo = Tema.coluna(340,
                Tema.campo("Novo usuário", txtLogin),
                Tema.campo("Senha", txtSenha));

        setContentPane(Tema.painelDialogo("Criar conta", "Preencha os dados para se cadastrar",
                corpo, btnCancelar, btnSalvar));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnSalvar);

        btnCancelar.addActionListener(e -> dispose());

        btnSalvar.addActionListener(e -> {
            String login = txtLogin.getText().trim();
            String senha = new String(txtSenha.getPassword()).trim();

            if (login.isEmpty() || senha.isEmpty()) {
                Mensagem.erro("Preencha o usuário e a senha!");
                return;
            }

            ResultadoCadastro resultado = controller.cadastrar(login, senha);

            switch (resultado) {
                case SUCESSO -> {
                    Mensagem.exibirMensagem("Usuário cadastrado com sucesso!");
                    dispose();
                }
                case JA_EXISTE -> Mensagem.erro("Erro: Usuário já existe!");
                case ERRO_BANCO -> Mensagem.erro("Erro: Falha ao cadastrar usuário no banco de dados!");
            }
        });
    }
}
