package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class MonederoTest {

    private Monedero crearMonedero() {
        return new Monedero("M1", "C1", "principal", TipoMonedero.PRINCIPAL);
    }

    @Test
    void monederoNuevoTieneSaldoCeroYSinHistorial() {
        Monedero monedero = crearMonedero();

        assertEquals(0.0, monedero.getSaldo(), 0.001);
        assertTrue(monedero.getHistorial().estaVacia());
        assertTrue(monedero.getReversibles().estaVacia());
    }

    @Test
    void acreditarSuma() {
        Monedero monedero = crearMonedero();

        monedero.acreditar(100.50);
        monedero.acreditar(20);

        assertEquals(120.50, monedero.getSaldo(), 0.001);
    }

    @Test
    void debitarResta() {
        Monedero monedero = crearMonedero();
        monedero.acreditar(100);

        monedero.debitar(40.25);

        assertEquals(59.75, monedero.getSaldo(), 0.001);
    }

    @Test
    void noSePuedeDebitarMasDeLoQueHayYElSaldoNoCambia() {
        Monedero monedero = crearMonedero();
        monedero.acreditar(50);

        try {
            monedero.debitar(50.01);
            fail("Debio lanzar SaldoInsuficienteException");
        } catch (SaldoInsuficienteException e) {
            // correcto: el saldo no puede quedar negativo
        }

        assertEquals(50.0, monedero.getSaldo(), 0.001);
    }
}