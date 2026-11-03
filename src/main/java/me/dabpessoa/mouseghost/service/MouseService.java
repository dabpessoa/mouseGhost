package me.dabpessoa.mouseghost.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MouseService {

    private static final String MSG_TEMPO_INATIVIDADE_INVALIDO = "Tempo de ociosidade inválido ({}). Usando tempo padrão.";
    private static final Long TEMPO_INATIVIDADE_MINIMO = 1L;
    private static final Long TEMPO_INATIVIDADE_MAXIMO = 3600L;

    private final MouseMonitor mouseMonitor;

    @Getter @Setter
    @Value("${app.config.ociosidade.segundos}")
    private String tempoDeInatividade;

    public void start() {
        if (!mouseMonitor.isMouseGhosting()) {
            log.info("----------======== Mouse Ghosting ========----------");
            log.info("[+] Monitor de ociosidade iniciado com sucesso.");
            log.info("[+] O mouse começará a se mover após " + tempoDeInatividade + " segundos parado.");
            log.info("[!] Para encerrar o monitoramento, clique no botão 'Parar', ou feche a janela principal.");
            log.info("----------======== ************** ========----------");
            log.info("");
        }

        long inatividadeLong = verificarTempoInatividade(tempoDeInatividade);
        mouseMonitor.startMonitor(inatividadeLong);
    }

    public void stop() {
        mouseMonitor.stopMonitor();
    }

    public long verificarTempoInatividade(String tempoOciosidade) {
        long tempo;
        try {
            tempo = Long.parseLong(tempoOciosidade);
            if (tempo >= TEMPO_INATIVIDADE_MINIMO && tempo <= TEMPO_INATIVIDADE_MAXIMO) {
                return tempo;
            } else {
                log.warn(MSG_TEMPO_INATIVIDADE_INVALIDO + " O tempo de inatividade deve estar entre os números " + TEMPO_INATIVIDADE_MINIMO + " e " + TEMPO_INATIVIDADE_MAXIMO + ".", tempoOciosidade);
            }
        } catch (NumberFormatException e) {
            log.warn(MSG_TEMPO_INATIVIDADE_INVALIDO, tempoOciosidade);
        }

        return Long.parseLong(SettingsFileUtils.PropertiesConfigEnum.OCIOSIDADE.getValorPadrao());
    }

}
