package co.edu.uniquindio.poo.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RelojSimuladoTest {

    @Test
    void avanzarDiasSumaDias() {
        RelojSimulado reloj = new RelojSimulado(LocalDateTime.of(2026, 11, 4, 10, 0));

        reloj.avanzarDias(3);

        assertEquals(LocalDateTime.of(2026, 11, 7, 10, 0), reloj.ahora());
    }

    @Test
    void fijarCambiaLaFecha() {
        RelojSimulado reloj = new RelojSimulado();

        reloj.fijar(LocalDateTime.of(2027, 1, 1, 8, 30));

        assertEquals(LocalDateTime.of(2027, 1, 1, 8, 30), reloj.ahora());
    }

    @Test
    void elRelojNoPuedeRetroceder() {
        RelojSimulado reloj = new RelojSimulado();

        try {
            reloj.avanzarDias(-1);
            fail("Debio lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // correcto: el tiempo simulado solo avanza
        }
    }
}