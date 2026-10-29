package me.dabpessoa.mouseghost.service;

import jakarta.annotation.PreDestroy;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.core.JavaFXApplication;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;

/**
 * Esconde a janela principal e deixa o app na bandeja do sistema (perto do relógio do S.O).
 */
@Slf4j
@Component
public class TrayService {

    private static final String TITULO = "Mouse Ghost";
    private static final String ICONE = "/images/icons/mouseghost-32.png";

    private TrayIcon trayIcon;
    private boolean avisoExibido;

    public void esconderNaBandeja() {
        Stage stage = JavaFXApplication.getPrincipalStage();

        if (!SystemTray.isSupported()) {
            log.warn("Bandeja do sistema não suportada; minimizando a janela.");
            stage.setIconified(true);
            return;
        }

        try {
            adicionarIcone();
        } catch (AWTException | IOException | RuntimeException e) {
            log.error("Falha ao criar o ícone da bandeja; minimizando a janela.", e);
            stage.setIconified(true);
            return;
        }

        // Sem isto, esconder a única janela visível encerraria o app
        Platform.setImplicitExit(false);
        stage.hide();

        if (!avisoExibido && trayIcon != null) {
            trayIcon.displayMessage(TITULO, "O app continua em execução na bandeja.", TrayIcon.MessageType.INFO);
            avisoExibido = true;
        }
    }

    private synchronized void adicionarIcone() throws AWTException, IOException {
        if (trayIcon != null) {
            return;
        }

        Image imagem;
        try (InputStream in = getClass().getResourceAsStream(ICONE)) {
            if (in == null) {
                throw new IOException("Ícone não encontrado: " + ICONE);
            }
            imagem = ImageIO.read(in);
        }

        PopupMenu menu = new PopupMenu();

        MenuItem abrir = new MenuItem("Abrir");
        abrir.addActionListener(e -> Platform.runLater(this::restaurar));

        MenuItem sair = new MenuItem("Sair");
        sair.addActionListener(e -> Platform.runLater(this::sair));

        menu.add(abrir);
        menu.addSeparator();
        menu.add(sair);

        TrayIcon icone = new TrayIcon(imagem, TITULO, menu);
        icone.setImageAutoSize(true);
        // Duplo clique no ícone; os listeners do AWT rodam fora da thread do JavaFX, por isso o runLater
        icone.addActionListener(e -> Platform.runLater(this::restaurar));

        SystemTray.getSystemTray().add(icone);
        trayIcon = icone;
    }

    private void restaurar() {
        Stage stage = JavaFXApplication.getPrincipalStage();

        removerIcone();
        // Volta ao padrão: com a janela visível, fechar no X encerra o app normalmente
        Platform.setImplicitExit(true);

        stage.show();
        stage.setIconified(false);
        stage.toFront();
        stage.requestFocus();
    }

    private void sair() {
        removerIcone();
        JavaFXApplication.stopApplication();
    }

    /** Também roda ao encerrar o Spring, para não deixar um ícone "fantasma" na bandeja. */
    @PreDestroy
    synchronized void removerIcone() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

}