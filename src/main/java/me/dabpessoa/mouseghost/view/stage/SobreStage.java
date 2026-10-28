package me.dabpessoa.mouseghost.view.stage;

import javafx.stage.Modality;
import javafx.stage.Stage;
import me.dabpessoa.mouseghost.view.generic.AbstractView;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SobreStage extends AbstractView {

    private static final String VIEW_PATH = "/view/menu/ajuda-sobre-view.fxml";
    private static final String CSS_PATH = "/css/ajuda-sobre-view.css";

    public SobreStage(ApplicationContext springContext) {
        super(VIEW_PATH, springContext);
    }

    @Override
    public void config(Stage stage) {
        stage.setTitle("Sobre");
        stage.setWidth(600);
        stage.setHeight(700);
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(CSS_PATH), "CSS não encontrado: " + CSS_PATH).toExternalForm());
        stage.centerOnScreen();
    }

}
