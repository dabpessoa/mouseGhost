package me.dabpessoa.mouseghost.view.scene.generic;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

@Getter
public abstract class AbstractViewScene implements IViewScene {

    private final String viewPath;
    private final ApplicationContext springContext;
    private Scene scene;

    public AbstractViewScene(String viewPath, ApplicationContext springContext) {
        this.viewPath = viewPath;
        this.springContext = springContext;
    }

    protected abstract void config(Stage stage);

    public void init(Stage stage) {
        stageDefaultConfig(stage);
        config(stage);
        stage.show();
    }

    public void stageDefaultConfig(Stage stage) {
        this.scene = createScene();
        stage.setScene(this.scene);
        stage.setMaximized(false);
        stage.centerOnScreen();
    }

    @Override
    public String getFxmlPath() {
        return getViewPath();
    }

    @Override
    public ApplicationContext getSpringContext() {
        return springContext;
    }

}
