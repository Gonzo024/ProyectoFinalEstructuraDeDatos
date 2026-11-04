package co.edu.uniquindio.poo.excepciones;

public class TransaccionInvalidaException extends MonederoException {
    public TransaccionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
