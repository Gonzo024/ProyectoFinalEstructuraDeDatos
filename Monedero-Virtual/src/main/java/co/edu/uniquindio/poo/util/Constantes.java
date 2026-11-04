package co.edu.uniquindio.poo.util;



/** Parámetros de negocio centralizados (cámbialos aquí, no en el código). */
public final class Constantes {

    private Constantes() {
    }

    /** Unidades de moneda por bloque de puntos ("1 punto por cada 100 unidades"). */
    public static final double UNIDADES_POR_BLOQUE_PUNTOS = 100;

    /** Comisión de retiros (1 %). */
    public static final double COMISION_RETIRO = 0.01;
    /** Comisión de transferencias a otros clientes (1.5 %), antes de descuentos por rango. */
    public static final double COMISION_TRANSFERENCIA = 0.015;
    /** Descuento del beneficio "10 % menos comisión en transferencias". */
    public static final double DESCUENTO_BENEFICIO_TRANSFERENCIA = 0.10;
    /** Valor del bono de saldo que se obtiene con 1000 puntos. */
    public static final double BONO_SALDO = 50;

    public static final double MONTO_MINIMO = 1;
    public static final double MONTO_MAXIMO = 10_000_000;

    /** Por debajo de este saldo se envía la alerta de saldo bajo. */
    public static final double UMBRAL_SALDO_BAJO = 100;
    /** Máximo de notificaciones guardadas por cliente (lista circular). */
    public static final int CAPACIDAD_NOTIFICACIONES = 30;
    /** Horas de anticipación del recordatorio de una transacción programada. */
    public static final int HORAS_RECORDATORIO = 24;
    /** Máximo de monederos por cliente. */
    public static final int MAX_MONEDEROS = 5;
    /** Tolerancia para comparar dinero en double. */
    public static final double EPSILON = 0.005;
}
