package co.edu.uniquindio.poo.excepciones;

public class PuntosInsuficientesException extends MonederoException {

    public PuntosInsuficientesException(long disponibles, long requeridos) {
        super(String.format("Puntos insuficientes: tienes %d y necesitas %d",
                disponibles, requeridos));
    }
}