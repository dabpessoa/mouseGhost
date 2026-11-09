package me.dabpessoa.mouseghost.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.model.MonitorListener;
import me.dabpessoa.mouseghost.model.MonitorSnapshot;
import me.dabpessoa.mouseghost.model.MonitorStatus;
import me.dabpessoa.mouseghost.model.MouseMoveDto;

import java.awt.MouseInfo;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MouseMoveRunnable implements Runnable {

    private static final Integer SLEEP_TIME_FOR_THREAD_IDLE_CHECK_IN_MILLIS = 1500;

    private final AtomicBoolean running;
    private final AtomicBoolean keyboardActivityDetected;

    private volatile Thread workerThread;

    private final MouseMonitor mouseMonitor;
    private final Random random;

    @Setter
    private MonitorListener monitorListener;

    @Getter
    private final MouseMoveDto mouseMoveDto;

    public MouseMoveRunnable(MouseMonitor mouseMonitor) {
        this.mouseMonitor = mouseMonitor;
        this.random = new Random();

        this.running = new AtomicBoolean(false);
        this.keyboardActivityDetected = new AtomicBoolean(false);

        mouseMoveDto = new MouseMoveDto(true);
    }

    @Override
    public void run() {
        workerThread = Thread.currentThread();
        running.set(true);

        try {
            while (isRunning() && !Thread.currentThread().isInterrupted()) {
                /*
                 * Verifica atividade do teclado antes de qualquer decisão
                 * sobre o próximo movimento.
                 */
                processKeyboardActivity();

                // Captura a posição real atual do mouse na tela
                mouseMoveDto.setPosicaoAtualReal(MouseInfo.getPointerInfo().getLocation());

                criarSnapshotEAtualizaListeners(mouseMoveDto);

                // Verificação de movimento físico do mouse.
                if (!mouseMoveDto.getPosicaoAtualReal().equals(mouseMoveDto.getUltimaPosicaoSalva())) {
                    if (mouseMoveDto.isModoBotAtivo()) {
                        log.info("-> Movimento humano detectado! Interrompendo o bot e reiniciando cronômetro.");
                        log.info("");

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
                        log.info("-> Computador ocioso por {}s. Iniciando movimentos orgânicos.", mouseMoveDto.getTempoDeInatividade());
                        mouseMoveDto.setModoBotAtivo(true);
                    }

                    // Define um destino aleatório na tela
                    int destinoX = random.nextInt(mouseMoveDto.getScreenSize().width - 50) + 25;
                    int destinoY = random.nextInt(mouseMoveDto.getScreenSize().height - 50) + 25;

                    // Tenta mover o mouse. Se o usuário mexer durante o trajeto, a função aborta.
                    boolean concluiuMovimento = mouseMonitor.fazerMovimentoDeMouseSimulandoMovimentoHumano(mouseMoveDto.getPosicaoAtualReal().x, mouseMoveDto.getPosicaoAtualReal().y, destinoX, destinoY );

                    if (concluiuMovimento) {
                        // Atualiza a última posição para onde o robô deixou o mouse
                        mouseMoveDto.setUltimaPosicaoSalva(MouseInfo.getPointerInfo().getLocation());

                        // Pausa orgânica original: entre 1.5 e 3.5 segundos.
                        int pausa = 1500 + random.nextInt(2000);

                        for (int i = 0; i < pausa / 50; i++) {
                            /*
                             * Verifica o teclado antes de cada intervalo
                             * da pausa orgânica.
                             */
                            if (processKeyboardActivity()) {
                                break;
                            }

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException exception) {
                                if (processKeyboardActivity()) {
                                    break;
                                }
                                throw exception;
                            }

                            // Verificação de movimento humano.
                            if (!MouseInfo.getPointerInfo().getLocation().equals(mouseMoveDto.getUltimaPosicaoSalva())) {
                                break;
                            }
                        }

                    } else {
                        /*
                         * O movimento foi interrompido pelo usuário.
                         * Reinicia o cronômetro e atualiza a posição.
                         *
                         * Este comportamento também atende à interrupção
                         * causada pelo teclado.
                         */
                        mouseMoveDto.setTempoUltimoMovimentoHumano(System.currentTimeMillis());
                        mouseMoveDto.setUltimaPosicaoSalva(MouseInfo.getPointerInfo().getLocation());
                        mouseMoveDto.setModoBotAtivo(false);
                    }
                } else {
                    try {
                        Thread.sleep(SLEEP_TIME_FOR_THREAD_IDLE_CHECK_IN_MILLIS);
                    } catch (InterruptedException exception) {
                        /*
                         * Se a interrupção veio do teclado, processa a
                         * atividade e continua o monitoramento.
                         *
                         * Se veio do stop(), encerra normalmente.
                         */
                        if (!processKeyboardActivity()) {
                            throw exception;
                        }
                    }
                }
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
            workerThread = null;
        }
    }

    private void criarSnapshotEAtualizaListeners(MouseMoveDto mouseMoveDto) {
        if (monitorListener == null) {
            return;
        }

        long tempoParado = System.currentTimeMillis() - mouseMoveDto.getTempoUltimoMovimentoHumano();

        MonitorStatus status;
        if (isRunning() && mouseMoveDto.isModoBotAtivo()) {
            status = MonitorStatus.MOVING;
        } else if (isRunning()) {
            status = MonitorStatus.WAITING;
        } else {
            status = MonitorStatus.STOPPED;
        }

        MonitorSnapshot snapshot = new MonitorSnapshot(
                tempoParado,
                mouseMoveDto.getTempoUltimoMovimentoHumano(),
                mouseMoveDto.getPosicaoAtualReal(),
                mouseMoveDto.isModoBotAtivo(),
                status
        );

        monitorListener.onMouseMove(snapshot);
    }

    public void stop() {
        running.set(false);

        Thread thread = workerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    /**
     * Chamado pelo listener de teclado.
     *
     * Apenas sinaliza a atividade e acorda o thread responsável pelo mouse.
     */
    public void notifyKeyboardActivity() {
        keyboardActivityDetected.set(true);
        Thread thread = workerThread;

        if (thread != null && running.get()) {
            thread.interrupt();
        }
    }

    /**
     * Consome a sinalização de atividade do teclado.
     */
    public boolean consumeKeyboardActivity() {
        return keyboardActivityDetected.getAndSet(false);
    }

    /**
     * Processa a atividade detectada e reinicia o cronômetro.
     */
    private boolean processKeyboardActivity() {
        if (!consumeKeyboardActivity()) {
            return false;
        }

        log.info("-> Atividade de teclado detectada! Interrompendo o bot e reiniciando cronômetro.");

        mouseMoveDto.setTempoUltimoMovimentoHumano(System.currentTimeMillis());
        mouseMoveDto.setUltimaPosicaoSalva(MouseInfo.getPointerInfo().getLocation());
        mouseMoveDto.setPosicaoAtualReal(mouseMoveDto.getUltimaPosicaoSalva());
        mouseMoveDto.setModoBotAtivo(false);

        return true;
    }

}