package co.edu.uniquindio.poo.estructuras.hash;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la tabla hash, incluida la resolucion de colisiones por encadenamiento.
 */
class TablaHashTest {

    /** Clave cuyo hashCode es siempre el mismo: todas caen en la misma cubeta (colision). */
    private static final class ClaveColision {
        private final String nombre;

        ClaveColision(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ClaveColision && ((ClaveColision) o).nombre.equals(nombre);
        }
    }

    private TablaHash<String, Integer> tabla;

    @BeforeEach
    void setUp() {
        tabla = new TablaHash<>();
        tabla.insertar("C1", 100);
        tabla.insertar("C2", 250);
        tabla.insertar("C3", 600);
    }

    @Test
    void insertarYObtener() {
        assertEquals(3, tabla.tamanio());
        assertEquals(250, tabla.obtener("C2"));
        assertNull(tabla.obtener("C9"));
        assertEquals(0, tabla.obtenerODefecto("C9", 0));
        assertTrue(tabla.contieneClave("C1"));
    }

    @Test
    void insertarClaveExistenteReemplazaElValor() {
        Integer anterior = tabla.insertar("C1", 999);
        assertEquals(100, anterior);
        assertEquals(999, tabla.obtener("C1"));
        assertEquals(3, tabla.tamanio());
    }

    @Test
    void colisionesSeResuelvenPorEncadenamiento() {
        TablaHash<ClaveColision, String> conColisiones = new TablaHash<>();
        ClaveColision a = new ClaveColision("a");
        ClaveColision b = new ClaveColision("b");
        ClaveColision c = new ClaveColision("c");

        conColisiones.insertar(a, "A");
        conColisiones.insertar(b, "B");
        conColisiones.insertar(c, "C");

        // Las tres tienen el mismo hash, pero ninguna pisa a la otra.
        assertEquals(3, conColisiones.tamanio());
        assertEquals("A", conColisiones.obtener(new ClaveColision("a")));
        assertEquals("B", conColisiones.obtener(new ClaveColision("b")));
        assertEquals("C", conColisiones.obtener(new ClaveColision("c")));
    }

    @Test
    void eliminarEnMedioDeUnaCadenaDeColisiones() {
        TablaHash<ClaveColision, String> conColisiones = new TablaHash<>();
        conColisiones.insertar(new ClaveColision("a"), "A");
        conColisiones.insertar(new ClaveColision("b"), "B");
        conColisiones.insertar(new ClaveColision("c"), "C");

        assertEquals("B", conColisiones.eliminar(new ClaveColision("b")));

        assertEquals(2, conColisiones.tamanio());
        assertNull(conColisiones.obtener(new ClaveColision("b")));
        assertEquals("A", conColisiones.obtener(new ClaveColision("a")));
        assertEquals("C", conColisiones.obtener(new ClaveColision("c")));
        assertNull(conColisiones.eliminar(new ClaveColision("z")));
    }

    @Test
    void creceSinPerderDatos() {
        TablaHash<String, Integer> grande = new TablaHash<>();
        for (int i = 0; i < 1000; i++) {
            grande.insertar("C" + i, i);
        }
        assertEquals(1000, grande.tamanio());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, grande.obtener("C" + i));
        }
        // Al pasar de 0.75 duplica su capacidad, asi las cadenas siguen cortas.
        assertTrue(grande.factorCarga() <= 0.75);
    }

    @Test
    void clavesYValoresDevuelvenTodo() {
        assertEquals(3, tabla.claves().getTamano());
        assertEquals(3, tabla.valores().getTamano());
    }
}