package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultimonederoServiceTest {

    // Escenario: Ana (C1) con M1 "Principal", M2 "Ahorros", M3 "Gastos"; Beto (C2) con M4.
    private ClienteService clientes;
    private TransaccionService transacciones;
    private MultimonederoService multimonedero;

    private void crearEscenario() {
        RelojSimulado reloj = new RelojSimulado(LocalDateTime.of(2026, 11, 14, 10, 0));
        clientes = new ClienteService(reloj);
        transacciones = new TransaccionService(clientes, reloj);
        multimonedero = new MultimonederoService(clientes);
        transacciones.agregarListener(multimonedero); // así el grafo se llena solo

        clientes.registrarCliente("Ana", "ana@correo.com", "1234");          // M1
        clientes.crearMonedero("C1", "Ahorros", TipoMonedero.AHORROS);      // M2
        clientes.crearMonedero("C1", "Gastos", TipoMonedero.GASTOS_DIARIOS); // M3
        clientes.registrarCliente("Beto", "beto@correo.com", "5678");       // M4
        transacciones.depositar("M1", Dinero.de(1000), "");
    }

    @Test
    void lasTransferenciasInternasCreanYSumanFlechas() {
        crearEscenario();

        transacciones.transferirEntreMisMonederos("M1", "M2", Dinero.de(200), "");
        transacciones.transferirEntreMisMonederos("M1", "M2", Dinero.de(100), "");
        transacciones.transferirEntreMisMonederos("M1", "M3", Dinero.de(50), "");

        assertEquals(Dinero.de(300), multimonedero.totalMovido("M1", "M2"));
        assertEquals(Dinero.de(350), multimonedero.totalEnviado("M1"));
        assertEquals(2, multimonedero.movimientosDelCliente("C1").getTamano());
    }

    @Test
    void lasOtrasOperacionesNoEntranAlGrafo() {
        crearEscenario();

        transacciones.retirar("M1", Dinero.de(100), "");
        transacciones.transferir("M1", "M4", Dinero.de(100), ""); // a otro cliente

        assertEquals(0, multimonedero.movimientosDelCliente("C1").getTamano());
    }

    @Test
    void alRevertirSeDescuentaDelGrafo() {
        crearEscenario();
        transacciones.transferirEntreMisMonederos("M1", "M2", Dinero.de(200), "");
        transacciones.transferirEntreMisMonederos("M1", "M2", Dinero.de(100), "");

        transacciones.revertirUltima("M1");

        assertEquals(Dinero.de(200), multimonedero.totalMovido("M1", "M2"));
    }

    @Test
    void destinosAlcanzablesSiguenElCamino() {
        crearEscenario();
        transacciones.transferirEntreMisMonederos("M1", "M2", Dinero.de(300), "");
        transacciones.transferirEntreMisMonederos("M2", "M3", Dinero.de(100), "");

        // De M1 el dinero llegó a M2 directamente y a M3 pasando por M2.
        assertEquals(2, multimonedero.destinosAlcanzables("M1").getTamano());
        assertTrue(multimonedero.destinosAlcanzables("M3").estaVacia());
    }
}