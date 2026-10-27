package me.dabpessoa.mouseghost.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.model.MouseMoveDto;

import java.awt.MouseInfo;
import java.util.Random;

@Slf4j
public class MouseMoveRunnable implements Runnable {

    private static final Integer SLEEP_TIME_FOR_THREAD_IDLE_CHECK_IN_MILLIS = 1500;

    @Getter @Setter
    private volatile boolean running;

    private final MouseMonitor mouseMonitor;
    private final Random random;

    @Getter
    private final MouseMoveDto mouseMoveDto;

    public MouseMoveRunnable(MouseMonitor mouseMonitor) {
        this.mouseMonitor = mouseMonitor;
        this.random = new Random();
        mouseMoveDto = new MouseMoveDto(true);
    }

    @Override
    public void run() {
        this.running = true;

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Captura a posição real atual do mouse na tela
                mouseMoveDto.setPosicaoAtualReal(MouseInfo.getPointerInfo().getLocation());

                // Se a posição mudou, o usuário mexeu o mouse fisicamente
                if (!mouseMoveDto.getPosicaoAtualReal().equals(mouseMoveDto.getUltimaPosicaoSalva())) {
                    if (mouseMoveDto.isModoBotAtivo()) {
                        log.info("\n[!] Movimento humano detectado! Interrompendo o bot e reiniciando cronômetro.");
                        mouseMoveDto.setModoBotAtivo(false);
                    }
                    // Atualiza o marcador de tempo para agora
                    mouseMoveDto.setTempoUltimoMovimentoHumano(System.currentTimeMillis());
                    mouseMoveDto.setUltimaPosicaoSalva(mouseMoveDto.getPosicaoAtualReal());
                }

                long tempoParado = System.currentTimeMillis() - mouseMoveDto.getTempoUltimoMovimentoHumano();
                long limiteMilissegundos = mouseMoveDto.getTempoDeInatividade() * 1000L;

                // Se o tempo parado exceder o limite, o robô assume o controle.
                if (tempoParado >= limiteMilissegundos) {
                    if (!mouseMoveDto.isModoBotAtivo()) {
                        log.info("\n[->] Computador ocioso por {}s. Iniciando movimentos orgânicos...", mouseMoveDto.getTempoDeInatividade());
                        mouseMoveDto.setModoBotAtivo(true);
                    }

                    // Define um destino aleatório na tela
                    int destinoX = random.nextInt(mouseMoveDto.getScreenSize().width - 50) + 25;
                    int destinoY = random.nextInt(mouseMoveDto.getScreenSize().height - 50) + 25;

                    // Tenta mover o mouse. Se o usuário mexer durante o trajeto, a função aborta.
                    boolean concluiuMovimento = mouseMonitor.fazerMovimentoDeMouseSimulandoMovimentoHumano(mouseMoveDto.getPosicaoAtualReal().x, mouseMoveDto.getPosicaoAtualReal().y, destinoX, destinoY);

                    if (concluiuMovimento) {
                        // Atualiza a última posição para onde o robô deixou o mouse
                        mouseMoveDto.setUltimaPosicaoSalva(MouseInfo.getPointerInfo().getLocation());

                        // Pequena pausa orgânica entre um destino e outro (1.5 a 3.5 segundos)
                        int pausa = 1500 + random.nextInt(2000);
                        for (int i = 0; i < pausa / 50; i++) {
                            Thread.sleep(50);
                            // Se o usuário mexer durante a pausa, quebra o ciclo imediatamente
                            if (!MouseInfo.getPointerInfo().getLocation().equals(mouseMoveDto.getUltimaPosicaoSalva())) {
                                break;
                            }
                        }
                    } else {
                        // Se o movimento foi interrompido, reseta os tempos para o ciclo humano voltar
                        mouseMoveDto.setTempoUltimoMovimentoHumano(System.currentTimeMillis());
                        mouseMoveDto.setUltimaPosicaoSalva(MouseInfo.getPointerInfo().getLocation());
                        mouseMoveDto.setModoBotAtivo(false);
                    }
                } else {
                    // Se ainda não deu o tempo de ociosidade, apenas aguarda um curto período antes de checar de novo
                    Thread.sleep(SLEEP_TIME_FOR_THREAD_IDLE_CHECK_IN_MILLIS);
                }

            } catch (InterruptedException e) {
                // É fundamental restaurar o status de interrupção
                Thread.currentThread().interrupt();
                break; // Encerra o loop de forma segura
            }
        }
    }

    public void stop() {
        setRunning(false);
        Thread.currentThread().interrupt();
    }

}
