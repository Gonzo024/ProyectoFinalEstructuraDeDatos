
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
            crearContexto();

    private ContextoSesion() {
        // Evita crear objetos de esta clase.
    }

    public static ContextoMonedero obtener() {
        return CONTEXTO;
    }

    // Crea el contexto de la aplicacion y le carga los datos de demostracion
    // para la sustentacion. Las pruebas no usan esta clase, asi que no se afectan.
    private static ContextoMonedero crearContexto() {
        ContextoMonedero contexto = new ContextoMonedero();
        try {
            DatosDemostracion.cargar(contexto);
        } catch (RuntimeException e) {
            // Si algo falla, la aplicacion abre igual (sin datos o con parte de ellos).
            System.err.println("No se pudieron cargar los datos de demostracion: "
                    + e.getMessage());
        }
        return contexto;
    }
}
