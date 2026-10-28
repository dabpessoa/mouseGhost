package me.dabpessoa.mouseghost.view.stage;

import javafx.stage.Modality;
import javafx.stage.Stage;
import me.dabpessoa.mouseghost.view.generic.AbstractView;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SobreStage extends AbstractView {

    private static final String VIEW_PATH = "/view/menu/ajuda-sobre-view.fxml";

    public SobreStage(ApplicationContext springContext) {
        super(VIEW_PATH, springContext);
    }

    @Override
    public void config(Stage stage) {
        stage.setTitle("Sobre");
        stage.setWidth(410);
        stage.setHeight(320);
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.centerOnScreen();
    }

}
