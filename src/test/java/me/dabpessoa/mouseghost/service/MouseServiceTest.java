
package me.dabpessoa.mouseghost.service;

import me.dabpessoa.mouseghost.exceptions.TempoInatividadeForaDoIntervaloException;
import me.dabpessoa.mouseghost.exceptions.TempoInatividadeInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MouseServiceTest {

    private MouseMonitor mouseMonitor;
    private MouseService mouseService;

    @BeforeEach
    void setUp() {
        mouseMonitor = mock(MouseMonitor.class);

        mouseService = new MouseService(mouseMonitor);

        mouseService.setTempoDeInatividade("30");
        mouseService.setIgnorarTeclado(false);
    }

    @Test
    void deveAceitarTempoDentroDoIntervalo() {
        assertEquals(
                120L,
                mouseService.validarTempoInatividade(" 120 ")
        );
    }

    @Test
    void deveRejeitarTempoForaDoIntervalo() {
        assertThrows(
                TempoInatividadeForaDoIntervaloException.class,
                () -> mouseService.validarTempoInatividade("0")
        );

        assertThrows(
                TempoInatividadeForaDoIntervaloException.class,
                () -> mouseService.validarTempoInatividade("3601")
        );
    }

    @Test
    void deveRejeitarTempoNaoNumerico() {
        assertThrows(
                TempoInatividadeInvalidoException.class,
                () -> mouseService.validarTempoInatividade("abc")
        );
    }

    @Test
    void deveUsarPadraoParaTempoInvalido() {
        assertEquals(
                5L,
                mouseService.verificarTempoInatividade("invalido")
        );
    }

    @Test
    void deveIniciarComValorNormalizado() {
        when(mouseMonitor.startMonitor(30L, false))
                .thenReturn(true);

        assertTrue(mouseService.start());

        assertEquals(
                "30",
                mouseService.getTempoDeInatividade()
        );

        verify(mouseMonitor).startMonitor(30L, false);
    }

    @Test
    void deveEnviarPreferenciaDeIgnorarTecladoAoIniciar() {
        mouseService.setIgnorarTeclado(true);

        when(mouseMonitor.startMonitor(30L, true))
                .thenReturn(true);

        assertTrue(mouseService.start());

        verify(mouseMonitor).startMonitor(30L, true);
    }

    @Test
    void devePermitirPararMesmoQuandoJaParado() {
        mouseService.stop();

        verify(mouseMonitor).stopMonitor();

        assertFalse(mouseService.isRunning());
    }

}