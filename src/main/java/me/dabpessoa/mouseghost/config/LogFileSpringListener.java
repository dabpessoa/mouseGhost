package me.dabpessoa.mouseghost.config;

import me.dabpessoa.mouseghost.utils.LogFileUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class LogFileSpringListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();

        String appName = env.getProperty("application.name");
        String appId   = env.getProperty("application.artifactId");

        String caminho = LogFileUtils.calcularCaminhoDeArquivoDeLog(appName, appId);

        env.getPropertySources().addFirst(
                new MapPropertySource("logFileConfig",
                        Map.of("logging.file.name", caminho)));
    }

    @Override
    public int getOrder() {
        // Depois de carregar o application.yml, antes de configurar o Logback
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }

}