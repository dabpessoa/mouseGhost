package me.dabpessoa.mouseghost.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    }

    @Test
    void deveAceitarTempoDentroDoIntervalo() {
        assertEquals(OptionalLong.of(120), mouseService.validarTempoInatividade(" 120 "));
    }

    @Test
    void deveRejeitarTempoForaDoIntervalo() {
        assertTrue(mouseService.validarTempoInatividade("0").isEmpty());
        assertTrue(mouseService.validarTempoInatividade("3601").isEmpty());
        assertTrue(mouseService.validarTempoInatividade("abc").isEmpty());
    }

    @Test
    void deveUsarPadraoParaTempoInvalido() {
        assertEquals(5L, mouseService.verificarTempoInatividade("invalido"));
    }

    @Test
    void deveIniciarComValorNormalizado() {
        when(mouseMonitor.startMonitor(30L, false)).thenReturn(true);

        assertTrue(mouseService.start());
        assertEquals("30", mouseService.getTempoDeInatividade());
        verify(mouseMonitor).startMonitor(30L, false);
    }

    @Test
    void deveEnviarPreferenciaDeIgnorarTecladoAoIniciar() {
        mouseService.setIgnorarTeclado(true);
        when(mouseMonitor.startMonitor(30L, true)).thenReturn(true);

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
