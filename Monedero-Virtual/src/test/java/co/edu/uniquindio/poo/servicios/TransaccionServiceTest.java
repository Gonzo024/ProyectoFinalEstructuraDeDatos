package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.poo.excepciones.TransaccionInvalidaException;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.EstadoTransaccion;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TransaccionServiceTest {

    // Escenario: Ana (C1) con monedero M1 "Principal" y M2 "Ahorros"; Beto (C2) con M3 "Principal".
    private ClienteService clientes;
    private TransaccionService servicio;

    private void crearEscenario() {
        RelojSimulado reloj = new RelojSimulado(LocalDateTime.of(2026, 11, 14, 10, 0));
        clientes = new ClienteService(reloj);
        servicio = new TransaccionService(clientes, reloj);
        clientes.registrarCliente("Ana", "ana@correo.com", "1234");               // M1
        clientes.crearMonedero("C1", "Ahorros", TipoMonedero.AHORROS);           // M2
        clientes.registrarCliente("Beto", "beto@correo.com", "5678");            // M3
    }

    @Test
    void depositarSumaAlSaldoYQuedaEnHistorialYPila() {
        crearEscenario();

        Transaccion t = servicio.depositar("M1", Dinero.de(1000), "Sueldo");

        assertEquals(Dinero.de(1000), servicio.consultarSaldo("M1"));
        assertEquals(1, servicio.consultarHistorial("M1").getTamano());
        assertEquals(t, clientes.obtenerMonedero("M1").getReversibles().cima());
    }

    @Test
    void retirarDescuentaMontoMasComisionDel1Porciento() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(1000), "");

        Transaccion t = servicio.retirar("M1", Dinero.de(200), "Cajero");

        assertEquals(Dinero.de(2), t.getComision());                  // 1 % de 200
        assertEquals(Dinero.de(798), servicio.consultarSaldo("M1"));  // 1000 - 202
    }

    @Test
    void retiroSinSaldoQuedaFallidoYElSaldoNoCambia() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(100), "");

        try {
            servicio.retirar("M1", Dinero.de(100), "");   // necesita 101 con la comisión
            fail("Debio lanzar SaldoInsuficienteException");
        } catch (SaldoInsuficienteException e) {
            // correcto
        }

        assertEquals(Dinero.de(100), servicio.consultarSaldo("M1"));
        Transaccion fallida = servicio.consultarHistorial("M1").obtenerValorNodo(1);
        assertEquals(EstadoTransaccion.FALLIDA, fallida.getEstado());
        assertEquals(1, clientes.obtenerMonedero("M1").getReversibles().getTamano()); // solo el depósito
    }

    @Test
    void pagarGuardaLaCategoria() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(500), "");

        Transaccion t = servicio.pagar("M1", Dinero.de("120.50"), CategoriaGasto.ALIMENTACION, "Mercado");

        assertEquals(CategoriaGasto.ALIMENTACION, t.getCategoria());
        assertEquals(Dinero.de("379.50"), servicio.consultarSaldo("M1"));
    }

    @Test
    void transferirAOtroClienteCobraComisionSegunRango() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(2000), "");

        Transaccion t = servicio.transferir("M1", "M3", Dinero.de(1000), "Arriendo");

        assertEquals(Dinero.de(15), t.getComision());                  // Bronce: 1.5 % de 1000
        assertEquals(Dinero.de(985), servicio.consultarSaldo("M1"));   // 2000 - 1015
        assertEquals(Dinero.de(1000), servicio.consultarSaldo("M3"));
        assertEquals(1, servicio.consultarHistorial("M3").getTamano()); // también en el historial de Beto
    }

    @Test
    void clienteOroPagaMenosComision() {
        crearEscenario();
        clientes.obtenerCliente("C1").setRango(Rango.ORO);
        servicio.depositar("M1", Dinero.de(2000), "");

        Transaccion t = servicio.transferir("M1", "M3", Dinero.de(1000), "");

        // 15 - 25 % de 15 = 15 - 3.75 = 11.25
        assertEquals(Dinero.de("11.25"), t.getComision());
    }

    @Test
    void noSePuedeTransferirAOtroClienteUsandoUnMonederoPropio() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(500), "");

        try {
            servicio.transferir("M1", "M2", Dinero.de(100), "");
            fail("Debio lanzar TransaccionInvalidaException");
        } catch (TransaccionInvalidaException e) {
            // correcto: M1 y M2 son de Ana
        }
    }

    @Test
    void transferirEntreMisMonederosNoCobraComision() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(500), "");

        servicio.transferirEntreMisMonederos("M1", "M2", Dinero.de(200), "Ahorrar");

        assertEquals(Dinero.de(300), servicio.consultarSaldo("M1"));
        assertEquals(Dinero.de(200), servicio.consultarSaldo("M2"));
    }

    @Test
    void montoFueraDeRangoNoSePermite() {
        crearEscenario();

        try {
            servicio.depositar("M1", Dinero.de("0.50"), "");
            fail("Debio lanzar TransaccionInvalidaException");
        } catch (TransaccionInvalidaException e) {
            // correcto: el mínimo es 1
        }
    }

    @Test
    void revertirRetiroDevuelveMontoYComision() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(1000), "");
        Transaccion retiro = servicio.retirar("M1", Dinero.de(200), "");

        Transaccion revertida = servicio.revertirUltima("M1");

        assertEquals(retiro, revertida);
        assertEquals(EstadoTransaccion.REVERTIDA, retiro.getEstado());
        assertEquals(Dinero.de(1000), servicio.consultarSaldo("M1"));
    }

    @Test
    void revertirTransferenciaDevuelveElDineroAlOrigen() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(2000), "");
        servicio.transferir("M1", "M3", Dinero.de(1000), "");

        servicio.revertirUltima("M1");

        assertEquals(Dinero.de(2000), servicio.consultarSaldo("M1"));
        assertEquals(Dinero.de(0), servicio.consultarSaldo("M3"));
    }

    @Test
    void noSeRevierteSiElDestinoYaGastoElDinero() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(2000), "");
        Transaccion transferencia = servicio.transferir("M1", "M3", Dinero.de(1000), "");
        servicio.pagar("M3", Dinero.de(800), CategoriaGasto.COMPRAS, ""); // Beto se gastó casi todo

        try {
            servicio.revertirUltima("M1");
            fail("Debio lanzar TransaccionInvalidaException");
        } catch (TransaccionInvalidaException e) {
            // correcto: Beto quedaría con saldo negativo
        }
        assertEquals(Dinero.de(200), servicio.consultarSaldo("M3"));
        assertEquals(transferencia, clientes.obtenerMonedero("M1").getReversibles().cima()); // sigue en la pila
        assertEquals(EstadoTransaccion.COMPLETADA, transferencia.getEstado());
    }

    @Test
    void revertirSinOperacionesLanzaError() {
        crearEscenario();

        try {
            servicio.revertirUltima("M1");
            fail("Debio lanzar TransaccionInvalidaException");
        } catch (TransaccionInvalidaException e) {
            // correcto
        }
    }

    @Test
    void integridadCorrectaDespuesDeVariasOperaciones() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(3000), "");
        servicio.retirar("M1", Dinero.de(100), "");
        servicio.transferir("M1", "M3", Dinero.de(500), "");
        servicio.transferirEntreMisMonederos("M1", "M2", Dinero.de(300), "");
        servicio.revertirUltima("M1");
        try {
            servicio.retirar("M1", Dinero.de(9000), ""); // fallida: no debe afectar
        } catch (SaldoInsuficienteException e) {
            // esperado
        }

        assertTrue(servicio.verificarIntegridadCliente("C1"));
        assertTrue(servicio.verificarIntegridad("M3"));
    }

    @Test
    void integridadDetectaUnSaldoModificadoPorFuera() {
        crearEscenario();
        servicio.depositar("M1", Dinero.de(1000), "");
        Monedero m1 = clientes.obtenerMonedero("M1");

        m1.acreditar(Dinero.de(50)); // cambio "trampa" sin pasar por el servicio

        assertFalse(servicio.verificarIntegridad("M1"));
    }

    @Test
    void losListenersSeEnteranDeLasOperacionesYReversiones() {
        crearEscenario();
        int[] contadores = new int[2]; // [completadas, revertidas]
        servicio.agregarListener(new TransaccionListener() {
            @Override
            public void onTransaccionCompletada(Transaccion t) {
                contadores[0]++;
            }

            @Override
            public void onTransaccionRevertida(Transaccion t) {
                contadores[1]++;
            }
        });

        servicio.depositar("M1", Dinero.de(1000), "");
        servicio.retirar("M1", Dinero.de(100), "");
        servicio.revertirUltima("M1");

        assertEquals(2, contadores[0]);
        assertEquals(1, contadores[1]);
    }
}
