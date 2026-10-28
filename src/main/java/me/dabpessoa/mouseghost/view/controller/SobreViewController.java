package me.dabpessoa.mouseghost.view.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
@Component
public class SobreViewController {

    private static final String BUY_ME_A_COFFEE_URL = "https://buymeacoffee.com/SEU_USUARIO";
    private static final String PIX_COPIA_E_COLA = "COLE_AQUI_O_CODIGO_PIX_COPIA_E_COLA";

    private static final String PIX_QRCODE_PATH = "/view/menu/img/pix-qrcode.png";

    private static final String TEXTO_BOTAO_COPIAR = "Copiar Código Pix";
    private static final String TEXTO_BOTAO_COPIADO = "Código copiado!";

    @FXML
    private ImageView qrImageView;

    @FXML
    private Label qrPlaceholder;

    @FXML
    private TextArea pixCodeArea;

    @FXML
    private Button copiarPixButton;

    private PauseTransition restaurarTextoBotao;

    @FXML
    public void initialize() {
        pixCodeArea.setText(PIX_COPIA_E_COLA);

        restaurarTextoBotao = new PauseTransition(Duration.seconds(2));
        restaurarTextoBotao.setOnFinished(e -> copiarPixButton.setText(TEXTO_BOTAO_COPIAR));

        carregarQrCode();
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

    private void carregarQrCode() {
        try (InputStream in = getClass().getResourceAsStream(PIX_QRCODE_PATH)) {
            if (in == null) {
                log.warn("QR Code do Pix não encontrado em '{}'. Exibindo o texto reserva.", PIX_QRCODE_PATH);
                return;
            }

            qrImageView.setImage(new Image(in));
            qrPlaceholder.setVisible(false);
            qrPlaceholder.setManaged(false);
        } catch (IOException e) {
            log.error("Falha ao carregar o QR Code do Pix", e);
        }
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

}
