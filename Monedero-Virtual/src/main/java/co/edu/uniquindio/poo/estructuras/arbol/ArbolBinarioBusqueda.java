package co.edu.uniquindio.poo.estructuras.arbol;



import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;

import java.util.function.BiConsumer;

/**
 * Árbol binario de búsqueda (ABB) genérico clave → valor.
 * <p>
 * Uso en el proyecto: ranking global de puntos ({@code PuntosService}). La clave es
 * {@code ClavePuntos(puntos, clienteId)}, así el recorrido inorden inverso entrega el
 * top de clientes y {@link #rango(Comparable, Comparable)} los clientes entre dos puntajes.
 * <p>
 * Complejidad: O(h) para insertar/buscar/eliminar, donde h es la altura. En promedio
 * h = O(log n), pero si las claves llegan ordenadas el árbol degenera en lista (h = n).
 * Por eso {@link ArbolAVL} extiende esta clase y sobrescribe {@link #balancear(NodoArbol)}
 * para garantizar h = O(log n).
 * <p>
 * Patrón "método plantilla": la inserción y eliminación son recursivas y, al volver de la
 * recursión, cada nodo pasa por {@code balancear}. Aquí solo actualiza la altura.
 */
public class ArbolBinarioBusqueda<K extends Comparable<K>, V> {

    protected NodoArbol<K, V> raiz;
    private int tamanio;

    // ---------------------------------------------------------------- inserción

    /** Inserta o reemplaza el valor asociado a la clave. */
    public void insertar(K clave, V valor) {
        if (clave == null) throw new IllegalArgumentException("La clave no puede ser null");
        raiz = insertar(raiz, clave, valor);
    }

    private NodoArbol<K, V> insertar(NodoArbol<K, V> nodo, K clave, V valor) {
        if (nodo == null) {
            tamanio++;
            return new NodoArbol<>(clave, valor);
        }
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) nodo.izquierdo = insertar(nodo.izquierdo, clave, valor);
        else if (cmp > 0) nodo.derecho = insertar(nodo.derecho, clave, valor);
        else nodo.valor = valor; // clave repetida: se actualiza
        return balancear(nodo);
    }

    // ---------------------------------------------------------------- búsqueda

    public V buscar(K clave) {
        NodoArbol<K, V> n = raiz;
        while (n != null) {
            int cmp = clave.compareTo(n.clave);
            if (cmp == 0) return n.valor;
            n = (cmp < 0) ? n.izquierdo : n.derecho;
        }
        return null;
    }

    public boolean contiene(K clave) {
        return buscar(clave) != null;
    }

    public K minimo() {
        if (raiz == null) return null;
        return minimo(raiz).clave;
    }

    public K maximo() {
        if (raiz == null) return null;
        NodoArbol<K, V> n = raiz;
        while (n.derecho != null) n = n.derecho;
        return n.clave;
    }

    // ---------------------------------------------------------------- eliminación

    public boolean eliminar(K clave) {
        int antes = tamanio;
        raiz = eliminar(raiz, clave);
        return tamanio < antes;
    }

    private NodoArbol<K, V> eliminar(NodoArbol<K, V> nodo, K clave) {
        if (nodo == null) return null;
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) {
            nodo.izquierdo = eliminar(nodo.izquierdo, clave);
        } else if (cmp > 0) {
            nodo.derecho = eliminar(nodo.derecho, clave);
        } else {
            // caso 1 y 2: cero o un hijo
            if (nodo.izquierdo == null || nodo.derecho == null) {
                tamanio--;
                return nodo.izquierdo != null ? nodo.izquierdo : nodo.derecho;
            }
            // caso 3: dos hijos -> se reemplaza por el sucesor inorden
            NodoArbol<K, V> sucesor = minimo(nodo.derecho);
            nodo.clave = sucesor.clave;
            nodo.valor = sucesor.valor;
            nodo.derecho = eliminar(nodo.derecho, sucesor.clave);
        }
        return balancear(nodo);
    }

    private NodoArbol<K, V> minimo(NodoArbol<K, V> nodo) {
        while (nodo.izquierdo != null) nodo = nodo.izquierdo;
        return nodo;
    }

    // ---------------------------------------------------------------- recorridos

    /** Valores en orden ascendente de clave. */
    public ListaEnlazada<V> inOrden() {
        ListaEnlazada<V> lista = new ListaEnlazada<>();
        inOrden(raiz, (k, v) -> lista.agregarFinal(v));
        return lista;
    }

    /** Recorre en inorden aplicando la acción a cada par clave/valor. */
    public void recorrerInOrden(BiConsumer<K, V> accion) {
        inOrden(raiz, accion);
    }

    private void inOrden(NodoArbol<K, V> nodo, BiConsumer<K, V> accion) {
        if (nodo == null) return;
        inOrden(nodo.izquierdo, accion);
        accion.accept(nodo.clave, nodo.valor);
        inOrden(nodo.derecho, accion);
    }

    /** Los {@code n} valores con las claves más grandes, de mayor a menor. O(h + n). */
    public ListaEnlazada<V> mayores(int n) {
        ListaEnlazada<V> lista = new ListaEnlazada<>();
        mayores(raiz, n, lista);
        return lista;
    }

    private void mayores(NodoArbol<K, V> nodo, int n, ListaEnlazada<V> acumulado) {
        if (nodo == null || acumulado.getTamano() >= n) return;
        mayores(nodo.derecho, n, acumulado);
        if (acumulado.getTamano() < n) acumulado.agregarFinal(nodo.valor);
        mayores(nodo.izquierdo, n, acumulado);
    }

    /**
     * Valores cuyas claves están en [desde, hasta], en orden ascendente.
     * Poda las ramas que no pueden contener claves del rango: O(h + k).
     */
    public ListaEnlazada<V> rango(K desde, K hasta) {
        ListaEnlazada<V> lista = new ListaEnlazada<>();
        rango(raiz, desde, hasta, lista);
        return lista;
    }

    private void rango(NodoArbol<K, V> nodo, K desde, K hasta, ListaEnlazada<V> lista) {
        if (nodo == null) return;
        int cmpDesde = desde.compareTo(nodo.clave);
        int cmpHasta = hasta.compareTo(nodo.clave);
        if (cmpDesde < 0) rango(nodo.izquierdo, desde, hasta, lista);
        if (cmpDesde <= 0 && cmpHasta >= 0) lista.agregarFinal(nodo.valor);
        if (cmpHasta > 0) rango(nodo.derecho, desde, hasta, lista);
    }

    // ---------------------------------------------------------------- información

    public int altura() {
        return altura(raiz);
    }

    protected int altura(NodoArbol<K, V> nodo) {
        return nodo == null ? 0 : nodo.altura;
    }

    protected void actualizarAltura(NodoArbol<K, V> nodo) {
        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public NodoArbol<K, V> getRaiz() {
        return raiz;
    }

    public void limpiar() {
        raiz = null;
        tamanio = 0;
    }

    /**
     * Gancho que se ejecuta en cada nodo al volver de insertar/eliminar.
     * En un ABB simple solo se actualiza la altura; el AVL además rota.
     */
    protected NodoArbol<K, V> balancear(NodoArbol<K, V> nodo) {
        actualizarAltura(nodo);
        return nodo;
    }
}

