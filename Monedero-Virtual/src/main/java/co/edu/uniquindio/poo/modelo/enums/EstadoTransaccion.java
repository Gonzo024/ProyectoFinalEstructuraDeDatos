package co.edu.uniquindio.poo.modelo.enums;

public enum EstadoTransaccion {
    COMPLETADA("Completada"),
    REVERTIDA("Revertida");

    private final String nombre;

    EstadoTransaccion(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

