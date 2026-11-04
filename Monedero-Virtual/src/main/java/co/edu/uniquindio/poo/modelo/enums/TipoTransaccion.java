package co.edu.uniquindio.poo.modelo.enums;



/**
 * Tipos de transacción. Cada tipo define cuántos puntos genera por cada 100 unidades
 * (regla del enunciado) y si el dinero sale del monedero de origen.
 */
public enum TipoTransaccion {

    DEPOSITO("Depósito", 1, false),
    RETIRO("Retiro", 2, true),
    TRANSFERENCIA("Transferencia", 3, true),
    PAGO("Pago / gasto", 2, true),
    TRANSFERENCIA_INTERNA("Entre mis monederos", 0, true),
    BONO("Bono por canje", 0, false);

    private final String nombre;
    private final int puntosPorCien;
    private final boolean esSalida;

    TipoTransaccion(String nombre, int puntosPorCien, boolean esSalida) {
        this.nombre = nombre;
        this.puntosPorCien = puntosPorCien;
        this.esSalida = esSalida;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntosPorCien() {
        return puntosPorCien;
    }

    public boolean esSalida() {
        return esSalida;
    }

    public boolean generaPuntos() {
        return puntosPorCien > 0;
    }

    /** Tipos que el usuario puede programar como recurrentes. */
    public boolean esProgramable() {
        return this == DEPOSITO || this == TRANSFERENCIA || this == PAGO || this == TRANSFERENCIA_INTERNA;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
