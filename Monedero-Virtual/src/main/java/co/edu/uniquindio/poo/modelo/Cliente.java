package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.Rango;
import java.time.LocalDateTime;
import java.util.Objects;

public class Cliente {

    private final String id;
    private String nombre;
    private String correo;
    private final String pin;
    private Rango rango;
    private final LocalDateTime fechaRegistro;

    public Cliente(String id, String nombre, String correo, String pin, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.pin = pin;
        this.rango = Rango.BRONCE;
        this.fechaRegistro = fechaRegistro;
    }

    public boolean pinCorrecto(String intento) {
        return pin.equals(intento);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rango getRango() {
        return rango;
    }

    public void setRango(Rango rango) {
        this.rango = rango;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cliente)) {
            return false;
        }
        Cliente otro = (Cliente) o;
        return otro.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}