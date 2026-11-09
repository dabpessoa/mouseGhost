package me.dabpessoa.mouseghost.model;

import java.awt.Point;
import java.util.Objects;

/**
 * Leitura imutável do monitor para a UI. O worker continua executando fora da
 * thread do JavaFX e a interface apenas consulta esta fotografia.
 */
public record MonitorSnapshot(
        long tempoParado,
        long tempoUltimoMovimentoHumano,
        Point posicaoAtual,
        MonitorStatus status) {

    public String posicaoAtualString() {
        return posicaoAtual != null ?
                String.format("(%d | %d)", posicaoAtual.x, posicaoAtual.y) :
                " - ";
    }

}
