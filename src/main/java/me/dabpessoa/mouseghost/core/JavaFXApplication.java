package me.dabpessoa.mouseghost.core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.RunApplication;
import me.dabpessoa.mouseghost.config.LogFileSpringListener;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;
import me.dabpessoa.mouseghost.utils.AppIcons;
import me.dabpessoa.mouseghost.view.views.MainView;
import me.dabpessoa.mouseghost.utils.textarea.TextAreaLogConfig;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
public class JavaFXApplication extends Application {

    @Getter
    private static Stage principalStage;

    @Getter
    private static ConfigurableApplicationContext springContext;

    @Override
    public void start(Stage stage) {
        principalStage = stage;
        AppIcons.aplicar(stage);

        MainView mainView = springContext.getBean(MainView.class);
        mainView.init(stage);
    }

    // O JavaFX chama o método init() primeiro (Thread de Inicialização)
    @Override
    public void init() {
        initSpring();
    }

    // Quando a janela fecha, o JavaFX chama o método stop()
    @Override
    public void stop() {
        stopApplication();
    }

    public static void stopApplication() {
        log.info("Encerrando aplicação...");
        TextAreaLogConfig.unregister();
        stopSpring();
        Platform.exit();
        System.exit(0);
    }

    private void initSpring() {
        // Garantindo a existência do arquivo de configurações e seus valores.
        SettingsFileUtils.garantirArquivoPadrao();

        // Inicializa o contexto do Spring Boot sem abrir um servidor Web (ótimo para Desktop)
        String[] args = getParameters().getRaw().toArray(new String[0]);
        springContext = new SpringApplicationBuilder()
                .sources(RunApplication.class)
                .listeners(new LogFileSpringListener())
                .headless(false)
                .run(args);

        String loggingFilePath = springContext.getBean(Environment.class).getProperty("logging.file.name");
        log.info("Log em arquivo: {}", loggingFilePath);
    }

    private static void stopSpring() {
        // Fecha o contexto do Spring para encerrar conexões com banco de dados, threads, etc.
        springContext.close();
    }

}
