package me.dabpessoa.mouseghost.model;

import java.awt.Point;

/**
 * Leitura imutável do monitor para a UI. O worker continua executando fora da
 * thread do JavaFX e a interface apenas consulta esta fotografia.
 */
public record MonitorSnapshot(
        long tempoParado,
        long tempoUltimoMovimentoHumano,
        long tempoLimiteDeInatividadeSegundos,
        Point posicaoAtual,
        boolean modoBotAtivo,
        MonitorStatus status,
        TipoAtividadeHumana tipoAtividadeHumana) {

    public String posicaoAtualString() {
        return posicaoAtual != null ?
                String.format("(%d | %d)", posicaoAtual.x, posicaoAtual.y) :
                " - ";
    }

    /**
     * Calcula o progresso da ociosidade entre 0.0 e 1.0.
     */
    public double progressoOciosidade(long agoraMillis) {
        if (status == null || status == MonitorStatus.STOPPED) {
            return 0.0;
        }

        if (status == MonitorStatus.MOVING || modoBotAtivo) {
            return 1.0;
        }

        if (tempoUltimoMovimentoHumano <= 0 || tempoLimiteDeInatividadeSegundos <= 0) {
            return 0.0;
        }

        long tempoDecorrido = Math.max(0L, agoraMillis - tempoUltimoMovimentoHumano);
        double progresso = (double) tempoDecorrido / (tempoLimiteDeInatividadeSegundos * 1000.0);

        return Math.max(0.0, Math.min(1.0, progresso));
    }

}
