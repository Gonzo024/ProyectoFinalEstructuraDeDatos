package co.edu.uniquindio.poo.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DineroTest {

    @Test
    void sumaDecimalesSinErrorDePrecision() {
        // Con double, 0.1 + 0.2 da 0.30000000000000004
        BigDecimal suma = Dinero.de("0.10").add(Dinero.de("0.20"));

        assertEquals(Dinero.de("0.30"), suma);
    }

    @Test
    void redondeaADosDecimalesHaciaArriba() {
        assertEquals(Dinero.de("10.13"), Dinero.redondear(new BigDecimal("10.125")));
        assertEquals(Dinero.de("10.12"), Dinero.redondear(new BigDecimal("10.124")));
    }

    @Test
    void calculaComisionDeTransferencia() {
        // 1.5 % de 1000 = 15.00
        assertEquals(Dinero.de(15),
                Dinero.porcentaje(Dinero.de(1000), Constantes.COMISION_TRANSFERENCIA));
    }

    @Test
    void comparaMontosConDistintaEscala() {
        assertTrue(Dinero.iguales(new BigDecimal("50"), Dinero.de("50.00")));
        assertTrue(Dinero.mayorOIgual(Dinero.de(50), Dinero.de("49.99")));
        assertFalse(Dinero.mayorOIgual(Dinero.de(50), Dinero.de("50.01")));
    }
}
