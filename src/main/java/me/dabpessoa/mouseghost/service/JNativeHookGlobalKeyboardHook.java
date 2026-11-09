package me.dabpessoa.mouseghost.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptador responsável pela integração com o JNativeHook.
 *
 * Captura eventos globais de teclado sem armazenar o conteúdo digitado.
 */

@Slf4j
@Component
public class JNativeHookGlobalKeyboardHook implements GlobalKeyboardHook, NativeKeyListener {

    private volatile Runnable onKeyPressed;

    private boolean listenerRegistered;
    private boolean ownsNativeHook;

    @Override
    public synchronized void register(Runnable onKeyPressed) {
        if (listenerRegistered) {
            return;
        }

        if (onKeyPressed == null) {
            throw new IllegalArgumentException(
                    "O callback de teclado não pode ser nulo."
            );
        }

        this.onKeyPressed = onKeyPressed;

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

            listenerRegistered = true;

            log.info("Captura global do teclado iniciada.");

        } catch (NativeHookException | RuntimeException exception) {
            GlobalScreen.removeNativeKeyListener(this);

            this.onKeyPressed = null;
            this.ownsNativeHook = false;
            this.listenerRegistered = false;

            throw new IllegalStateException(
                    "Não foi possível iniciar a captura global do teclado.",
                    exception
            );
        }
    }

    @Override
    public synchronized void unregister() {
        if (!listenerRegistered) {
            return;
        }

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
                log.warn(
                        "Não foi possível encerrar completamente o hook do teclado.",
                        exception
                );
            }
        }

        this.onKeyPressed = null;
        this.listenerRegistered = false;
        this.ownsNativeHook = false;
    }

    /**
     * Chamado pelo JNativeHook quando qualquer tecla é pressionada.
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        System.out.println("========= TECLA PRESSIONADA ===========> " + event);

        Runnable callback = this.onKeyPressed;

        if (callback == null) {
            return;
        }

        try {
            callback.run();
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

}
