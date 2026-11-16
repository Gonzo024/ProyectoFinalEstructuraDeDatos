package co.edu.uniquindio.poo.servicios;


import co.edu.uniquindio.poo.excepciones.TransaccionInvalidaException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.RegistroPuntos;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoBeneficio;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoMovimientoPuntos;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del sistema de puntos: calculo, acumulacion, rangos y reversiones.
 * Se usa el ContextoMonedero real para probar todo conectado (patron Observador).
 */
class PuntosServiceTest {

    private ContextoMonedero contexto;
    private TransaccionService transacciones;
    private PuntosService puntos;
    private Cliente ana;
    private Cliente beto;
    private String monederoAna;
    private String monederoBeto;

    @BeforeEach
    void setUp() {
        contexto = new ContextoMonedero(new RelojSimulado(LocalDateTime.of(2026, 11, 15, 10, 0)));
        transacciones = contexto.getTransaccionService();
        puntos = contexto.getPuntosService();

        ClienteService clientes = contexto.getClienteService();
        ana = clientes.registrarCliente("Ana", "ana@prueba.com", "1234");
        beto = clientes.registrarCliente("Beto", "beto@prueba.com", "1234");
        monederoAna = principal(ana);
        monederoBeto = principal(beto);
    }

    // ============================================================ calculo

    @Test
    void depositoDaUnPuntoPorCadaCien() {
        Transaccion t = transacciones.depositar(monederoAna, Dinero.de(250), "deposito");

        // 250 / 100 = 2 bloques (los 50 sobrantes no cuentan) x 1 punto
        assertEquals(2, t.getPuntosGenerados());
        assertEquals(2, puntos.consultarPuntosDisponibles(ana.getId()));
    }

    @Test
    void cadaTipoDeTransaccionDaSusPuntos() {
        transacciones.depositar(monederoAna, Dinero.de(10000), "deposito");                       // 100 x 1 = 100
        Transaccion retiro = transacciones.retirar(monederoAna, Dinero.de(300), "retiro");         // 3 x 2 = 6
        Transaccion transf = transacciones.transferir(monederoAna, monederoBeto, Dinero.de(1000), "a Beto"); // 10 x 3 = 30
        Transaccion pago = transacciones.pagar(monederoAna, Dinero.de(500), CategoriaGasto.ALIMENTACION, "pago"); // 5 x 2 = 10

        assertEquals(6, retiro.getPuntosGenerados());
        assertEquals(30, transf.getPuntosGenerados());
        assertEquals(10, pago.getPuntosGenerados());
        assertEquals(146, puntos.consultarPuntosAcumulados(ana.getId()));
        // Los puntos son de quien hace la transaccion, no de quien recibe.
        assertEquals(0, puntos.consultarPuntosAcumulados(beto.getId()));
    }

    @Test
    void moverDineroEntreMisMonederosNoDaPuntos() {
        String ahorros = contexto.getClienteService()
                .crearMonedero(ana.getId(), "Ahorros", TipoMonedero.AHORROS).getId();
        transacciones.depositar(monederoAna, Dinero.de(5000), "deposito"); // 50 puntos

        Transaccion interna = transacciones.transferirEntreMisMonederos(
                monederoAna, ahorros, Dinero.de(2000), "a ahorros");

        assertEquals(0, interna.getPuntosGenerados());
        assertEquals(50, puntos.consultarPuntosAcumulados(ana.getId()));
    }

    @Test
    void elMultiplicadorDelRangoSeAplica() {
        transacciones.depositar(monederoAna, Dinero.de(60000), "deposito"); // 600 -> Plata
        assertEquals(Rango.PLATA, ana.getRango());

        Transaccion t = transacciones.depositar(monederoAna, Dinero.de(1000), "deposito");

        // 10 puntos x 1.10 (Plata) = 11
        assertEquals(11, t.getPuntosGenerados());
    }

    // ============================================================ rangos

    @Test
    void elRangoSeActualizaAlCambiarLosPuntos() {
        transacciones.depositar(monederoAna, Dinero.de(50000), "deposito"); // 500
        assertEquals(Rango.BRONCE, ana.getRango());                         // limite de Bronce

        transacciones.depositar(monederoAna, Dinero.de(100), "deposito");   // 501
        assertEquals(Rango.PLATA, ana.getRango());

        transacciones.depositar(monederoAna, Dinero.de(50000), "deposito"); // 500 x 1.10 = 550 -> 1051
        assertEquals(Rango.ORO, ana.getRango());

        RangoService rangos = contexto.getRangoService();
        assertEquals(1, rangos.contarClientes(Rango.ORO));
        assertEquals(ana.getId(), rangos.clientesDeRango(Rango.ORO).obtenerValorNodo(0));
        assertTrue(rangos.estaBalanceado());
    }

    @Test
    void clasificacionOrdenaDeMayorAMenor() {
        Cliente carla = contexto.getClienteService().registrarCliente("Carla", "carla@prueba.com", "1234");
        transacciones.depositar(monederoAna, Dinero.de(30000), "deposito");  // 300
        transacciones.depositar(monederoBeto, Dinero.de(70000), "deposito"); // 700
        puntos.sincronizarClientes();                                       // Carla aparece con 0

        ListaEnlazada<RegistroPuntos> top = puntos.clasificacion(3);

        assertEquals(beto.getId(), top.obtenerValorNodo(0).getClienteId());
        assertEquals(ana.getId(), top.obtenerValorNodo(1).getClienteId());
        assertEquals(carla.getId(), top.obtenerValorNodo(2).getClienteId());
    }

    // ============================================================ reversiones

    @Test
    void revertirRestaLosPuntosYPuedeBajarDeRango() {
        transacciones.depositar(monederoAna, Dinero.de(50000), "deposito"); // 500
        transacciones.depositar(monederoAna, Dinero.de(1000), "deposito");  // 10 -> 510 Plata
        assertEquals(Rango.PLATA, ana.getRango());

        transacciones.revertirUltima(monederoAna);

        assertEquals(500, puntos.consultarPuntosAcumulados(ana.getId()));
        assertEquals(500, puntos.consultarPuntosDisponibles(ana.getId()));
        assertEquals(Rango.BRONCE, ana.getRango());

        ListaEnlazada<MovimientoPuntos> historial = puntos.consultarHistorial(ana.getId());
        MovimientoPuntos ultimo = historial.obtenerValorNodo(historial.getTamano() - 1);
        assertEquals(TipoMovimientoPuntos.REVERTIDOS, ultimo.getTipo());
        assertEquals(-10, ultimo.getPuntos());
    }

    @Test
    void noSePuedeRevertirSiLosPuntosYaSeCanjearon() {
        transacciones.depositar(monederoAna, Dinero.de(10000), "deposito"); // 100 puntos
        contexto.getBeneficioService()
                .canjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null); // gasta los 100

        assertThrows(TransaccionInvalidaException.class,
                () -> transacciones.revertirUltima(monederoAna));

        // No cambio nada: ni el dinero ni los puntos.
        assertEquals(0, transacciones.consultarSaldo(monederoAna).compareTo(Dinero.de(10000)));
        assertEquals(100, puntos.consultarPuntosAcumulados(ana.getId()));
    }

    private String principal(Cliente cliente) {
        return contexto.getClienteService().monederosDe(cliente.getId()).obtenerValorNodo(0).getId();
    }
}