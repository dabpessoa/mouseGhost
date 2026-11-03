package me.dabpessoa.mouseghost.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.core.JavaFXApplication;
import me.dabpessoa.mouseghost.utils.DicasAlert;
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
    private MenuItem menuItemConfiguracoes;

    @FXML
    private MenuItem menuItemInicio;

    @FXML
    protected void abrirConfiguracoesPadroes() {
        mainView.navigateTo("#conteudoPrincipal", "/view/menu/configuracoes-padroes-view.fxml");

        menuItemConfiguracoes.setDisable(true);
        menuItemInicio.setDisable(false);
    }

    @FXML
    protected void abrirAjudaDicas() {
        DicasAlert.criar(versao, JavaFXApplication.getPrincipalStage()).showAndWait();
    }

    @FXML
    protected void abrirAjudaSobre() {
        Stage stage = new Stage();
        stage.initOwner(JavaFXApplication.getPrincipalStage());   // a janela principal

        sobreView.init(stage);
    }

    @FXML
    protected void abrirAcoesInicio() {
        mainView.navigateTo("#conteudoPrincipal", "/view/menu/inicio-view.fxml");

        menuItemInicio.setDisable(true);
        menuItemConfiguracoes.setDisable(false);
    }

    @FXML
    protected void sairAction() {
        JavaFXApplication.stopApplication();
    }

    @FXML
    public void initialize() {
        menuItemInicio.setDisable(true);
    }

    public void monitoramentoIniciado() {
        menuItemConfiguracoes.setDisable(true);
    }

    public void monitoramentoParado() {
        menuItemConfiguracoes.setDisable(false);
    }

}
