package me.dabpessoa.mouseghost.service;

/**
 * Contrato para captura global de eventos de teclado.
 *
 * A aplicação conhece somente esta interface.
 * A implementação concreta fica isolada no adaptador JNativeHook.
 */
public interface GlobalKeyboardHook {

    /**
     * Registra a captura global do teclado.
     *
     * @param onKeyPressed callback executado quando uma tecla é pressionada.
     */
    void register(Runnable onKeyPressed);

    /**
     * Remove o listener e encerra a captura quando ela pertence
     * a esta implementação.
     */
    void unregister();

}
