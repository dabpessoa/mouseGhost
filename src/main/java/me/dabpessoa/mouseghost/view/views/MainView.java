package me.dabpessoa.mouseghost.view.views;

import javafx.stage.Stage;
import me.dabpessoa.mouseghost.view.generic.AbstractView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MainView extends AbstractView {

    private static final String VIEW_PATH = "/view/main/main-view.fxml";

    @Value("${spring.application.name}")
    private String title;

    public MainView(ApplicationContext springContext) {
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
