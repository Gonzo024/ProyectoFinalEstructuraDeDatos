package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.TipoMovimientoPuntos;

import java.time.LocalDateTime;

/** Una línea del historial de puntos de un cliente. */
public class MovimientoPuntos {

    private final TipoMovimientoPuntos tipo;
    private final long puntos;            // + ganados, - canjeados o revertidos
    private final LocalDateTime fecha;
    private final String descripcion;
    private final String transaccionId;  // null cuando es un canje

    public MovimientoPuntos(TipoMovimientoPuntos tipo, long puntos, LocalDateTime fecha,
                            String descripcion, String transaccionId) {
        this.tipo = tipo;
        this.puntos = puntos;
        this.fecha = fecha;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.transaccionId = transaccionId;
    }

    public TipoMovimientoPuntos getTipo() { return tipo; }
    public long getPuntos() { return puntos; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getTransaccionId() { return transaccionId; }

    /** "+30" o "-100", listo para mostrar en la tabla. */
    public String getPuntosTexto() {
        return (puntos > 0 ? "+" : "") + puntos;
    }
}