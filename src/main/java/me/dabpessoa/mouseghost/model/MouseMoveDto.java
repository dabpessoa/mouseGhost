package me.dabpessoa.mouseghost.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.MouseInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MouseMoveDto {

    private Point ultimaPosicaoSalva;
    private Point posicaoAtualReal;
    private boolean modoBotAtivo;
    private long tempoUltimoMovimentoHumano;
    private long tempoDeInatividade;
    private Dimension screenSize;

    public MouseMoveDto(boolean configurarValoresPadores) {
        if (configurarValoresPadores) {
            configurarValoresPadroes();
        }
    }

    public void configurarValoresPadroes() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ultimaPosicaoSalva = MouseInfo.getPointerInfo().getLocation();
        posicaoAtualReal = MouseInfo.getPointerInfo().getLocation();
        tempoUltimoMovimentoHumano = System.currentTimeMillis();
        tempoDeInatividade = 5; // In seconds.
        modoBotAtivo = false;
    }

}
