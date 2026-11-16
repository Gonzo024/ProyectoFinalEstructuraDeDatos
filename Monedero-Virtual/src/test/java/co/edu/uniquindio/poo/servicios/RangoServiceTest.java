package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de RangoService: clasificacion en Bronce, Plata, Oro y Platino usando el arbol AVL.
 * Se usa el ContextoMonedero real, asi los puntos llegan por las transacciones (patron Observador).
 */
class RangoServiceTest {

    private ContextoMonedero contexto;
    private ClienteService clientes;
    private TransaccionService transacciones;
    private PuntosService puntos;
    private RangoService rangos;
    private Cliente ana;
    private Cliente beto;
    private String monederoAna;
    private String monederoBeto;

    @BeforeEach
    void setUp() {
        contexto = new ContextoMonedero(new RelojSimulado(LocalDateTime.of(2026, 11, 15, 10, 0)));
        clientes = contexto.getClienteService();
        transacciones = contexto.getTransaccionService();
        puntos = contexto.getPuntosService();
        rangos = contexto.getRangoService();

        ana = clientes.registrarCliente("Ana", "ana@prueba.com", "1234");
        beto = clientes.registrarCliente("Beto", "beto@prueba.com", "1234");
        monederoAna = principal(ana);
        monederoBeto = principal(beto);
    }

    // ============================================================ funcion de clasificacion

    @Test
    void losLimitesDeCadaRangoSonLosDelEnunciado() {
        assertEquals(Rango.BRONCE, Rango.desdePuntos(0));
        assertEquals(Rango.BRONCE, Rango.desdePuntos(500));
        assertEquals(Rango.PLATA, Rango.desdePuntos(501));
        assertEquals(Rango.PLATA, Rango.desdePuntos(1000));
        assertEquals(Rango.ORO, Rango.desdePuntos(1001));
        assertEquals(Rango.ORO, Rango.desdePuntos(5000));
        assertEquals(Rango.PLATINO, Rango.desdePuntos(5001));
        assertEquals(Rango.PLATINO, Rango.desdePuntos(1_000_000));
    }

    // ============================================================ arbol de rangos

    @Test
    void losClientesNuevosEntranEnBronce() {
        puntos.sincronizarClientes(); // entran al arbol con 0 puntos

        assertEquals(2, rangos.cantidadClientes());
        assertEquals(2, rangos.contarClientes(Rango.BRONCE));
        assertEquals(0, rangos.contarClientes(Rango.PLATA));
        assertEquals(0, rangos.contarClientes(Rango.ORO));
        assertEquals(0, rangos.contarClientes(Rango.PLATINO));
    }

    @Test
    void alSubirDeRangoElClienteCambiaDeGrupoSinDuplicarse() {
        puntos.sincronizarClientes();

        transacciones.depositar(monederoAna, Dinero.de(60000), "deposito"); // 600 puntos -> Plata

        assertEquals(Rango.PLATA, ana.getRango());
        assertEquals(1, rangos.contarClientes(Rango.PLATA));
        assertEquals(1, rangos.contarClientes(Rango.BRONCE));
        assertEquals(ana.getId(), rangos.clientesDeRango(Rango.PLATA).obtenerValorNodo(0));
        // Se elimino la clave vieja y se inserto la nueva: sigue habiendo un nodo por cliente.
        assertEquals(2, rangos.cantidadClientes());
    }

    @Test
    void alBajarDeRangoElClienteVuelveAlGrupoAnterior() {
        transacciones.depositar(monederoAna, Dinero.de(50000), "deposito"); // 500 -> Bronce
        transacciones.depositar(monederoAna, Dinero.de(1000), "deposito");  // 510 -> Plata
        assertEquals(1, rangos.contarClientes(Rango.PLATA));

        transacciones.revertirUltima(monederoAna);                          // vuelve a 500

        assertEquals(0, rangos.contarClientes(Rango.PLATA));
        assertEquals(1, rangos.contarClientes(Rango.BRONCE));
        assertEquals(1, rangos.cantidadClientes());
    }

    @Test
    void losClientesDeUnRangoSalenDeMenosAMasPuntos() {
        Cliente carla = clientes.registrarCliente("Carla", "carla@prueba.com", "1234");
        transacciones.depositar(monederoAna, Dinero.de(20000), "deposito");     // 200
        transacciones.depositar(monederoBeto, Dinero.de(40000), "deposito");    // 400
        transacciones.depositar(principal(carla), Dinero.de(10000), "deposito"); // 100

        ListaEnlazada<String> bronce = rangos.clientesDeRango(Rango.BRONCE);

        assertEquals(3, bronce.getTamano());
        assertEquals(carla.getId(), bronce.obtenerValorNodo(0));
        assertEquals(ana.getId(), bronce.obtenerValorNodo(1));
        assertEquals(beto.getId(), bronce.obtenerValorNodo(2));
    }

    // ============================================================ notificaciones

    @Test
    void soloElCambioDeRangoGeneraNotificacion() {
        NotificacionService notificaciones = contexto.getNotificacionService();
        int antes = notificaciones.cantidadPendientes();

        transacciones.depositar(monederoAna, Dinero.de(10000), "deposito"); // 100: sigue en Bronce
        assertEquals(antes, notificaciones.cantidadPendientes());

        transacciones.depositar(monederoAna, Dinero.de(50000), "deposito"); // 600: sube a Plata
        assertEquals(antes + 1, notificaciones.cantidadPendientes());
    }

    // ============================================================ balance

    @Test
    void elArbolSigueBalanceadoAunqueLosPuntosLleguenEnOrden() {
        // 200 clientes con puntos crecientes (1, 2, 3...): el peor caso para un ABB sin balancear.
        for (int i = 1; i <= 200; i++) {
            Cliente cliente = clientes.registrarCliente("Cliente " + i, "c" + i + "@prueba.com", "1234");
            transacciones.depositar(principal(cliente), Dinero.de(i * 100), "deposito");
        }

        assertEquals(200, rangos.cantidadClientes());
        assertTrue(rangos.estaBalanceado());
        // Un AVL de 200 nodos no puede medir mas de 10 niveles.
        assertTrue(rangos.altura() <= 10, "altura del AVL: " + rangos.altura());
        // El ABB de la clasificacion recibio las mismas claves y quedo mucho mas alto.
        assertTrue(puntos.alturaRanking() > rangos.altura());
    }

    private String principal(Cliente cliente) {
        return clientes.monederosDe(cliente.getId()).obtenerValorNodo(0).getId();
    }
}
