package co.edu.uniquindio.poo.modelo.enums;

/**
 * Rangos de cliente según puntos acumulados (enunciado):
 * Bronce 0–500, Plata 501–1000, Oro 1001–5000, Platino &gt; 5000.
 * <p>
 * Beneficios por rango: descuento sobre la comisión de transferencias y un
 * multiplicador (bono) de puntos.
 */
public enum Rango {

    BRONCE("Bronce", 0, 500, 0.00, 1.00, "#b87333"),
    PLATA("Plata", 501, 1000, 0.10, 1.10, "#a8a9ad"),
    ORO("Oro", 1001, 5000, 0.25, 1.25, "#d4af37"),
    PLATINO("Platino", 5001, Long.MAX_VALUE, 0.50, 1.50, "#5b8fa8");

    private final String nombre;
    private final long puntosMinimos;
    private final long puntosMaximos;
    private final double descuentoComisionTransferencia;
    private final double multiplicadorPuntos;
    private final String colorHex;

    Rango(String nombre, long min, long max, double descuento, double multiplicador, String colorHex) {
        this.nombre = nombre;
        this.puntosMinimos = min;
        this.puntosMaximos = max;
        this.descuentoComisionTransferencia = descuento;
        this.multiplicadorPuntos = multiplicador;
        this.colorHex = colorHex;
    }

    /** Función de clasificación pedida por el enunciado. O(1) (4 rangos). */
    public static Rango desdePuntos(long puntos) {
        for (Rango r : values()) {
            if (puntos >= r.puntosMinimos && puntos <= r.puntosMaximos) return r;
        }
        return BRONCE; // puntos negativos no deberían ocurrir
    }

    /** Rango siguiente o null si ya es el máximo. */
    public Rango siguiente() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i] : null;
    }

    public String getNombre() {
        return nombre;
    }

    public long getPuntosMinimos() {
        return puntosMinimos;
    }

    public long getPuntosMaximos() {
        return puntosMaximos;
    }

    public double getDescuentoComisionTransferencia() {
        return descuentoComisionTransferencia;
    }

    public double getMultiplicadorPuntos() {
        return multiplicadorPuntos;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String descripcionBeneficios() {
        if (this == BRONCE) return "Rango inicial";
        return String.format("%d%% menos comisión en transferencias · puntos x%.2f",
                Math.round(descuentoComisionTransferencia * 100), multiplicadorPuntos);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
