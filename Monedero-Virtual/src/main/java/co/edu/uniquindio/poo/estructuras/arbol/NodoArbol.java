package co.edu.uniquindio.poo.estructuras.arbol;

/**
 * Nodo de árbol binario con clave/valor. Guarda su altura para que el AVL
 * pueda calcular el factor de balance en O(1).
 */
public class NodoArbol<K, V> {

    K clave;
    V valor;
    NodoArbol<K, V> izquierdo;
    NodoArbol<K, V> derecho;
    int altura = 1;

    NodoArbol(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public K getClave() {
        return clave;
    }

    public V getValor() {
        return valor;
    }

    public NodoArbol<K, V> getIzquierdo() {
        return izquierdo;
    }

    public NodoArbol<K, V> getDerecho() {
        return derecho;
    }

    public int getAltura() {
        return altura;
    }
}

