package me.dabpessoa.mouseghost.core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.RunApplication;
import me.dabpessoa.mouseghost.config.LogFileSpringListener;
import me.dabpessoa.mouseghost.view.scene.MainViewScene;
import me.dabpessoa.mouseghost.view.service.TextAreaLogConfig;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
public class JavaFXApplication extends Application {

    @Getter
    private static Stage principalStage;

    private ConfigurableApplicationContext springContext;

    @Override
    public void start(Stage stage) {
        principalStage = stage;

        MainViewScene principalScene = springContext.getBean(MainViewScene.class);
        principalScene.init(stage);
    }

    // O JavaFX chama o método init() primeiro (Thread de Inicialização)
    @Override
    public void init() {
        initSpring();
    }

    // Quando a janela fecha, o JavaFX chama o método stop()
    @Override
    public void stop() {
        log.info("Encerrando aplicação...");
        TextAreaLogConfig.unregister();
        stopSpring();
        Platform.exit();
        System.exit(0);
    }

    private void initSpring() {
        // Inicializa o contexto do Spring Boot sem abrir um servidor Web (ótimo para Desktop)
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.springContext = new SpringApplicationBuilder()
                .sources(RunApplication.class)
                .listeners(new LogFileSpringListener())
                .headless(false)
                .run(args);

        String loggingFilePath = springContext.getBean(Environment.class).getProperty("logging.file.name");
        log.info("Log em arquivo: {}", loggingFilePath);
    }

    private void stopSpring() {
        // Fecha o contexto do Spring para encerrar conexões com banco de dados, threads, etc.
        this.springContext.close();
    }

}
