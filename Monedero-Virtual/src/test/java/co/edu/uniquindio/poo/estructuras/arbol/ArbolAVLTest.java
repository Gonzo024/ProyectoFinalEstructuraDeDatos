package co.edu.uniquindio.poo.estructuras.arbol;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.ClavePuntos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del AVL: los 4 casos de rotacion y que se mantiene balanceado.
 */
class ArbolAVLTest {

    @Test
    void rotacionSimpleADerecha() {
        // Caso izquierda-izquierda:  30 -> 20 -> 10
        ArbolAVL<Integer, String> arbol = insertar(30, 20, 10);
        verificarRaiz20(arbol);
    }

    @Test
    void rotacionSimpleAIzquierda() {
        // Caso derecha-derecha:  10 -> 20 -> 30
        ArbolAVL<Integer, String> arbol = insertar(10, 20, 30);
        verificarRaiz20(arbol);
    }

    @Test
    void rotacionDobleIzquierdaDerecha() {
        // Caso izquierda-derecha:  30 -> 10 -> 20
        ArbolAVL<Integer, String> arbol = insertar(30, 10, 20);
        verificarRaiz20(arbol);
    }

    @Test
    void rotacionDobleDerechaIzquierda() {
        // Caso derecha-izquierda:  10 -> 30 -> 20
        ArbolAVL<Integer, String> arbol = insertar(10, 30, 20);
        verificarRaiz20(arbol);
    }

    @Test
    void milClavesOrdenadasNoDegeneranElArbol() {
        ArbolAVL<Integer, Integer> arbol = new ArbolAVL<>();
        for (int i = 1; i <= 1000; i++) {
            arbol.insertar(i, i);
        }
        assertEquals(1000, arbol.tamanio());
        assertTrue(arbol.estaBalanceado());
        // Un AVL tiene altura menor a 1.44 * log2(n): con 1000 claves, a lo sumo 14.
        // El mismo caso en el ABB simple da altura 1000.
        assertTrue(arbol.altura() <= 14, "altura = " + arbol.altura());
    }

    @Test
    void eliminarMantieneElBalance() {
        ArbolAVL<Integer, Integer> arbol = new ArbolAVL<>();
        for (int i = 1; i <= 100; i++) {
            arbol.insertar(i, i);
        }
        for (int i = 1; i <= 50; i++) {
            assertTrue(arbol.eliminar(i));
        }
        assertEquals(50, arbol.tamanio());
        assertTrue(arbol.estaBalanceado());
        assertNull(arbol.buscar(10));
        assertEquals(75, arbol.buscar(75));
    }

    @Test
    void clientesConLosMismosPuntosNoSePisan() {
        ArbolAVL<ClavePuntos, String> arbol = new ArbolAVL<>();
        arbol.insertar(new ClavePuntos(300, "C1"), "C1");
        arbol.insertar(new ClavePuntos(300, "C2"), "C2");
        arbol.insertar(new ClavePuntos(800, "C3"), "C3");

        assertEquals(3, arbol.tamanio());
        ListaEnlazada<String> conTrescientos =
                arbol.rango(ClavePuntos.desde(300), ClavePuntos.hasta(300));
        assertEquals(2, conTrescientos.getTamano());
    }

    // ------------------------------------------------------------ ayudas

    private ArbolAVL<Integer, String> insertar(int... claves) {
        ArbolAVL<Integer, String> arbol = new ArbolAVL<>();
        for (int clave : claves) {
            arbol.insertar(clave, "v" + clave);
        }
        return arbol;
    }

    /** Despues de la rotacion los tres casos quedan iguales: 20 arriba, 10 y 30 abajo. */
    private void verificarRaiz20(ArbolAVL<Integer, String> arbol) {
        NodoArbol<Integer, String> raiz = arbol.getRaiz();
        assertEquals(20, raiz.getClave());
        assertEquals(10, raiz.getIzquierdo().getClave());
        assertEquals(30, raiz.getDerecho().getClave());
        assertEquals(2, arbol.altura());
        assertTrue(arbol.estaBalanceado());
    }
}