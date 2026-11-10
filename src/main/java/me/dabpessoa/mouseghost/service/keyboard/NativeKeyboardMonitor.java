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

    public void initNativeListener() {
        this.keyboardListeners = new CopyOnWriteArrayList<>();

        // Reduz a quantidade de mensagens internas da biblioteca.
        Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.WARNING);

        try {
            /*
             * Verifica se outro componente já registrou o hook.
             * Nesse caso, não assumimos a propriedade do hook global.
             */
            boolean hookAlreadyRegistered = GlobalScreen.isNativeHookRegistered();

            GlobalScreen.addNativeKeyListener(this);

            if (!hookAlreadyRegistered) {
                GlobalScreen.registerNativeHook();
                ownsNativeHook = true;
            }
        } catch (NativeHookException | RuntimeException exception) {
            destroy();
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
        System.out.println("========= TECLA PRESSIONADA ===========> " + event);

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

    public void stopNativeListener() {
        GlobalScreen.removeNativeKeyListener(this);

        /*
         * Só encerra o hook global se ele foi iniciado por esta classe.
         * Isso evita desligar um hook que eventualmente pertença
         * a outro componente da aplicação.
         */
        if (ownsNativeHook) {
            try {
                GlobalScreen.unregisterNativeHook();
                log.info("Captura global do teclado encerrada.");
            } catch (NativeHookException | RuntimeException exception) {
                log.warn("Não foi possível encerrar completamente o hook do teclado.", exception);
            }
        }

        this.ownsNativeHook = false;
    }

    @PreDestroy
    public void destroy() {
        if (keyboardListeners != null) {
            keyboardListeners.clear();
            keyboardListeners = null;
        }
        stopNativeListener();
    }

}
