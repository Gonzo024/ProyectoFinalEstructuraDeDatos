package co.edu.uniquindio.poo.servicios;

import java.math.BigDecimal;

/**
 * Lo único que TransaccionService necesita saber de los beneficios canjeados.
 * Usar una interfaz evita la dependencia circular con BeneficioService.
 */
public interface ProveedorDescuentos {

    /** Tasa extra de descuento sobre la comisión de transferencia (0.10 = 10 %). 0 si no tiene. */
    BigDecimal descuentoComisionTransferencia(String clienteId);

    /** true si el cliente tiene activo "un mes sin cargos por retiros". */
    boolean retiroSinComision(String clienteId);
}