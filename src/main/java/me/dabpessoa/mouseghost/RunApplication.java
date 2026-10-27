package me.dabpessoa.mouseghost;

import javafx.application.Application;
import me.dabpessoa.mouseghost.core.JavaFXApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class RunApplication {

    private RunApplication() {
        // Private Constructor.
    }

    static void main(String[] args) {
        Application.launch(JavaFXApplication.class, args);
    }

}
