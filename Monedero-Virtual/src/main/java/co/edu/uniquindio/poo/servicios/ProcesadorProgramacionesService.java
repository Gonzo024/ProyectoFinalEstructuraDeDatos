
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;

public class ProcesadorProgramacionesService {

    // Representa la operacion que ejecutara el dinero.
    @FunctionalInterface
    public interface EjecutorOperacion {
        boolean ejecutar(TransaccionProgramada programacion);
    }

    private final ProgramacionService programaciones;
    private final NotificacionService notificaciones;
    private final EjecutorOperacion ejecutor;

    public ProcesadorProgramacionesService(
            ProgramacionService programaciones,
            NotificacionService notificaciones,
            EjecutorOperacion ejecutor) {

        if (programaciones == null
                || notificaciones == null
                || ejecutor == null) {

            throw new IllegalArgumentException(
                    "Los servicios y el ejecutor son obligatorios"
            );
        }

        this.programaciones = programaciones;
        this.notificaciones = notificaciones;
        this.ejecutor = ejecutor;
    }

    // Procesa todas las operaciones que ya llegaron a su fecha.
    // Retorna la cantidad de operaciones procesadas.
    public int procesarVencidas() {

        int cantidadProcesada = 0;

        // La cola de prioridad mantiene primero la fecha mas cercana.
        while (programaciones.hayTransaccionesVencidas()) {

            TransaccionProgramada actual =
                    programaciones.retirarSiguienteVencida();

            if (actual == null) {
                break;
            }

            cantidadProcesada++;

            boolean ejecucionExitosa = false;

            try {

                // El ejecutor indica si la operacion funciono.
                ejecucionExitosa = ejecutor.ejecutar(actual);

            } catch (RuntimeException error) {

                // Si ocurre un error, no declaramos
                // que la operacion se ejecuto correctamente.
                ejecucionExitosa = false;
            }

            if (ejecucionExitosa) {

                procesarExito(actual);

            } else {

                procesarFallo(actual);
            }
        }

        // Pasa los avisos generados a la lista circular.
        notificaciones.entregarNotificaciones();

        return cantidadProcesada;
    }

    private void procesarExito(
            TransaccionProgramada programacion) {

        String mensaje = "La programacion "
                + programacion.getId()
                + " fue ejecutada correctamente";

        notificaciones.crearNotificacion(
                programacion.getClienteOrigenId(),
                TipoNotificacion.PROGRAMADA_EJECUTADA,
                mensaje
        );

        // Para UNA_VEZ, el modelo la desactiva.
        // Para DIARIA, SEMANAL o MENSUAL, calcula
        // la siguiente fecha y vuelve a introducirla
        // en la cola de prioridad.
        programaciones.reprogramar(programacion);
    }

    private void procesarFallo(
            TransaccionProgramada programacion) {

        // No volvemos a ejecutar automaticamente
        // una operacion que fallo.
        programacion.cancelar();

        String mensaje = "No se pudo ejecutar la programacion "
                + programacion.getId()
                + ". La operacion quedo detenida";

        notificaciones.crearNotificacion(
                programacion.getClienteOrigenId(),
                TipoNotificacion.PROGRAMADA_FALLIDA,
                mensaje
        );
    }
}

