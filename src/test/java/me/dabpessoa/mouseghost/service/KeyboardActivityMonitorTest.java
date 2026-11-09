
package me.dabpessoa.mouseghost.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KeyboardActivityMonitorTest {

    private GlobalKeyboardHook keyboardHook;
    private KeyboardActivityMonitor monitor;

    @BeforeEach
    void setUp() {
        keyboardHook = mock(GlobalKeyboardHook.class);
        monitor = new KeyboardActivityMonitor(keyboardHook);
    }

    @Test
    void deveRegistrarCapturaGlobal() {
        Runnable callback = () -> { };

        monitor.start(callback);

        assertTrue(monitor.isRunning());

        verify(keyboardHook).register(callback);
    }

    @Test
    void deveEncerrarCapturaGlobal() {
        monitor.start(() -> { });

        monitor.stop();

        assertFalse(monitor.isRunning());

        verify(keyboardHook).unregister();
    }

    @Test
    void deveIgnorarInicializacaoDuplicada() {
        Runnable callback = () -> { };

        monitor.start(callback);
        monitor.start(callback);

        verify(keyboardHook).register(callback);
    }

    @Test
    void devePermitirNotificarAtividadePeloCallback() {
        AtomicBoolean activityDetected = new AtomicBoolean(false);

        monitor.start(() -> activityDetected.set(true));

        /*
         * O callback é entregue ao adaptador.
         * Aqui validamos o contrato do serviço sem depender
         * de um teclado físico ou do sistema operacional.
         */
        org.mockito.ArgumentCaptor<Runnable> captor =
                org.mockito.ArgumentCaptor.forClass(Runnable.class);

        verify(keyboardHook).register(captor.capture());

        captor.getValue().run();

        assertTrue(activityDetected.get());
    }
}
