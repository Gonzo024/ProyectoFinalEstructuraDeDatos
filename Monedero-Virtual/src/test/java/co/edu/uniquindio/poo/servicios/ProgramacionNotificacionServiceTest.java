
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ProgramacionNotificacionServiceTest {

    @Test
    void programarGeneraNotificacion() {

        RelojSimulado reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0));

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService servicio =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones);

        TransaccionProgramada transaccion =
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 20, 10, 0));

        servicio.programarYNotificar(transaccion);

        assertEquals(1, programaciones.cantidadPendientes());

        assertEquals(
                1,
                notificaciones.consultarNotificaciones("C1")
                        .getTamano());

        assertEquals(
                1,
                notificaciones.contarNoLeidas("C1"));

        String mensaje = notificaciones
                .consultarNotificaciones("C1")
                .obtenerValorNodo(0)
                .getMensaje();

        assertTrue(mensaje.contains("P1"));
    }

    @Test
    void cancelarGeneraOtraNotificacion() {

        RelojSimulado reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0));

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService servicio =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones);

        servicio.programarYNotificar(
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 20, 10, 0)));

        boolean cancelada = servicio.cancelarYNotificar("P1");

        assertTrue(cancelada);

        assertEquals(
                0,
                programaciones.cantidadPendientes());

        assertEquals(
                2,
                notificaciones.consultarNotificaciones("C1")
                        .getTamano());

        assertEquals(
                2,
                notificaciones.contarNoLeidas("C1"));

        // No se debe cancelar ni notificar dos veces.
        assertFalse(servicio.cancelarYNotificar("P1"));

        assertEquals(
                2,
                notificaciones.consultarNotificaciones("C1")
                        .getTamano());
    }

    @Test
    void programacionInvalidaNoGeneraAvisos() {

        RelojSimulado reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0));

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService servicio =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones);

        TransaccionProgramada vencida =
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 13, 10, 0));

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.programarYNotificar(vencida));

        assertEquals(0, programaciones.cantidadPendientes());

        assertEquals(
                0,
                notificaciones.consultarNotificaciones("C1")
                        .getTamano());
    }

    @Test
    void noPermiteProgramacionesConIdRepetido() {

        RelojSimulado reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0));

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService servicio =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones);

        TransaccionProgramada primera =
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 20, 10, 0));

        TransaccionProgramada repetida =
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 21, 10, 0));

        servicio.programarYNotificar(primera);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.programarYNotificar(repetida));

        assertEquals(1, programaciones.cantidadPendientes());
        assertEquals(
                1,
                notificaciones.contarNoLeidas("C1"));
    }

    // Crea una operacion para reutilizar en las pruebas.
    private TransaccionProgramada crearProgramacion(
            String id,
            LocalDateTime fecha) {

        return new TransaccionProgramada(
                id,
                TipoTransaccion.PAGO,
                "C1",
                "M1",
                null,
                null,
                Dinero.de(50000),
                fecha,
                "UNA_VEZ"
        );
    }
}
