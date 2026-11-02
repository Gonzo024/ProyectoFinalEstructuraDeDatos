package co.edu.uniquindio.poo.estructuras.lista;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class NodoListaTest {

    @Test
    void nodoNuevoGuardaElDatoYNoTieneSiguiente() {
        NodoLista<String> nodo = new NodoLista<String>("T1");

        assertEquals("T1", nodo.getDato());
        assertNull(nodo.getSiguiente());
    }

    @Test
    void sePuedeEnlazarUnNodoConOtro() {
        NodoLista<String> primero = new NodoLista<String>("T1");
        NodoLista<String> segundo = new NodoLista<String>("T2");

        primero.setSiguiente(segundo);

        assertSame(segundo, primero.getSiguiente());
    }
}
