package co.edu.uniquindio.poo.util;

public final class Dinero {
    private Dinero() {
    }

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public static boolean iguales(double a, double b) {
        return Math.abs(a - b) < Constantes.EPSILON;
    }

    public static boolean mayorOIgual(double a, double b) {
        return a > b || iguales(a, b);
    }
}


