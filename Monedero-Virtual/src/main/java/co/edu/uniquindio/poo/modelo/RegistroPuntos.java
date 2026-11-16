package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.excepciones.PuntosInsuficientesException;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoMovimientoPuntos;

import java.time.LocalDateTime;

/**
 * Puntos de un cliente.
 * - acumulados: histórico. Define el RANGO. Canjear no lo baja.
 * - disponibles: lo que puede gastar en canjes.
 * Revertir una transacción resta de los dos.
 */
public class RegistroPuntos {

    private final String clienteId;
    private long puntosAcumulados;
    private long puntosDisponibles;
    private final ListaEnlazada<MovimientoPuntos> historial = new ListaEnlazada<>();

    public RegistroPuntos(String clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        this.clienteId = clienteId;
    }

    public void ganar(long puntos, String transaccionId, String descripcion, LocalDateTime fecha) {
        if (puntos <= 0) {
            return;
        }
        puntosAcumulados += puntos;
        puntosDisponibles += puntos;
        historial.agregarFinal(new MovimientoPuntos(
                TipoMovimientoPuntos.GANADOS, puntos, fecha, descripcion, transaccionId));
    }

    public void canjear(long costo, String descripcion, LocalDateTime fecha) {
        if (costo <= 0) {
            throw new IllegalArgumentException("El costo debe ser positivo");
        }
        if (!puedeCanjear(costo)) {
            throw new PuntosInsuficientesException(puntosDisponibles, costo);
        }
        puntosDisponibles -= costo;
        historial.agregarFinal(new MovimientoPuntos(
                TipoMovimientoPuntos.CANJEADOS, -costo, fecha, descripcion, null));
    }

    public void descontarPorReversion(long puntos, String transaccionId, LocalDateTime fecha) {
        if (puntos <= 0) {
            return;
        }
        puntosAcumulados = Math.max(0, puntosAcumulados - puntos);
        puntosDisponibles = Math.max(0, puntosDisponibles - puntos);
        historial.agregarFinal(new MovimientoPuntos(
                TipoMovimientoPuntos.REVERTIDOS, -puntos, fecha,
                "Reversión de " + transaccionId, transaccionId));
    }

    public boolean puedeCanjear(long costo) {
        return puntosDisponibles >= costo;
    }

    public Rango getRango() {
        return Rango.desdePuntos(puntosAcumulados);
    }

    /** Clave de este cliente en los árboles con sus puntos ACTUALES. */
    public ClavePuntos getClave() {
        return new ClavePuntos(puntosAcumulados, clienteId);
    }

    public String getClienteId() { return clienteId; }
    public long getPuntosAcumulados() { return puntosAcumulados; }
    public long getPuntosDisponibles() { return puntosDisponibles; }
    public ListaEnlazada<MovimientoPuntos> getHistorial() { return historial; }
}