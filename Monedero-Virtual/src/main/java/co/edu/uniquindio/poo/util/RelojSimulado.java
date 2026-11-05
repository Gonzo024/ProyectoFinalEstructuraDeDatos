package co.edu.uniquindio.poo.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RelojSimulado {

    private LocalDateTime ahora;

    public RelojSimulado(LocalDateTime inicio) {
        this.ahora = inicio;
    }

    public RelojSimulado() {
        this(LocalDateTime.now());
    }

    public LocalDateTime ahora() {
        return ahora;
    }

    public LocalDate hoy() {
        return ahora.toLocalDate();
    }

    public void avanzarDias(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("El reloj no puede retroceder");
        }
        ahora = ahora.plusDays(dias);
    }

    public void fijar(LocalDateTime nueva) {
        ahora = nueva;
    }
}