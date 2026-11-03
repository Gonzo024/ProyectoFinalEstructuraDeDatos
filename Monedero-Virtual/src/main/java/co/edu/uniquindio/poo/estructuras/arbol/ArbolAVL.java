package co.edu.uniquindio.poo.estructuras.arbol;



/**
 * Árbol AVL: árbol binario de búsqueda auto-balanceado.
 * <p>
 * Uso en el proyecto: clasificación de clientes por rango ({@code RangoService}).
 * Los puntos de los clientes cambian todo el tiempo (cada transacción, reversiones), lo que
 * implica muchas eliminaciones + inserciones. El AVL garantiza que la altura sea O(log n)
 * y por tanto cada actualización cueste O(log n), aunque las claves lleguen ordenadas.
 * <p>
 * Invariante: para todo nodo, |altura(izq) - altura(der)| &lt;= 1.
 */
public class ArbolAVL<K extends Comparable<K>, V> extends ArbolBinarioBusqueda<K, V> {

    @Override
    protected NodoArbol<K, V> balancear(NodoArbol<K, V> nodo) {
        actualizarAltura(nodo);
        int balance = factorBalance(nodo);

        if (balance > 1) { // cargado a la izquierda
            if (factorBalance(nodo.izquierdo) < 0) {
                nodo.izquierdo = rotarIzquierda(nodo.izquierdo); // caso izquierda-derecha
            }
            return rotarDerecha(nodo); // caso izquierda-izquierda
        }
        if (balance < -1) { // cargado a la derecha
            if (factorBalance(nodo.derecho) > 0) {
                nodo.derecho = rotarDerecha(nodo.derecho); // caso derecha-izquierda
            }
            return rotarIzquierda(nodo); // caso derecha-derecha
        }
        return nodo;
    }

    private int factorBalance(NodoArbol<K, V> nodo) {
        return nodo == null ? 0 : altura(nodo.izquierdo) - altura(nodo.derecho);
    }

    /*
     *        y                x
     *       / \              / \
     *      x   C   ---->    A   y
     *     / \                  / \
     *    A   B                B   C
     */
    private NodoArbol<K, V> rotarDerecha(NodoArbol<K, V> y) {
        NodoArbol<K, V> x = y.izquierdo;
        y.izquierdo = x.derecho;
        x.derecho = y;
        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    private NodoArbol<K, V> rotarIzquierda(NodoArbol<K, V> x) {
        NodoArbol<K, V> y = x.derecho;
        x.derecho = y.izquierdo;
        y.izquierdo = x;
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    /** Verifica recursivamente la invariante AVL (útil en pruebas). */
    public boolean estaBalanceado() {
        return estaBalanceado(raiz);
    }

    private boolean estaBalanceado(NodoArbol<K, V> nodo) {
        if (nodo == null) return true;
        return Math.abs(factorBalance(nodo)) <= 1
                && estaBalanceado(nodo.izquierdo)
                && estaBalanceado(nodo.derecho);
    }
}

