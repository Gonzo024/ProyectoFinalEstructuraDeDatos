package co.edu.uniquindio.poo.estructuras.grafo;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.util.Dinero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GrafoDirigidoTest {

    @Test
    void transferenciasRepetidasSeSumanEnLaMismaArista() {
        GrafoDirigido grafo = new GrafoDirigido();
        grafo.agregarArista("M1", "M2", Dinero.de(200));
        grafo.agregarArista("M1", "M2", Dinero.de(100));

        assertEquals(2, grafo.cantidadVertices());
        assertEquals(1, grafo.aristasDesde("M1").getTamano());
        assertEquals(Dinero.de(300), grafo.pesoEntre("M1", "M2"));
        assertEquals(2, grafo.buscarArista("M1", "M2").getCantidad());
    }

    @Test
    void esDirigido() {
        GrafoDirigido grafo = new GrafoDirigido();
        grafo.agregarArista("M1", "M2", Dinero.de(200));

        assertEquals(Dinero.de(0), grafo.pesoEntre("M2", "M1"));
        assertNull(grafo.buscarArista("M2", "M1"));
    }

    @Test
    void restarYBorrarAristaVacia() {
        GrafoDirigido grafo = new GrafoDirigido();
        grafo.agregarArista("M1", "M2", Dinero.de(200));
        grafo.agregarArista("M1", "M2", Dinero.de(100));

        grafo.restarArista("M1", "M2", Dinero.de(100));
        assertEquals(Dinero.de(200), grafo.pesoEntre("M1", "M2"));

        grafo.restarArista("M1", "M2", Dinero.de(200));
        assertNull(grafo.buscarArista("M1", "M2"));
        assertTrue(grafo.aristasDesde("M1").estaVacia());
    }

    @Test
    void totalesEntranteYSaliente() {
        GrafoDirigido grafo = new GrafoDirigido();
        grafo.agregarArista("M1", "M2", Dinero.de(200));
        grafo.agregarArista("M1", "M3", Dinero.de(50));
        grafo.agregarArista("M3", "M2", Dinero.de(30));

        assertEquals(Dinero.de(250), grafo.totalSaliente("M1"));
        assertEquals(Dinero.de(230), grafo.totalEntrante("M2"));
    }

    @Test
    void alcanzablesConRecorridoRecursivoSinCiclosInfinitos() {
        GrafoDirigido grafo = new GrafoDirigido();
        grafo.agregarArista("M1", "M2", Dinero.de(10));
        grafo.agregarArista("M2", "M3", Dinero.de(10));
        grafo.agregarArista("M3", "M1", Dinero.de(10)); // ciclo
        grafo.agregarVertice("M4");

        ListaEnlazada<String> alcanzables = grafo.alcanzablesDesde("M1");

        assertEquals(2, alcanzables.getTamano());
        assertTrue(alcanzables.obtenerPosicionNodo("M2") != -1);
        assertTrue(alcanzables.obtenerPosicionNodo("M3") != -1);
        assertEquals(-1, alcanzables.obtenerPosicionNodo("M4"));
    }
}
