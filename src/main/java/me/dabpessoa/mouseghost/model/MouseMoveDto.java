package me.dabpessoa.mouseghost.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;

import java.awt.*;

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

    public MouseMoveDto(boolean configurarValoresPadroes) {
        if (configurarValoresPadroes) {
            configurarValoresPadroes();
        }
    }

    public void configurarValoresPadroes() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Point pointer = MouseInfo.getPointerInfo() == null ? new Point(0, 0) : MouseInfo.getPointerInfo().getLocation();
        ultimaPosicaoSalva = new Point(pointer);
        posicaoAtualReal = new Point(pointer);

        tempoUltimoMovimentoHumano = System.currentTimeMillis();
        tempoDeInatividade = Long.parseLong(SettingsFileUtils.PropertiesConfigEnum.OCIOSIDADE.getValorPadrao());
        modoBotAtivo = false;
    }
}
