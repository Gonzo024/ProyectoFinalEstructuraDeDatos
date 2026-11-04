package co.edu.uniquindio.poo.modelo.enums;



public enum CategoriaGasto {
    ALIMENTACION("Alimentación"),
    TRANSPORTE("Transporte"),
    SERVICIOS("Servicios públicos"),
    VIVIENDA("Vivienda"),
    ENTRETENIMIENTO("Entretenimiento"),
    SALUD("Salud"),
    EDUCACION("Educación"),
    COMPRAS("Compras"),
    OTROS("Otros");

    private final String nombre;

    CategoriaGasto(String nombre) {
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
