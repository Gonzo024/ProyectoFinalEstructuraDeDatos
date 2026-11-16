package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.poo.excepciones.TransaccionInvalidaException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Constantes;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Operaciones de dinero del monedero
 * - Cada operación se guarda en el historial (ListaEnlazada) de los monederos involucrados.
 * - Las operaciones exitosas se apilan en la Pila del monedero que las hizo, para poder revertir
 *   la más reciente (LIFO).
 * - Los intentos fallidos (p. ej. saldo insuficiente) también quedan en el historial como FALLIDA.
 * - Después de cada operación se avisa a los listeners (puntos, notificaciones...): patrón Observador.
 */
public class TransaccionService {

    private final ClienteService clienteService;
    private final RelojSimulado reloj;
    private final ListaEnlazada<TransaccionListener> listeners = new ListaEnlazada<TransaccionListener>();
    private int contadorTransacciones = 0;
    /** Beneficios canjeados (Integrante 2). Si es null no se aplican descuentos extra. */
    private ProveedorDescuentos proveedor;

    public TransaccionService(ClienteService clienteService, RelojSimulado reloj) {
        this.clienteService = clienteService;
        this.reloj = reloj;
    }

    /** Conecta los beneficios canjeados (lo hace ContextoMonedero). */
    public void setProveedorDescuentos(ProveedorDescuentos proveedor) {
        this.proveedor = proveedor;
    }

    /** Los otros servicios (puntos, notificaciones...) se inscriben aquí para enterarse de cada movimiento. */
    public void agregarListener(TransaccionListener listener) {
        listeners.agregarFinal(listener);
    }

    // =========================================================== operaciones básicas

    /** Depósito: entra dinero al monedero. No tiene comisión. */
    public Transaccion depositar(String monederoId, BigDecimal monto, String descripcion) {
        validarMonto(monto);
        Monedero destino = clienteService.obtenerMonedero(monederoId);

        Transaccion t = Transaccion.builder(nuevoId(), TipoTransaccion.DEPOSITO, monto, reloj.ahora())
                .origen(destino.getClienteId(), null)
                .destino(destino.getClienteId(), destino.getId())
                .descripcion(descripcion)
                .construir();

        destino.acreditar(t.getMonto());
        registrarExitosa(t, destino, null);
        return t;
    }

    /** Retiro: sale dinero del monedero más una comisión del 1 %. */
    public Transaccion retirar(String monederoId, BigDecimal monto, String descripcion) {
        validarMonto(monto);
        Monedero origen = clienteService.obtenerMonedero(monederoId);
        boolean sinCargo = proveedor != null && proveedor.retiroSinComision(origen.getClienteId());
        BigDecimal comision = sinCargo
                ? Dinero.CERO
                : Dinero.porcentaje(monto, Constantes.COMISION_RETIRO);

        Transaccion t = Transaccion.builder(nuevoId(), TipoTransaccion.RETIRO, monto, reloj.ahora())
                .comision(comision)
                .origen(origen.getClienteId(), origen.getId())
                .descripcion(descripcion)
                .construir();

        debitarORegistrarFallida(origen, t);
        registrarExitosa(t, origen, null);
        return t;
    }

    /** Pago o gasto: sale dinero del monedero con una categoría (alimentación, transporte...). Sin comisión. */
    public Transaccion pagar(String monederoId, BigDecimal monto, CategoriaGasto categoria, String descripcion) {
        validarMonto(monto);
        if (categoria == null) {
            throw new TransaccionInvalidaException("El pago debe tener una categoria");
        }
        Monedero origen = clienteService.obtenerMonedero(monederoId);

        Transaccion t = Transaccion.builder(nuevoId(), TipoTransaccion.PAGO, monto, reloj.ahora())
                .origen(origen.getClienteId(), origen.getId())
                .categoria(categoria)
                .descripcion(descripcion)
                .construir();

        debitarORegistrarFallida(origen, t);
        registrarExitosa(t, origen, null);
        return t;
    }

    /**
     * Transferencia a otro cliente. Cobra 1.5 % de comisión al que envía, con el descuento de su rango
     * (Plata, Oro, Platino pagan menos).
     */
    public Transaccion transferir(String monederoOrigenId, String monederoDestinoId,
                                  BigDecimal monto, String descripcion) {
        validarMonto(monto);
        Monedero origen = clienteService.obtenerMonedero(monederoOrigenId);
        Monedero destino = clienteService.obtenerMonedero(monederoDestinoId);
        if (origen.getClienteId().equals(destino.getClienteId())) {
            throw new TransaccionInvalidaException(
                    "Para mover dinero entre tus propios monederos usa transferirEntreMisMonederos");
        }
        BigDecimal comision = calcularComisionTransferencia(origen.getClienteId(), monto);

        Transaccion t = Transaccion.builder(nuevoId(), TipoTransaccion.TRANSFERENCIA, monto, reloj.ahora())
                .comision(comision)
                .origen(origen.getClienteId(), origen.getId())
                .destino(destino.getClienteId(), destino.getId())
                .descripcion(descripcion)
                .construir();

        debitarORegistrarFallida(origen, t);
        destino.acreditar(t.getMonto());
        registrarExitosa(t, origen, destino);
        return t;
    }

    /** Multimonedero: mover dinero entre dos monederos del mismo cliente. Sin comisión. */
    public Transaccion transferirEntreMisMonederos(String monederoOrigenId, String monederoDestinoId,
                                                   BigDecimal monto, String descripcion) {
        validarMonto(monto);
        Monedero origen = clienteService.obtenerMonedero(monederoOrigenId);
        Monedero destino = clienteService.obtenerMonedero(monederoDestinoId);
        if (!origen.getClienteId().equals(destino.getClienteId())) {
            throw new TransaccionInvalidaException("Los dos monederos deben ser del mismo cliente");
        }
        if (origen.equals(destino)) {
            throw new TransaccionInvalidaException("El monedero de origen y destino deben ser distintos");
        }

        Transaccion t = Transaccion.builder(nuevoId(), TipoTransaccion.TRANSFERENCIA_INTERNA, monto, reloj.ahora())
                .origen(origen.getClienteId(), origen.getId())
                .destino(destino.getClienteId(), destino.getId())
                .descripcion(descripcion)
                .construir();

        debitarORegistrarFallida(origen, t);
        destino.acreditar(t.getMonto());
        registrarExitosa(t, origen, destino);
        return t;
    }

    /**
     * Bono de saldo por canje de puntos (lo llama el servicio de puntos del Integrante 2).
     * No se puede revertir, por eso no se apila.
     */
    public Transaccion acreditarBono(String monederoId, BigDecimal monto) {
        validarMonto(monto);
        Monedero destino = clienteService.obtenerMonedero(monederoId);

        Transaccion t = Transaccion.builder(nuevoId(), TipoTransaccion.BONO, monto, reloj.ahora())
                .origen(destino.getClienteId(), null)
                .destino(destino.getClienteId(), destino.getId())
                .descripcion("Bono por canje de puntos")
                .construir();

        destino.acreditar(t.getMonto());
        destino.getHistorial().agregarFinal(t);
        avisarCompletada(t);
        return t;
    }

    // =========================================================== consultas

    public BigDecimal consultarSaldo(String monederoId) {
        return clienteService.obtenerMonedero(monederoId).getSaldo();
    }

    public ListaEnlazada<Transaccion> consultarHistorial(String monederoId) {
        return clienteService.obtenerMonedero(monederoId).getHistorial();
    }

    // =========================================================== reversión (Pila)

    /**
     * Revierte la operación más reciente del monedero (la cima de su pila).
     * Forma elegida: la transacción original queda REVERTIDA (no se crea una nueva).
     * No se permite si algún monedero quedaría con saldo negativo.
     */
    public Transaccion revertirUltima(String monederoId) {
        Monedero monedero = clienteService.obtenerMonedero(monederoId);
        if (monedero.getReversibles().estaVacia()) {
            throw new TransaccionInvalidaException("No hay operaciones para revertir en este monedero");
        }
        // Se mira la cima sin sacarla: si la reversión falla, la operación debe seguir en la pila.
        Transaccion t = monedero.getReversibles().cima();

        for (TransaccionListener listener : listeners) {
            listener.validarReversion(t); // p. ej. puntos ya canjeados: el listener lanza excepción
        }

        Monedero origen = t.getMonederoOrigenId() == null ? null
                : clienteService.obtenerMonedero(t.getMonederoOrigenId());
        Monedero destino = t.getMonederoDestinoId() == null ? null
                : clienteService.obtenerMonedero(t.getMonederoDestinoId());

        // 1. Quitar el dinero que entró al destino (si no lo tiene, no se puede revertir).
        if (destino != null) {
            if (!destino.tieneSaldo(t.getMonto())) {
                throw new TransaccionInvalidaException(
                        "No se puede revertir: el monedero '" + destino.getNombre()
                                + "' quedaria con saldo negativo");
            }
            destino.debitar(t.getMonto());
        }
        // 2. Devolver al origen todo lo que salió (monto + comisión).
        if (origen != null) {
            origen.acreditar(t.getTotalDebitado());
        }

        monedero.getReversibles().pop();
        t.marcarRevertida(reloj.ahora());
        for (TransaccionListener listener : listeners) {
            listener.onTransaccionRevertida(t); // aquí el Integrante 2 resta los puntos
        }
        return t;
    }

    // =========================================================== integridad (recursiva)

    /**
     * Verifica que el saldo del monedero coincida con la suma de los efectos de su historial.
     * Si alguien cambió el saldo "por fuera" de este servicio, la verificación da false.
     */
    public boolean verificarIntegridad(String monederoId) {
        Monedero monedero = clienteService.obtenerMonedero(monederoId);
        BigDecimal saldoReconstruido = sumarEfectos(monedero.getHistorial().iterator(), monederoId);
        return Dinero.iguales(saldoReconstruido, monedero.getSaldo());
    }

    /**
     * Recursión: efecto de la transacción actual + suma de efectos del resto de la lista.
     * Caso base: no quedan transacciones → 0.
     */
    private BigDecimal sumarEfectos(Iterator<Transaccion> it, String monederoId) {
        if (!it.hasNext()) {
            return Dinero.CERO;
        }
        Transaccion actual = it.next();
        return actual.efectoSobre(monederoId).add(sumarEfectos(it, monederoId));
    }

    /** Verifica todos los monederos de un cliente (recorrido recursivo de la lista de monederos). */
    public boolean verificarIntegridadCliente(String clienteId) {
        return verificarMonederos(clienteService.monederosDe(clienteId).iterator());
    }

    private boolean verificarMonederos(Iterator<Monedero> it) {
        if (!it.hasNext()) {
            return true;
        }
        Monedero actual = it.next();
        return verificarIntegridad(actual.getId()) && verificarMonederos(it);
    }

    // =========================================================== ayudas privadas

    private String nuevoId() {
        contadorTransacciones++;
        return "T" + contadorTransacciones;
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null) {
            throw new TransaccionInvalidaException("El monto es obligatorio");
        }
        if (monto.compareTo(Constantes.MONTO_MINIMO) < 0 || monto.compareTo(Constantes.MONTO_MAXIMO) > 0) {
            throw new TransaccionInvalidaException("El monto debe estar entre "
                    + Constantes.MONTO_MINIMO + " y " + Constantes.MONTO_MAXIMO);
        }
    }

    /** Comisión de transferencia = monto x 1.5 %, menos el descuento del rango y el del beneficio canjeado. */
    private BigDecimal calcularComisionTransferencia(String clienteId, BigDecimal monto) {
        Cliente cliente = clienteService.obtenerCliente(clienteId);
        BigDecimal comision = Dinero.porcentaje(monto, Constantes.COMISION_TRANSFERENCIA);
        BigDecimal descuento = Dinero.porcentaje(comision, cliente.getRango().getDescuentoComisionTransferencia());
        comision = comision.subtract(descuento);
        // Descuento extra por beneficio canjeado (se aplica sobre lo que queda tras el del rango).
        if (proveedor != null) {
            BigDecimal extra = proveedor.descuentoComisionTransferencia(clienteId);
            comision = comision.subtract(Dinero.porcentaje(comision, extra));
        }
        return comision;
    }

    /**
     * Intenta sacar monto + comisión del origen. Si no alcanza, guarda la transacción como FALLIDA
     * en el historial y vuelve a lanzar la excepción para que la interfaz muestre el error.
     */
    private void debitarORegistrarFallida(Monedero origen, Transaccion t) {
        try {
            origen.debitar(t.getTotalDebitado());
        } catch (SaldoInsuficienteException e) {
            t.marcarFallida();
            origen.getHistorial().agregarFinal(t);
            throw e;
        }
    }

    /**
     * Guarda la transacción exitosa en el historial del monedero principal (y del otro monedero, si hay),
     * la apila en el monedero principal para poder revertirla, y avisa a los listeners.
     */
    private void registrarExitosa(Transaccion t, Monedero principal, Monedero otro) {
        principal.getHistorial().agregarFinal(t);
        if (otro != null) {
            otro.getHistorial().agregarFinal(t);
        }
        principal.getReversibles().push(t);
        avisarCompletada(t);
    }

    private void avisarCompletada(Transaccion t) {
        for (TransaccionListener listener : listeners) {
            listener.onTransaccionCompletada(t);
        }
    }
}
