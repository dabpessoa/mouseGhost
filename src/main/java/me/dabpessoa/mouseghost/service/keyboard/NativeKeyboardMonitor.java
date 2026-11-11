package me.dabpessoa.mouseghost.service.keyboard;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.exceptions.ApplicationRuntimeException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptador responsável pela integração com o JNativeHook.
 *
 * Captura eventos globais de teclado sem armazenar o conteúdo digitado.
 */

@Slf4j
@Component
public class NativeKeyboardMonitor implements KeyboardListener, NativeKeyListener {

    private List<Runnable> keyboardListeners;
    private boolean ownsNativeHook;
    private boolean nativeKeyListenerRegistered;

    public void initNativeListener() {
        this.keyboardListeners = new CopyOnWriteArrayList<>();

        // Reduz a quantidade de mensagens internas da biblioteca.
        Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.WARNING);

        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
                ownsNativeHook = true;
                log.debug("Hook global do teclado registrado.");
            } else {
                log.debug("Hook global do teclado já estava registrado.");
            }

            /*
             * Adiciona o listener somente uma vez.
             */
            if (!nativeKeyListenerRegistered) {
                GlobalScreen.addNativeKeyListener(this);
                nativeKeyListenerRegistered = true;
                log.debug("Listener global do teclado registrado.");
            }
        } catch (NativeHookException | RuntimeException exception) {
            throw new IllegalStateException("Não foi possível iniciar a captura global do teclado.", exception);
        }
    }

    @Override
    public synchronized void addListener(Runnable listener) {
        if (listener == null) {
            throw new IllegalArgumentException("O callback de teclado não pode ser nulo.");
        }

        checkKeyboardListeners();
        keyboardListeners.add(listener);
        log.debug(
                "Listener de atividade do teclado adicionado. Total: {}",
                keyboardListeners.size()
        );
    }

    @Override
    public synchronized boolean removeListener(Runnable listener) {
        if (listener == null) {
            return false;
        }

        checkKeyboardListeners();
        return keyboardListeners.remove(listener);
    }

    @Override
    public boolean hasAnyListener() {
        checkKeyboardListeners();
        return !keyboardListeners.isEmpty();
    }

    @Override
    public void clearListeners() {
        if (keyboardListeners != null) {
            boolean possuiAlgoParaLimpar = !keyboardListeners.isEmpty();
            keyboardListeners.clear();

            if (possuiAlgoParaLimpar) {
                log.debug("Listeners da sessão de teclado removidos.");
            }
        }

    }

    public void checkKeyboardListeners() {
        if (keyboardListeners == null) {
            throw new ApplicationRuntimeException("Listeners não carregados. Verifique se o método init() foi devidamente chamado.");
        }
    }

    /**
     * Chamado pelo JNativeHook quando qualquer tecla é pressionada.
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        log.debug("Evento global de teclado detectado.");

        if (keyboardListeners == null || keyboardListeners.isEmpty()) {
            return;
        }

        try {
            keyboardListeners.forEach(Runnable::run);
        } catch (RuntimeException exception) {
            log.error("Erro ao notificar atividade do teclado.", exception);
        }
    }

    /**
     * Não precisamos tratar liberação de tecla.
     */
    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
        // Sem implementação intencional.
    }

    /**
     * Não precisamos tratar caracteres digitados.
     */
    @Override
    public void nativeKeyTyped(NativeKeyEvent event) {
        // Sem implementação intencional.
    }

    @PreDestroy
    public void destroy() {
        clearListeners();
        if (nativeKeyListenerRegistered) {
            GlobalScreen.removeNativeKeyListener(this);
            nativeKeyListenerRegistered = false;
        }

        /*
         * Só encerramos o hook se ele pertence a esta aplicação.
         *
         * Isso evita desligar um hook que tenha sido iniciado por
         * outro componente.
         */
        if (ownsNativeHook && GlobalScreen.isNativeHookRegistered()) {
            try {
                GlobalScreen.unregisterNativeHook();
                log.info("Hook global do teclado encerrado definitivamente.");
            } catch (NativeHookException | RuntimeException exception) {
                log.warn("Não foi possível encerrar completamente o hook global.", exception);
            }
        }

        ownsNativeHook = false;
    }

}
