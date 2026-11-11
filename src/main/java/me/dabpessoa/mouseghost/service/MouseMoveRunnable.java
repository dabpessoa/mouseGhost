package me.dabpessoa.mouseghost.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.dabpessoa.mouseghost.model.MonitorListener;
import me.dabpessoa.mouseghost.model.MouseMoveDto;
import me.dabpessoa.mouseghost.model.MonitorStatus;
import me.dabpessoa.mouseghost.model.TipoAtividadeHumana;
import me.dabpessoa.mouseghost.model.MonitorSnapshot;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MouseMoveRunnable implements Runnable {

    private static final Integer SLEEP_TIME_FOR_THREAD_IDLE_CHECK_IN_MILLIS = 1500;
    private static final int BASE_SLEEP_TIME_BETWEEN_MOVES_IN_STEP_IN_MILLIS = 6;
    private static final int BASE_MOVES_TIMES_FOR_STEP = 60;

    private final AtomicBoolean running;
    private final AtomicBoolean keyboardActivityDetected;

    private volatile Thread workerThread;
    private final Random random;
    private final Robot robot;

    @Setter
    private MonitorListener monitorListener;

    @Getter
    private final MouseMoveDto mouseMoveDto;

    public MouseMoveRunnable() throws AWTException {
        this.random = new Random();
        this.robot = new Robot();

        this.running = new AtomicBoolean(false);
        this.keyboardActivityDetected = new AtomicBoolean(false);

        mouseMoveDto = new MouseMoveDto(true);
    }

    @Override
    public void run() {
        workerThread = Thread.currentThread();
        running.set(true);

        try {
            while (isRunning() && workerThread != null && !workerThread.isInterrupted()) {
                mouseMoveDto.setPosicaoAtualReal(MouseInfo.getPointerInfo().getLocation());

                TipoAtividadeHumana tipoAtividadeHumana = verificaEProcessaAtividadeHumana();
                // Após verificação de atividade humana, setar o controlador de atividade do teclado para "false".
                keyboardActivityDetected.set(false);

                criarSnapshotEAtualizaListeners(mouseMoveDto, tipoAtividadeHumana);

                if (!tipoAtividadeHumana.houveAtividadeHumana()) {
                    verificaMovimentoOrganicoDoMouse();
                }
            }
        } catch (InterruptedException exception) {
            if (workerThread != null) {
                workerThread.interrupt();
            }
        } finally {
            running.set(false);
            workerThread = null;
        }
    }

    private void verificaMovimentoOrganicoDoMouse() throws InterruptedException {
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
            boolean concluiuMovimento = fazerMovimentoDeMouseSimulandoMovimentoHumano(mouseMoveDto.getPosicaoAtualReal().x, mouseMoveDto.getPosicaoAtualReal().y, destinoX, destinoY );

            if (concluiuMovimento) {
                // Atualiza a última posição para onde o robô deixou o mouse
                mouseMoveDto.setUltimaPosicaoSalva(MouseInfo.getPointerInfo().getLocation());

                // Pequena pausa orgânica entre um destino e outro (1.5 a 3.5 segundos)
                int pausa = 1500 + random.nextInt(2000);
                for (int i = 0; i < pausa / 50; i++) {
                    // Se houver atividade humana durante a pausa, quebra o ciclo imediatamente.
                    TipoAtividadeHumana tipoAtividadeHumana = verificaAtividadeHumana();
                    if (tipoAtividadeHumana.houveAtividadeHumana()) {
                        break;
                    }

                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
                }
            } else {
                // Se o movimento foi interrompido, reseta os tempos para o ciclo humano voltar
                mouseMoveDto.setTempoUltimoMovimentoHumano(System.currentTimeMillis());
                mouseMoveDto.setUltimaPosicaoSalva(MouseInfo.getPointerInfo().getLocation());
            }
        } else {
            // Se ainda não deu o tempo de ociosidade, apenas aguarda um curto período antes de checar de novo
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(SLEEP_TIME_FOR_THREAD_IDLE_CHECK_IN_MILLIS));
        }
    }

    /**
     * Move o mouse de forma orgânica.
     *
     * Retorna true se concluiu o movimento.
     * Retorna false se houve intervenção humana.
     */
    private boolean fazerMovimentoDeMouseSimulandoMovimentoHumano(int x1, int y1, int x2, int y2) throws InterruptedException {
        int ctrlX1 = x1 + (x2 - x1) / 4 + (random.nextInt(160) - 80);
        int ctrlY1 = y1 + (y2 - y1) / 4 + (random.nextInt(160) - 80);
        int ctrlX2 = x1 + 3 * (x2 - x1) / 4 + (random.nextInt(160) - 80);
        int ctrlY2 = y1 + 3 * (y2 - y1) / 4 + (random.nextInt(160) - 80);

        int passos = BASE_MOVES_TIMES_FOR_STEP + random.nextInt(30);
        int ultimoXColocado = x1;
        int ultimoYColocado = y1;

        for (int i = 0; i <= passos; i++) {
            // Verificações de interrupção.
            boolean moveuMouse = i > 0 &&  houveAtividadeNoMouse(new Point(ultimoXColocado, ultimoYColocado));
            boolean atividadeNoTeclado = houveAtividadeNoTeclado();
            if (moveuMouse || atividadeNoTeclado) {
                return false; // Interrompe imediatamente, pois o humano tomou o controle.
            }

            double tLinear = (double) i / passos;
            double tHumano = tLinear * tLinear * (3.0 - 2.0 * tLinear); // Smoothstep

            double temp = 1.0 - tHumano;
            int x = (int) (Math.pow(temp, 3) * x1 + 3 * Math.pow(temp, 2) * tHumano * ctrlX1 + 3 * temp * Math.pow(tHumano, 2) * ctrlX2 + Math.pow(tHumano, 3) * x2);
            int y = (int) (Math.pow(temp, 3) * y1 + 3 * Math.pow(temp, 2) * tHumano * ctrlY1 + 3 * temp * Math.pow(tHumano, 2) * ctrlY2 + Math.pow(tHumano, 3) * y2);

            robot.mouseMove(x, y);

            // Salva onde o robô acabou de colocar o cursor para fins de comparação no próximo passo
            ultimoXColocado = x;
            ultimoYColocado = y;

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(BASE_SLEEP_TIME_BETWEEN_MOVES_IN_STEP_IN_MILLIS + random.nextInt(5)));
        }

        return true;
    }

    private TipoAtividadeHumana verificaEProcessaAtividadeHumana() {
        TipoAtividadeHumana tipoAtividadeHumana = verificaAtividadeHumana();
        if (tipoAtividadeHumana.houveAtividadeHumana()) {
            if (tipoAtividadeHumana == TipoAtividadeHumana.TECLADO) {
                if (mouseMoveDto.isModoBotAtivo()) {
                    log.info("-> Ação no TECLADO detectada!");
                }
            } else if (tipoAtividadeHumana == TipoAtividadeHumana.MOUSE) {
                if (mouseMoveDto.isModoBotAtivo()) {
                    log.info("-> Ação no MOUSE detectada!");
                }
            }

            if (mouseMoveDto.isModoBotAtivo()) {
                log.info("Interrompendo o bot e reiniciando cronômetro.");
                log.info("");
                mouseMoveDto.setModoBotAtivo(false);
            }

            // Atualiza o marcador de tempo para agora
            mouseMoveDto.setTempoUltimoMovimentoHumano(System.currentTimeMillis());
            mouseMoveDto.setUltimaPosicaoSalva(mouseMoveDto.getPosicaoAtualReal());
        }

        return tipoAtividadeHumana;
    }

    private TipoAtividadeHumana verificaAtividadeHumana() {
        boolean atividadeNoTeclado = houveAtividadeNoTeclado();
        boolean atividadeNoMouse = houveAtividadeNoMouse(mouseMoveDto.getUltimaPosicaoSalva());

        TipoAtividadeHumana tipoAtividadeHumana;

        if (atividadeNoTeclado) {
            tipoAtividadeHumana = TipoAtividadeHumana.TECLADO;
        } else if (atividadeNoMouse) {
            tipoAtividadeHumana = TipoAtividadeHumana.MOUSE;
        } else {
            tipoAtividadeHumana = TipoAtividadeHumana.NENHUM;
        }

        return tipoAtividadeHumana;
    }

    private void criarSnapshotEAtualizaListeners(MouseMoveDto mouseMoveDto, TipoAtividadeHumana tipoAtividadeHumana) {
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
                mouseMoveDto.getTempoDeInatividade(),
                mouseMoveDto.getPosicaoAtualReal(),
                mouseMoveDto.isModoBotAtivo(),
                status,
                tipoAtividadeHumana
        );

        monitorListener.houveAtividadeHumana(snapshot);
    }

    private boolean houveAtividadeNoTeclado() {
        return keyboardActivityDetected.get();
    }

    private boolean houveAtividadeNoMouse(Point pontoDeComparacao) {
        return !MouseInfo.getPointerInfo().getLocation().equals(pontoDeComparacao);
    }

    public void notificarAtividadeNoTeclado() {
        keyboardActivityDetected.set(true);

        Thread thread = workerThread;
        if (thread != null) {
            LockSupport.unpark(thread);
        }
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

}