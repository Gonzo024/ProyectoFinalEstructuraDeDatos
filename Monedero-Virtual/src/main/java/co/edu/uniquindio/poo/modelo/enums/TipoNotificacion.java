package co.edu.uniquindio.poo.modelo.enums;

public enum TipoNotificacion {
    SALDO_BAJO("Saldo bajo", "⚠"),
    TRANSACCION("Movimiento", "↔"),
    RECORDATORIO_PROGRAMADA("Recordatorio", "⏰"),
    PROGRAMADA_EJECUTADA("Programada ejecutada", "✔"),
    PROGRAMADA_FALLIDA("Programada fallida", "✖"),
    PUNTOS("Puntos", "★"),
    CAMBIO_RANGO("Cambio de rango", "▲"),
    BENEFICIO("Beneficio", "🎁"),
    SEGURIDAD("Seguridad", "🛡");

    private final String nombre;
    private final String icono;

    TipoNotificacion(String nombre, String icono) {
        this.nombre = nombre;
        this.icono = icono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIcono() {
        return icono;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
