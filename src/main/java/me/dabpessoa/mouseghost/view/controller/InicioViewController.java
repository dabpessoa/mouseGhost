package me.dabpessoa.mouseghost.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.core.JavaFXApplication;
import me.dabpessoa.mouseghost.service.MouseService;
import me.dabpessoa.mouseghost.service.TrayService;
import me.dabpessoa.mouseghost.utils.textarea.TextAreaAppender;
import me.dabpessoa.mouseghost.utils.textarea.TextAreaLogConfig;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InicioViewController {

    private final MouseService mouseService;
    private final MenuViewController menuViewController;
    private final TrayService trayService;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button bandejaButton;

    @FXML
    private Button doarCafeButton;

    @FXML
    private TextArea consoleTextArea;

    @FXML
    protected void onStartButtonClick() {
        log.info("(*) Botão de início clicado.");

        TextAreaLogConfig.register();
        TextAreaAppender.setTextArea(consoleTextArea);

        mouseService.start();

        startButton.setDisable(true);
        stopButton.setDisable(false);
        bandejaButton.setDisable(false);

        menuViewController.monitoramentoIniciado();
    }

    @FXML
    protected void onStopButtonClick() {
        log.info("(*) Botão de Parar clicado.");
        mouseService.stop();

        startButton.setDisable(false);
        stopButton.setDisable(true);
        bandejaButton.setDisable(true);

        menuViewController.monitoramentoParado();
    }

    @FXML
    public void onEsconderNaBandejaClick() {
        trayService.esconderNaBandeja();
    }

    @FXML
    public void onDoarCafeClick() {
        menuViewController.abrirAjudaSobre();
    }

    @FXML
    public void initialize() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        bandejaButton.setDisable(true);

        consoleTextArea.setEditable(false); // Impede que o usuário digite no log
        consoleTextArea.setWrapText(true);   // Quebra as linhas longas automaticamente.
    }

}
