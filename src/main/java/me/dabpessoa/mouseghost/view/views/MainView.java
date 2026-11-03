package me.dabpessoa.mouseghost.view.views;

import javafx.stage.Stage;
import me.dabpessoa.mouseghost.view.generic.AbstractView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MainView extends AbstractView {

    private static final String VIEW_PATH = "/view/main/main-view.fxml";
    private static final String DEFAULT_CSS_PATH = "/css/default-style.css";
    private static final String DOAR_CAFE_BOTAO_CSS = "/css/botao-doar-cafe.css";

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

        setStyleSheet(DEFAULT_CSS_PATH);
        addStyleSheet(DOAR_CAFE_BOTAO_CSS);
    }

}
