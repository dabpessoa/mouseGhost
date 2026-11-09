
package me.dabpessoa.mouseghost.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Serviço responsável pelo ciclo de vida da captura global do teclado.
 */

@Slf4j
@Component
public class KeyboardActivityMonitor {

    private final GlobalKeyboardHook keyboardHook;

    private boolean running;

    public KeyboardActivityMonitor(GlobalKeyboardHook keyboardHook) {
        this.keyboardHook = keyboardHook;
    }

    /**
     * Inicia a captura global do teclado.
     *
     * @param onKeyPressed ação executada quando uma tecla for pressionada.
     */
    public synchronized void start(Runnable onKeyPressed) {
        if (running) {
            return;
        }

        keyboardHook.register(onKeyPressed);
        running = true;

        log.info("Monitor de atividade do teclado iniciado.");
    }

    /**
     * Encerra a captura global.
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }

        keyboardHook.unregister();
        running = false;

        log.info("Monitor de atividade do teclado parado.");
    }

    public synchronized boolean isRunning() {
        return running;
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

}
