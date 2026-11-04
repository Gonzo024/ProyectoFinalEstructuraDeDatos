package co.edu.uniquindio.poo.modelo;



import co.edu.uniquindio.poo.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;

import java.util.Objects;

/**
 * Un monedero dentro de la cuenta de un cliente (principal, ahorros, gastos diarios...).
 * Solo los servicios deben modificar el saldo (acreditar/debitar).
 */
public class Monedero {

    private final String id;
    private final String clienteId;
    private String nombre;
    private final TipoMonedero tipo;
    private double saldo;

    public Monedero(String id, String clienteId, String nombre, TipoMonedero tipo) {
        this.id = id;
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public void acreditar(double monto) {
        saldo = Dinero.redondear(saldo + monto);
    }

    public void debitar(double monto) {
        if (!tieneSaldo(monto)) {
            throw new SaldoInsuficienteException(nombre, saldo, monto);
        }
        saldo = Dinero.redondear(saldo - monto);
    }

    public boolean tieneSaldo(double monto) {
        return Dinero.mayorOIgual(saldo, monto);
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoMonedero getTipo() {
        return tipo;
    }

    public double getSaldo() {
        return saldo;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Monedero m && m.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " (" + tipo + ")";
    }
}

