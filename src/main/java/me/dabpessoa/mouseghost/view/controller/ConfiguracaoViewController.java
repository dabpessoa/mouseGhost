package me.dabpessoa.mouseghost.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.exceptions.ApplicationRuntimeException;
import me.dabpessoa.mouseghost.service.MouseService;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;
import org.springframework.stereotype.Component;

import java.util.OptionalLong;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfiguracaoViewController {

    private final MenuViewController menuViewController;
    private final MouseService mouseService;

    @FXML
    private TextField tempoOciosidadeInput;

    @FXML
    private CheckBox ignorarTecladoCheckBox;

    @FXML
    private Label validationMessage;

    @FXML
    protected void salvarAction() {
        long tempoInatividadeValido;
        try {
            tempoInatividadeValido = mouseService.validarTempoInatividade(tempoOciosidadeInput.getText());
        } catch (ApplicationRuntimeException e) {
            mostrarMensagem(e.getMessage(), false);
            tempoOciosidadeInput.requestFocus();
            tempoOciosidadeInput.selectAll();
            return;
        }

        String tempo = Long.toString(tempoInatividadeValido);
        boolean ignorarTeclado = ignorarTecladoCheckBox.isSelected();

        boolean saved = SettingsFileUtils.atualizarArquivoDePropriedades(
            Map.of(
                SettingsFileUtils.PropertiesConfigEnum.OCIOSIDADE, tempo,
                SettingsFileUtils.PropertiesConfigEnum.IGNORAR_TECLADO, Boolean.toString(ignorarTeclado)
            )
        );

        if (!saved) {
            mostrarMensagem("Não foi possível salvar. Verifique as permissões da pasta de configurações.", false);
            return;
        }

        mouseService.setTempoDeInatividade(tempo);
        mouseService.setIgnorarTeclado(ignorarTeclado);

        tempoOciosidadeInput.setText(tempo);

        mostrarMensagem("Configuração salva com sucesso.", true);
    }

    @FXML
    protected void restaurarPadraoAction() {
        tempoOciosidadeInput.setText(SettingsFileUtils.PropertiesConfigEnum.OCIOSIDADE.getValorPadrao());
        ignorarTecladoCheckBox.setSelected(Boolean.getBoolean(SettingsFileUtils.PropertiesConfigEnum.IGNORAR_TECLADO.getValorPadrao()));
        salvarAction();
    }

    @FXML
    protected void voltarInicioAction() {
        menuViewController.abrirAcoesInicio();
    }

    @FXML
    public void initialize() {
        tempoOciosidadeInput.setText(mouseService.getTempoDeInatividade());
        ignorarTecladoCheckBox.setSelected(mouseService.getIgnorarTeclado());
        validationMessage.setManaged(false);
        validationMessage.setVisible(false);
    }

    private void mostrarMensagem(String mensagem, boolean sucesso) {
        validationMessage.setText(mensagem);
        validationMessage.getStyleClass().removeAll("form-message-success", "form-message-error");
        validationMessage.getStyleClass().add(sucesso ? "form-message-success" : "form-message-error");
        validationMessage.setManaged(true);
        validationMessage.setVisible(true);
    }

}
