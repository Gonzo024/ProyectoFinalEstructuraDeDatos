package co.edu.uniquindio.poo.estructuras.pila;

import co.edu.uniquindio.poo.estructuras.lista.NodoLista;
import java.util.NoSuchElementException;

public class Pila<T> {

    private NodoLista<T> nodoCima;
    private int tamano;

    public Pila() {
        this.nodoCima = null;
        this.tamano = 0;
    }

    public boolean estaVacia() {
        return nodoCima == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void push(T dato) {
        NodoLista<T> nuevo = new NodoLista<T>(dato);
        nuevo.setSiguiente(nodoCima);
        nodoCima = nuevo;
        tamano++;
    }

    public T pop() {
        if (estaVacia()) {
            throw new NoSuchElementException("La pila esta vacia");
        }
        T dato = nodoCima.getDato();
        nodoCima = nodoCima.getSiguiente();
        tamano--;
        return dato;
    }

    public T cima() {
        if (estaVacia()) {
            throw new NoSuchElementException("La pila esta vacia");
        }
        return nodoCima.getDato();
    }
}
