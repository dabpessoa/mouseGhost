package me.dabpessoa.mouseghost.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.core.JavaFXApplication;
import me.dabpessoa.mouseghost.view.stage.MainStage;
import me.dabpessoa.mouseghost.view.stage.SobreStage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuViewController {

    private final MainStage mainStage;
    private final SobreStage sobreStage;

    @FXML
    protected void abrirConfiguracoesPadroes() {
        mainStage.navigateTo("#conteudoPrincipal", "/view/menu/configuracoes-padroes-view.fxml");
    }

    @FXML
    protected void abrirAjudaDicas() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Dicas");
        alerta.setHeaderText("Mouse Ghost");
        alerta.setContentText("Versão 1.0.0");
        alerta.initOwner(JavaFXApplication.getPrincipalStage());
        alerta.showAndWait();
    }

    @FXML
    protected void abrirAjudaSobre() {
        Stage stage = new Stage();
        stage.initOwner(JavaFXApplication.getPrincipalStage());   // a janela principal

        sobreStage.init(stage);
    }

    @FXML
    protected void abrirAcoesInicio() {
        mainStage.navigateTo("#conteudoPrincipal", "/view/menu/inicio-view.fxml");
    }

    @FXML
    protected void sairAction() {
        JavaFXApplication.stopApplication();
    }

}
