
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;

import java.time.format.DateTimeFormatter;

public class ProgramacionNotificacionService {

    private final ProgramacionService programacionService;
    private final NotificacionService notificacionService;

    private final ListaEnlazada<TransaccionProgramada> registradas =
            new ListaEnlazada<>();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ProgramacionNotificacionService(
            ProgramacionService programacionService,
            NotificacionService notificacionService) {

        if (programacionService == null
                || notificacionService == null) {

            throw new IllegalArgumentException(
                    "Los servicios son obligatorios");
        }

        this.programacionService = programacionService;
        this.notificacionService = notificacionService;
    }

    // Registra una programacion y avisa al cliente.
    public void programarYNotificar(
            TransaccionProgramada transaccion) {

        if (transaccion == null) {
            throw new IllegalArgumentException(
                    "La programacion es obligatoria");
        }

        if (transaccion.getId() == null
                || transaccion.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "La programacion debe tener un ID");
        }

        if (transaccion.getClienteOrigenId() == null
                || transaccion.getClienteOrigenId().isBlank()) {

            throw new IllegalArgumentException(
                    "La programacion debe tener un cliente");
        }

        // Evita registrar dos veces el mismo identificador.
        if (buscarProgramacion(transaccion.getId()) != null) {

            throw new IllegalArgumentException(
                    "Ya existe una programacion con ese ID");
        }

        // Primero registramos en la cola de prioridad.
        // Si falla, no generamos la notificacion.
        programacionService.programar(transaccion);

        registradas.agregarFinal(transaccion);

        String mensaje = "Se registro la programacion "
                + transaccion.getId()
                + " para el "
                + transaccion.getFechaProgramada()
                        .format(formatoFecha);

        notificacionService.crearNotificacion(
                transaccion.getClienteOrigenId(),
                TipoNotificacion.TRANSACCION,
                mensaje
        );

        notificacionService.entregarNotificaciones();
    }

    // Cancela una programacion y avisa al cliente.
    public boolean cancelarYNotificar(String id) {

        if (id == null || id.isBlank()) {
            return false;
        }

        TransaccionProgramada transaccion =
                buscarProgramacion(id);

        if (transaccion == null || !transaccion.isActiva()) {
            return false;
        }

        boolean cancelada =
                programacionService.cancelarTransaccion(id);

        if (!cancelada) {
            return false;
        }

        String mensaje = "Se cancelo la programacion " + id;

        notificacionService.crearNotificacion(
                transaccion.getClienteOrigenId(),
                TipoNotificacion.TRANSACCION,
                mensaje
        );

        notificacionService.entregarNotificaciones();

        return true;
    }

    // Busca una programacion registrada por este servicio.
    private TransaccionProgramada buscarProgramacion(String id) {

        for (TransaccionProgramada transaccion : registradas) {

            if (transaccion.getId().equals(id)) {
                return transaccion;
            }
        }

        return null;
    }
}
