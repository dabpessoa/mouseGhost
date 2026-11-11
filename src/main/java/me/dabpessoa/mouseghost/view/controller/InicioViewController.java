package me.dabpessoa.mouseghost.view.controller;

import jakarta.annotation.PreDestroy;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.model.MonitorSnapshot;
import me.dabpessoa.mouseghost.model.MonitorStatus;
import me.dabpessoa.mouseghost.service.MouseService;
import me.dabpessoa.mouseghost.service.TrayService;
import me.dabpessoa.mouseghost.utils.textarea.TextAreaAppender;
import me.dabpessoa.mouseghost.utils.textarea.TextAreaLogConfig;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class InicioViewController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Duration PROGRESS_ANIMATION_DURATION = Duration.millis(900);

    private final MouseService mouseService;
    private final MenuViewController menuViewController;
    private final TrayService trayService;

    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button bandejaButton;
    @FXML private Button doarCafeButton;
    @FXML private Button clearLogButton;
    @FXML private TextArea consoleTextArea;
    @FXML private Circle statusDot;
    @FXML private Label statusLabel;
    @FXML private Label statusDetailLabel;
    @FXML private Label nextMoveLabel;
    @FXML private Label lastEventLabel;
    @FXML private Label positionLabel;
    @FXML private ProgressBar inactivityProgress;
    @FXML private VBox emptyConsoleLabel;

    private Timeline inactivityProgressAnimation;
    private Timeline statusTimeline;

    private boolean lastRenderedRunning;

    @FXML
    protected void onStartButtonClick() {
        TextAreaLogConfig.register();
        TextAreaAppender.setTextArea(consoleTextArea);

        try {
            boolean started = mouseService.start();
            if (!started) {
                log.info("O monitor já está em execução.");
            }

            emptyConsoleLabel.setVisible(false);
            emptyConsoleLabel.setManaged(false);

            startButton.setDisable(true);
            stopButton.setDisable(false);
            bandejaButton.setDisable(false);

            menuViewController.monitoramentoIniciado();

            refreshStatus();
        } catch (RuntimeException exception) {
            log.error("Não foi possível iniciar o monitor.", exception);
            statusDetailLabel.setText("Não foi possível iniciar. Consulte o log para mais detalhes.");
        }
    }

    @FXML
    protected void onStopButtonClick() {
        mouseService.stop();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        bandejaButton.setDisable(true);
        menuViewController.monitoramentoParado();
        refreshStatus();
    }

    @FXML
    public void onEsconderNaBandejaClick() {
        trayService.esconderNaBandeja();
    }

    @FXML
    public void onDoarCafeClick() {
        menuViewController.abrirAjudaSobre();
    }

    @FXML
    protected void onClearLogClick() {
        consoleTextArea.clear();
        atualizarEstadoVazio();
    }

    @FXML
    public void initialize() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        bandejaButton.setDisable(true);
        consoleTextArea.setEditable(false);
        consoleTextArea.setWrapText(true);
        atualizarEstadoVazio();

        if (statusTimeline != null) {
            statusTimeline.stop();
        }

        // Timer para rodar por tempo indeterminado.
        statusTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshStatus()));
        statusTimeline.setCycleCount(Timeline.INDEFINITE);
        statusTimeline.play();

        refreshStatus();
    }

    /*
        Executado dentro do objeto "TimeLine" do javaFX
        que funciona como um Timer e executa uma ação periódica.
     */
    private void refreshStatus() {
        MonitorSnapshot snapshot = mouseService.getSnapShot();

        statusDot.getStyleClass().removeIf(style -> style.startsWith("status-dot-"));
        statusDot.getStyleClass().add(snapshot.status() == null ? MonitorStatus.getDefaultCssDotStyle() : snapshot.status().getCssStyleClassName());

        positionLabel.setText(snapshot.posicaoAtualString());
        statusLabel.setText(Objects.requireNonNullElse(snapshot.status(), MonitorStatus.STOPPED).getTitulo());
        statusDetailLabel.setText(Objects.requireNonNullElse(snapshot.status(), MonitorStatus.STOPPED).getDetalhe());
        nextMoveLabel.setText(Objects.requireNonNullElse(snapshot.status(), MonitorStatus.STOPPED).getProximaAcao());

        animateInactivityProgress(snapshot.progressoOciosidade(System.currentTimeMillis()));

        if (snapshot.status() == MonitorStatus.WAITING) {
            lastEventLabel.setText("O movimento humano está sendo monitorado.");
        } else if (snapshot.status() != MonitorStatus.STOPPED && snapshot.tempoUltimoMovimentoHumano() > 0) {
            lastEventLabel.setText("Última interação humana detectada às " + TIME_FORMATTER.format(Instant.ofEpochMilli(snapshot.tempoUltimoMovimentoHumano())));
        } else {
            lastEventLabel.setText("Nenhuma histórico de atividade registrada.");
        }

        atualizarEstadoVazio();
    }

    private void atualizarEstadoVazio() {
        boolean mostrarEstadoVazio = !mouseService.isRunning() && consoleTextArea.getText().isBlank();

        emptyConsoleLabel.setVisible(mostrarEstadoVazio);
        emptyConsoleLabel.setManaged(mostrarEstadoVazio);
    }

    private void animateInactivityProgress(double targetProgress) {
        // Garante que o valor esteja entre 0 e 1.
        targetProgress = Math.max(0.0, Math.min(1.0, targetProgress));

        // Interrompe uma animação anterior, caso ainda esteja executando.
        if (inactivityProgressAnimation != null) {
            inactivityProgressAnimation.stop();
        }

        // Cria a transição para o novo valor.
        inactivityProgressAnimation = new Timeline(
            new KeyFrame(PROGRESS_ANIMATION_DURATION, new KeyValue(inactivityProgress.progressProperty(), targetProgress, Interpolator.EASE_BOTH))
        );

        inactivityProgressAnimation.playFromStart();
    }

    @PreDestroy
    public synchronized void destroy() {
        if (statusTimeline != null) {
            statusTimeline.stop();
            statusTimeline = null;
            log.info("Barra de status (Timer) finalizado com sucesso.");
        }

        if (inactivityProgressAnimation != null) {
            inactivityProgressAnimation.stop();
            inactivityProgressAnimation = null;
            log.info("Barra de animação (Timer) finalizada com sucesso.");
        }
    }

}
