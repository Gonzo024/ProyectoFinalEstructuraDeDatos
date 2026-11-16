package co.edu.uniquindio.poo.modelo.enums;


public enum TipoMovimientoPuntos {

    GANADOS("Ganados"),
    CANJEADOS("Canjeados"),
    REVERTIDOS("Descontados por reversión");

    private final String nombre;

    TipoMovimientoPuntos(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
