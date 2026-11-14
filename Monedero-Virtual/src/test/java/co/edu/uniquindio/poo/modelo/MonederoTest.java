package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;
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

        assertEquals(Dinero.CERO, monedero.getSaldo());
        assertTrue(monedero.getHistorial().estaVacia());
        assertTrue(monedero.getReversibles().estaVacia());
    }

    @Test
    void acreditarSuma() {
        Monedero monedero = crearMonedero();

        monedero.acreditar(Dinero.de("100.50"));
        monedero.acreditar(Dinero.de(20));

        assertEquals(Dinero.de("120.50"), monedero.getSaldo());
    }

    @Test
    void debitarResta() {
        Monedero monedero = crearMonedero();
        monedero.acreditar(Dinero.de(100));

        monedero.debitar(Dinero.de("40.25"));

        assertEquals(Dinero.de("59.75"), monedero.getSaldo());
    }

    @Test
    void noSePuedeDebitarMasDeLoQueHayYElSaldoNoCambia() {
        Monedero monedero = crearMonedero();
        monedero.acreditar(Dinero.de(50));

        try {
            monedero.debitar(Dinero.de("50.01"));
            fail("Debio lanzar SaldoInsuficienteException");
        } catch (SaldoInsuficienteException e) {
            // correcto: el saldo no puede quedar negativo
        }

        assertEquals(Dinero.de(50), monedero.getSaldo());
    }
}