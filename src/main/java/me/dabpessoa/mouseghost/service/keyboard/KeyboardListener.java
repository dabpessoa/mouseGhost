package me.dabpessoa.mouseghost.service.keyboard;

/**
 * Contrato para captura de eventos de teclado.
 */
public interface KeyboardListener {

    /**
     * Adicionar um listener para evento de teclado.
     *
     * @param listener callback executado quando uma tecla é pressionada.
     */
    void addListener(Runnable listener);

    boolean removeListener(Runnable listener);

    boolean hasAnyListener();

}
