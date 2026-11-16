package co.edu.uniquindio.poo.modelo;

import java.util.Objects;

/**
 * Clave de los árboles de puntos (ABB y AVL).
 * Ordena primero por puntos y, si empatan, por id del cliente.
 * Así dos clientes con los mismos puntos no se pisan en el árbol.
 */
public final class ClavePuntos implements Comparable<ClavePuntos> {

    private final long puntos;
    private final String clienteId;

    public ClavePuntos(long puntos, String clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        this.puntos = puntos;
        this.clienteId = clienteId;
    }

    /** La clave más pequeña posible con esos puntos. Sirve como INICIO de un intervalo. */
    public static ClavePuntos desde(long puntos) {
        return new ClavePuntos(puntos, "");
    }

    /** La clave más grande posible con esos puntos. Sirve como FIN de un intervalo. */
    public static ClavePuntos hasta(long puntos) {
        return new ClavePuntos(puntos, "\uffff");
    }

    @Override
    public int compareTo(ClavePuntos otra) {
        int comparacion = Long.compare(puntos, otra.puntos);
        if (comparacion != 0) {
            return comparacion;
        }
        return clienteId.compareTo(otra.clienteId);
    }

    public long getPuntos() {
        return puntos;
    }

    public String getClienteId() {
        return clienteId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClavePuntos)) {
            return false;
        }
        ClavePuntos otra = (ClavePuntos) o;
        return otra.puntos == puntos && otra.clienteId.equals(clienteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(puntos, clienteId);
    }

    @Override
    public String toString() {
        return clienteId + "(" + puntos + ")";
    }
}