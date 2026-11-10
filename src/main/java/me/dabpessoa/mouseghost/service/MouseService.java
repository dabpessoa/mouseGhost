package me.dabpessoa.mouseghost.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.exceptions.ApplicationRuntimeException;
import me.dabpessoa.mouseghost.exceptions.TempoInatividadeForaDoIntervaloException;
import me.dabpessoa.mouseghost.exceptions.TempoInatividadeInvalidoException;
import me.dabpessoa.mouseghost.model.MonitorListener;
import me.dabpessoa.mouseghost.model.MonitorSnapshot;
import me.dabpessoa.mouseghost.model.MonitorStatus;
import me.dabpessoa.mouseghost.model.TipoAtividadeHumana;
import me.dabpessoa.mouseghost.utils.SettingsFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.MouseInfo;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class MouseService implements MonitorListener {

    private static final long TEMPO_INATIVIDADE_PADRAO = Long.parseLong(SettingsFileUtils.PropertiesConfigEnum.OCIOSIDADE.getValorPadrao());
    private static final long TEMPO_INATIVIDADE_MINIMO = 1L;
    private static final long TEMPO_INATIVIDADE_MAXIMO = 3600L;
    private static final String MSG_PADRAO_TEMPO_INATIVIDADE_INVALIDO = "Tempo de ociosidade inválido ({}). Usando o padrão de " + TEMPO_INATIVIDADE_PADRAO + " segundos.";

    private final MouseMonitor mouseMonitor;

    private AtomicReference<MonitorSnapshot> atomicMonitorSnapshot;

    @Getter @Setter
    @Value("${app.config.ociosidade.segundos}")
    private String tempoDeInatividade;

    @Getter @Setter
    @Value("${app.config.teclado.ignorar}")
    private Boolean ignorarTeclado;

    public boolean start() {
        long tempoDeInatividadeValido = verificarTempoInatividade(tempoDeInatividade);
        tempoDeInatividade = Long.toString(tempoDeInatividadeValido);
        ignorarTeclado = ignorarTeclado != null ? ignorarTeclado : Boolean.getBoolean(SettingsFileUtils.PropertiesConfigEnum.IGNORAR_TECLADO.getValorPadrao());

        mouseMonitor.setListener(this);
        boolean started = mouseMonitor.startMonitor(tempoDeInatividadeValido, ignorarTeclado);

        if (started) {
            log.info("Monitor iniciado. O mouse se moverá após {} segundos sem interação.", tempoDeInatividadeValido);
            log.info("Considerar atividade do teclado: {}", !ignorarTeclado);
            log.info("[!] Para encerrar o monitoramento, clique no botão 'Parar', ou feche a janela principal.");
        }

        return started;
    }

    public void stop() {
        mouseMonitor.stopMonitor();
        log.info("Monitor parado.");
    }

    public boolean isRunning() {
        return mouseMonitor.isMouseGhosting();
    }

    public long validarTempoInatividade(String tempoOciosidade) {
        long tempo;

        try {
            if (tempoOciosidade == null) {
                throw new NumberFormatException();
            }

            tempo = Long.parseLong(tempoOciosidade.trim());
        } catch (NumberFormatException e) {
            throw new TempoInatividadeInvalidoException("O tempo de inatividade deve ser um número inteiro.");
        }

        if (tempo < TEMPO_INATIVIDADE_MINIMO || tempo > TEMPO_INATIVIDADE_MAXIMO) {
            throw new TempoInatividadeForaDoIntervaloException(String.format("O tempo de inatividade deve estar entre os números %d e %d.", TEMPO_INATIVIDADE_MINIMO, TEMPO_INATIVIDADE_MAXIMO));
        }

        return tempo;
    }

    public long verificarTempoInatividade(String tempoOciosidade) {
        String mensagem = String.format(MSG_PADRAO_TEMPO_INATIVIDADE_INVALIDO, tempoOciosidade);

        try {
            return validarTempoInatividade(tempoOciosidade);
        } catch (ApplicationRuntimeException e) {
            if (e instanceof TempoInatividadeForaDoIntervaloException) {
                log.warn(mensagem + " O tempo de inatividade deve estar entre os números {} e {}.", tempoOciosidade, TEMPO_INATIVIDADE_MINIMO, TEMPO_INATIVIDADE_MAXIMO);
            } else {
                log.warn(mensagem, tempoOciosidade);
            }
        }

        return TEMPO_INATIVIDADE_PADRAO;
    }

    public MonitorSnapshot getSnapShot() {
        if (atomicMonitorSnapshot == null || !mouseMonitor.isMouseGhosting()) {
            return new MonitorSnapshot(-1, -1, MouseInfo.getPointerInfo().getLocation(), false, MonitorStatus.STOPPED, TipoAtividadeHumana.NENHUM);
        }
        return atomicMonitorSnapshot.get();
    }

    @Override
    public void onMouseMove(MonitorSnapshot snapshot) {
        if (atomicMonitorSnapshot == null) {
            atomicMonitorSnapshot = new AtomicReference<>(snapshot);
            return;
        }
        atomicMonitorSnapshot.set(snapshot);
    }

}
