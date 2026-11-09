package me.dabpessoa.mouseghost.view.views;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.dabpessoa.mouseghost.core.JavaFXApplication;
import me.dabpessoa.mouseghost.service.MouseService;
import me.dabpessoa.mouseghost.view.generic.AbstractView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MainView extends AbstractView {

    private static final String VIEW_PATH = "/view/main/main-view.fxml";
    private static final String DEFAULT_CSS_PATH = "/css/default-style.css";
    private static final String BOTAO_DOAR_CAFE_CSS_PATH = "/css/botao-doar-cafe.css";

    private final MouseService mouseService;

    @Value("${spring.application.name}")
    private String title;

    public MainView(ApplicationContext springContext, MouseService mouseService) {
        super(VIEW_PATH, springContext);
        this.mouseService = mouseService;
    }

    @Override
    public void config(Stage stage) {
        stage.setTitle(title);
        stage.setWidth(820);
        stage.setHeight(590);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.centerOnScreen();
        stage.setOnCloseRequest(criarEvendoDeClose(stage));

        setStyleSheet(DEFAULT_CSS_PATH);
        addStyleSheet(BOTAO_DOAR_CAFE_CSS_PATH);
    }

    private EventHandler<WindowEvent> criarEvendoDeClose(Stage stage) {
        return event -> {
            if (!mouseService.isRunning()) {
                return;
            }

            event.consume();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(stage);
            alert.setTitle("Encerrar monitoramento");
            alert.setHeaderText("O Mouse Ghost ainda está ativo");
            alert.setContentText("Deseja parar o monitor e sair do aplicativo?");

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource(DEFAULT_CSS_PATH), "CSS não encontrado: " + DEFAULT_CSS_PATH).toExternalForm());

            ButtonType botaoSairType = new ButtonType("Sair", ButtonBar.ButtonData.OK_DONE);
            ButtonType botaoCancelarType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(botaoCancelarType, botaoSairType);

            Button botaoCancelar = (Button) dialogPane.lookupButton(botaoCancelarType);
            if (botaoCancelar != null) {
                botaoCancelar.getStyleClass().add("secondary-button");
            }

            Button botaoSair = (Button) dialogPane.lookupButton(botaoSairType);
            if (botaoSair != null) {
                botaoSair.getStyleClass().add("primary-button");
            }

            ButtonBar buttonBar = (ButtonBar) dialogPane.lookup(".button-bar");
            if (buttonBar != null) {
                // Força o alinhamento de todos os botões à direita
                buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
            }

            alert.showAndWait().ifPresent(button -> {
                if (button.getText().equals("Sair")) {
                    mouseService.stop();
                    stage.close();
                    JavaFXApplication.stopApplication();
                }
            });
        };
    }

}
