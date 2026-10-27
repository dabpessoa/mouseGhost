package me.dabpessoa.mouseghost.view.scene.generic;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public interface IViewScene {

    String getFxmlPath();
    ApplicationContext getSpringContext();

    default FXMLLoader createLoader() {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(getFxmlPath()));

        // Dizemos ao JavaFX para usar o Spring para instanciar os Controllers.
        fxmlLoader.setControllerFactory(getSpringContext()::getBean);

        return fxmlLoader;
    }

    default Parent loadSceneParentFromFXML() {
        try {
            return createLoader().load();
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível carregar o arquivo '.fxml' a partir do caminho: " + getFxmlPath(), e);
        }
    }

    default Scene createScene() {
        return new Scene(loadSceneParentFromFXML());
    }

}
