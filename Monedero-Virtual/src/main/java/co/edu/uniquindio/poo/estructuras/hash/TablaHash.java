package co.edu.uniquindio.poo.estructuras.hash;


import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;

/**
 * Tabla hash genérica con encadenamiento separado (cada cubeta es una lista de nodos).
 * <p>
 * Uso en el proyecto: acceso O(1) promedio a clientes, monederos, transacciones y, como
 * pide el enunciado, a los puntos acumulados de cada cliente ({@code PuntosService}).
 * También es la lista de adyacencia del {@code GrafoDirigido}.
 * <p>
 * Cuando el factor de carga supera 0.75 la tabla duplica su capacidad y redistribuye
 * (rehash), para que las cadenas se mantengan cortas.
 */
public class TablaHash<K, V> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAX = 0.75;

    private static class Entrada<K, V> {
        final K clave;
        V valor;
        Entrada<K, V> siguiente;

        Entrada(K clave, V valor, Entrada<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Entrada<K, V>[] cubetas;
    private int tamanio;

    public TablaHash() {
        this(CAPACIDAD_INICIAL);
    }

    @SuppressWarnings("unchecked")
    public TablaHash(int capacidad) {
        cubetas = (Entrada<K, V>[]) new Entrada[Math.max(4, capacidad)];
    }

    /** Inserta o reemplaza. Devuelve el valor anterior (o null). */
    public V insertar(K clave, V valor) {
        if (clave == null) throw new IllegalArgumentException("La clave no puede ser null");
        int i = indice(clave, cubetas.length);
        for (Entrada<K, V> e = cubetas[i]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                V anterior = e.valor;
                e.valor = valor;
                return anterior;
            }
        }
        cubetas[i] = new Entrada<>(clave, valor, cubetas[i]);
        tamanio++;
        if ((double) tamanio / cubetas.length > FACTOR_CARGA_MAX) redimensionar();
        return null;
    }

    public V obtener(K clave) {
        if (clave == null) return null;
        for (Entrada<K, V> e = cubetas[indice(clave, cubetas.length)]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) return e.valor;
        }
        return null;
    }

    public V obtenerODefecto(K clave, V porDefecto) {
        V v = obtener(clave);
        return v != null ? v : porDefecto;
    }

    public boolean contieneClave(K clave) {
        if (clave == null) return false;
        for (Entrada<K, V> e = cubetas[indice(clave, cubetas.length)]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) return true;
        }
        return false;
    }

    public V eliminar(K clave) {
        if (clave == null) return null;
        int i = indice(clave, cubetas.length);
        Entrada<K, V> previo = null;
        for (Entrada<K, V> e = cubetas[i]; e != null; previo = e, e = e.siguiente) {
            if (e.clave.equals(clave)) {
                if (previo == null) cubetas[i] = e.siguiente;
                else previo.siguiente = e.siguiente;
                tamanio--;
                return e.valor;
            }
        }
        return null;
    }

    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> lista = new ListaEnlazada<>();
        for (Entrada<K, V> cubeta : cubetas) {
            for (Entrada<K, V> e = cubeta; e != null; e = e.siguiente) lista.agregarFinal(e.clave);
        }
        return lista;
    }

    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> lista = new ListaEnlazada<>();
        for (Entrada<K, V> cubeta : cubetas) {
            for (Entrada<K, V> e = cubeta; e != null; e = e.siguiente) lista.agregarFinal(e.valor);
        }
        return lista;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public void limpiar() {
        for (int i = 0; i < cubetas.length; i++) cubetas[i] = null;
        tamanio = 0;
    }

    public double factorCarga() {
        return (double) tamanio / cubetas.length;
    }

    // ---------------------------------------------------------------- interno

    /** Dispersa los bits altos del hashCode para reducir colisiones y evita índices negativos. */
    private int indice(K clave, int capacidad) {
        int h = clave.hashCode();
        h ^= (h >>> 16);
        return (h & 0x7fffffff) % capacidad;
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        Entrada<K, V>[] viejas = cubetas;
        cubetas = (Entrada<K, V>[]) new Entrada[viejas.length * 2];
        for (Entrada<K, V> cubeta : viejas) {
            Entrada<K, V> e = cubeta;
            while (e != null) {
                Entrada<K, V> sig = e.siguiente;
                int i = indice(e.clave, cubetas.length);
                e.siguiente = cubetas[i];
                cubetas[i] = e;
                e = sig;
            }
        }
    }
}
