
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Notificacion;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;

import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;

import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class IntegracionFinalMonederoTest {

    private ContextoMonedero contexto;
    private Cliente cliente;
    private Monedero principal;

    @BeforeEach
    void preparar() {

        RelojSimulado reloj = new RelojSimulado(
                LocalDateTime.of(2026, 11, 15, 10, 0)
        );

        contexto = new ContextoMonedero(reloj);

        cliente = contexto.getClienteService()
                .registrarCliente(
                        "Ana",
                        "ana@integracion.com",
                        "1234"
                );

        principal = contexto.getClienteService()
                .monederosDe(cliente.getId())
                .obtenerValorNodo(0);

        // Todos los casos empiezan con $100.000.
        contexto.getTransaccionService().depositar(
                principal.getId(),
                Dinero.de(100000),
                "Saldo inicial de prueba"
        );
    }

    private TransaccionProgramada crearProgramacion(
            String id,
            TipoTransaccion tipo,
            String destinoCliente,
            String destinoMonedero,
            long monto,
            String frecuencia) {

        return new TransaccionProgramada(
                id,
                tipo,
                cliente.getId(),
                principal.getId(),
                destinoCliente,
                destinoMonedero,
                Dinero.de(monto),
                contexto.getReloj().ahora().plusDays(1),
                frecuencia
        );
    }

    @Test
    void pagoApareceEnSaldoHistorialYReporte() {

        // Simula un pago registrado en el modulo 3.
        TransaccionProgramada pago = crearProgramacion(
                "P1",
                TipoTransaccion.PAGO,
                null,
                null,
                30000,
                "UNA_VEZ"
        );

        contexto.getProgramacionNotificacionService()
                .programarYNotificar(pago);

        // Programar no debe descontar dinero.
        assertEquals(
                0,
                principal.getSaldo()
                        .compareTo(Dinero.de(100000))
        );

        // Avanzar al vencimiento.
        contexto.getReloj().avanzarDias(1);

        assertEquals(
                1,
                contexto.getProcesador().procesarVencidas()
        );

        // El modulo 1 debe ver el saldo actualizado.
        assertEquals(
                0,
                contexto.getTransaccionService()
                        .consultarSaldo(principal.getId())
                        .compareTo(Dinero.de(70000))
        );

        // Deposito inicial + pago programado.
        assertEquals(
                2,
                contexto.getTransaccionService()
                        .consultarHistorial(principal.getId())
                        .getTamano()
        );

        // El modulo 3 debe ver el gasto del mes.
        assertEquals(
                0,
                contexto.getReporteService()
                        .calcularCategoriaMensual(
                                cliente.getId(),
                                CategoriaGasto.OTROS,
                                2026,
                                11
                        )
                        .compareTo(Dinero.de(30000))
        );

        // Registro de programacion + ejecucion.
        // (No se cuentan los avisos de cambio de rango del sistema de puntos.)
        assertEquals(
                2,
                contarNoLeidasSinCambioDeRango(cliente.getId())
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_EJECUTADA,
                ultimaNotificacion(cliente.getId()).getTipo()
        );

        // No puede cobrar dos veces el mismo pago.
        assertEquals(
                0,
                contexto.getProcesador().procesarVencidas()
        );

        assertTrue(
                contexto.getTransaccionService()
                        .verificarIntegridad(principal.getId())
        );
    }

    @Test
    void transferenciaInternaActualizaGrafoUnaVez() {

        Monedero ahorros = contexto.getClienteService()
                .crearMonedero(
                        cliente.getId(),
                        "Ahorros",
                        TipoMonedero.AHORROS
                );

        TransaccionProgramada transferencia =
                crearProgramacion(
                        "P2",
                        TipoTransaccion.TRANSFERENCIA_INTERNA,
                        cliente.getId(),
                        ahorros.getId(),
                        25000,
                        "UNA_VEZ"
                );

        contexto.getProgramacionNotificacionService()
                .programarYNotificar(transferencia);

        contexto.getReloj().avanzarDias(1);

        assertEquals(
                1,
                contexto.getProcesador().procesarVencidas()
        );

        assertEquals(
                0,
                principal.getSaldo()
                        .compareTo(Dinero.de(75000))
        );

        assertEquals(
                0,
                ahorros.getSaldo()
                        .compareTo(Dinero.de(25000))
        );

        // El listener registra la transferencia
        // en el grafo sin duplicar su valor.
        assertEquals(
                0,
                contexto.getMultimonederoService()
                        .totalMovido(
                                principal.getId(),
                                ahorros.getId()
                        )
                        .compareTo(Dinero.de(25000))
        );

        // No hay vencimientos adicionales.
        assertEquals(
                0,
                contexto.getProcesador().procesarVencidas()
        );

        assertEquals(
                0,
                contexto.getMultimonederoService()
                        .totalMovido(
                                principal.getId(),
                                ahorros.getId()
                        )
                        .compareTo(Dinero.de(25000))
        );

        // Los dos historiales y saldos son coherentes.
        assertTrue(
                contexto.getTransaccionService()
                        .verificarIntegridad(principal.getId())
        );

        assertTrue(
                contexto.getTransaccionService()
                        .verificarIntegridad(ahorros.getId())
        );
    }

    @Test
    void pagoSinSaldoNoDescuentaYGeneraAviso() {

        // El monedero tiene 100.000.
        // Intentamos pagar 150.000.
        TransaccionProgramada pago = crearProgramacion(
                "P3",
                TipoTransaccion.PAGO,
                null,
                null,
                150000,
                "UNA_VEZ"
        );

        contexto.getProgramacionNotificacionService()
                .programarYNotificar(pago);

        contexto.getReloj().avanzarDias(1);

        assertEquals(
                1,
                contexto.getProcesador().procesarVencidas()
        );

        // No debe descontar el dinero disponible.
        assertEquals(
                0,
                principal.getSaldo()
                        .compareTo(Dinero.de(100000))
        );

        assertFalse(pago.isActiva());

        assertEquals(
                0,
                contexto.getProgramacionService()
                        .cantidadPendientes()
        );

        assertEquals(
                TipoNotificacion.PROGRAMADA_FALLIDA,
                ultimaNotificacion(cliente.getId()).getTipo()
        );

        assertEquals(
                0,
                contexto.getProcesador().procesarVencidas()
        );

        assertTrue(
                contexto.getTransaccionService()
                        .verificarIntegridad(principal.getId())
        );
    }

    @Test
    void programacionDiariaSeEjecutaEnDiasDistintos() {

        TransaccionProgramada pagoDiario =
                crearProgramacion(
                        "P4",
                        TipoTransaccion.PAGO,
                        null,
                        null,
                        10000,
                        "DIARIA"
                );

        contexto.getProgramacionNotificacionService()
                .programarYNotificar(pagoDiario);

        // Primer dia.
        contexto.getReloj().avanzarDias(1);

        assertEquals(
                1,
                contexto.getProcesador().procesarVencidas()
        );

        assertEquals(
                0,
                principal.getSaldo()
                        .compareTo(Dinero.de(90000))
        );

        // Repetir en el mismo dia no debe cobrar.
        assertEquals(
                0,
                contexto.getProcesador().procesarVencidas()
        );

        // Segundo dia.
        contexto.getReloj().avanzarDias(1);

        assertEquals(
                1,
                contexto.getProcesador().procesarVencidas()
        );

        assertEquals(
                0,
                principal.getSaldo()
                        .compareTo(Dinero.de(80000))
        );

        // Sigue activa para el tercer dia.
        assertTrue(pagoDiario.isActiva());

        assertEquals(
                1,
                contexto.getProgramacionService()
                        .cantidadPendientes()
        );

        // Deposito inicial y dos pagos.
        assertEquals(
                3,
                contexto.getTransaccionService()
                        .consultarHistorial(principal.getId())
                        .getTamano()
        );

        assertEquals(
                0,
                contexto.getReporteService()
                        .calcularTotalMensual(
                                cliente.getId(),
                                2026,
                                11
                        )
                        .compareTo(Dinero.de(20000))
        );

        assertTrue(
                contexto.getTransaccionService()
                        .verificarIntegridad(principal.getId())
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

    /** No leidas del cliente, sin contar los avisos de cambio de rango. */
    private int contarNoLeidasSinCambioDeRango(String clienteId) {
        int cantidad = 0;
        for (Notificacion n : contexto.getNotificacionService()
                .consultarNotificaciones(clienteId)) {
            if (!n.isLeida() && n.getTipo() != TipoNotificacion.CAMBIO_RANGO) {
                cantidad++;
            }
        }
        return cantidad;
    }
}
