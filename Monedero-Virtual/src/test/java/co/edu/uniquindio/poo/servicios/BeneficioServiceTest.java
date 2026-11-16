package co.edu.uniquindio.poo.servicios;


import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.excepciones.PuntosInsuficientesException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoBeneficio;
import co.edu.uniquindio.poo.modelo.enums.TipoMovimientoPuntos;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del canje de puntos por beneficios y de los descuentos que producen.
 */
class BeneficioServiceTest {

    private ContextoMonedero contexto;
    private TransaccionService transacciones;
    private PuntosService puntos;
    private BeneficioService beneficios;
    private Cliente ana;
    private String monederoAna;
    private String monederoBeto;

    @BeforeEach
    void setUp() {
        contexto = new ContextoMonedero(new RelojSimulado(LocalDateTime.of(2026, 11, 15, 10, 0)));
        transacciones = contexto.getTransaccionService();
        puntos = contexto.getPuntosService();
        beneficios = contexto.getBeneficioService();

        ClienteService clientes = contexto.getClienteService();
        ana = clientes.registrarCliente("Ana", "ana@prueba.com", "1234");
        Cliente beto = clientes.registrarCliente("Beto", "beto@prueba.com", "1234");
        monederoAna = clientes.monederosDe(ana.getId()).obtenerValorNodo(0).getId();
        monederoBeto = clientes.monederosDe(beto.getId()).obtenerValorNodo(0).getId();
    }

    @Test
    void canjeConPuntosSuficientes() {
        transacciones.depositar(monederoAna, Dinero.de(10000), "deposito"); // 100 puntos

        beneficios.canjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);

        assertEquals(0, puntos.consultarPuntosDisponibles(ana.getId()));
        assertEquals(100, puntos.consultarPuntosAcumulados(ana.getId()));
        assertTrue(beneficios.tieneVigente(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS));

        ListaEnlazada<MovimientoPuntos> historial = puntos.consultarHistorial(ana.getId());
        MovimientoPuntos ultimo = historial.obtenerValorNodo(historial.getTamano() - 1);
        assertEquals(TipoMovimientoPuntos.CANJEADOS, ultimo.getTipo());
        assertEquals(-100, ultimo.getPuntos());
    }

    @Test
    void canjeConPuntosInsuficientesNoQuitaNada() {
        transacciones.depositar(monederoAna, Dinero.de(5000), "deposito"); // 50 puntos

        assertFalse(beneficios.puedeCanjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS));
        assertThrows(PuntosInsuficientesException.class,
                () -> beneficios.canjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null));

        assertEquals(50, puntos.consultarPuntosDisponibles(ana.getId()));
        assertFalse(beneficios.tieneVigente(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS));
    }

    @Test
    void bonoAcreditaCincuentaYCanjearNoBajaDeRango() {
        transacciones.depositar(monederoAna, Dinero.de(100000), "deposito"); // 1000 puntos -> Plata
        assertEquals(Rango.PLATA, ana.getRango());

        beneficios.canjear(ana.getId(), TipoBeneficio.BONO_SALDO, monederoAna);

        assertEquals(0, transacciones.consultarSaldo(monederoAna).compareTo(Dinero.de(100050)));
        assertEquals(0, puntos.consultarPuntosDisponibles(ana.getId()));
        // El rango depende de los acumulados, que no cambian al canjear.
        assertEquals(1000, puntos.consultarPuntosAcumulados(ana.getId()));
        assertEquals(Rango.PLATA, ana.getRango());
    }

    @Test
    void elBonoSoloSePuedeRecibirEnUnMonederoPropio() {
        transacciones.depositar(monederoAna, Dinero.de(100000), "deposito"); // 1000 puntos

        assertThrows(MonederoException.class,
                () -> beneficios.canjear(ana.getId(), TipoBeneficio.BONO_SALDO, monederoBeto));

        // La validacion va antes de descontar: no pierde puntos.
        assertEquals(1000, puntos.consultarPuntosDisponibles(ana.getId()));
    }

    @Test
    void descuentoCanjeadoBajaLaComisionDeTransferencia() {
        transacciones.depositar(monederoAna, Dinero.de(10000), "deposito"); // 100 puntos
        beneficios.canjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);

        Transaccion t = transacciones.transferir(monederoAna, monederoBeto, Dinero.de(1000), "a Beto");

        // 1000 x 1.5 % = 15.00; menos 10 % del canje = 13.50
        assertEquals(0, t.getComision().compareTo(Dinero.de("13.50")));
    }

    @Test
    void retirosSinCargoDuranteTreintaDias() {
        transacciones.depositar(monederoAna, Dinero.de(50000), "deposito"); // 500 puntos
        beneficios.canjear(ana.getId(), TipoBeneficio.RETIROS_SIN_CARGO, null);

        Transaccion gratis = transacciones.retirar(monederoAna, Dinero.de(1000), "retiro");
        assertEquals(0, gratis.getComision().compareTo(Dinero.CERO));

        contexto.getReloj().avanzarDias(31);
        assertFalse(beneficios.tieneVigente(ana.getId(), TipoBeneficio.RETIROS_SIN_CARGO));

        Transaccion conComision = transacciones.retirar(monederoAna, Dinero.de(1000), "retiro");
        assertEquals(0, conComision.getComision().compareTo(Dinero.de(10))); // 1 %
    }

    @Test
    void noSePuedeCanjearDosVecesUnDescuentoActivo() {
        transacciones.depositar(monederoAna, Dinero.de(20000), "deposito"); // 200 puntos
        beneficios.canjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);

        assertThrows(MonederoException.class,
                () -> beneficios.canjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null));
        assertEquals(100, puntos.consultarPuntosDisponibles(ana.getId()));
    }
}