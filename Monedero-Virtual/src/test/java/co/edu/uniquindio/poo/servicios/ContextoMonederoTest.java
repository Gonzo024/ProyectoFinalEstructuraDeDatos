
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Notificacion;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ContextoMonederoTest {

    private ContextoMonedero contexto;

    @BeforeEach
    void preparar() {

        RelojSimulado reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 15, 10, 0)
        );

        contexto = new ContextoMonedero(reloj);
    }

    @Test
    void losServiciosCompartenLosMismosClientes() {

        ClienteService clientes = contexto.getClienteService();

        Cliente cliente = clientes.registrarCliente(
                "Ana",
                "ana@prueba.com",
                "1234"
        );

        Monedero monedero = clientes.monederosDe(
                cliente.getId()
        ).obtenerValorNodo(0);

        // Simulamos un deposito hecho desde el modulo 1.
        contexto.getTransaccionService().depositar(
                monedero.getId(),
                Dinero.de(100000),
                "Deposito inicial"
        );

        // El modulo 3 debe consultar al mismo cliente
        // y al mismo monedero.
        assertSame(
                cliente,
                contexto.getClienteService()
                        .obtenerCliente(cliente.getId())
        );

        assertEquals(
                0,
                contexto.getClienteService()
                        .obtenerMonedero(monedero.getId())
                        .getSaldo()
                        .compareTo(Dinero.de(100000))
        );
    }

    @Test
    void programacionUsaElSaldoCompartido() {

        Cliente cliente =
                contexto.getClienteService().registrarCliente(
                        "Luis",
                        "luis@prueba.com",
                        "5678"
                );

        Monedero monedero =
                contexto.getClienteService()
                        .monederosDe(cliente.getId())
                        .obtenerValorNodo(0);

        // Dinero ingresado por el servicio financiero.
        contexto.getTransaccionService().depositar(
                monedero.getId(),
                Dinero.de(100000),
                "Saldo inicial"
        );

        TransaccionProgramada pago =
                new TransaccionProgramada(
                        "P1",
                        TipoTransaccion.PAGO,
                        cliente.getId(),
                        monedero.getId(),
                        null,
                        null,
                        Dinero.de(30000),
                        contexto.getReloj().ahora().plusDays(1),
                        "UNA_VEZ"
                );

        contexto.getProgramacionNotificacionService()
                .programarYNotificar(pago);

        // Antes del vencimiento, el saldo no cambia.
        assertEquals(
                0,
                monedero.getSaldo()
                        .compareTo(Dinero.de(100000))
        );

        contexto.getReloj().avanzarDias(1);

        // El procesador debe descontar dinero
        // del mismo monedero del cliente.
        assertEquals(
                1,
                contexto.getProcesador().procesarVencidas()
        );

        assertEquals(
                0,
                monedero.getSaldo()
                        .compareTo(Dinero.de(70000))
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_EJECUTADA,
                ultimaNotificacion(cliente.getId()).getTipo()
        );

        // No debe descontar el dinero nuevamente.
        assertEquals(
                0,
                contexto.getProcesador().procesarVencidas()
        );
    }

    @Test
    void elGrafoRecibeLasTransferenciasInternas() {

        Cliente cliente =
                contexto.getClienteService().registrarCliente(
                        "Maria",
                        "maria@prueba.com",
                        "1234"
                );

        Monedero principal =
                contexto.getClienteService()
                        .monederosDe(cliente.getId())
                        .obtenerValorNodo(0);

        Monedero ahorros =
                contexto.getClienteService().crearMonedero(
                        cliente.getId(),
                        "Ahorros",
                        TipoMonedero.AHORROS
                );

        contexto.getTransaccionService().depositar(
                principal.getId(),
                Dinero.de(100000),
                "Saldo inicial"
        );

        // Transferencia realizada desde el servicio.
        contexto.getTransaccionService()
                .transferirEntreMisMonederos(
                        principal.getId(),
                        ahorros.getId(),
                        Dinero.de(20000),
                        "Transferencia de prueba"
                );

        // El listener debe actualizar el grafo.
        assertEquals(
                0,
                contexto.getMultimonederoService()
                        .totalMovido(
                                principal.getId(),
                                ahorros.getId()
                        )
                        .compareTo(Dinero.de(20000))
        );

        assertEquals(
                0,
                ahorros.getSaldo()
                        .compareTo(Dinero.de(20000))
        );
    }

    /**
     * Ultima notificacion del cliente. Con el sistema de puntos tambien llegan
     * avisos de cambio de rango, asi que no se puede asumir una posicion fija.
     */
    private Notificacion ultimaNotificacion(String clienteId) {
        ListaEnlazada<Notificacion> lista = contexto.getNotificacionService()
                .consultarNotificaciones(clienteId);
        return lista.obtenerValorNodo(lista.getTamano() - 1);
    }
}
