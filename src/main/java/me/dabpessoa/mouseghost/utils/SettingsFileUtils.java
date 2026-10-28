package me.dabpessoa.mouseghost.utils;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Properties;

/**
 * Configurações do usuário, gravadas em %APPDATA%\MouseGhost\settings.properties.
 * Os valores padrão ficam no código; o arquivo só guarda o que o usuário alterou.
 */
public final class SettingsFileUtils {

    private static final String PASTA = "MouseGhost";
    private static final String NOME_ARQUIVO = "settings.properties";

    private SettingsFileUtils() {
        // Do nothing. Construtor protegido para evitar instanciação de classe utilitária.
    }

    public static void garantirArquivoPadrao() {
        Path arquivo = resolveFilePath();
        if (Files.exists(arquivo)) {
            return; // nunca sobrescreve o que o usuário já configurou
        }

        salvar(PropertiesConfigEnum.createDefaultProperties(), arquivo);
    }

    public static void atualizarArquivoDePropriedades(PropertiesConfigEnum configEnum, String valor) {
        atualizarArquivoDePropriedades(Map.of(configEnum, valor));
    }

    public static void atualizarArquivoDePropriedades(Map<PropertiesConfigEnum, String> valuesMap) {
        Properties arquivo = lerArquivoDePropriedades();

        for (Map.Entry<PropertiesConfigEnum, String> entry : valuesMap.entrySet()) {
            arquivo.setProperty(entry.getKey().getChave(), entry.getValue());
        }

        salvar(arquivo, resolveFilePath());
    }

    public static Properties lerArquivoDePropriedades() {
        Path path = resolveFilePath();

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
            System.out.println("Configurações carregadas de " + path);
        } catch (IOException | RuntimeException e) {
            // Arquivo corrompido ou ilegível não pode impedir o app de abrir
            System.err.println("Não foi possível ler {}; Arquivo corrompido ou ilegível. Path: " + path);
        }

        return properties;
    }

    private static void salvar(Properties properties, Path path) {
        try {
            Files.createDirectories(path.getParent());

            // Grava em arquivo temporário e troca no final, para nunca deixar um arquivo pela metade
            Path temporario = path.resolveSibling(NOME_ARQUIVO + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                properties.store(writer, "Mouse Ghost - Configurações do Usuário");
            }

            Files.move(temporario, path, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Configurações salvas em " + path);
        } catch (IOException | RuntimeException e) {
            // Não impede o app de abrir: o import é "optional" e os padrões do application.yml valem
            System.err.println("Não foi possível criar " + path + ": " + e.getMessage());
        }
    }

    private static Path resolveFilePath() {
        String appData = System.getenv("APPDATA");
        if (!StringUtils.hasText(appData)) {
            appData = System.getProperty("user.home");
        }

        Path base = Path.of(appData);
        return base.resolve(PASTA).resolve(NOME_ARQUIVO);
    }

    @Getter
    public enum PropertiesConfigEnum {
        OCIOSIDADE("ociosidade.segundos", "5"),
        ;

        private final String chave;
        private final String valorPadrao;

        PropertiesConfigEnum(String chave, String valorPadrao) {
            this.chave = chave;
            this.valorPadrao = valorPadrao;
        }

        public static Properties createDefaultProperties() {
            Properties properties = new Properties();
            for (PropertiesConfigEnum pce : PropertiesConfigEnum.values()) {
                properties.setProperty(pce.getChave(), pce.getValorPadrao());
            }

            return properties;
        }
    }

}