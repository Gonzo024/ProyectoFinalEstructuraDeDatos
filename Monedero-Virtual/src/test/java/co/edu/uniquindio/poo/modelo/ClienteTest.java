package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.Rango;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClienteTest {

    private Cliente crearCliente() {
        return new Cliente("C1", "Ana", "ana@correo.com", "1234", LocalDateTime.of(2026, 11, 4, 10, 0));
    }

    @Test
    void clienteNuevoEmpiezaEnBronce() {
        Cliente cliente = crearCliente();

        assertEquals("Ana", cliente.getNombre());
        assertEquals(Rango.BRONCE, cliente.getRango());
    }

    @Test
    void elRangoSePuedeActualizar() {
        Cliente cliente = crearCliente();

        cliente.setRango(Rango.ORO);

        assertEquals(Rango.ORO, cliente.getRango());
    }

    @Test
    void verificaElPin() {
        Cliente cliente = crearCliente();

        assertTrue(cliente.pinCorrecto("1234"));
        assertFalse(cliente.pinCorrecto("0000"));
    }
}
