package me.dabpessoa.mouseghost.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.service.MouseService;
import me.dabpessoa.mouseghost.view.service.TextAreaAppender;
import me.dabpessoa.mouseghost.view.service.TextAreaLogConfig;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainViewController {

    private final MouseService mouseService;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private TextArea consoleTextArea;

    @FXML
    protected void onStartButtonClick() {
        log.info("Botão de start clicado...");
        mouseService.start();

        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    @FXML
    protected void onStopButtonClick() {
        log.info("Botão de stop clicado...");
        mouseService.stop();

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    @FXML
    public void initialize() {
        startButton.setDisable(false);
        stopButton.setDisable(true);

        consoleTextArea.setEditable(false); // Impede que o usuário digite no log
        consoleTextArea.setWrapText(true);   // Quebra as linhas longas automaticamente.
        redirecionarConsole(consoleTextArea);
    }

    private void redirecionarConsole(TextArea textArea) {
        TextAreaLogConfig.register();
        TextAreaAppender.setTextArea(textArea);
    }

}
