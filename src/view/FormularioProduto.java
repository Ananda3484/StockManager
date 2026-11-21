package view;

import controller.ProdutoController;
import model.Produto;
import util.Funcoes;
import util.ResultadoCadastro;
import util.Tema;

import javax.swing.*;
import java.time.LocalDate;

public class FormularioProduto extends JDialog {

    private JTextField txtNome;
    private JTextField txtMarca;
    private JTextField txtPreco;
    private JComboBox<String> cbTipo;
    private JTextField txtQuantidade;
    private JTextField txtEstoqueMinimo;
    private JTextField txtLote;
    private JTextField txtValidade;

    private JButton btnSalvar, btnCancelar;

    private final ProdutoController controller;
    private Produto produto;
    private CRUDProduto parent;

    public FormularioProduto(CRUDProduto parent, ProdutoController controller, Produto produto) {
        super(parent, true);

        this.parent = parent;
        this.controller = controller;
        this.produto = produto;

        setTitle(produto == null ? "Cadastrar Produto" : "Editar Produto");

        txtNome = Tema.campoTexto();
        txtMarca = Tema.campoTexto();
        txtPreco = Tema.campoTexto();
        cbTipo = Tema.combo(new String[]{"Não perecível", "Perecível"});
        txtQuantidade = Tema.campoTexto();
        txtEstoqueMinimo = Tema.campoTexto();
        txtLote = Tema.campoTexto();
        txtValidade = Tema.campoTexto();

        btnSalvar = Tema.botaoPrimario("Salvar");
        btnCancelar = Tema.botaoSecundario("Cancelar");

        JPanel corpo = Tema.coluna(500,
                Tema.campo("Nome", txtNome),
                Tema.linha(Tema.campo("Marca", txtMarca), Tema.campo("Preço (R$)", txtPreco)),
                Tema.linha(Tema.campo("Tipo", cbTipo), Tema.campo("Quantidade", txtQuantidade)),
                Tema.linha(Tema.campo("Estoque mínimo", txtEstoqueMinimo), Tema.vazio()),
                Tema.secao("Lote (somente produtos perecíveis)"),
                Tema.linha(Tema.campo("Número do lote", txtLote), Tema.campo("Validade (dd/mm/aaaa)", txtValidade)));

        setContentPane(Tema.painelDialogo(
                produto == null ? "Novo produto" : "Editar produto",
                produto == null ? "Preencha os dados do produto" : "Altere os dados do produto",
                corpo, btnCancelar, btnSalvar));
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(btnSalvar);

        // Lote e validade só fazem sentido para produto perecível
        cbTipo.addActionListener(e -> atualizarCamposDeLote());

        if (produto != null) {
            // Edição: o tipo não muda e os lotes são geridos na tela de Lotes
            txtNome.setText(produto.getNome());
            txtMarca.setText(produto.getMarca());
            txtPreco.setText(String.valueOf(produto.getPreco()));
            cbTipo.setSelectedItem(produto.exigeValidade() ? "Perecível" : "Não perecível");
            txtQuantidade.setText(String.valueOf(produto.getQuantidade()));
            txtEstoqueMinimo.setText(String.valueOf(produto.getEstoqueMinimo()));

            cbTipo.setEnabled(false);
            txtLote.setEnabled(false);
            txtValidade.setEnabled(false);
            txtQuantidade.setEnabled(!produto.exigeValidade());
        } else {
            atualizarCamposDeLote();
        }

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarProduto());
    }

    private boolean perecivelSelecionado() {
        return "Perecível".equals(cbTipo.getSelectedItem());
    }

    private void atualizarCamposDeLote() {
        boolean perecivel = perecivelSelecionado();
        txtLote.setEnabled(perecivel);
        txtValidade.setEnabled(perecivel);
        if (!perecivel) {
            txtLote.setText("");
            txtValidade.setText("");
        }
    }

    private void salvarProduto() {
        try {
            String nome = txtNome.getText().trim();
            String marca = txtMarca.getText().trim();
            String precoTexto = txtPreco.getText().trim().replace(",", ".");

            if (nome.isEmpty() || precoTexto.isEmpty()
                    || txtQuantidade.getText().trim().isEmpty()
                    || txtEstoqueMinimo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!");
                return;
            }

            double preco = Double.parseDouble(precoTexto);
            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            int estoqueMinimo = Integer.parseInt(txtEstoqueMinimo.getText().trim());

            if (preco < 0 || quantidade < 0 || estoqueMinimo < 0) {
                JOptionPane.showMessageDialog(this, "Preço, quantidade e estoque mínimo não podem ser negativos!");
                return;
            }

            if (produto == null) {
                boolean perecivel = perecivelSelecionado();
                String numeroLote = txtLote.getText().trim();
                LocalDate validade = null;

                if (perecivel) {
                    validade = Funcoes.lerData(txtValidade.getText());
                    if (numeroLote.isEmpty() || validade == null) {
                        JOptionPane.showMessageDialog(this,
                                "Produto perecível exige número do lote e validade no formato dd/mm/aaaa!");
                        return;
                    }
                    if (quantidade == 0) {
                        JOptionPane.showMessageDialog(this, "Informe a quantidade do lote!");
                        return;
                    }
                }

                ResultadoCadastro resultado = controller.cadastrarProduto(
                        nome, marca, preco, perecivel, quantidade, estoqueMinimo, numeroLote, validade);

                if (resultado == ResultadoCadastro.JA_EXISTE) {
                    JOptionPane.showMessageDialog(this, "Produto já existe!");
                    return;
                } else if (resultado == ResultadoCadastro.ERRO_BANCO) {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar produto no banco de dados!");
                    return;
                }

            } else {
                produto.setNome(nome);
                produto.setMarca(marca);
                produto.setPreco(preco);
                produto.setQuantidade(quantidade);
                produto.setEstoqueMinimo(estoqueMinimo);

                if (!controller.atualizarProduto(produto)) {
                    JOptionPane.showMessageDialog(this, "Erro ao atualizar produto (o nome já pode estar em uso)!");
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Produto salvo com sucesso!");

            parent.atualizarTabela();
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Informe valores numéricos válidos!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar produto: " + ex.getMessage());
        }
    }
}
