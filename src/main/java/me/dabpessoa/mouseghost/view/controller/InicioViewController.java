package me.dabpessoa.mouseghost.view.controller;

import jakarta.annotation.PreDestroy;
import javafx.animation.KeyFrame;
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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class InicioViewController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

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

//        MonitorSnapshot snapshot = mouseService.getSnapShot();

//        String title = "title";
//        String detail = "detail";
//        String nextAction = "nextAction";

//        MonitorStatus status = active ? snapshot.status() : MonitorStatus.STOPPED;
//        switch (status) {
//            case WAITING -> {
//                title = "Aguardando";
//                detail = "Monitor ativo e observando sua atividade.";
//                nextAction = snapshot.secondsUntilMovement() > 0
//                        ? "Próximo movimento em " + snapshot.secondsUntilMovement() + "s"
//                        : "Preparando próximo ciclo";
//            }
//            case MOVING -> {
//                title = "Movimentando";
//                detail = "O cursor está sendo movido suavemente.";
//                nextAction = "Mova o mouse para retomar o controle";
//            }
//            default -> {
//                title = "Parado";
//                detail = "Clique em Iniciar para começar.";
//                nextAction = "O monitor está desligado";
//            }
//        }
//
//        statusLabel.setText(title);
//        statusDetailLabel.setText(detail);
//        nextMoveLabel.setText(nextAction);
//        statusDot.getStyleClass().removeIf(style -> style.startsWith("status-dot-"));
//        statusDot.getStyleClass().add("status-dot-" + status.name().toLowerCase());
//
//        double progress = 0.0;
//        if (status == MonitorStatus.WAITING && snapshot.inactivitySeconds() > 0) {
//            progress = 1.0 - ((double) snapshot.secondsUntilMovement()
//                    / snapshot.inactivitySeconds());
//            progress = Math.max(0.0, Math.min(1.0, progress));
//        } else if (status == MonitorStatus.MOVING) {
//            progress = 1.0;
//        }
//        inactivityProgress.setProgress(progress);
//
//        Point position = snapshot.currentPosition();
//        positionLabel.setText(position == null
//                ? "Cursor: —"
//                : "Cursor: " + position.x + ", " + position.y);
//
//        if (snapshot.running() && snapshot.lastHumanMovementAt() > 0) {
//            lastEventLabel.setText("Última interação detectada às "
//                    + TIME_FORMATTER.format(Instant.ofEpochMilli(snapshot.lastHumanMovementAt())));
//        } else {
//            lastEventLabel.setText("Nenhuma atividade registrada nesta sessão.");
//        }

        atualizarEstadoVazio();
    }

    private void atualizarEstadoVazio() {
        boolean mostrarEstadoVazio = !mouseService.isRunning()
                && consoleTextArea.getText().isBlank();
        emptyConsoleLabel.setVisible(mostrarEstadoVazio);
        emptyConsoleLabel.setManaged(mostrarEstadoVazio);
    }

    @PreDestroy
    public synchronized void destroy() {
        if (statusTimeline != null) {
            statusTimeline.stop();
        }
    }

}
