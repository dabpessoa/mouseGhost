package me.dabpessoa.mouseghost.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MouseService {

    // Altere este valor para definir quantos segundos o mouse deve ficar parado antes de iniciar.
    private static final long TEMPO_DE_INATIVIDADE = 5;

    private final MouseMonitor mouseMonitor;

    public void start() {
        if (!mouseMonitor.isMouseGhosting()) {
            log.info("----------======== Mouse Ghosting ========----------");
            log.info("[+] Monitor de ociosidade iniciado com sucesso.");
            log.info("[+] O mouse começará a se mover após " + TEMPO_DE_INATIVIDADE + " segundos parado.");
            log.info("[!] Para encerrar o monitoramento, clique no botão 'Parar', ou feche a janela principal.");
            log.info("----------======== ************** ========----------");
            log.info("");
        }

        mouseMonitor.startMonitor(TEMPO_DE_INATIVIDADE);
    }

    public void stop() {
        mouseMonitor.stopMonitor();
    }

}
