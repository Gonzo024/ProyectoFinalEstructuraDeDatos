
package co.edu.uniquindio.poo.estructuras.cola;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class ColaTest {

    @Test
    void colaNuevaEstaVacia() {

        Cola<String> cola = new Cola<>();

        assertTrue(cola.estaVacia());
        assertEquals(0, cola.tamanio());
    }

    @Test
    void encolarAumentaElTamano() {

        Cola<String> cola = new Cola<>();

        cola.encolar("N1");
        cola.encolar("N2");

        assertFalse(cola.estaVacia());
        assertEquals(2, cola.tamanio());
    }

    @Test
    void desencolarRespetaOrdenFIFO() {

        Cola<String> cola = new Cola<>();

        cola.encolar("N1");
        cola.encolar("N2");
        cola.encolar("N3");

        assertEquals("N1", cola.desencolar());
        assertEquals("N2", cola.desencolar());
        assertEquals("N3", cola.desencolar());

        assertTrue(cola.estaVacia());
    }

    @Test
    void consultarFrenteNoEliminaElemento() {

        Cola<String> cola = new Cola<>();

        cola.encolar("N1");
        cola.encolar("N2");

        assertEquals("N1", cola.frente());
        assertEquals(2, cola.tamanio());
    }

    @Test
    void desencolarDeColaVaciaLanzaError() {

        Cola<String> cola = new Cola<>();

        assertThrows(
                NoSuchElementException.class,
                () -> cola.desencolar()
        );
    }

    @Test
    void consultarFrenteVacioLanzaError() {

        Cola<String> cola = new Cola<>();

        assertThrows(
                NoSuchElementException.class,
                () -> cola.frente()
        );
    }

    @Test
    void sePuedeVolverAEncolarDespuesDeVaciar() {

        Cola<String> cola = new Cola<>();

        cola.encolar("N1");
        cola.desencolar();

        cola.encolar("N2");

        assertEquals("N2", cola.frente());
        assertEquals(1, cola.tamanio());
    }

    @Test
    void iteradorRecorreEnOrden() {

        Cola<String> cola = new Cola<>();

        cola.encolar("N1");
        cola.encolar("N2");
        cola.encolar("N3");

        String resultado = "";

        for (String notificacion : cola) {
            resultado += notificacion;
        }

        assertEquals("N1N2N3", resultado);
    }
}

