package co.edu.uniquindio.poo.estructuras.lista;

import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ListaEnlazadaTest {

    private ListaEnlazada<String> crearListaABC() {
        ListaEnlazada<String> lista = new ListaEnlazada<String>();
        lista.agregarFinal("A");
        lista.agregarFinal("B");
        lista.agregarFinal("C");
        return lista;
    }

    @Test
    void listaNuevaEstaVacia() {
        ListaEnlazada<String> lista = new ListaEnlazada<String>();

        assertTrue(lista.estaVacia());
        assertEquals(0, lista.getTamano());
    }

    @Test
    void agregarFinalMantieneElOrden() {
        ListaEnlazada<String> lista = crearListaABC();

        assertFalse(lista.estaVacia());
        assertEquals(3, lista.getTamano());
        assertEquals("A", lista.obtenerValorNodo(0));
        assertEquals("B", lista.obtenerValorNodo(1));
        assertEquals("C", lista.obtenerValorNodo(2));
    }

    @Test
    void agregarInicioPoneElElementoAlPrincipio() {
        ListaEnlazada<String> lista = new ListaEnlazada<String>();
        lista.agregarFinal("B");
        lista.agregarInicio("A");

        assertEquals("A", lista.obtenerValorNodo(0));
        assertEquals("B", lista.obtenerValorNodo(1));
    }

    @Test
    void agregarFinalDespuesDeAgregarInicioEnListaVacia() {
        ListaEnlazada<String> lista = new ListaEnlazada<String>();
        lista.agregarInicio("A");
        lista.agregarFinal("B");

        assertEquals(2, lista.getTamano());
        assertEquals("B", lista.obtenerValorNodo(1));
    }

    @Test
    void obtenerPosicionNodoEncuentraODevuelveMenosUno() {
        ListaEnlazada<String> lista = crearListaABC();

        assertEquals(2, lista.obtenerPosicionNodo("C"));
        assertEquals(-1, lista.obtenerPosicionNodo("Z"));
    }

    @Test
    void eliminarPrimeroDevuelveElPrimeroYReduceElTamano() {
        ListaEnlazada<String> lista = crearListaABC();

        assertEquals("A", lista.eliminarPrimero());
        assertEquals(2, lista.getTamano());
        assertEquals("B", lista.obtenerValorNodo(0));
    }

    @Test
    void eliminarUltimoActualizaElFinalDeLaLista() {
        ListaEnlazada<String> lista = crearListaABC();

        assertEquals("C", lista.eliminarUltimo());
        assertEquals(2, lista.getTamano());

        lista.agregarFinal("D");
        assertEquals("B", lista.obtenerValorNodo(1));
        assertEquals("D", lista.obtenerValorNodo(2));
    }

    @Test
    void eliminarElUnicoElementoDejaLaListaVacia() {
        ListaEnlazada<String> lista = new ListaEnlazada<String>();
        lista.agregarFinal("A");

        lista.eliminarUltimo();
        assertTrue(lista.estaVacia());

        lista.agregarFinal("B");
        assertEquals("B", lista.obtenerValorNodo(0));
    }

    @Test
    void eliminarPrimeroEnListaVaciaLanzaError() {
        ListaEnlazada<String> lista = new ListaEnlazada<String>();
        try {
            lista.eliminarPrimero();
            fail("Debio lanzar NoSuchElementException");
        } catch (NoSuchElementException e) {
            // correcto: no se puede eliminar de una lista vacia
        }
    }

    @Test
    void eliminarPorValorDelMedio() {
        ListaEnlazada<String> lista = crearListaABC();

        assertTrue(lista.eliminar("B"));
        assertEquals(2, lista.getTamano());
        assertEquals("C", lista.obtenerValorNodo(1));
    }

    @Test
    void eliminarPorValorDelPrimero() {
        ListaEnlazada<String> lista = crearListaABC();

        assertTrue(lista.eliminar("A"));
        assertEquals("B", lista.obtenerValorNodo(0));
    }

    @Test
    void eliminarPorValorDelUltimoActualizaElFinal() {
        ListaEnlazada<String> lista = crearListaABC();

        assertTrue(lista.eliminar("C"));
        lista.agregarFinal("D");

        assertEquals(3, lista.getTamano());
        assertEquals("D", lista.obtenerValorNodo(2));
    }

    @Test
    void eliminarValorInexistenteDevuelveFalse() {
        ListaEnlazada<String> lista = crearListaABC();

        assertFalse(lista.eliminar("Z"));
        assertEquals(3, lista.getTamano());
    }

    @Test
    void sePuedeRecorrerConFor() {
        ListaEnlazada<String> lista = crearListaABC();
        String resultado = "";

        for (String elemento : lista) {
            resultado = resultado + elemento;
        }

        assertEquals("ABC", resultado);
    }

    @Test
    void borrarListaLaDejaVacia() {
        ListaEnlazada<String> lista = crearListaABC();

        lista.borrarLista();

        assertTrue(lista.estaVacia());
        assertEquals(0, lista.getTamano());
    }
}