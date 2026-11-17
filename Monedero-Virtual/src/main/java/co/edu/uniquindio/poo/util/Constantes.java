package co.edu.uniquindio.poo.util;

import java.math.BigDecimal;

/** Parámetros de negocio centralizados (cámbialos aquí, no en el código). */
public final class Constantes {

    private Constantes() {
    }

    /** Unidades de moneda por bloque de puntos ("1 punto por cada 100 unidades"). */
    public static final BigDecimal UNIDADES_POR_BLOQUE_PUNTOS = new BigDecimal("100");

    /** Comisión de retiros (1 %). */
    public static final BigDecimal COMISION_RETIRO = new BigDecimal("0.01");
    /** Comisión de transferencias a otros clientes (1.5 %), antes de descuentos por rango. */
    public static final BigDecimal COMISION_TRANSFERENCIA = new BigDecimal("0.015");
    /** Descuento del beneficio "10 % menos comisión en transferencias". */
    public static final BigDecimal DESCUENTO_BENEFICIO_TRANSFERENCIA = new BigDecimal("0.10");
    /** Valor del bono de saldo que se obtiene con 1000 puntos. */
    public static final BigDecimal BONO_SALDO = Dinero.de(50);

    public static final BigDecimal MONTO_MINIMO = Dinero.de(1);
    public static final BigDecimal MONTO_MAXIMO = Dinero.de(10_000_000);

    /** Por debajo de este saldo se envía la alerta de saldo bajo. */
    public static final BigDecimal UMBRAL_SALDO_BAJO = Dinero.de(100);
    /** Máximo de notificaciones guardadas por cliente (lista circular). */
    public static final int CAPACIDAD_NOTIFICACIONES = 30;
    /** Horas de anticipación del recordatorio de una transacción programada. */
    public static final int HORAS_RECORDATORIO = 24;
    /** Máximo de monederos por cliente. */
    public static final int MAX_MONEDEROS = 5;

    /** Usuario y contraseña del administrador (inicio de sesión). */
    public static final String ADMIN_USUARIO = "admin";
    public static final String ADMIN_CLAVE = "admin123";
}
