package co.edu.uniquindio.poo.estructuras.lista;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ListaEnlazada<T> implements Iterable<T> {

    private NodoLista<T> nodoPrimero;
    private NodoLista<T> nodoUltimo;
    private int tamano;

    public ListaEnlazada() {
        this.nodoPrimero = null;
        this.nodoUltimo = null;
        this.tamano = 0;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int getTamano() {
        return tamano;
    }

    public void agregarInicio(T dato) {
        NodoLista<T> nuevo = new NodoLista<T>(dato);
        if (estaVacia()) {
            nodoPrimero = nuevo;
            nodoUltimo = nuevo;
        } else {
            nuevo.setSiguiente(nodoPrimero);
            nodoPrimero = nuevo;
        }
        tamano++;
    }

    public void agregarFinal(T dato) {
        NodoLista<T> nuevo = new NodoLista<T>(dato);
        if (estaVacia()) {
            nodoPrimero = nuevo;
            nodoUltimo = nuevo;
        } else {
            nodoUltimo.setSiguiente(nuevo);
            nodoUltimo = nuevo;
        }
        tamano++;
    }

    public T obtenerValorNodo(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Indice invalido: " + indice);
        }
        NodoLista<T> actual = nodoPrimero;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }

    public int obtenerPosicionNodo(T dato) {
        NodoLista<T> actual = nodoPrimero;
        int posicion = 0;
        while (actual != null) {
            if (actual.getDato().equals(dato)) {
                return posicion;
            }
            actual = actual.getSiguiente();
            posicion++;
        }
        return -1;
    }

    public T eliminarPrimero() {
        if (estaVacia()) {
            throw new NoSuchElementException("La lista esta vacia");
        }
        T dato = nodoPrimero.getDato();
        nodoPrimero = nodoPrimero.getSiguiente();
        tamano--;
        if (nodoPrimero == null) {
            nodoUltimo = null;
        }
        return dato;
    }

    public T eliminarUltimo() {
        if (estaVacia()) {
            throw new NoSuchElementException("La lista esta vacia");
        }
        T dato = nodoUltimo.getDato();
        if (tamano == 1) {
            nodoPrimero = null;
            nodoUltimo = null;
        } else {
            NodoLista<T> penultimo = nodoPrimero;
            while (penultimo.getSiguiente() != nodoUltimo) {
                penultimo = penultimo.getSiguiente();
            }
            penultimo.setSiguiente(null);
            nodoUltimo = penultimo;
        }
        tamano--;
        return dato;
    }

    public boolean eliminar(T dato) {
        NodoLista<T> anterior = null;
        NodoLista<T> actual = nodoPrimero;
        while (actual != null && !actual.getDato().equals(dato)) {
            anterior = actual;
            actual = actual.getSiguiente();
        }
        if (actual == null) {
            return false;
        }
        if (anterior == null) {
            eliminarPrimero();
        } else {
            anterior.setSiguiente(actual.getSiguiente());
            if (actual == nodoUltimo) {
                nodoUltimo = anterior;
            }
            tamano--;
        }
        return true;
    }

    public void borrarLista() {
        nodoPrimero = null;
        nodoUltimo = null;
        tamano = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteradorLista();
    }

    private class IteradorLista implements Iterator<T> {

        private NodoLista<T> actual = nodoPrimero;

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if (actual == null) {
                throw new NoSuchElementException();
            }
            T dato = actual.getDato();
            actual = actual.getSiguiente();
            return dato;
        }
    }
}