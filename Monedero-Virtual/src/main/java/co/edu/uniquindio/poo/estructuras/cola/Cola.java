package co.edu.uniquindio.poo.estructuras.cola;


import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cola (FIFO) enlazada.
 * <p>
 * Uso en el proyecto: cola de notificaciones pendientes por despachar. Las notificaciones
 * se generan en cualquier servicio, se encolan y se entregan en orden de llegada a la
 * bandeja (lista circular) de cada cliente. encolar/desencolar O(1).
 */
public class Cola<T> implements Iterable<T> {

    private static class Nodo<T> {
        final T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamanio;

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (fin == null) {
            frente = fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamanio++;
    }

    public T desencolar() {
        if (estaVacia()) throw new NoSuchElementException("La cola está vacía");
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamanio--;
        return dato;
    }

    public T frente() {
        if (estaVacia()) throw new NoSuchElementException("La cola está vacía");
        return frente.dato;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int tamanio() {
        return tamanio;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = frente;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) throw new NoSuchElementException();
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}
