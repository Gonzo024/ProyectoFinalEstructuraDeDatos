package co.edu.uniquindio.poo.estructuras.arbol;



import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del ABB: insercion, busqueda, eliminacion, recorridos y consultas.
 */
class ArbolBinarioBusquedaTest {

    private ArbolBinarioBusqueda<Integer, String> arbol;

    @BeforeEach
    void setUp() {
        //            50
        //          /    \
        //        30      70
        //       /  \    /  \
        //     20   40  60   80
        arbol = new ArbolBinarioBusqueda<>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80};
        for (int clave : claves) {
            arbol.insertar(clave, "v" + clave);
        }
    }

    @Test
    void insertarYBuscar() {
        assertEquals(7, arbol.tamanio());
        assertEquals("v40", arbol.buscar(40));
        assertEquals("v80", arbol.buscar(80));
        assertNull(arbol.buscar(45));
        assertTrue(arbol.contiene(60));
        assertFalse(arbol.contiene(10));
    }

    @Test
    void cadaClaveQuedaEnSuLugar() {
        NodoArbol<Integer, String> raiz = arbol.getRaiz();
        assertEquals(50, raiz.getClave());
        assertEquals(30, raiz.getIzquierdo().getClave());
        assertEquals(70, raiz.getDerecho().getClave());
        assertEquals(20, raiz.getIzquierdo().getIzquierdo().getClave());
        assertEquals(80, raiz.getDerecho().getDerecho().getClave());
        assertEquals(3, arbol.altura());
    }

    @Test
    void claveRepetidaReemplazaElValor() {
        arbol.insertar(40, "nuevo");
        assertEquals(7, arbol.tamanio());
        assertEquals("nuevo", arbol.buscar(40));
    }

    @Test
    void inOrdenEntregaLasClavesOrdenadas() {
        assertEquals("v20,v30,v40,v50,v60,v70,v80", unir(arbol.inOrden()));
    }

    @Test
    void minimoYMaximo() {
        assertEquals(20, arbol.minimo());
        assertEquals(80, arbol.maximo());
    }

    @Test
    void eliminarHojaNodoConUnHijoYNodoConDosHijos() {
        assertTrue(arbol.eliminar(20));   // hoja
        assertTrue(arbol.eliminar(30));   // queda con un solo hijo (40)
        assertTrue(arbol.eliminar(50));   // raiz con dos hijos: la reemplaza su sucesor (60)

        assertEquals(4, arbol.tamanio());
        assertEquals(60, arbol.getRaiz().getClave());
        assertEquals("v40,v60,v70,v80", unir(arbol.inOrden()));
        assertFalse(arbol.eliminar(99));  // no existe
    }

    @Test
    void mayoresDevuelveElTopDeMayorAMenor() {
        assertEquals("v80,v70,v60", unir(arbol.mayores(3)));
    }

    @Test
    void rangoDevuelveSoloLasClavesDelIntervalo() {
        assertEquals("v40,v50,v60", unir(arbol.rango(35, 65)));
    }

    @Test
    void conClavesOrdenadasElAbbDegeneraEnLista() {
        ArbolBinarioBusqueda<Integer, Integer> lista = new ArbolBinarioBusqueda<>();
        for (int i = 1; i <= 1000; i++) {
            lista.insertar(i, i);
        }
        // Cada clave nueva queda a la derecha de la anterior: altura = n.
        assertEquals(1000, lista.altura());
    }

    private String unir(ListaEnlazada<String> lista) {
        StringBuilder sb = new StringBuilder();
        for (String valor : lista) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(valor);
        }
        return sb.toString();
    }
}