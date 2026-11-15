
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EjecutorFinancieroProgramadoTest {

    private RelojSimulado reloj;
    private ClienteService clientes;
    private TransaccionService transacciones;
    private EjecutorFinancieroProgramado ejecutor;

    private Cliente clienteUno;
    private Cliente clienteDos;

    private Monedero monederoUno;
    private Monedero monederoDos;

    @BeforeEach
    void preparar() {

        reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0)
        );

        clientes = new ClienteService(reloj);
        transacciones = new TransaccionService(clientes, reloj);

        ejecutor = new EjecutorFinancieroProgramado(
                clientes,
                transacciones
        );

        clienteUno = clientes.registrarCliente(
                "Ana",
                "ana@test.com",
                "1234"
        );

        clienteDos = clientes.registrarCliente(
                "Luis",
                "luis@test.com",
                "5678"
        );

        monederoUno = clientes.monederosDe(
                clienteUno.getId()
        ).obtenerValorNodo(0);

        monederoDos = clientes.monederosDe(
                clienteDos.getId()
        ).obtenerValorNodo(0);
    }

    private TransaccionProgramada crear(
            String id,
            TipoTransaccion tipo,
            String clienteOrigen,
            String monederoOrigen,
            String clienteDestino,
            String monederoDestino,
            long monto) {

        return new TransaccionProgramada(
                id,
                tipo,
                clienteOrigen,
                monederoOrigen,
                clienteDestino,
                monederoDestino,
                Dinero.de(monto),
                reloj.ahora().plusDays(1),
                "UNA_VEZ"
        );
    }

    @Test
    void ejecutarDepositoAumentaSaldo() {

        TransaccionProgramada deposito = crear(
                "P1",
                TipoTransaccion.DEPOSITO,
                clienteUno.getId(),
                monederoUno.getId(),
                null,
                null,
                100000
        );

        assertTrue(ejecutor.ejecutar(deposito));

        assertEquals(
                0,
                monederoUno.getSaldo()
                        .compareTo(Dinero.de(100000))
        );

        assertEquals(
                1,
                transacciones.consultarHistorial(
                        monederoUno.getId()
                ).getTamano()
        );
    }

    @Test
    void ejecutarPagoDescuentaSaldo() {

        transacciones.depositar(
                monederoUno.getId(),
                Dinero.de(100000),
                "Saldo inicial"
        );

        TransaccionProgramada pago = crear(
                "P2",
                TipoTransaccion.PAGO,
                clienteUno.getId(),
                monederoUno.getId(),
                null,
                null,
                30000
        );

        assertTrue(ejecutor.ejecutar(pago));

        assertEquals(
                0,
                monederoUno.getSaldo()
                        .compareTo(Dinero.de(70000))
        );

        assertEquals(
                2,
                transacciones.consultarHistorial(
                        monederoUno.getId()
                ).getTamano()
        );
    }

    @Test
    void ejecutarTransferenciaMueveDinero() {

        transacciones.depositar(
                monederoUno.getId(),
                Dinero.de(100000),
                "Saldo inicial"
        );

        TransaccionProgramada transferencia = crear(
                "P3",
                TipoTransaccion.TRANSFERENCIA,
                clienteUno.getId(),
                monederoUno.getId(),
                clienteDos.getId(),
                monederoDos.getId(),
                20000
        );

        assertTrue(ejecutor.ejecutar(transferencia));

        // El destinatario recibe exactamente el monto.
        assertEquals(
                0,
                monederoDos.getSaldo()
                        .compareTo(Dinero.de(20000))
        );

        // El origen paga tambien la comision.
        assertTrue(
                monederoUno.getSaldo()
                        .compareTo(Dinero.de(80000)) < 0
        );
    }

    @Test
    void ejecutarTransferenciaInternaNoCobraComision() {

        Monedero ahorros = clientes.crearMonedero(
                clienteUno.getId(),
                "Ahorros",
                TipoMonedero.AHORROS
        );

        transacciones.depositar(
                monederoUno.getId(),
                Dinero.de(100000),
                "Saldo inicial"
        );

        TransaccionProgramada transferencia = crear(
                "P4",
                TipoTransaccion.TRANSFERENCIA_INTERNA,
                clienteUno.getId(),
                monederoUno.getId(),
                clienteUno.getId(),
                ahorros.getId(),
                40000
        );

        assertTrue(ejecutor.ejecutar(transferencia));

        assertEquals(
                0,
                monederoUno.getSaldo()
                        .compareTo(Dinero.de(60000))
        );

        assertEquals(
                0,
                ahorros.getSaldo()
                        .compareTo(Dinero.de(40000))
        );
    }

    @Test
    void rechazarMonederoQueNoPerteneceAlCliente() {

        TransaccionProgramada pago = crear(
                "P5",
                TipoTransaccion.PAGO,
                clienteUno.getId(),
                monederoDos.getId(),
                null,
                null,
                10000
        );

        assertFalse(ejecutor.ejecutar(pago));

        assertEquals(
                0,
                monederoDos.getSaldo()
                        .compareTo(Dinero.CERO)
        );
    }

    @Test
    void saldoInsuficienteNoAcreditaDinero() {

        TransaccionProgramada pago = crear(
                "P6",
                TipoTransaccion.PAGO,
                clienteUno.getId(),
                monederoUno.getId(),
                null,
                null,
                50000
        );

        assertThrows(
                RuntimeException.class,
                () -> ejecutor.ejecutar(pago)
        );

        assertEquals(
                0,
                monederoUno.getSaldo()
                        .compareTo(Dinero.CERO)
        );
    }
}
