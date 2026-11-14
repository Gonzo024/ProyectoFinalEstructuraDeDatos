package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.grafo.Arista;
import co.edu.uniquindio.poo.estructuras.grafo.GrafoDirigido;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;

import java.math.BigDecimal;

/**
 * Multimonedero epresenta con un GRAFO DIRIGIDO cómo se mueve el dinero
 * entre los monederos de un mismo cliente.
 * <p>
 * - Vértices: ids de monederos ("M1", "M2"...).
 * - Flecha M1 -> M2: dinero que salió de M1 hacia M2, con el total acumulado (peso).
 * <p>
 * Es un TransaccionListener: se inscribe en TransaccionService y el grafo se llena solo
 * cada vez que hay una transferencia entre monederos propios (y se descuenta si se revierte).
 */
public class MultimonederoService implements TransaccionListener {

    private final ClienteService clienteService;
    private final GrafoDirigido grafo = new GrafoDirigido();

    public MultimonederoService(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // ============reacciones (listener)

    @Override
    public void onTransaccionCompletada(Transaccion t) {
        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA) {
            grafo.agregarArista(t.getMonederoOrigenId(), t.getMonederoDestinoId(), t.getMonto());
        }
    }

    @Override
    public void onTransaccionRevertida(Transaccion t) {
        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA) {
            grafo.restarArista(t.getMonederoOrigenId(), t.getMonederoDestinoId(), t.getMonto());
        }
    }

    // =======consultas

    /** Todas las flechas entre los monederos de un cliente (para mostrar en la interfaz). */
    public ListaEnlazada<Arista> movimientosDelCliente(String clienteId) {
        ListaEnlazada<Arista> resultado = new ListaEnlazada<>();
        for (Monedero monedero : clienteService.monederosDe(clienteId)) {
            for (Arista arista : grafo.aristasDesde(monedero.getId())) {
                resultado.agregarFinal(arista);
            }
        }
        return resultado;
    }

    /** Total que ha pasado de un monedero a otro. */
    public BigDecimal totalMovido(String monederoOrigenId, String monederoDestinoId) {
        return grafo.pesoEntre(monederoOrigenId, monederoDestinoId);
    }

    /** Cuánto ha enviado un monedero a los demás monederos del cliente. */
    public BigDecimal totalEnviado(String monederoId) {
        return grafo.totalSaliente(monederoId);
    }

    /** Cuánto ha recibido un monedero de los demás monederos del cliente. */
    public BigDecimal totalRecibido(String monederoId) {
        return grafo.totalEntrante(monederoId);
    }

    /** Monederos a los que ha llegado dinero desde este, directa o indirectamente (DFS recursivo). */
    public ListaEnlazada<String> destinosAlcanzables(String monederoId) {
        return grafo.alcanzablesDesde(monederoId);
    }

    public GrafoDirigido getGrafo() {
        return grafo;
    }
}
