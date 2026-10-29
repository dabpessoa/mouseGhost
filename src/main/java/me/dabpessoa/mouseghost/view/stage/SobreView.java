package me.dabpessoa.mouseghost.view.stage;

import javafx.stage.Modality;
import javafx.stage.Stage;
import me.dabpessoa.mouseghost.utils.AppIcons;
import me.dabpessoa.mouseghost.view.generic.AbstractView;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.text.html.CSS;
import java.util.Objects;

@Component
public class SobreView extends AbstractView {

    private static final String VIEW_PATH = "/view/menu/ajuda-sobre-view.fxml";
    private static final String CSS_PATH = "/css/ajuda-sobre-view.css";

    public SobreView(ApplicationContext springContext) {
        super(VIEW_PATH, springContext);
    }

    @Override
    public void config(Stage stage) {
        AppIcons.aplicar(stage);
        stage.setTitle("Sobre");
        stage.setWidth(600);
        stage.setHeight(700);
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        setStyleSheet(CSS_PATH);
        stage.centerOnScreen();
    }

}
