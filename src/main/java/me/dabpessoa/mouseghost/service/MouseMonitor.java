package me.dabpessoa.mouseghost.service;

import org.springframework.stereotype.Service;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;

import java.util.Random;

@Service
public class MouseMonitor {

    private static final Integer BASE_SLEEP_TIME_BETWEEN_MOVES_IN_STEP_IN_MILLIS = 6;
    private static final Integer BASE_MOVES_TIMES_FOR_STEP = 60;

    private final Random random;
    private final Robot robot;
    private final MouseMoveRunnable mouseMoveRunnable;

    public MouseMonitor() throws AWTException {
        random = new Random();
        robot = new Robot();
        mouseMoveRunnable = new MouseMoveRunnable(this);
    }

    public void startMonitor(long tempoDeInatividade) {
        if (isMouseGhosting()) {
            return;
        }

        if (tempoDeInatividade != 0) {
            mouseMoveRunnable.getMouseMoveDto().setTempoDeInatividade(tempoDeInatividade);
        }

        Thread thread = new Thread(mouseMoveRunnable);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopMonitor() {
        this.mouseMoveRunnable.stop();
    }

    public boolean isMouseGhosting() {
        return mouseMoveRunnable != null && mouseMoveRunnable.isRunning();
    }

    /**
     * Move o mouse de forma humana, mas monitora a cada milissegundo se o usuário tentou mover o cursor.
     * Retorna 'true' se concluiu o trajeto com sucesso ou 'false' se foi interrompido pelo usuário.
     */
    public boolean fazerMovimentoDeMouseSimulandoMovimentoHumano(int x1, int y1, int x2, int y2) throws InterruptedException {
        int ctrlX1 = x1 + (x2 - x1) / 4 + (random.nextInt(160) - 80);
        int ctrlY1 = y1 + (y2 - y1) / 4 + (random.nextInt(160) - 80);
        int ctrlX2 = x1 + 3 * (x2 - x1) / 4 + (random.nextInt(160) - 80);
        int ctrlY2 = y1 + 3 * (y2 - y1) / 4 + (random.nextInt(160) - 80);

        int passos = BASE_MOVES_TIMES_FOR_STEP + random.nextInt(30);
        int ultimoXColocado = x1;
        int ultimoYColocado = y1;

        for (int i = 0; i <= passos; i++) {
            Point posicaoFisicaAgora = MouseInfo.getPointerInfo().getLocation();

            // VERIFICAÇÃO DE INTERRUPÇÃO: Se a posição física do mouse for diferente da
            // última coordenada que o NOSSO ROBÔ definiu, significa que a mão do usuário moveu o mouse.
            if (i > 0 && (posicaoFisicaAgora.x != ultimoXColocado || posicaoFisicaAgora.y != ultimoYColocado)) {
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

            Thread.sleep(BASE_SLEEP_TIME_BETWEEN_MOVES_IN_STEP_IN_MILLIS + random.nextInt(5));
        }

        return true;
    }

}
