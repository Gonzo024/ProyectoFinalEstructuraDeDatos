package co.edu.uniquindio.poo.modelo.enums;

public enum TipoMonedero {
    PRINCIPAL("Principal"),
    AHORROS("Ahorros"),
    GASTOS_DIARIOS("Gastos diarios"),
    METAS("Metas"),
    OTRO("Otro");

    private final String nombre;

    TipoMonedero(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
