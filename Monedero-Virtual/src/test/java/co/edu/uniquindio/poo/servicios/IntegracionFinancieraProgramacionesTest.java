
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class IntegracionFinancieraProgramacionesTest {

    private RelojSimulado reloj;
    private ClienteService clientes;
    private TransaccionService transacciones;
    private ProgramacionService programaciones;
    private NotificacionService notificaciones;
    private ProcesadorProgramacionesService procesador;

    private Cliente cliente;
    private Monedero monedero;

    @BeforeEach
    void preparar() {

        reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 14, 10, 0)
        );

        clientes = new ClienteService(reloj);
        transacciones = new TransaccionService(clientes, reloj);
        programaciones = new ProgramacionService(reloj);
        notificaciones = new NotificacionService(reloj);

        EjecutorFinancieroProgramado ejecutor =
                new EjecutorFinancieroProgramado(
                        clientes,
                        transacciones
                );

        procesador = new ProcesadorProgramacionesService(
                programaciones,
                notificaciones,
                ejecutor
        );

        cliente = clientes.registrarCliente(
                "Ana",
                "ana@prueba.com",
                "1234"
        );

        monedero = clientes.monederosDe(
                cliente.getId()
        ).obtenerValorNodo(0);
    }

    private TransaccionProgramada crear(
            String id,
            TipoTransaccion tipo,
            String origen,
            String destino,
            long monto) {

        return new TransaccionProgramada(
                id,
                tipo,
                cliente.getId(),
                origen,
                cliente.getId(),
                destino,
                Dinero.de(monto),
                reloj.ahora().plusDays(1),
                "UNA_VEZ"
        );
    }

    @Test
    void depositoDesdeJavaFxDebeAumentarSaldo() {

        // La interfaz usa origen null y destino M1.
        TransaccionProgramada deposito = crear(
                "P1",
                TipoTransaccion.DEPOSITO,
                null,
                monedero.getId(),
                100000
        );

        programaciones.programar(deposito);

        // Antes del vencimiento no debe mover dinero.
        assertEquals(0, procesador.procesarVencidas());

        assertEquals(
                0,
                monedero.getSaldo().compareTo(Dinero.CERO)
        );

        reloj.avanzarDias(1);

        assertEquals(1, procesador.procesarVencidas());

        assertEquals(
                0,
                monedero.getSaldo()
                        .compareTo(Dinero.de(100000))
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_EJECUTADA,
                notificaciones.consultarNotificaciones(
                        cliente.getId()
                ).obtenerValorNodo(0).getTipo()
        );

        // No debe volver a ingresar el mismo dinero.
        assertEquals(0, procesador.procesarVencidas());

        assertEquals(
                0,
                monedero.getSaldo()
                        .compareTo(Dinero.de(100000))
        );
    }

    @Test
    void pagoProgramadoDescuentaSaldoReal() {

        transacciones.depositar(
                monedero.getId(),
                Dinero.de(100000),
                "Saldo inicial"
        );

        TransaccionProgramada pago = crear(
                "P2",
                TipoTransaccion.PAGO,
                monedero.getId(),
                null,
                30000
        );

        programaciones.programar(pago);

        reloj.avanzarDias(1);

        assertEquals(1, procesador.procesarVencidas());

        assertEquals(
                0,
                monedero.getSaldo()
                        .compareTo(Dinero.de(70000))
        );

        assertEquals(
                2,
                transacciones.consultarHistorial(
                        monedero.getId()
                ).getTamano()
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_EJECUTADA,
                notificaciones.consultarNotificaciones(
                        cliente.getId()
                ).obtenerValorNodo(0).getTipo()
        );
    }

    @Test
    void pagoSinSaldoDebeFallarSinDescontarDinero() {

        TransaccionProgramada pago = crear(
                "P3",
                TipoTransaccion.PAGO,
                monedero.getId(),
                null,
                50000
        );

        programaciones.programar(pago);

        reloj.avanzarDias(1);

        assertEquals(1, procesador.procesarVencidas());

        assertEquals(
                0,
                monedero.getSaldo().compareTo(Dinero.CERO)
        );

        assertFalse(pago.isActiva());
        assertEquals(0, programaciones.cantidadPendientes());

        assertEquals(
                TipoNotificacion.PROGRAMADA_FALLIDA,
                notificaciones.consultarNotificaciones(
                        cliente.getId()
                ).obtenerValorNodo(0).getTipo()
        );

        // Intentarlo otra vez no genera otro movimiento.
        assertEquals(0, procesador.procesarVencidas());
        assertEquals(
                1,
                notificaciones.contarNoLeidas(cliente.getId())
        );
    }

    @Test
    void depositoNoPuedeIrAlMonederoDeOtroCliente() {

        Cliente otro = clientes.registrarCliente(
                "Luis",
                "luis@prueba.com",
                "5678"
        );

        Monedero monederoAjeno = clientes.monederosDe(
                otro.getId()
        ).obtenerValorNodo(0);

        TransaccionProgramada deposito = crear(
                "P4",
                TipoTransaccion.DEPOSITO,
                null,
                monederoAjeno.getId(),
                50000
        );

        programaciones.programar(deposito);

        reloj.avanzarDias(1);

        assertEquals(1, procesador.procesarVencidas());

        assertEquals(
                0,
                monederoAjeno.getSaldo()
                        .compareTo(Dinero.CERO)
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_FALLIDA,
                notificaciones.consultarNotificaciones(
                        cliente.getId()
                ).obtenerValorNodo(0).getTipo()
        );
    }
}
