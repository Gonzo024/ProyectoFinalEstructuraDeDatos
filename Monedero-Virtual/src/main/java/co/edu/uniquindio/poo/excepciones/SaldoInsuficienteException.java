package co.edu.uniquindio.poo.excepciones;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends TransaccionInvalidaException {

    public SaldoInsuficienteException(String nombreMonedero, BigDecimal saldo, BigDecimal monto) {
        super(String.format("Saldo insuficiente en '%s': disponible %.2f, requerido %.2f",
                nombreMonedero, saldo, monto));
    }
}