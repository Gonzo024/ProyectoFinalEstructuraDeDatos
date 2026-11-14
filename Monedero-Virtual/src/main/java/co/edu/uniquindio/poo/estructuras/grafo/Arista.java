package co.edu.uniquindio.poo.estructuras.grafo;

import co.edu.uniquindio.poo.util.Dinero;

import java.math.BigDecimal;

/**
 * Flecha de un grafo dirigido: de "origen" hacia "destino".
 * Acumula cuánto dinero ha pasado por ella (peso) y en cuántas transferencias (cantidad).
 */
public class Arista {

    private final String origen;
    private final String destino;
    private BigDecimal peso;
    private int cantidad;

    public Arista(String origen, String destino) {
        this.origen = origen;
        this.destino = destino;
        this.peso = Dinero.CERO;
        this.cantidad = 0;
    }

    /** Se llama cuando hay una transferencia nueva por esta flecha. */
    public void sumar(BigDecimal monto) {
        peso = peso.add(monto);
        cantidad++;
    }

    /** Se llama cuando se revierte una transferencia que pasó por esta flecha. */
    public void restar(BigDecimal monto) {
        if (cantidad == 0) {
            throw new IllegalStateException("No hay transferencias para restar en " + origen + " -> " + destino);
        }
        if (peso.compareTo(monto) < 0) {
            throw new IllegalStateException("El peso no puede quedar negativo");
        }
        peso = peso.subtract(monto);
        cantidad--;
    }

    /** ¿Ya no queda ninguna transferencia? Sirve para que el grafo pueda borrar la flecha. */
    public boolean estaVacia() {
        return cantidad == 0;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public int getCantidad() {
        return cantidad;
    }

    @Override
    public String toString() {
        return origen + " -> " + destino + " (" + peso + ", " + cantidad + " veces)";
    }
}