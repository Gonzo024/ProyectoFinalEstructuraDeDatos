package co.edu.uniquindio.poo.estructuras.grafo;

import co.edu.uniquindio.poo.estructuras.hash.TablaHash;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.util.Dinero;

import java.math.BigDecimal;

/**
 * Grafo dirigido y con pesos, guardado como LISTA DE ADYACENCIA:
 * una TablaHash donde la clave es el id de un vértice ("M1", "C1", "ALIMENTACION"...)
 * y el valor es la lista de flechas (Aristas) que SALEN de ese vértice.
 *
 *
 * Se usa para el multimonedero (monederos de un cliente) y para el análisis
 * de transferencias entre clientes y de gastos por categoría.
 */
public class GrafoDirigido {

    private final TablaHash<String, ListaEnlazada<Arista>> adyacencia = new TablaHash<>();

    // ============= vértices

    /** Agrega el vértice si no existe todavía (con su lista de flechas vacía). O(1) promedio. */
    public void agregarVertice(String id) {
        if (!adyacencia.contieneClave(id)) {
            adyacencia.insertar(id, new ListaEnlazada<>());
        }
    }

    public boolean contieneVertice(String id) {
        return adyacencia.contieneClave(id);
    }

    public int cantidadVertices() {
        return adyacencia.tamanio();
    }

    public ListaEnlazada<String> vertices() {
        return adyacencia.claves();
    }

    // ========== aristas

    /**
     * Registra una transferencia de "origen" a "destino".
     * Si la flecha ya existe se le suma el monto; si no, se crea.
     */
    public void agregarArista(String origen, String destino, BigDecimal monto) {
        agregarVertice(origen);
        agregarVertice(destino);
        Arista arista = buscarArista(origen, destino);
        if (arista == null) {
            arista = new Arista(origen, destino);
            adyacencia.obtener(origen).agregarFinal(arista);
        }
        arista.sumar(monto);
    }

    /**
     * Deshace una transferencia (cuando se revierte). Si la flecha queda vacía, se borra.
     */
    public void restarArista(String origen, String destino, BigDecimal monto) {
        Arista arista = buscarArista(origen, destino);
        if (arista == null) {
            throw new IllegalArgumentException("No existe la arista " + origen + " -> " + destino);
        }
        arista.restar(monto);
        if (arista.estaVacia()) {
            adyacencia.obtener(origen).eliminar(arista);
        }
    }

    /** Busca la flecha origen -> destino recorriendo la lista del origen. Devuelve null si no existe. */
    public Arista buscarArista(String origen, String destino) {
        ListaEnlazada<Arista> salientes = adyacencia.obtener(origen);
        if (salientes == null) {
            return null;
        }
        for (Arista arista : salientes) {
            if (arista.getDestino().equals(destino)) {
                return arista;
            }
        }
        return null;
    }

    /** Flechas que salen de un vértice (lista vacía si el vértice no existe). */
    public ListaEnlazada<Arista> aristasDesde(String origen) {
        ListaEnlazada<Arista> salientes = adyacencia.obtener(origen);
        return salientes == null ? new ListaEnlazada<>() : salientes;
    }

    /** Total de dinero que ha pasado de origen a destino (0 si no hay flecha). */
    public BigDecimal pesoEntre(String origen, String destino) {
        Arista arista = buscarArista(origen, destino);
        return arista == null ? Dinero.CERO : arista.getPeso();
    }

    /** Total de dinero que ha SALIDO de un vértice (suma de sus flechas). */
    public BigDecimal totalSaliente(String id) {
        BigDecimal total = Dinero.CERO;
        for (Arista arista : aristasDesde(id)) {
            total = total.add(arista.getPeso());
        }
        return total;
    }

    /** Total de dinero que ha LLEGADO a un vértice (revisa las flechas de todos los vértices). */
    public BigDecimal totalEntrante(String id) {
        BigDecimal total = Dinero.CERO;
        for (String vertice : vertices()) {
            Arista arista = buscarArista(vertice, id);
            if (arista != null) {
                total = total.add(arista.getPeso());
            }
        }
        return total;
    }

    // ============================================================ recorrido (recursivo)

    /**
     * Vértices a los que llega dinero desde "origen", directa o indirectamente
     * (p. ej. M1 -> M2 -> M3: desde M1 se alcanzan M2 y M3).
     * Recorrido en profundidad (DFS) recursivo.
     */
    public ListaEnlazada<String> alcanzablesDesde(String origen) {
        ListaEnlazada<String> visitados = new ListaEnlazada<>();
        if (contieneVertice(origen)) {
            visitar(origen, visitados);
            visitados.eliminar(origen); // el origen no cuenta como alcanzable
        }
        return visitados;
    }

    private void visitar(String vertice, ListaEnlazada<String> visitados) {
        if (visitados.obtenerPosicionNodo(vertice) != -1) {
            return; // caso base: ya lo visitamos (evita ciclos infinitos)
        }
        visitados.agregarFinal(vertice);
        for (Arista arista : aristasDesde(vertice)) {
            visitar(arista.getDestino(), visitados); // paso recursivo
        }
    }
}
