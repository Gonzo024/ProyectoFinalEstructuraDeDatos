package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.estructuras.pila.Pila;
import co.edu.uniquindio.poo.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Un monedero dentro de la cuenta de un cliente (principal, ahorros, gastos diarios...).
 * Solo los servicios deben modificar el saldo (acreditar/debitar).
 * Guarda su historial (lista enlazada) y la pila de operaciones reversibles.
 */
public class Monedero {

    private final String id;
    private final String clienteId;
    private String nombre;
    private final TipoMonedero tipo;
    private BigDecimal saldo = Dinero.CERO;
    private final ListaEnlazada<Transaccion> historial = new ListaEnlazada<Transaccion>();
    private final Pila<Transaccion> reversibles = new Pila<Transaccion>();

    public Monedero(String id, String clienteId, String nombre, TipoMonedero tipo) {
        this.id = id;
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public void acreditar(BigDecimal monto) {
        validarMonto(monto);
        saldo = Dinero.redondear(saldo.add(monto));
    }

    public void debitar(BigDecimal monto) {
        validarMonto(monto);
        if (!tieneSaldo(monto)) {
            throw new SaldoInsuficienteException(nombre, saldo, monto);
        }
        saldo = Dinero.redondear(saldo.subtract(monto));
    }

    public boolean tieneSaldo(BigDecimal monto) {
        return Dinero.mayorOIgual(saldo, monto);
    }

    private void validarMonto(BigDecimal monto) {
        if (!Dinero.esPositivo(monto)) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
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

    public BigDecimal getSaldo() {
        return saldo;
    }

    public ListaEnlazada<Transaccion> getHistorial() {
        return historial;
    }

    public Pila<Transaccion> getReversibles() {
        return reversibles;
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