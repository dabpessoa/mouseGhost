package me.dabpessoa.mouseghost.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MouseService {
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

        long inatividadeLong = Long.parseLong(tempoDeInatividade);
        mouseMonitor.startMonitor(inatividadeLong);
    }

    public void stop() {
        mouseMonitor.stopMonitor();
    }

}
