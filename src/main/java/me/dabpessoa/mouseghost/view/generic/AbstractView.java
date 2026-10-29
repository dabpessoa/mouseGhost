package me.dabpessoa.mouseghost.view.generic;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

@Getter
public abstract class AbstractView implements IViewLoader {

    @Setter
    private String viewPath;
    private final ApplicationContext springContext;
    private Stage stage;

    public AbstractView(String viewPath, ApplicationContext springContext) {
        this.viewPath = viewPath;
        this.springContext = springContext;
    }

    protected abstract void config(Stage stage);

    public void init() {
        init(new Stage());
    }

    public void init(Stage stage) {
        stageDefaultConfig(stage);
        config(stage);
        stage.show();
    }

    public void changeScene(String viewPath) {
        setViewPath(viewPath);
        changeScene(new Scene(loadFXML()));
    }

    public void changeScene(Scene scene) {
        if (getStage() == null) {
            return;
        }

        getStage().setScene(scene);
    }

    public void putFXMLOnNode(String viewPath, Pane pane) {
        setViewPath(viewPath);
        putFXMLOnNode(loadFXML(), pane);
    }

    public void putFXMLOnNode(Parent fxmlParent, Pane pane) {
        if (pane == null) {
            return;
        }

        pane.getChildren().setAll(fxmlParent);
    }

    public void navigateTo(String renderViewId, String viewPath) {
        Node conteudoPrincipal = lookup(renderViewId);
        if (!(conteudoPrincipal instanceof Pane)) {
            throw new RuntimeException("O conteúdo principal para navegação deve ser filho de Pane");
        }

        putFXMLOnNode(viewPath, (Pane) conteudoPrincipal);
    }

    public Node lookup(String id) {
        if (getStage() == null || getStage().getScene() == null) {
            return null;
        }

        return getStage().getScene().lookup(id);
    }

    public void stageDefaultConfig(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(loadFXML()));
        stage.setMaximized(false);
        stage.centerOnScreen();
    }

    public void setStyleSheet(String cssPath) {
        if (getStage() == null || getStage().getScene() == null || getStage().getScene().getStylesheets() == null) {
            return;
        }

        clearStyleSheets();
        addStyleSheet(cssPath);
    }

    public void addStyleSheet(String cssPath) {
        if (getStage() == null || getStage().getScene() == null || getStage().getScene().getStylesheets() == null) {
            return;
        }

        getStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath), "CSS não encontrado: " + cssPath).toExternalForm());
    }

    public void clearStyleSheets() {
        if (getStage() == null || getStage().getScene() == null) {
            return;
        }

        getStage().getScene().getStylesheets().clear();
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
