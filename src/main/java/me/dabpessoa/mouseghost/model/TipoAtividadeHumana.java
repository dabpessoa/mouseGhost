package me.dabpessoa.mouseghost.model;

public enum TipoAtividadeHumana {
    NENHUM,
    MOUSE,
    TECLADO,
    ;

    public boolean houveAtividadeHumana() {
        return this != NENHUM;
    }

}
