package me.dabpessoa.mouseghost.view.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.utils.SvgHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

@Slf4j
@Component
public class SobreViewController {

    private static final String BUY_ME_A_COFFEE_URL = "https://buymeacoffee.com/dabpessoa";
    private static final String PIX_COPIA_E_COLA = "00020101021126330014br.gov.bcb.pix0111017792113765204000053039865802BR5925DIEGO AUGUSTO BEZERRA PES6009SAO PAULO622905251M61DJD8W126RJ510EWWNSVK06304F58D";

    private static final String PIX_QRCODE_PATH = "/images/pix-qrcode.svg";

    private static final String TEXTO_BOTAO_COPIAR = "Copiar Código Pix";
    private static final String TEXTO_BOTAO_COPIADO = "Código copiado!";

    @FXML private StackPane qrStackPane;

    @FXML
    private Label qrPlaceholder;

    @FXML
    private SVGPath qrSvgPath;

    @FXML
    private TextArea pixCodeArea;

    @FXML
    private Button copiarPixButton;

    private PauseTransition restaurarTextoBotao;

    @FXML
    public void initialize() {
        pixCodeArea.setText(PIX_COPIA_E_COLA);

        String conteudoSVG = SvgHelper.extrairAtributoD(PIX_QRCODE_PATH);
        if (StringUtils.hasText(conteudoSVG)) {
            qrPlaceholder.setVisible(false);
            qrPlaceholder.setManaged(false);
        }
        qrSvgPath.setContent(conteudoSVG);

        restaurarTextoBotao = new PauseTransition(Duration.seconds(2));
        restaurarTextoBotao.setOnFinished(e -> copiarPixButton.setText(TEXTO_BOTAO_COPIAR));

        configurarRedimensionamentoSvg();
    }

    @FXML
    protected void abrirBuyMeACoffee() {
        abrirNoNavegador(BUY_ME_A_COFFEE_URL);
    }

    @FXML
    protected void copiarCodigoPix() {
        ClipboardContent conteudo = new ClipboardContent();
        conteudo.putString(pixCodeArea.getText());
        Clipboard.getSystemClipboard().setContent(conteudo);

        copiarPixButton.setText(TEXTO_BOTAO_COPIADO);
        restaurarTextoBotao.playFromStart();
    }

    private void abrirNoNavegador(String url) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            log.warn("Não foi possível abrir o navegador neste ambiente. Link: {}", url);
            return;
        }

        // Fora da thread do JavaFX, para a interface não travar enquanto o navegador abre
        Thread.ofPlatform().daemon().name("abrir-navegador").start(() -> {
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException | RuntimeException e) {
                log.error("Falha ao abrir o link {}", url, e);
            }
        });
    }

    private void configurarRedimensionamentoSvg() {
        // Calcular as bordas originais (bounds) do SVG construído
        double larguraOriginalSvg = qrSvgPath.getBoundsInLocal().getWidth();
        double alturaOriginalSvg = qrSvgPath.getBoundsInLocal().getHeight();

        // A escala do componente será baseada no tamanho do StackPane.
        // Dividimos a largura do StackPane pela largura real do desenho vetorial.
        qrSvgPath.scaleXProperty().bind(qrStackPane.widthProperty().divide(larguraOriginalSvg));
        qrSvgPath.scaleYProperty().bind(qrStackPane.heightProperty().divide(alturaOriginalSvg));
    }

}
