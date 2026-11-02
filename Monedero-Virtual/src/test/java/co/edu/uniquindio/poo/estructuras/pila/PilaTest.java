package co.edu.uniquindio.poo.estructuras.pila;

import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PilaTest {

    @Test
    void pilaNuevaEstaVacia() {
        Pila<String> pila = new Pila<String>();

        assertTrue(pila.estaVacia());
        assertEquals(0, pila.getTamano());
    }

    @Test
    void pushYPopRespetanLIFO() {
        Pila<String> pila = new Pila<String>();
        pila.push("A");
        pila.push("B");
        pila.push("C");

        assertEquals(3, pila.getTamano());
        assertEquals("C", pila.pop());
        assertEquals("B", pila.pop());
        assertEquals("A", pila.pop());
        assertTrue(pila.estaVacia());
    }

    @Test
    void cimaMuestraElUltimoSinSacarlo() {
        Pila<String> pila = new Pila<String>();
        pila.push("A");
        pila.push("B");

        assertEquals("B", pila.cima());
        assertEquals(2, pila.getTamano());
        assertFalse(pila.estaVacia());
    }

    @Test
    void popEnPilaVaciaLanzaError() {
        Pila<String> pila = new Pila<String>();
        try {
            pila.pop();
            fail("Debio lanzar NoSuchElementException");
        } catch (NoSuchElementException e) {
            // correcto: no se puede desapilar de una pila vacia
        }
    }

    @Test
    void cimaEnPilaVaciaLanzaError() {
        Pila<String> pila = new Pila<String>();
        try {
            pila.cima();
            fail("Debio lanzar NoSuchElementException");
        } catch (NoSuchElementException e) {
            // correcto: no hay nada que mirar
        }
    }

    @Test
    void laPilaSePuedeReutilizarDespuesDeVaciarse() {
        Pila<String> pila = new Pila<String>();
        pila.push("A");
        pila.pop();
        pila.push("B");

        assertEquals("B", pila.cima());
        assertEquals(1, pila.getTamano());
    }
}
