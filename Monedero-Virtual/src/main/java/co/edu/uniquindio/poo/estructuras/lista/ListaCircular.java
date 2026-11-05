
package co.edu.uniquindio.poo.estructuras.lista;

public class ListaCircular<T> {

    private NodoLista<T> primero;
    private NodoLista<T> ultimo;
    private int tamanio;

    public void agregar(T dato) {
        NodoLista<T> nuevo = new NodoLista<>(dato);

        if (estaVacia()) {
            primero = nuevo;
            ultimo = nuevo;
            nuevo.setSiguiente(primero);
        } else {
            ultimo.setSiguiente(nuevo);
            ultimo = nuevo;
            ultimo.setSiguiente(primero);
        }

        tamanio++;
    }

    public T obtener(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException(
                "Posicion fuera de la lista"
            );
        }

        NodoLista<T> actual = primero;

        for (int i = 0; i < posicion; i++) {
            actual = actual.getSiguiente();
        }

        return actual.getDato();
    }

    public boolean estaVacia() {
        return primero == null;
    }

    public int tamanio() {
        return tamanio;
    }
}
