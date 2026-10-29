package me.dabpessoa.mouseghost.utils;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;

/**
 * Alerta "Dicas" do menu Ajuda.
 *
 * O ContentText do Alert aceita apenas texto simples (sem cor nem negrito). Por isso há duas versões:
 *  - criar(...)              : conteúdo formatado, com cores, negrito, numeração e recuo (recomendada);
 *  - criarTextoSimples(...)  : usa o ContentText, com numeração e recuo feitos só com texto.
 */
public final class DicasAlert {

    private static final String TEXTO_SIMPLES = """
            1. COMO USAR
                1.1  Clique em Iniciar para ativar o app e em Parar para desativá-lo.
                1.2  Acompanhe o console da tela inicial: ele mostra o que o app está fazendo.
                1.3  Ajuste o tempo de ociosidade em Configurações > Configurações Padrões
                       e clique em Salvar.

            2. COMO APROVEITAR MELHOR
                2.1  Use um tempo de ociosidade menor que o bloqueio automático de tela do Windows.
                       - Exemplo: se a tela bloqueia após 5 minutos, escolha um valor bem
                         menor, como 120 segundos.
                2.2  Clique em Ocultar na bandeja para deixar o app em segundo plano.
                       - Duplo clique no ícone da bandeja reabre a janela.
                       - O menu do ícone tem as opções Abrir e Sair.
                       - Se não encontrar o ícone, procure atrás da seta ^ da barra de tarefas.
                2.3  Clique em Parar quando terminar, para o Windows voltar ao comportamento normal.
                2.4  Se o computador for de uma empresa, confirme se a política de TI permite o uso.

            3. COMO APOIAR O PROJETO
                3.1  Abra Ajuda > Sobre e escolha uma opção de doação.
                3.2  Buy Me a Coffee: cartão de crédito ou PayPal. O botão abre o navegador.
                3.3  Pix: leia o QR Code com o app do banco, ou clique em Copiar Código Pix
                       e cole no campo Pix Copia e Cola do banco.
                3.4  A doação é voluntária e de qualquer valor. Ela mantém o app gratuito
                       e livre de anúncios.

            Obrigado por usar o Mouse Ghost!
            """;

    private static final String FONTE = "Segoe UI";

    private static final Color COR_TITULO = Color.web("#4338A8");
    private static final Color COR_NUMERO = Color.web("#6C5CE7");
    private static final Color COR_TEXTO = Color.web("#1F2340");
    private static final Color COR_APOIO = Color.web("#5C6280");
    private static final Color COR_PIX = Color.web("#0F8B7E");
    private static final Color COR_CAFE = Color.web("#A67C00");

    private DicasAlert() {
        // Do nothing. Construtor protegido para evitar instanciação de classe utilitária.
    }

    // ------------------------------------------------------------------
    // Versão formatada
    // ------------------------------------------------------------------
    public static Alert criar(String versao, Window dono) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Dicas");
        alerta.setHeaderText("Mouse Ghost - Versão " + versao);
        alerta.initOwner(dono);

        VBox conteudo = new VBox(2);
        conteudo.setPadding(new Insets(0, 8, 8, 4));
        conteudo.getChildren().addAll(

                secao("1", "Como usar"),
                item("1.1", 0, texto("Clique em "), destaque("Iniciar"),
                        texto(" para ativar o app e em "), destaque("Parar"), texto(" para desativá-lo.")),
                item("1.2", 0, texto("Acompanhe o "), destaque("console"),
                        texto(" da tela inicial: ele mostra o que o app está fazendo.")),
                item("1.3", 0, texto("Ajuste o tempo de ociosidade em "),
                        destaque("Configurações > Configurações Padrões"),
                        texto(" e clique em "), destaque("Salvar"), texto(".")),

                secao("2", "Como aproveitar melhor"),
                item("2.1", 0, texto("Use um tempo de ociosidade "), destaque("menor"),
                        texto(" que o bloqueio automático de tela do Windows.")),
                item("•", 1, texto("Exemplo: se a tela bloqueia após 5 minutos, escolha um valor bem menor, "
                        + "como 120 segundos.", COR_APOIO, false)),
                item("2.2", 0, texto("Clique em "), destaque("Ocultar na bandeja"),
                        texto(" para deixar o app em segundo plano, sem ocupar a tela.")),
                item("•", 1, texto("Duplo clique no ícone da bandeja reabre a janela.", COR_APOIO, false)),
                item("•", 1, texto("O menu do ícone tem as opções ", COR_APOIO, false),
                        texto("Abrir", COR_APOIO, true), texto(" e ", COR_APOIO, false),
                        texto("Sair", COR_APOIO, true), texto(".", COR_APOIO, false)),
                item("•", 1, texto("Se não encontrar o ícone, procure atrás da seta ^ da barra de tarefas.",
                        COR_APOIO, false)),
                item("2.3", 0, texto("Clique em "), destaque("Parar"),
                        texto(" quando terminar, para o Windows voltar ao comportamento normal.")),
                item("2.4", 0, texto("Se o computador for de uma empresa, confirme se a política de TI permite "
                        + "o uso do app.")),

                secao("3", "Como apoiar o projeto"),
                item("3.1", 0, texto("Abra "), destaque("Ajuda > Sobre"), texto(" e escolha uma opção de doação.")),
                item("3.2", 0, texto("Buy Me a Coffee", COR_CAFE, true),
                        texto(": apoie com cartão de crédito ou PayPal. O botão abre o navegador.")),
                item("3.3", 0, texto("Pix", COR_PIX, true),
                        texto(": leia o QR Code com o app do seu banco, ou clique em "),
                        destaque("Copiar Código Pix"), texto(" e cole no campo "),
                        texto("Pix Copia e Cola", COR_TEXTO, true), texto(" do banco.")),
                item("3.4", 0, texto("A doação é voluntária e de qualquer valor. Ela mantém o app gratuito "
                        + "e livre de anúncios.")),

                agradecimento("Obrigado por usar o Mouse Ghost!")
        );

        ScrollPane rolagem = new ScrollPane(conteudo);
        rolagem.setFitToWidth(true);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rolagem.setPrefViewportWidth(540);
        rolagem.setPrefViewportHeight(400);
        rolagem.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        alerta.getDialogPane().setContent(rolagem);
        alerta.setResizable(true);
        return alerta;
    }

    private static Label secao(String numero, String titulo) {
        Label rotulo = new Label(numero + ".  " + titulo);
        rotulo.setFont(Font.font(FONTE, FontWeight.BOLD, 15));
        rotulo.setTextFill(COR_TITULO);
        rotulo.setPadding(new Insets(14, 0, 4, 0));
        return rotulo;
    }

    /** Uma linha numerada, com recuo suspenso: o texto que quebra de linha fica alinhado ao texto, não ao número. */
    private static HBox item(String marcador, int nivel, Text... partes) {
        Label rotuloMarcador = new Label(marcador);
        rotuloMarcador.setFont(Font.font(FONTE, FontWeight.BOLD, 13));
        rotuloMarcador.setTextFill(COR_NUMERO);
        rotuloMarcador.setMinWidth(32);

        TextFlow fluxo = new TextFlow(partes);
        fluxo.setLineSpacing(2);
        fluxo.setMinWidth(0);
        HBox.setHgrow(fluxo, Priority.ALWAYS);

        HBox linha = new HBox(8, rotuloMarcador, fluxo);
        linha.setPadding(new Insets(2, 0, 2, nivel * 24));
        return linha;
    }

    private static Label agradecimento(String mensagem) {
        Label rotulo = new Label(mensagem);
        rotulo.setFont(Font.font(FONTE, FontWeight.BOLD, 13));
        rotulo.setTextFill(COR_APOIO);
        rotulo.setPadding(new Insets(16, 0, 0, 0));
        return rotulo;
    }

    private static Text texto(String conteudo) {
        return texto(conteudo, COR_TEXTO, false);
    }

    private static Text destaque(String conteudo) {
        return texto(conteudo, COR_NUMERO, true);
    }

    private static Text texto(String conteudo, Color cor, boolean negrito) {
        Text texto = new Text(conteudo);
        texto.setFill(cor);
        texto.setFont(Font.font(FONTE, negrito ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        return texto;
    }

    // ------------------------------------------------------------------
    // Versão em texto simples (ContentText)
    // ------------------------------------------------------------------
    public static Alert criarTextoSimples(String versao, Window dono) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Dicas");
        alerta.setHeaderText("Mouse Ghost - Versão " + versao);
        alerta.setContentText(TEXTO_SIMPLES);
        alerta.initOwner(dono);
        alerta.getDialogPane().setPrefWidth(640);
        alerta.setResizable(true);
        return alerta;
    }

}