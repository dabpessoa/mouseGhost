package me.dabpessoa.mouseghost.model;

import lombok.Getter;

/** Estado operacional apresentado na tela principal e na bandeja do sistema. */
@Getter
public enum MonitorStatus {
    STOPPED("Parado", "Clique em Iniciar para começar.", "O monitor está desligado", "status-dot-stopped"),
    WAITING("Aguardando", "Monitor ativo e observando sua atividade.", "", "status-dot-waiting"),
    MOVING("Movimentando", "O cursor está sendo movido suavemente.", "Mova o mouse para retomar o controle", "status-dot-moving")
    ;

    private final String titulo;
    private final String detalhe;
    private final String proximaAcao;
    private final String cssStyleClassName;

    MonitorStatus(String titulo, String detalhe, String proximaAcao, String cssStyleClassName) {
        this.titulo = titulo;
        this.detalhe = detalhe;
        this.proximaAcao = proximaAcao;
        this.cssStyleClassName = cssStyleClassName;
    }

    public static String getDefaultCssDotStyle() {
        return "status-dot";
    }

}
