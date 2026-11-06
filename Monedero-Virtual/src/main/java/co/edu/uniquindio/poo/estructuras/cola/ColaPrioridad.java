
package co.edu.uniquindio.poo.estructuras.cola;

public class ColaPrioridad<T extends Comparable<T>> {

    private Object[] elementos;
    private int tamanio;

    public ColaPrioridad() {
        elementos = new Object[10];
        tamanio = 0;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public int tamanio() {
        return tamanio;
    }

    @SuppressWarnings("unchecked")
    private T obtener(int posicion) {
        return (T) elementos[posicion];
    }

    public void insertar(T dato) {

        if (tamanio == elementos.length) {
            Object[] nuevo = new Object[elementos.length * 2];

            for (int i = 0; i < tamanio; i++) {
                nuevo[i] = elementos[i];
            }

            elementos = nuevo;
        }

        elementos[tamanio] = dato;
        int posicion = tamanio;
        tamanio++;

        while (posicion > 0) {
            int padre = (posicion - 1) / 2;

            if (obtener(posicion).compareTo(obtener(padre)) >= 0) {
                break;
            }

            Object auxiliar = elementos[posicion];
            elementos[posicion] = elementos[padre];
            elementos[padre] = auxiliar;

            posicion = padre;
        }
    }

    public T primero() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola esta vacia");
        }

        return obtener(0);
    }

    public T extraer() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola esta vacia");
        }

        T resultado = obtener(0);

        tamanio--;
        elementos[0] = elementos[tamanio];
        elementos[tamanio] = null;

        int posicion = 0;

        while (true) {
            int izquierdo = posicion * 2 + 1;
            int derecho = posicion * 2 + 2;
            int menor = posicion;

            if (izquierdo < tamanio &&
                obtener(izquierdo).compareTo(obtener(menor)) < 0) {
                menor = izquierdo;
            }

            if (derecho < tamanio &&
                obtener(derecho).compareTo(obtener(menor)) < 0) {
                menor = derecho;
            }

            if (menor == posicion) {
                break;
            }

            Object auxiliar = elementos[posicion];
            elementos[posicion] = elementos[menor];
            elementos[menor] = auxiliar;

            posicion = menor;
        }

        return resultado;
    }
}
