package co.edu.uniquindio.poo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidades para manejar dinero con {@link BigDecimal}.
 * Todos los montos se guardan con 2 decimales y redondeo HALF_UP,
 * así evitamos los errores de precisión de double (0.1 + 0.2 != 0.3).
 */
public final class Dinero {

    public static final int ESCALA = 2;
    public static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    public static final BigDecimal CERO = BigDecimal.ZERO.setScale(ESCALA);

    private Dinero() {
    }

    /** Crea un monto a partir de texto, p. ej. {@code Dinero.de("100.50")}. */
    public static BigDecimal de(String valor) {
        return redondear(new BigDecimal(valor));
    }

    /** Crea un monto entero, p. ej. {@code Dinero.de(50000)}. */
    public static BigDecimal de(long valor) {
        return redondear(BigDecimal.valueOf(valor));
    }

    /** Deja el valor con 2 decimales. Un null se trata como cero. */
    public static BigDecimal redondear(BigDecimal valor) {
        if (valor == null) return CERO;
        return valor.setScale(ESCALA, REDONDEO);
    }

    /** monto * tasa, redondeado. Útil para comisiones (tasa = 0.015 → 1.5 %). */
    public static BigDecimal porcentaje(BigDecimal monto, BigDecimal tasa) {
        return redondear(monto.multiply(tasa));
    }

    public static boolean iguales(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) == 0;
    }

    public static boolean mayorOIgual(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0;
    }

    public static boolean esPositivo(BigDecimal valor) {
        return valor != null && valor.signum() > 0;
    }
}
