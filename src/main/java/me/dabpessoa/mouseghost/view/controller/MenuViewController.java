package me.dabpessoa.mouseghost.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.core.JavaFXApplication;
import me.dabpessoa.mouseghost.view.views.MainView;
import me.dabpessoa.mouseghost.view.views.SobreView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuViewController {

    private final MainView mainView;
    private final SobreView sobreView;

    @Value("${application.version}")
    private String versao;

    @FXML
    protected void abrirConfiguracoesPadroes() {
        mainView.setStyleSheet("/css/default-pane.css");
        mainView.navigateTo("#conteudoPrincipal", "/view/menu/configuracoes-padroes-view.fxml");
    }

    @FXML
    protected void abrirAjudaDicas() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Dicas");
        alerta.setHeaderText("Mouse Ghost");
        alerta.setContentText("Versão " + versao);
        alerta.initOwner(JavaFXApplication.getPrincipalStage());
        alerta.showAndWait();
    }

    @FXML
    protected void abrirAjudaSobre() {
        Stage stage = new Stage();
        stage.initOwner(JavaFXApplication.getPrincipalStage());   // a janela principal

        sobreView.init(stage);
    }

    @FXML
    protected void abrirAcoesInicio() {
        mainView.clearStyleSheets();
        mainView.navigateTo("#conteudoPrincipal", "/view/menu/inicio-view.fxml");
    }

    @FXML
    protected void sairAction() {
        JavaFXApplication.stopApplication();
    }

}
