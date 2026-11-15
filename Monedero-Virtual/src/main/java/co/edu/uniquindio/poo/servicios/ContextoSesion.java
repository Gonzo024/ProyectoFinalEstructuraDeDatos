
package co.edu.uniquindio.poo.servicios;

/**
 * Guarda el contexto compartido de la aplicacion.
 *
 * Los modulos obtienen los servicios desde aqui
 * para trabajar con los mismos clientes, monederos,
 * transacciones y reloj.
 *
 * Los datos se mantienen mientras la aplicacion
 * esta abierta. No es almacenamiento permanente.
 */
public final class ContextoSesion {

    private static final ContextoMonedero CONTEXTO =
            new ContextoMonedero();

    private ContextoSesion() {
        // Evita crear objetos de esta clase.
    }

    public static ContextoMonedero obtener() {
        return CONTEXTO;
    }
}
