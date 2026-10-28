package me.dabpessoa.mouseghost.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public final class LogFileUtils {

    private LogFileUtils() {
        // Do nothing. Construtor protegido para evitar instanciação de classe utilitária.
    }

    public static String calcularCaminhoDeArquivoDeLog(String appName, String appId) {
        Path principal = Path.of("./", "logs", appName);
        Path reserva1   = Path.of(System.getProperty("user.dir"), "logs", appName);
        Path reserva2   = Path.of(System.getProperty("user.home"), "logs", appName);

        Path escolhida = podeEscrever(principal) ? principal :
                podeEscrever(reserva1) ? reserva1 :
                podeEscrever(reserva2) ? reserva2 :
                null;

        String caminhoEscolhido;
        if (escolhida != null) {
            caminhoEscolhido = escolhida.resolve(appId + ".log").toString();
        } else {
            caminhoEscolhido = "";
            log.info("Não foi possível encontrar local válido para salvar arquivo de log: {} | {} | {}", principal, reserva1, reserva2);
        }

        return caminhoEscolhido;
    }

    private static boolean podeEscrever(Path dir) {
        try {
            Files.createDirectories(dir);
            Path teste = Files.createTempFile(dir, ".teste-", ".tmp");
            Files.deleteIfExists(teste);
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

}
