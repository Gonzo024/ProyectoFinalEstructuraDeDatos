package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.arbol.ArbolBinarioBusqueda;
import co.edu.uniquindio.poo.estructuras.hash.TablaHash;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.excepciones.TransaccionInvalidaException;
import co.edu.uniquindio.poo.modelo.ClavePuntos;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.RegistroPuntos;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.util.Constantes;
import co.edu.uniquindio.poo.util.RelojSimulado;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Sistema de puntos (Integrante 2).
 * <p>
 * - TablaHash clienteId → RegistroPuntos: consulta de puntos en O(1) promedio.
 * - ArbolBinarioBusqueda (puntos, id) → clienteId: clasificación de clientes.
 * - RangoService (ArbolAVL): rangos Bronce, Plata, Oro y Platino.
 * <p>
 * Escucha a TransaccionService (patrón Observador): suma puntos cuando una
 * transacción se completa y los resta cuando se revierte.
 */
public class PuntosService implements TransaccionListener {

    private final TablaHash<String, RegistroPuntos> registros = new TablaHash<>();
    private final ArbolBinarioBusqueda<ClavePuntos, String> ranking = new ArbolBinarioBusqueda<>();

    private final ClienteService clienteService;
    private final RangoService rangoService;
    private final NotificacionService notificacionService;
    private final RelojSimulado reloj;

    public PuntosService(ClienteService clienteService, RangoService rangoService,
                         NotificacionService notificacionService, RelojSimulado reloj) {
        if (clienteService == null || rangoService == null
                || notificacionService == null || reloj == null) {
            throw new IllegalArgumentException("Los servicios son obligatorios");
        }
        this.clienteService = clienteService;
        this.rangoService = rangoService;
        this.notificacionService = notificacionService;
        this.reloj = reloj;
    }

    // =========================================================== cálculo

    /**
     * Puntos que genera una transacción:
     * (monto / 100, sin decimales) × puntos del tipo × multiplicador del rango del cliente.
     */
    public long calcularPuntos(Transaccion t) {
        if (t == null || !t.getTipo().generaPuntos()) {
            return 0;
        }
        long bloques = t.getMonto()
                .divide(Constantes.UNIDADES_POR_BLOQUE_PUNTOS, 0, RoundingMode.DOWN)
                .longValue();
        long base = bloques * t.getTipo().getPuntosPorCien();

        Rango rango = clienteService.obtenerCliente(t.getClienteOrigenId()).getRango();
        return rango.getMultiplicadorPuntos()
                .multiply(BigDecimal.valueOf(base))
                .setScale(0, RoundingMode.DOWN)
                .longValue();
    }

    // =========================================================== listener (Observador)

    @Override
    public void onTransaccionCompletada(Transaccion t) {
        if (!t.estaCompletada()) {
            return;
        }
        long puntos = calcularPuntos(t);
        if (puntos <= 0) {
            return;
        }
        String clienteId = t.getClienteOrigenId();
        RegistroPuntos registro = obtenerRegistro(clienteId);

        ClavePuntos anterior = registro.getClave();          // 1. clave vieja
        registro.ganar(puntos, t.getId(),
                t.getTipo().getNombre() + " por " + t.getMonto(), reloj.ahora());
        t.setPuntosGenerados(puntos);
        reubicar(clienteId, anterior, registro.getClave());  // 2. clave nueva

    }

    /**
     * Se llama ANTES de revertir. Si el cliente ya gastó esos puntos en un canje,
     * no se permite revertir (los puntos disponibles quedarían negativos).
     */
    @Override
    public void validarReversion(Transaccion t) {
        long puntos = t.getPuntosGenerados();
        if (puntos <= 0) {
            return;
        }
        RegistroPuntos registro = registros.obtener(t.getClienteOrigenId());
        if (registro != null && registro.getPuntosDisponibles() < puntos) {
            throw new TransaccionInvalidaException(
                    "No se puede revertir: los " + puntos
                            + " puntos de esta transaccion ya fueron canjeados");
        }
    }

    @Override
    public void onTransaccionRevertida(Transaccion t) {
        long puntos = t.getPuntosGenerados();
        if (puntos <= 0) {
            return;
        }
        String clienteId = t.getClienteOrigenId();
        RegistroPuntos registro = obtenerRegistro(clienteId);

        ClavePuntos anterior = registro.getClave();
        registro.descontarPorReversion(puntos, t.getId(), reloj.ahora());
        reubicar(clienteId, anterior, registro.getClave());

    }

    // =========================================================== canje

    /**
     * Resta puntos DISPONIBLES (lo usa BeneficioService).
     * No cambia los acumulados, así que el cliente no se mueve en los árboles
     * ni baja de rango.
     */
    public void canjear(String clienteId, long costo, String descripcion) {
        obtenerRegistro(clienteId).canjear(costo, descripcion, reloj.ahora());
    }

    // =========================================================== consultas

    /**
     * Registro de puntos del cliente. Si todavía no tiene, se le crea uno con 0
     * puntos y se agrega a los árboles (queda en Bronce).
     */
    public RegistroPuntos obtenerRegistro(String clienteId) {
        RegistroPuntos registro = registros.obtener(clienteId);
        if (registro == null) {
            clienteService.obtenerCliente(clienteId); // lanza excepción si no existe
            registro = new RegistroPuntos(clienteId);
            registros.insertar(clienteId, registro);
            ranking.insertar(registro.getClave(), clienteId);
            rangoService.actualizar(clienteId, null, registro.getClave());
        }
        return registro;
    }

    public long consultarPuntosDisponibles(String clienteId) {
        return obtenerRegistro(clienteId).getPuntosDisponibles();
    }

    public long consultarPuntosAcumulados(String clienteId) {
        return obtenerRegistro(clienteId).getPuntosAcumulados();
    }

    public ListaEnlazada<MovimientoPuntos> consultarHistorial(String clienteId) {
        return obtenerRegistro(clienteId).getHistorial();
    }

    /**
     * Los n clientes con más puntos, de mayor a menor.
     * Usa el recorrido inorden inverso del ABB: O(h + n).
     */
    public ListaEnlazada<RegistroPuntos> clasificacion(int n) {
        ListaEnlazada<RegistroPuntos> resultado = new ListaEnlazada<>();
        for (String clienteId : ranking.mayores(n)) {
            resultado.agregarFinal(registros.obtener(clienteId));
        }
        return resultado;
    }

    /** Asegura que todos los clientes registrados aparezcan en la clasificación (con 0 si son nuevos). */
    public void sincronizarClientes() {
        for (Cliente cliente : clienteService.listarClientes()) {
            obtenerRegistro(cliente.getId());
        }
    }

    public int alturaRanking() {
        return ranking.altura();
    }

    // =========================================================== interno

    /** Saca al cliente con su clave vieja y lo mete con la nueva en el ABB y en el AVL. */
    private void reubicar(String clienteId, ClavePuntos anterior, ClavePuntos nueva) {
        ranking.eliminar(anterior);
        ranking.insertar(nueva, clienteId);
        rangoService.actualizar(clienteId, anterior, nueva);
    }
}