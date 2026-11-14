
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class IntegracionModuloTresTest {

    // Fecha fija para que las pruebas siempre den
    // el mismo resultado, sin importar el dia actual.
    private RelojSimulado crearReloj() {

        return new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0)
        );
    }

    private TransaccionProgramada crearProgramacion(
            String id,
            int dia,
            String frecuencia) {

        return new TransaccionProgramada(
                id,
                TipoTransaccion.PAGO,
                "C1",
                "M1",
                null,
                null,
                Dinero.de(50000),
                LocalDateTime.of(2026, 11, dia, 10, 0),
                frecuencia
        );
    }

    // PRUEBA 1
    // Registrar una programacion debe crear su aviso.
    @Test
    void registrarProgramacionGeneraNotificacion() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService integracion =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones
                );

        TransaccionProgramada pago =
                crearProgramacion("P1", 20, "UNA_VEZ");

        integracion.programarYNotificar(pago);

        assertEquals(
                1,
                programaciones.cantidadPendientes()
        );

        assertEquals(
                1,
                notificaciones.contarNoLeidas("C1")
        );

        String mensaje = notificaciones
                .consultarNotificaciones("C1")
                .obtenerValorNodo(0)
                .getMensaje();

        assertTrue(mensaje.contains("P1"));
    }

    // PRUEBA 2
    // Cancelar debe retirar la programacion y avisar.
    @Test
    void cancelarProgramacionGeneraOtroAviso() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService integracion =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones
                );

        integracion.programarYNotificar(
                crearProgramacion("P1", 20, "UNA_VEZ")
        );

        boolean cancelada =
                integracion.cancelarYNotificar("P1");

        assertTrue(cancelada);

        assertEquals(
                0,
                programaciones.cantidadPendientes()
        );

        assertEquals(
                2,
                notificaciones.contarNoLeidas("C1")
        );

        // No debe generar otro aviso por cancelar
        // una programacion que ya esta cancelada.
        assertFalse(
                integracion.cancelarYNotificar("P1")
        );

        assertEquals(
                2,
                notificaciones.contarNoLeidas("C1")
        );
    }

    // PRUEBA 3
    // Flujo completo: registrar, vencer,
    // intentar ejecutar y notificar el fallo.
    @Test
    void procesarVencimientoGeneraAvisoDeFallo() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService integracion =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones
                );

        // Simulamos que aun no hay un ejecutor financiero.
        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        operacion -> false
                );

        TransaccionProgramada pago =
                crearProgramacion("P1", 15, "UNA_VEZ");

        // Paso 1: registrar.
        integracion.programarYNotificar(pago);

        assertEquals(
                1,
                programaciones.cantidadPendientes()
        );

        // Paso 2: todavia no ha llegado la fecha.
        assertEquals(
                0,
                procesador.procesarVencidas()
        );

        // Paso 3: avanzamos el reloj un dia.
        reloj.avanzarDias(1);

        // Paso 4: procesamos el vencimiento.
        assertEquals(
                1,
                procesador.procesarVencidas()
        );

        // Paso 5: la operacion se detuvo.
        assertFalse(pago.isActiva());

        assertEquals(
                0,
                programaciones.cantidadPendientes()
        );

        // Paso 6: hay dos avisos:
        // registro de P1 y fallo de ejecucion.
        assertEquals(
                2,
                notificaciones.contarNoLeidas("C1")
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_FALLIDA,
                notificaciones.consultarNotificaciones("C1")
                        .obtenerValorNodo(1)
                        .getTipo()
        );

        // Paso 7: procesar otra vez no debe repetir P1.
        assertEquals(
                0,
                procesador.procesarVencidas()
        );

        assertEquals(
                2,
                notificaciones.contarNoLeidas("C1")
        );
    }

    // PRUEBA 4
    // Varias operaciones deben procesarse en orden
    // y generar una notificacion por cada fallo.
    @Test
    void procesarVariasProgramacionesEnOrden() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProgramacionNotificacionService integracion =
                new ProgramacionNotificacionService(
                        programaciones,
                        notificaciones
                );

        StringBuilder orden = new StringBuilder();

        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        operacion -> {
                            orden.append(operacion.getId());
                            return false;
                        }
                );

        // Registramos primero la mas lejana.
        integracion.programarYNotificar(
                crearProgramacion("P2", 18, "UNA_VEZ")
        );

        integracion.programarYNotificar(
                crearProgramacion("P1", 15, "UNA_VEZ")
        );

        // Avanzamos cinco dias.
        reloj.avanzarDias(5);

        assertEquals(
                2,
                procesador.procesarVencidas()
        );

        // La cola debe entregar primero P1,
        // porque vence antes que P2.
        assertEquals(
                "P1P2",
                orden.toString()
        );

        assertEquals(
                0,
                programaciones.cantidadPendientes()
        );

        // Dos avisos al registrar y dos por fallo.
        assertEquals(
                4,
                notificaciones.contarNoLeidas("C1")
        );
    }
}
