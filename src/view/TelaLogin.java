package view;

import controller.UsuarioController;
import model.Usuario;
import util.Mensagem;
import util.Tema;

import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JFrame {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar, btnCadastrar;

    private final UsuarioController controller = new UsuarioController();

    public TelaLogin() {
        setTitle("Controle de Estoque - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        txtLogin = Tema.campoTexto();
        txtSenha = Tema.campoSenha();
        btnEntrar = Tema.botaoPrimario("Entrar");
        btnCadastrar = Tema.botaoSecundario("Criar conta");

        // Cabeçalho centralizado: logo, título e subtítulo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setOpaque(false);
        logoPanel.add(new Tema.Logo(56));
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel titulo = Tema.titulo("Controle de Estoque");
        JLabel subtitulo = Tema.subtitulo("Entre com a sua conta para continuar");

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        for (JComponent item : new JComponent[]{logoPanel, titulo, subtitulo}) {
            item.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        cabecalho.add(logoPanel);
        cabecalho.add(Box.createVerticalStrut(16));
        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(6));
        cabecalho.add(subtitulo);
        cabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel botoes = new JPanel(new GridLayout(0, 1, 0, 10));
        botoes.setOpaque(false);
        botoes.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 0, 0));
        botoes.add(btnEntrar);
        botoes.add(btnCadastrar);

        JPanel conteudo = Tema.coluna(340,
                cabecalho,
                Tema.campo("Usuário", txtLogin),
                Tema.campo("Senha", txtSenha),
                botoes);

        Tema.Cartao cartao = new Tema.Cartao(16, 32);
        cartao.setLayout(new BorderLayout());
        cartao.add(conteudo, BorderLayout.CENTER);

        JPanel fundo = new JPanel(new GridBagLayout());
        fundo.setBackground(Tema.FUNDO);
        fundo.add(cartao);

        setContentPane(fundo);
        setSize(520, 640);
        setLocationRelativeTo(null);

        getRootPane().setDefaultButton(btnEntrar);

        btnEntrar.addActionListener(e -> {
            String login = txtLogin.getText().trim();
            String senha = new String(txtSenha.getPassword());

            Usuario usuario = controller.login(login, senha);

            if (usuario != null) {
                dispose();
                new TelaMenu(usuario).setVisible(true);
            } else {
                Mensagem.erro("Login ou senha incorretos!");
            }
        });

        btnCadastrar.addActionListener(e -> new TelaCadastroUsuario().setVisible(true));
    }
}
