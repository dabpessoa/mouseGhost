package me.dabpessoa.mouseghost.view.scene;

import javafx.stage.Stage;
import me.dabpessoa.mouseghost.view.scene.generic.AbstractViewScene;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MainViewScene extends AbstractViewScene {

    private static final String VIEW_PATH = "/view/main/main-view.fxml";

    @Value("${spring.application.name}")
    private String title;

    public MainViewScene(ApplicationContext springContext) {
        super(VIEW_PATH, springContext);
    }

    @Override
    public void config(Stage stage) {
        stage.setTitle(title);
        stage.setWidth(610);
        stage.setHeight(360);
        stage.centerOnScreen();
    }

}
