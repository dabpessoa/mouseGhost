
package me.dabpessoa.mouseghost.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.model.MonitorListener;
import me.dabpessoa.mouseghost.service.keyboard.NativeKeyboardMonitor;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.AWTException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class MouseMonitor {

    private final ExecutorService executor;
    private final MouseMoveRunnable mouseMoveRunnable;
    private final NativeKeyboardMonitor nativeKeyboardMonitor;

    public MouseMonitor(@Value("${application.artifactId}") String appId, NativeKeyboardMonitor nativeKeyboardMonitor) throws AWTException {
        this.nativeKeyboardMonitor = nativeKeyboardMonitor;
        this.mouseMoveRunnable = new MouseMoveRunnable();

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, appId + "-monitor-thread");
            thread.setDaemon(true);
            return thread;
        });
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

        if (!ignorarTeclado) {
            nativeKeyboardMonitor.initNativeListener();
            nativeKeyboardMonitor.addListener(mouseMoveRunnable::notificarAtividadeNoTeclado);
        } else {
            nativeKeyboardMonitor.clearListeners();
        }

        try {
            executor.submit(mouseMoveRunnable);
        } catch (RuntimeException exception) {
            nativeKeyboardMonitor.clearListeners();
            stopMonitor();
            throw exception;
        }

        return true;
    }

    public synchronized void stopMonitor() {
        nativeKeyboardMonitor.clearListeners();

        if (mouseMoveRunnable != null) {
            mouseMoveRunnable.stop();
        }
    }

    public boolean isMouseGhosting() {
        return mouseMoveRunnable != null && mouseMoveRunnable.isRunning();
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
