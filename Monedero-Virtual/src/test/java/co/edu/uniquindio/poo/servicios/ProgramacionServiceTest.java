
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.RelojSimulado;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ProgramacionServiceTest {

    @Test
    void programarUnaTransaccion() {

        RelojSimulado reloj = new RelojSimulado(
            LocalDateTime.of(2026, 11, 5, 10, 0)
        );

        ProgramacionService servicio =
            new ProgramacionService(reloj);

        TransaccionProgramada transaccion =
            new TransaccionProgramada(
                "TP001",
                TipoTransaccion.TRANSFERENCIA,
                "C001", "M001",
                "C002", "M002",
                50000,
                LocalDateTime.of(2026, 11, 8, 10, 0),
                "UNA_VEZ"
            );

        servicio.programar(transaccion);

        assertEquals(1, servicio.cantidadPendientes());
        assertFalse(servicio.hayTransaccionesVencidas());
    }

    @Test
    void retirarCuandoLlegaLaFecha() {

        RelojSimulado reloj = new RelojSimulado(
            LocalDateTime.of(2026, 11, 5, 10, 0)
        );

        ProgramacionService servicio =
            new ProgramacionService(reloj);

        TransaccionProgramada transaccion =
            new TransaccionProgramada(
                "TP002",
                TipoTransaccion.TRANSFERENCIA,
                "C001", "M001",
                "C002", "M002",
                30000,
                LocalDateTime.of(2026, 11, 8, 10, 0),
                "UNA_VEZ"
            );

        servicio.programar(transaccion);

        reloj.avanzarDias(4);

        assertTrue(servicio.hayTransaccionesVencidas());

        TransaccionProgramada retirada =
            servicio.retirarSiguienteVencida();

        assertEquals("TP002", retirada.getId());
        assertEquals(0, servicio.cantidadPendientes());
    }

    @Test
    void noPermitirFechasPasadas() {

        RelojSimulado reloj = new RelojSimulado(
            LocalDateTime.of(2026, 11, 10, 10, 0)
        );

        ProgramacionService servicio =
            new ProgramacionService(reloj);

        TransaccionProgramada transaccion =
            new TransaccionProgramada(
                "TP003",
                TipoTransaccion.TRANSFERENCIA,
                "C001", "M001",
                "C002", "M002",
                20000,
                LocalDateTime.of(2026, 11, 8, 10, 0),
                "UNA_VEZ"
            );

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.programar(transaccion)
        );

        assertEquals(0, servicio.cantidadPendientes());
    }


@Test
void cancelarUnaTransaccionProgramada() {

    RelojSimulado reloj = new RelojSimulado(
        LocalDateTime.of(2026, 11, 5, 10, 0)
    );

    ProgramacionService servicio =
        new ProgramacionService(reloj);

    TransaccionProgramada primera =
        new TransaccionProgramada(
            "TP001",
            TipoTransaccion.TRANSFERENCIA,
            "C001", "M001",
            "C002", "M002",
            10000,
            LocalDateTime.of(2026, 11, 8, 10, 0),
            "UNA_VEZ"
        );

    TransaccionProgramada segunda =
        new TransaccionProgramada(
            "TP002",
            TipoTransaccion.TRANSFERENCIA,
            "C001", "M001",
            "C002", "M002",
            20000,
            LocalDateTime.of(2026, 11, 12, 10, 0),
            "UNA_VEZ"
        );

    servicio.programar(primera);
    servicio.programar(segunda);

    assertEquals(2, servicio.cantidadPendientes());

    boolean cancelada =
        servicio.cancelarTransaccion("TP002");

    assertTrue(cancelada);
    assertFalse(segunda.isActiva());
    assertEquals(1, servicio.cantidadPendientes());

    assertEquals(
        "TP001",
        servicio.consultarSiguiente().getId()
    );

    assertFalse(
        servicio.cancelarTransaccion("TP999")
    );
}

}
