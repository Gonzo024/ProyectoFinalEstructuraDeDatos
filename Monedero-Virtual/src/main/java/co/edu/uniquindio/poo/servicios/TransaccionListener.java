package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.Transaccion;

/**
 * Patrón Observador: los servicios interesados (puntos, notificaciones, análisis,
 * multimonedero) se suscriben a {@link TransaccionService} y reaccionan a cada movimiento
 * sin que TransaccionService tenga que conocerlos. Esto mantiene bajo el acoplamiento.
 */
public interface TransaccionListener {

    /** Se llama después de registrar una transacción exitosa. */
    void onTransaccionCompletada(Transaccion t);

    /** Se llama después de revertir una transacción. */
    void onTransaccionRevertida(Transaccion t);

    /**
     * Se llama ANTES de revertir. Un listener puede vetar la reversión lanzando una
     * excepción (p. ej. si los puntos de esa transacción ya se canjearon).
     */
    default void validarReversion(Transaccion t) {
    }
}

