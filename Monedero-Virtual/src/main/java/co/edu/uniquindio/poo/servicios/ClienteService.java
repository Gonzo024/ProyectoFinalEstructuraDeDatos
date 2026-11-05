package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Constantes;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

public class ClienteService {

    private final RelojSimulado reloj;
    private final ListaEnlazada<Cliente> clientes = new ListaEnlazada<Cliente>();
    private final ListaEnlazada<Monedero> monederos = new ListaEnlazada<Monedero>();
    private int contadorClientes = 0;
    private int contadorMonederos = 0;

    public ClienteService(RelojSimulado reloj) {
        this.reloj = reloj;
    }

    public Cliente registrarCliente(String nombre, String correo, String pin) {
        if (nombre == null || nombre.isBlank()) {
            throw new MonederoException("El nombre es obligatorio");
        }
        if (correo == null || correo.isBlank()) {
            throw new MonederoException("El correo es obligatorio");
        }
        if (pin == null || pin.isBlank()) {
            throw new MonederoException("El PIN es obligatorio");
        }
        for (Cliente existente : clientes) {
            if (existente.getCorreo().equalsIgnoreCase(correo.trim())) {
                throw new MonederoException("Ya existe un cliente con ese correo");
            }
        }
        contadorClientes++;
        Cliente cliente = new Cliente("C" + contadorClientes, nombre.trim(), correo.trim(), pin, reloj.ahora());
        clientes.agregarFinal(cliente);
        crearMonedero(cliente.getId(), "Principal", TipoMonedero.PRINCIPAL);
        return cliente;
    }

    public Cliente obtenerCliente(String id) {
        for (Cliente cliente : clientes) {
            if (cliente.getId().equals(id)) {
                return cliente;
            }
        }
        throw new MonederoException("Cliente no encontrado: " + id);
    }

    public ListaEnlazada<Cliente> listarClientes() {
        return clientes;
    }

    public Monedero crearMonedero(String clienteId, String nombre, TipoMonedero tipo) {
        obtenerCliente(clienteId);
        if (nombre == null || nombre.isBlank()) {
            throw new MonederoException("El nombre del monedero es obligatorio");
        }
        int cantidad = 0;
        for (Monedero existente : monederos) {
            if (existente.getClienteId().equals(clienteId)) {
                cantidad++;
                if (existente.getNombre().equalsIgnoreCase(nombre.trim())) {
                    throw new MonederoException("El cliente ya tiene un monedero con ese nombre");
                }
            }
        }
        if (cantidad >= Constantes.MAX_MONEDEROS) {
            throw new MonederoException("Maximo de monederos por cliente: " + Constantes.MAX_MONEDEROS);
        }
        contadorMonederos++;
        Monedero monedero = new Monedero("M" + contadorMonederos, clienteId, nombre.trim(), tipo);
        monederos.agregarFinal(monedero);
        return monedero;
    }

    public Monedero obtenerMonedero(String id) {
        for (Monedero monedero : monederos) {
            if (monedero.getId().equals(id)) {
                return monedero;
            }
        }
        throw new MonederoException("Monedero no encontrado: " + id);
    }

    public ListaEnlazada<Monedero> monederosDe(String clienteId) {
        obtenerCliente(clienteId);
        ListaEnlazada<Monedero> resultado = new ListaEnlazada<Monedero>();
        for (Monedero monedero : monederos) {
            if (monedero.getClienteId().equals(clienteId)) {
                resultado.agregarFinal(monedero);
            }
        }
        return resultado;
    }

    public double saldoTotal(String clienteId) {
        double total = 0;
        for (Monedero monedero : monederosDe(clienteId)) {
            total += monedero.getSaldo();
        }
        return Dinero.redondear(total);
    }
}
