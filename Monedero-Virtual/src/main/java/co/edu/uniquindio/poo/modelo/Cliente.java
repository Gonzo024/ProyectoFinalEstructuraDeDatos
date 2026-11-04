package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.Rango;

import java.time.LocalDate;
import java.time.LocalTime;

public class Cliente {

    private final String id;
    private String nombre;
    private String correo;
    private String pin;
    private Rango rango = Rango.BRONCE;
    private final LocalTime fechaRegistro;

    public Cliente(String id, String nombre, String correo, String pin, Rango rango) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.pin = pin;
        this.rango = rango;
        this.fechaRegistro = fechaRegistro;

    }
}
