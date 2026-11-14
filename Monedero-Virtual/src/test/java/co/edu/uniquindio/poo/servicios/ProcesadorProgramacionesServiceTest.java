
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ProcesadorProgramacionesServiceTest {

    private RelojSimulado crearReloj() {

        return new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0)
        );
    }

    private TransaccionProgramada crearProgramacion(
            String id,
            LocalDateTime fecha,
            String frecuencia) {

        return new TransaccionProgramada(
                id,
                TipoTransaccion.PAGO,
                "C1",
                "M1",
                null,
                null,
                Dinero.de(50000),
                fecha,
                frecuencia
        );
    }

    @Test
    void noEjecutarAntesDeLaFecha() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        AtomicInteger llamadas = new AtomicInteger();

        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        programacion -> {
                            llamadas.incrementAndGet();
                            return true;
                        }
                );

        programaciones.programar(
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 20, 10, 0),
                        "UNA_VEZ"
                )
        );

        int procesadas = procesador.procesarVencidas();

        assertEquals(0, procesadas);
        assertEquals(0, llamadas.get());
        assertEquals(1, programaciones.cantidadPendientes());
        assertEquals(0, notificaciones.contarNoLeidas("C1"));
    }

    @Test
    void ejecutarAlLlegarLaFecha() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        AtomicInteger llamadas = new AtomicInteger();

        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        programacion -> {
                            llamadas.incrementAndGet();
                            return true;
                        }
                );

        programaciones.programar(
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 15, 10, 0),
                        "UNA_VEZ"
                )
        );

        // Simulamos que paso un dia.
        reloj.avanzarDias(1);

        int procesadas = procesador.procesarVencidas();

        assertEquals(1, procesadas);
        assertEquals(1, llamadas.get());
        assertEquals(0, programaciones.cantidadPendientes());

        assertEquals(
                1,
                notificaciones.contarNoLeidas("C1")
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_EJECUTADA,
                notificaciones.consultarNotificaciones("C1")
                        .obtenerValorNodo(0)
                        .getTipo()
        );

        // Procesar nuevamente no debe duplicar la ejecucion.
        assertEquals(0, procesador.procesarVencidas());
        assertEquals(1, llamadas.get());
    }

    @Test
    void reprogramarOperacionDiaria() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        AtomicInteger llamadas = new AtomicInteger();

        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        programacion -> {
                            llamadas.incrementAndGet();
                            return true;
                        }
                );

        programaciones.programar(
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 15, 10, 0),
                        "DIARIA"
                )
        );

        reloj.avanzarDias(1);

        assertEquals(1, procesador.procesarVencidas());
        assertEquals(1, llamadas.get());

        // La programacion sigue pendiente para el siguiente dia.
        assertEquals(1, programaciones.cantidadPendientes());

        assertEquals(
                LocalDateTime.of(2026, 11, 16, 10, 0),
                programaciones.consultarSiguiente()
                        .getFechaProgramada()
        );

        assertEquals(1, notificaciones.contarNoLeidas("C1"));
    }

    @Test
    void falloDetieneLaProgramacion() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        TransaccionProgramada programacion =
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 15, 10, 0),
                        "DIARIA"
                );

        // El ejecutor simula que el pago fue rechazado.
        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        operacion -> false
                );

        programaciones.programar(programacion);

        reloj.avanzarDias(1);

        assertEquals(1, procesador.procesarVencidas());

        assertEquals(0, programaciones.cantidadPendientes());
        assertFalse(programacion.isActiva());

        assertEquals(
                TipoNotificacion.PROGRAMADA_FALLIDA,
                notificaciones.consultarNotificaciones("C1")
                        .obtenerValorNodo(0)
                        .getTipo()
        );

        assertEquals(1, notificaciones.contarNoLeidas("C1"));
    }

    @Test
    void errorDelEjecutorNoDetieneElProcesador() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        operacion -> {
                            throw new IllegalStateException(
                                    "Error simulado"
                            );
                        }
                );

        programaciones.programar(
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 15, 10, 0),
                        "UNA_VEZ"
                )
        );

        reloj.avanzarDias(1);

        assertDoesNotThrow(procesador::procesarVencidas);

        assertEquals(0, programaciones.cantidadPendientes());

        assertEquals(
                TipoNotificacion.PROGRAMADA_FALLIDA,
                notificaciones.consultarNotificaciones("C1")
                        .obtenerValorNodo(0)
                        .getTipo()
        );
    }

    @Test
    void procesaPrimeroLaFechaMasCercana() {

        RelojSimulado reloj = crearReloj();

        ProgramacionService programaciones =
                new ProgramacionService(reloj);

        NotificacionService notificaciones =
                new NotificacionService(reloj);

        StringBuilder orden = new StringBuilder();

        ProcesadorProgramacionesService procesador =
                new ProcesadorProgramacionesService(
                        programaciones,
                        notificaciones,
                        operacion -> {
                            orden.append(operacion.getId());
                            return true;
                        }
                );

        programaciones.programar(
                crearProgramacion(
                        "P2",
                        LocalDateTime.of(2026, 11, 17, 10, 0),
                        "UNA_VEZ"
                )
        );

        programaciones.programar(
                crearProgramacion(
                        "P1",
                        LocalDateTime.of(2026, 11, 15, 10, 0),
                        "UNA_VEZ"
                )
        );

        reloj.avanzarDias(4);

        assertEquals(2, procesador.procesarVencidas());
        assertEquals("P1P2", orden.toString());

        assertEquals(0, programaciones.cantidadPendientes());
        assertEquals(2, notificaciones.contarNoLeidas("C1"));
    }
}
