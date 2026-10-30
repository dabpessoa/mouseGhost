package me.dabpessoa.mouseghost.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.service.MouseService;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfiguracaoViewController {

    private final MenuViewController menuViewController;
    private final MouseService mouseService;

    @FXML
    private TextField tempoOciosidadeInput;

    @FXML
    protected void salvarAction() {
        Long tempoOciosidade = mouseService.verificarTempoInatividade(tempoOciosidadeInput.getText());
        SettingsFileUtils.atualizarArquivoDePropriedades(SettingsFileUtils.PropertiesConfigEnum.OCIOSIDADE, tempoOciosidade + "");

        mouseService.setTempoDeInatividade(tempoOciosidade + "");
        log.info("Tempo de ociosidade alterado para " + tempoOciosidade + " segundos.");
    }

    @FXML
    protected void voltarInicioAction() {
        menuViewController.abrirAcoesInicio();
    }

    @FXML
    public void initialize() {
        tempoOciosidadeInput.setText(mouseService.getTempoDeInatividade());
    }

}
