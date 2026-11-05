
package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import java.time.LocalDateTime;

public class TransaccionProgramada
        implements Comparable<TransaccionProgramada> {

    private String id;
    private TipoTransaccion tipo;
    private String clienteOrigenId;
    private String monederoOrigenId;
    private String clienteDestinoId;
    private String monederoDestinoId;
    private double monto;
    private LocalDateTime fechaProgramada;
    private String frecuencia;
    private boolean activa;

    public TransaccionProgramada(
            String id,
            TipoTransaccion tipo,
            String clienteOrigenId,
            String monederoOrigenId,
            String clienteDestinoId,
            String monederoDestinoId,
            double monto,
            LocalDateTime fechaProgramada,
            String frecuencia) {

        if (tipo == null || !tipo.esProgramable()) {
            throw new IllegalArgumentException(
                "Tipo de transaccion no permitido"
            );
        }

        if (monto <= 0) {
            throw new IllegalArgumentException(
                "El monto debe ser mayor que cero"
            );
        }

        if (fechaProgramada == null) {
            throw new IllegalArgumentException(
                "Debe ingresar una fecha"
            );
        }

        this.id = id;
        this.tipo = tipo;
        this.clienteOrigenId = clienteOrigenId;
        this.monederoOrigenId = monederoOrigenId;
        this.clienteDestinoId = clienteDestinoId;
        this.monederoDestinoId = monederoDestinoId;
        this.monto = monto;
        this.fechaProgramada = fechaProgramada;
        this.frecuencia = frecuencia;
        this.activa = true;
    }

    @Override
    public int compareTo(TransaccionProgramada otra) {
        int resultado = fechaProgramada.compareTo(
            otra.fechaProgramada
        );

        if (resultado == 0) {
            return id.compareTo(otra.id);
        }

        return resultado;
    }

    public void actualizarFecha() {
        if (frecuencia.equals("DIARIA")) {
            fechaProgramada = fechaProgramada.plusDays(1);
        } else if (frecuencia.equals("SEMANAL")) {
            fechaProgramada = fechaProgramada.plusWeeks(1);
        } else if (frecuencia.equals("MENSUAL")) {
            fechaProgramada = fechaProgramada.plusMonths(1);
        } else {
            activa = false;
        }
    }

    public void cancelar() {
        activa = false;
    }

    public String getId() {
        return id;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public String getClienteOrigenId() {
        return clienteOrigenId;
    }

    public String getMonederoOrigenId() {
        return monederoOrigenId;
    }

    public String getClienteDestinoId() {
        return clienteDestinoId;
    }

    public String getMonederoDestinoId() {
        return monederoDestinoId;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public boolean isActiva() {
        return activa;
    }

    @Override
    public String toString() {
        return id + " - " + tipo + " - " + monto
                + " - " + fechaProgramada;
    }
}
