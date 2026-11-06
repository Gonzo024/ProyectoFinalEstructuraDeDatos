package co.edu.uniquindio.poo.estructuras.cola;


import co.edu.uniquindio.poo.estructuras.cola.ColaPrioridad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColaPrioridadTest {

    @Test
    void extraerEnOrdenDePrioridad() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        cola.insertar(30);
        cola.insertar(10);
        cola.insertar(50);
        cola.insertar(20);

        assertEquals(10, cola.extraer());
        assertEquals(20, cola.extraer());
        assertEquals(30, cola.extraer());
        assertEquals(50, cola.extraer());
    }

    @Test
    void comprobarColaVacia() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        assertTrue(cola.estaVacia());

        cola.insertar(15);

        assertFalse(cola.estaVacia());
        assertEquals(1, cola.tamanio());
    }

    @Test
    void consultarPrimeroSinEliminarlo() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        cola.insertar(40);
        cola.insertar(5);
        cola.insertar(20);

        assertEquals(5, cola.primero());
        assertEquals(3, cola.tamanio());
    }

    @Test
    void ampliarCapacidadDeLaCola() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        for (int i = 20; i >= 1; i--) {
            cola.insertar(i);
        }

        assertEquals(20, cola.tamanio());

        for (int i = 1; i <= 20; i++) {
            assertEquals(i, cola.extraer());
        }

        assertTrue(cola.estaVacia());
    }

    @Test
    void extraerDeColaVaciaLanzaError() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        assertThrows(
            IllegalStateException.class,
            () -> cola.extraer()
        );
    }
}
