package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.hash.TablaHash;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Constantes;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import java.math.BigDecimal;

/**
 * Registro de clientes y de sus monederos
 * Optimización con tablas hash: antes cada búsqueda recorría una lista completa (O(n)).
 * Ahora se busca directamente por la clave en una TablaHash (O(1) en promedio):

 *   clientesPorId: "C1" → Cliente</li>
 *   clientesPorCorreo: "ana@correo.com" → Cliente (para no repetir correos)
 *   monederosPorId: "M1" → Monedero
 *   monederosPorCliente: "C1" → lista de sus monederos (máximo 5)

 * La lista "clientes" se mantiene solo para poder mostrarlos en el orden en que se registraron.
 */
public class ClienteService {

    private final RelojSimulado reloj;

    private final ListaEnlazada<Cliente> clientes = new ListaEnlazada<Cliente>();
    private final TablaHash<String, Cliente> clientesPorId = new TablaHash<String, Cliente>();
    private final TablaHash<String, Cliente> clientesPorCorreo = new TablaHash<String, Cliente>();
    private final TablaHash<String, Monedero> monederosPorId = new TablaHash<String, Monedero>();
    private final TablaHash<String, ListaEnlazada<Monedero>> monederosPorCliente =
            new TablaHash<String, ListaEnlazada<Monedero>>();

    private int contadorClientes = 0;
    private int contadorMonederos = 0;

    public ClienteService(RelojSimulado reloj) {
        this.reloj = reloj;
    }

    // clientes

    /** Registra un cliente y le crea el monedero Principal. O(1) en promedio. */
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

        // El correo se guarda en minúscula para que "ANA@correo.com" y "ana@correo.com" cuenten igual.
        String correoClave = correo.trim().toLowerCase();
        if (clientesPorCorreo.contieneClave(correoClave)) {
            throw new MonederoException("Ya existe un cliente con ese correo");
        }

        contadorClientes++;
        Cliente cliente = new Cliente("C" + contadorClientes, nombre.trim(), correo.trim(), pin, reloj.ahora());

        clientes.agregarFinal(cliente);
        clientesPorId.insertar(cliente.getId(), cliente);
        clientesPorCorreo.insertar(correoClave, cliente);
        monederosPorCliente.insertar(cliente.getId(), new ListaEnlazada<Monedero>());

        crearMonedero(cliente.getId(), "Principal", TipoMonedero.PRINCIPAL);
        return cliente;
    }

    /** Busca un cliente por su ID. O(1) en promedio (antes O(n)). */
    public Cliente obtenerCliente(String id) {
        Cliente cliente = clientesPorId.obtener(id);
        if (cliente == null) {
            throw new MonederoException("Cliente no encontrado: " + id);
        }
        return cliente;
    }

    /** Todos los clientes en el orden en que se registraron. */
    public ListaEnlazada<Cliente> listarClientes() {
        return clientes;
    }

    //  monederos

    /** Crea un monedero para el cliente. O(1) en promedio (solo recorre sus máximo 5 monederos). */
    public Monedero crearMonedero(String clienteId, String nombre, TipoMonedero tipo) {
        obtenerCliente(clienteId);
        if (nombre == null || nombre.isBlank()) {
            throw new MonederoException("El nombre del monedero es obligatorio");
        }

        ListaEnlazada<Monedero> delCliente = monederosPorCliente.obtener(clienteId);
        if (delCliente.getTamano() >= Constantes.MAX_MONEDEROS) {
            throw new MonederoException("Maximo de monederos por cliente: " + Constantes.MAX_MONEDEROS);
        }
        for (Monedero existente : delCliente) {
            if (existente.getNombre().equalsIgnoreCase(nombre.trim())) {
                throw new MonederoException("El cliente ya tiene un monedero con ese nombre");
            }
        }

        contadorMonederos++;
        Monedero monedero = new Monedero("M" + contadorMonederos, clienteId, nombre.trim(), tipo);

        monederosPorId.insertar(monedero.getId(), monedero);
        delCliente.agregarFinal(monedero);
        return monedero;
    }

    /** Busca un monedero por su ID. O(1) en promedio (antes O(n)). Es el método más usado del sistema. */
    public Monedero obtenerMonedero(String id) {
        Monedero monedero = monederosPorId.obtener(id);
        if (monedero == null) {
            throw new MonederoException("Monedero no encontrado: " + id);
        }
        return monedero;
    }

    /**
     * Monederos de un cliente. O(1) en promedio para encontrarlos (antes recorría TODOS los monederos).
     * Se devuelve una copia para que quien la reciba no pueda dañar la lista interna.
     */
    public ListaEnlazada<Monedero> monederosDe(String clienteId) {
        obtenerCliente(clienteId);
        ListaEnlazada<Monedero> copia = new ListaEnlazada<Monedero>();
        for (Monedero monedero : monederosPorCliente.obtener(clienteId)) {
            copia.agregarFinal(monedero);
        }
        return copia;
    }

    public BigDecimal saldoTotal(String clienteId) {
        BigDecimal total = Dinero.CERO;
        for (Monedero monedero : monederosDe(clienteId)) {
            total = total.add(monedero.getSaldo());
        }
        return Dinero.redondear(total);
    }
}