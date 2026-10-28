package me.dabpessoa.mouseghost.view.generic;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public interface IViewLoader {

    String getFxmlPath();
    ApplicationContext getSpringContext();

    default FXMLLoader createLoader() {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(getFxmlPath()));

        // Dizemos ao JavaFX para usar o Spring para instanciar os Controllers.
        fxmlLoader.setControllerFactory(getSpringContext()::getBean);

        return fxmlLoader;
    }

    default Parent loadFXML() {
        try {
            return createLoader().load();
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível carregar o arquivo '.fxml' a partir do caminho: " + getFxmlPath(), e);
        }
    }

}
