package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.EstadoTransaccion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Movimiento de dinero. Se construye con {@link Builder} porque tiene muchos campos opcionales
 * (destino, categoría, programada de origen...).
 * <p>
 * Convención de origen/destino según el tipo:
 * <ul>
 *   <li>DEPOSITO / BONO: solo destino (el monedero que recibe). clienteOrigen = mismo cliente.</li>
 *   <li>RETIRO / PAGO: solo origen.</li>
 *   <li>TRANSFERENCIA: origen y destino de clientes distintos.</li>
 *   <li>TRANSFERENCIA_INTERNA: origen y destino del mismo cliente.</li>
 * </ul>
 */
public class Transaccion {

    private final String id;
    private final TipoTransaccion tipo;
    private final double monto;
    private final double comision;
    private final LocalDateTime fecha;
    private final String clienteOrigenId;
    private final String monederoOrigenId;
    private final String clienteDestinoId;
    private final String monederoDestinoId;
    private final CategoriaGasto categoria;
    private final String descripcion;
    private final String programadaId;
    private EstadoTransaccion estado = EstadoTransaccion.COMPLETADA;
    private long puntosGenerados;
    private LocalDateTime fechaReversion;

    private Transaccion(Builder b) {
        this.id = b.id;
        this.tipo = b.tipo;
        this.monto = Dinero.redondear(b.monto);
        this.comision = Dinero.redondear(b.comision);
        this.fecha = b.fecha;
        this.clienteOrigenId = b.clienteOrigenId;
        this.monederoOrigenId = b.monederoOrigenId;
        this.clienteDestinoId = b.clienteDestinoId;
        this.monederoDestinoId = b.monederoDestinoId;
        this.categoria = b.categoria;
        this.descripcion = b.descripcion == null ? "" : b.descripcion;
        this.programadaId = b.programadaId;
    }

    public static Builder builder(String id, TipoTransaccion tipo, double monto, LocalDateTime fecha) {
        return new Builder(id, tipo, monto, fecha);
    }

    /** Lo que efectivamente sale del monedero de origen (monto + comisión). */
    public double getTotalDebitado() {
        return Dinero.redondear(monto + comision);
    }

    /** ¿La transacción involucra al cliente (como origen o destino)? */
    public boolean involucraA(String clienteId) {
        return clienteId.equals(clienteOrigenId) || clienteId.equals(clienteDestinoId);
    }

    /**
     * Efecto de esta transacción sobre el saldo de un monedero (positivo = entra dinero).
     * Usado por la verificación recursiva de integridad para reconstruir saldos.
     */
    public double efectoSobre(String monederoId) {
        if (estado != EstadoTransaccion.COMPLETADA) return 0;
        double efecto = 0;
        if (monederoId.equals(monederoOrigenId)) efecto -= getTotalDebitado();
        if (monederoId.equals(monederoDestinoId)) efecto += monto;
        return efecto;
    }

    public void marcarRevertida(LocalDateTime fecha) {
        this.estado = EstadoTransaccion.REVERTIDA;
        this.fechaReversion = fecha;
    }

    public void marcarFallida() {
        this.estado = EstadoTransaccion.FALLIDA;
    }

    public boolean estaCompletada() {
        return estado == EstadoTransaccion.COMPLETADA;
    }

    // ---------------------------------------------------------------- getters

    public String getId() { return id; }
    public TipoTransaccion getTipo() { return tipo; }
    public double getMonto() { return monto; }
    public double getComision() { return comision; }
    public LocalDateTime getFecha() { return fecha; }
    public String getClienteOrigenId() { return clienteOrigenId; }
    public String getMonederoOrigenId() { return monederoOrigenId; }
    public String getClienteDestinoId() { return clienteDestinoId; }
    public String getMonederoDestinoId() { return monederoDestinoId; }
    public CategoriaGasto getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public String getProgramadaId() { return programadaId; }
    public EstadoTransaccion getEstado() { return estado; }
    public LocalDateTime getFechaReversion() { return fechaReversion; }
    public long getPuntosGenerados() { return puntosGenerados; }

    public void setPuntosGenerados(long puntosGenerados) {
        this.puntosGenerados = puntosGenerados;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Transaccion t && t.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " " + tipo + " " + monto + " [" + estado + "]";
    }

    // ---------------------------------------------------------------- builder

    public static class Builder {
        private final String id;
        private final TipoTransaccion tipo;
        private final double monto;
        private final LocalDateTime fecha;
        private double comision;
        private String clienteOrigenId;
        private String monederoOrigenId;
        private String clienteDestinoId;
        private String monederoDestinoId;
        private CategoriaGasto categoria;
        private String descripcion;
        private String programadaId;

        private Builder(String id, TipoTransaccion tipo, double monto, LocalDateTime fecha) {
            this.id = id;
            this.tipo = tipo;
            this.monto = monto;
            this.fecha = fecha;
        }

        public Builder comision(double comision) { this.comision = comision; return this; }

        public Builder origen(String clienteId, String monederoId) {
            this.clienteOrigenId = clienteId;
            this.monederoOrigenId = monederoId;
            return this;
        }

        public Builder destino(String clienteId, String monederoId) {
            this.clienteDestinoId = clienteId;
            this.monederoDestinoId = monederoId;
            return this;
        }

        public Builder categoria(CategoriaGasto categoria) { this.categoria = categoria; return this; }
        public Builder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public Builder programada(String programadaId) { this.programadaId = programadaId; return this; }

        public Transaccion construir() {
            return new Transaccion(this);
        }
    }
}