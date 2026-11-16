
package co.edu.uniquindio.poo.estructuras.lista;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListaCircularTest {

    @Test
    void listaNuevaEstaVacia() {

        ListaCircular<String> lista =
                new ListaCircular<>();

        assertTrue(lista.estaVacia());
        assertEquals(0, lista.tamanio());
    }

    @Test
    void agregarPrimerElemento() {

        ListaCircular<String> lista =
                new ListaCircular<>();

        lista.agregar("N1");

        assertFalse(lista.estaVacia());
        assertEquals(1, lista.tamanio());
        assertEquals("N1", lista.obtener(0));
    }

    @Test
    void agregarVariosMantieneElOrden() {

        ListaCircular<String> lista =
                new ListaCircular<>();

        lista.agregar("N1");
        lista.agregar("N2");
        lista.agregar("N3");

        assertEquals(3, lista.tamanio());

        assertEquals("N1", lista.obtener(0));
        assertEquals("N2", lista.obtener(1));
        assertEquals("N3", lista.obtener(2));
    }

    @Test
    void obtenerPosicionNegativaLanzaError() {

        ListaCircular<String> lista =
                new ListaCircular<>();

        lista.agregar("N1");

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> lista.obtener(-1)
        );
    }

    @Test
    void obtenerFueraDelTamanoLanzaError() {

        ListaCircular<String> lista =
                new ListaCircular<>();

        lista.agregar("N1");
        lista.agregar("N2");

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> lista.obtener(2)
        );
    }

    @Test
    void obtenerDeListaVaciaLanzaError() {

        ListaCircular<String> lista =
                new ListaCircular<>();

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> lista.obtener(0)
        );
    }

    @Test
    void agregarDespuesDeVariosElementos() {

        ListaCircular<String> lista =
                new ListaCircular<>();

        lista.agregar("N1");
        lista.agregar("N2");
        lista.agregar("N3");
        lista.agregar("N4");

        assertEquals(4, lista.tamanio());
        assertEquals("N4", lista.obtener(3));
        assertEquals("N1", lista.obtener(0));
    }
}
