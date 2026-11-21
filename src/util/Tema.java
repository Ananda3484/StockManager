package util;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

/**
 * Visual do sistema: cores, fontes e componentes estilizados.
 * Se a biblioteca FlatLaf estiver na pasta lib/, ela é usada automaticamente.
 */
public class Tema {

    public static final Color PRIMARIA = new Color(0x2563EB);
    public static final Color PRIMARIA_ESCURA = new Color(0x1D4ED8);
    public static final Color FUNDO = new Color(0xF3F4F6);
    public static final Color CARTAO = Color.WHITE;
    public static final Color TEXTO = new Color(0x111827);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);
    public static final Color BORDA = new Color(0xD1D5DB);
    public static final Color LINHA = new Color(0xE5E7EB);
    public static final Color PERIGO = new Color(0xDC2626);
    public static final Color SUCESSO = new Color(0x16A34A);
    public static final Color AVISO = new Color(0xD97706);
    public static final Color MENU_LATERAL = new Color(0x111827);
    public static final Color MENU_HOVER = new Color(0x1F2937);
    public static final Color SELECAO = new Color(0xDBEAFE);

    private static String familia = Font.SANS_SERIF;

    // ---------------------------------------------------------------- configuração geral

    public static void aplicar() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        familia = escolherFamilia();
        boolean flat = tentarFlatLaf();

        if (!flat) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // mantém o visual padrão do Java
            }
        }

        Font base = fonte(Font.PLAIN, 14);
        String[] chaves = {"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "Table.font", "List.font", "TextArea.font", "OptionPane.messageFont",
                "OptionPane.buttonFont", "MenuItem.font", "Menu.font", "ToolTip.font", "CheckBox.font"};
        for (String chave : chaves) {
            UIManager.put(chave, base);
        }
        UIManager.put("TableHeader.font", fonte(Font.BOLD, 13));
        UIManager.put("Panel.background", CARTAO);
        UIManager.put("OptionPane.background", CARTAO);

        if (flat) {
            UIManager.put("defaultFont", base);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 12);
        }
    }

    private static boolean tentarFlatLaf() {
        try {
            Class<?> flat = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flat.getMethod("setup").invoke(null);
            return true;
        } catch (Exception | LinkageError e) {
            return false;
        }
    }

    private static String escolherFamilia() {
        Set<String> disponiveis = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Roboto"}) {
            if (disponiveis.contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font fonte(int estilo, float tamanho) {
        return new Font(familia, estilo, Math.round(tamanho));
    }

    private static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ---------------------------------------------------------------- textos

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(Font.BOLD, 22));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(Font.PLAIN, 13));
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static JLabel secao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(Font.BOLD, 13));
        l.setForeground(TEXTO);
        return l;
    }

    // ---------------------------------------------------------------- botões

    public static class Botao extends JButton {
        private final Color base, hover, pressionado;
        private final boolean contorno;

        Botao(String texto, Color base, Color hover, Color pressionado, Color corTexto, boolean contorno) {
            super(texto);
            this.base = base;
            this.hover = hover;
            this.pressionado = pressionado;
            this.contorno = contorno;
            setFont(fonte(Font.BOLD, 13));
            setForeground(corTexto);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            Color fundo;
            if (!isEnabled()) {
                fundo = new Color(0xE5E7EB);
            } else if (getModel().isPressed()) {
                fundo = pressionado;
            } else if (getModel().isRollover()) {
                fundo = hover;
            } else {
                fundo = base;
            }
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (contorno) {
                g2.setColor(BORDA);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton botaoPrimario(String texto) {
        return new Botao(texto, PRIMARIA, PRIMARIA_ESCURA, new Color(0x1E40AF), Color.WHITE, false);
    }

    public static JButton botaoSecundario(String texto) {
        return new Botao(texto, Color.WHITE, new Color(0xF3F4F6), new Color(0xE5E7EB), TEXTO, true);
    }

    public static JButton botaoPerigo(String texto) {
        return new Botao(texto, PERIGO, new Color(0xB91C1C), new Color(0x991B1B), Color.WHITE, false);
    }

    /** Botão do menu lateral (fundo escuro). */
    public static JButton botaoMenu(String texto) {
        JButton b = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (isEnabled() && getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    suavizar(g2);
                    g2.setColor(MENU_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        b.setFont(fonte(Font.PLAIN, 14));
        b.setForeground(new Color(0xE5E7EB));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(11, 16, 11, 16));
        return b;
    }

    // ---------------------------------------------------------------- campos de formulário

    public static JTextField campoTexto() {
        JTextField c = new JTextField();
        estilizarCampo(c);
        return c;
    }

    public static JPasswordField campoSenha() {
        JPasswordField c = new JPasswordField();
        estilizarCampo(c);
        return c;
    }

    private static void estilizarCampo(JTextComponent c) {
        c.setFont(fonte(Font.PLAIN, 14));
        c.setForeground(TEXTO);
        c.setBackground(Color.WHITE);
        c.setCaretColor(TEXTO);
        c.setDisabledTextColor(TEXTO_SUAVE);
        c.setBorder(bordaCampo(BORDA));
        c.addPropertyChangeListener("enabled", e -> c.setBackground(c.isEnabled() ? Color.WHITE : FUNDO));
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                c.setBorder(bordaCampo(PRIMARIA));
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.setBorder(bordaCampo(BORDA));
            }
        });
    }

    private static Border bordaCampo(Color cor) {
        return new CompoundBorder(new LineBorder(cor, 1, true), new EmptyBorder(8, 10, 8, 10));
    }

    public static <T> JComboBox<T> combo(T[] itens) {
        JComboBox<T> c = new JComboBox<>(itens);
        c.setFont(fonte(Font.PLAIN, 14));
        c.setBackground(Color.WHITE);
        c.setForeground(TEXTO);
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 38));
        return c;
    }

    /** Campo com o rótulo em cima. */
    public static JPanel campo(String rotulo, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(rotulo);
        l.setFont(fonte(Font.BOLD, 12));
        l.setForeground(TEXTO_SUAVE);
        p.add(l, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    /** Vários campos lado a lado, com a mesma largura. */
    public static JPanel linha(JComponent... itens) {
        JPanel p = new JPanel(new GridLayout(1, itens.length, 14, 0));
        p.setOpaque(false);
        for (JComponent item : itens) {
            p.add(item);
        }
        return p;
    }

    /** Mantém o botão no tamanho dele, centralizado na vertical (não estica junto com o título). */
    public static JPanel centralizar(JComponent componente) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(componente);
        return p;
    }

    public static JPanel vazio() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    /** Coluna vertical de campos, com espaço entre eles. */
    public static JPanel coluna(int largura, JComponent... itens) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (int i = 0; i < itens.length; i++) {
            JComponent item = itens[i];
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, item.getPreferredSize().height));
            p.add(item);
            if (i < itens.length - 1) {
                p.add(Box.createVerticalStrut(14));
            }
        }
        p.setPreferredSize(new Dimension(largura, p.getPreferredSize().height));
        return p;
    }

    /** Janela de formulário: título, corpo e botões alinhados à direita. */
    public static JPanel painelDialogo(String titulo, String subtitulo, JComponent corpo, JComponent... botoes) {
        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(CARTAO);
        raiz.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel cabecalho = new JPanel(new GridLayout(0, 1, 0, 4));
        cabecalho.setOpaque(false);
        cabecalho.add(titulo(titulo));
        if (subtitulo != null) {
            cabecalho.add(subtitulo(subtitulo));
        }

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setOpaque(false);
        for (JComponent b : botoes) {
            rodape.add(b);
        }

        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(corpo, BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);
        return raiz;
    }

    // ---------------------------------------------------------------- cartões e logo

    public static class Cartao extends JPanel {
        private final int raio;

        public Cartao(int raio, int margem) {
            this.raio = raio;
            setOpaque(false);
            setBackground(CARTAO);
            setBorder(new EmptyBorder(margem, margem, margem, margem));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
            g2.setColor(BORDA);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class Logo extends JComponent {
        private final int tamanho;

        public Logo(int tamanho) {
            this.tamanho = tamanho;
            setPreferredSize(new Dimension(tamanho, tamanho));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(PRIMARIA);
            g2.fillRoundRect(0, 0, tamanho, tamanho, tamanho / 3, tamanho / 3);
            g2.setColor(Color.WHITE);
            g2.setFont(fonte(Font.BOLD, tamanho * 0.5f));
            FontMetrics fm = g2.getFontMetrics();
            String letra = "E";
            int x = (tamanho - fm.stringWidth(letra)) / 2;
            int y = (tamanho - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(letra, x, y);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------- tabelas

    public static class RenderizadorCelula extends DefaultTableCellRenderer {
        private final int alinhamento;

        public RenderizadorCelula(int alinhamento) {
            this.alinhamento = alinhamento;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(alinhamento);
            setForeground(TEXTO);
            setBackground(selecionada ? SELECAO : Color.WHITE);
            return this;
        }
    }

    private static class RenderizadorCabecalho extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            setText(String.valueOf(valor).toUpperCase());
            setOpaque(true);
            setBackground(new Color(0xF9FAFB));
            setForeground(TEXTO_SUAVE);
            setFont(fonte(Font.BOLD, 11));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINHA), new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    public static void estilizarTabela(JTable t) {
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(LINHA);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(SELECAO);
        t.setSelectionForeground(TEXTO);
        t.setFont(fonte(Font.PLAIN, 14));
        t.setForeground(TEXTO);
        t.setBackground(Color.WHITE);
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new RenderizadorCelula(SwingConstants.LEFT));

        JTableHeader cabecalho = t.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 40));
        cabecalho.setDefaultRenderer(new RenderizadorCabecalho());
    }

    public static JScrollPane rolagem(JComponent componente) {
        JScrollPane s = new JScrollPane(componente);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    /** Cartão fino e quadrado para envolver tabelas. */
    public static Cartao cartaoTabela(JComponent conteudo) {
        Cartao c = new Cartao(0, 1);
        c.setLayout(new BorderLayout());
        c.add(conteudo, BorderLayout.CENTER);
        return c;
    }
}
