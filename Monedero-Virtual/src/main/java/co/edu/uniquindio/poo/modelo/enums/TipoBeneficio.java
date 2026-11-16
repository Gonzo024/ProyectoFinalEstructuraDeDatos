package co.edu.uniquindio.poo.modelo.enums;

/** Catálogo de beneficios que se pueden canjear con puntos (enunciado). */
public enum TipoBeneficio {

    DESCUENTO_TRANSFERENCIAS("10% menos comision en transferencias", 100, 30,
            "Durante 30 dias pagas 10% menos de comision al transferir"),
    RETIROS_SIN_CARGO("Un mes sin cargos por retiros", 500, 30,
            "Durante 30 dias tus retiros no pagan comision"),
    BONO_SALDO("Bono de 50 en tu monedero", 1000, 0,
            "Se acreditan 50 unidades al monedero que elijas");

    private final String nombre;
    private final long costoPuntos;
    private final int diasVigencia;
    private final String descripcion;

    TipoBeneficio(String nombre, long costoPuntos, int diasVigencia, String descripcion) {
        this.nombre = nombre;
        this.costoPuntos = costoPuntos;
        this.diasVigencia = diasVigencia;
        this.descripcion = descripcion;
    }

    /** true si dura un tiempo (descuentos); false si se entrega una sola vez (bono). */
    public boolean esTemporal() {
        return diasVigencia > 0;
    }

    public String getNombre() { return nombre; }
    public long getCostoPuntos() { return costoPuntos; }
    public int getDiasVigencia() { return diasVigencia; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() {
        return nombre + " (" + costoPuntos + " pts)";
    }
}