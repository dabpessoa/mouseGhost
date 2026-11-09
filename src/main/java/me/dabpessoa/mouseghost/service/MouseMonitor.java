
package me.dabpessoa.mouseghost.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.model.MonitorListener;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class MouseMonitor {

    private static final int BASE_SLEEP_TIME_BETWEEN_MOVES_IN_STEP_IN_MILLIS = 6;
    private static final int BASE_MOVES_TIMES_FOR_STEP = 60;

    private final Random random;
    private final Robot robot;
    private final ExecutorService executor;
    private final MouseMoveRunnable mouseMoveRunnable;
    private final KeyboardActivityMonitor keyboardActivityMonitor;

    public MouseMonitor(@Value("${application.artifactId}") String appId, KeyboardActivityMonitor keyboardActivityMonitor) throws AWTException {
        this.random = new Random();
        this.robot = new Robot();

        this.keyboardActivityMonitor = keyboardActivityMonitor;
        this.mouseMoveRunnable = new MouseMoveRunnable(this);

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, appId + "-monitor-thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Inicia monitor. Por padrão, o teclado será considerado atividade humana.
     */
    public synchronized boolean startMonitor(long tempoDeInatividadeSegundos) {
        return startMonitor(tempoDeInatividadeSegundos, false);
    }

    /**
     * Inicia o monitoramento do mouse e, opcionalmente, do teclado.
     *
     * ignorarTeclado = false: teclado reinicia o cronômetro.
     * ignorarTeclado = true: teclado não interfere no cronômetro.
     */
    public synchronized boolean startMonitor(long tempoDeInatividadeSegundos, boolean ignorarTeclado) {
        if (isMouseGhosting() || executor.isShutdown()) {
            return false;
        }

        if (tempoDeInatividadeSegundos <= 0) {
            tempoDeInatividadeSegundos = Long.parseLong(SettingsFileUtils.PropertiesConfigEnum.OCIOSIDADE.getValorPadrao());
        }

        mouseMoveRunnable.getMouseMoveDto().setTempoDeInatividade(tempoDeInatividadeSegundos);

        try {

            if (!ignorarTeclado) {
                keyboardActivityMonitor.start(mouseMoveRunnable::notifyKeyboardActivity);
            } else {
                keyboardActivityMonitor.stop();
            }

            executor.submit(mouseMoveRunnable);
            return true;

        } catch (RuntimeException exception) {
            keyboardActivityMonitor.stop();
            stopMonitor();

            throw exception;
        }
    }

    public synchronized void stopMonitor() {
        keyboardActivityMonitor.stop();

        if (mouseMoveRunnable != null) {
            mouseMoveRunnable.stop();
        }
    }

    public boolean isMouseGhosting() {
        return mouseMoveRunnable != null && mouseMoveRunnable.isRunning();
    }

    /**
     * Move o mouse de forma orgânica.
     *
     * Retorna true se concluiu o movimento.
     * Retorna false se houve intervenção humana.
     */
    public boolean fazerMovimentoDeMouseSimulandoMovimentoHumano(int x1, int y1, int x2, int y2) throws InterruptedException {
        int ctrlX1 = x1 + (x2 - x1) / 4 + (random.nextInt(160) - 80);
        int ctrlY1 = y1 + (y2 - y1) / 4 + (random.nextInt(160) - 80);
        int ctrlX2 = x1 + 3 * (x2 - x1) / 4 + (random.nextInt(160) - 80);
        int ctrlY2 = y1 + 3 * (y2 - y1) / 4 + (random.nextInt(160) - 80);

        int passos = BASE_MOVES_TIMES_FOR_STEP + random.nextInt(30);

        int ultimoXColocado = x1;
        int ultimoYColocado = y1;

        for (int i = 0; i <= passos; i++) {
            /*
             * Verifica primeiro se uma tecla foi pressionada.
             * A notificação pode ter interrompido o Thread.sleep.
             */
            if (mouseMoveRunnable.consumeKeyboardActivity()) {
                return false;
            }

            Point posicaoFisicaAgora = MouseInfo.getPointerInfo().getLocation();

            // Verificação original de movimento humano do mouse.
            if (i > 0 && (posicaoFisicaAgora.x != ultimoXColocado || posicaoFisicaAgora.y != ultimoYColocado)) {
                return false;
            }

            double tLinear = (double) i / passos;
            double tHumano = tLinear * tLinear * (3.0 - 2.0 * tLinear);
            double temp = 1.0 - tHumano;

            int x = (int) (Math.pow(temp, 3) * x1 + 3 * Math.pow(temp, 2) * tHumano * ctrlX1 + 3 * temp * Math.pow(tHumano, 2) * ctrlX2 + Math.pow(tHumano, 3) * x2);
            int y = (int) (Math.pow(temp, 3) * y1 + 3 * Math.pow(temp, 2) * tHumano * ctrlY1 + 3 * temp * Math.pow(tHumano, 2) * ctrlY2 + Math.pow(tHumano, 3) * y2);

            robot.mouseMove(x, y);

            ultimoXColocado = x;
            ultimoYColocado = y;

            /*
             * A interrupção causada pelo teclado acorda imediatamente
             * este sleep, sem aguardar o restante do trajeto.
             */
            try {

                Thread.sleep(BASE_SLEEP_TIME_BETWEEN_MOVES_IN_STEP_IN_MILLIS + random.nextInt(5));

            } catch (InterruptedException exception) {
                if (mouseMoveRunnable.consumeKeyboardActivity()) {
                    return false;
                }

                throw exception;
            }
        }

        return true;
    }

    public void setListener(MonitorListener monitorListener) {
        if (mouseMoveRunnable == null) {
            return;
        }

        mouseMoveRunnable.setMonitorListener(monitorListener);
    }

    @PreDestroy
    public synchronized void destroy() {
        stopMonitor();
        executor.shutdownNow();
    }

}
