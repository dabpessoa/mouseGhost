package me.dabpessoa.mouseghost.utils;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Aplica o ícone do aplicativo nas janelas (barra de título e barra de tarefas).
 * Vários tamanhos são registrados para o sistema escolher o mais adequado.
 */
@Slf4j
public final class AppIcons {

    private static final int[] TAMANHOS = {16, 24, 32, 48, 64, 128, 256};
    private static final String CAMINHO = "/images/icons/mouseghost-%d.png";

    private static List<Image> imagens;

    private AppIcons() {
        // Do nothing. Construtor protegido para evitar instanciação de classe utilitária.
    }

    public static void aplicar(Stage stage) {
        stage.getIcons().setAll(carregar());
    }

    private static synchronized List<Image> carregar() {
        if (imagens == null) {
            List<Image> lista = new ArrayList<>();

            for (int tamanho : TAMANHOS) {
                String caminho = CAMINHO.formatted(tamanho);
                try (InputStream in = AppIcons.class.getResourceAsStream(caminho)) {
                    if (in == null) {
                        log.warn("Ícone não encontrado: {}", caminho);
                        continue;
                    }
                    lista.add(new Image(in));
                } catch (IOException e) {
                    log.error("Falha ao carregar o ícone {}", caminho, e);
                }
            }

            imagens = List.copyOf(lista);
        }
        return imagens;
    }

}