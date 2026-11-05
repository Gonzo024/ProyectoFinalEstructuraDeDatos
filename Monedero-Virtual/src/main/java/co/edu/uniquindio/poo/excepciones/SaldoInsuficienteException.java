package co.edu.uniquindio.poo.excepciones;

public class SaldoInsuficienteException extends TransaccionInvalidaException {

    public SaldoInsuficienteException(String nombreMonedero, double saldo, double monto) {
        super(String.format("Saldo insuficiente en '%s': disponible %.2f, requerido %.2f",
                nombreMonedero, saldo, monto));
    }
}