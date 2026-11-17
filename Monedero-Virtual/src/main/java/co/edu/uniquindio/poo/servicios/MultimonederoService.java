
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.grafo.Arista;
import co.edu.uniquindio.poo.estructuras.grafo.GrafoDirigido;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;

import java.math.BigDecimal;

public class MultimonederoService implements TransaccionListener {

    private final ClienteService clienteService;

    // Grafo original: movimientos entre monederos propios.
    private final GrafoDirigido grafo = new GrafoDirigido();

    // Nuevo grafo: transferencias entre clientes distintos.
    private final GrafoDirigido grafoClientes = new GrafoDirigido();

    public MultimonederoService(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    public void onTransaccionCompletada(Transaccion t) {

        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA) {
            grafo.agregarArista(
                    t.getMonederoOrigenId(),
                    t.getMonederoDestinoId(),
                    t.getMonto()
            );
        }

        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            grafoClientes.agregarArista(
                    t.getClienteOrigenId(),
                    t.getClienteDestinoId(),
                    t.getMonto()
            );
        }
    }

    @Override
    public void onTransaccionRevertida(Transaccion t) {

        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA) {
            grafo.restarArista(
                    t.getMonederoOrigenId(),
                    t.getMonederoDestinoId(),
                    t.getMonto()
            );
        }

        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            grafoClientes.restarArista(
                    t.getClienteOrigenId(),
                    t.getClienteDestinoId(),
                    t.getMonto()
            );
        }
    }

    // ==========================================
    // CONSULTAS ORIGINALES ENTRE MONEDEROS
    // ==========================================

    public ListaEnlazada<Arista> movimientosDelCliente(String clienteId) {
        ListaEnlazada<Arista> resultado = new ListaEnlazada<>();

        for (Monedero monedero : clienteService.monederosDe(clienteId)) {
            for (Arista arista : grafo.aristasDesde(monedero.getId())) {
                resultado.agregarFinal(arista);
            }
        }

        return resultado;
    }

    public BigDecimal totalMovido(
            String monederoOrigenId,
            String monederoDestinoId) {

        return grafo.pesoEntre(
                monederoOrigenId,
                monederoDestinoId
        );
    }

    public BigDecimal totalEnviado(String monederoId) {
        return grafo.totalSaliente(monederoId);
    }

    public BigDecimal totalRecibido(String monederoId) {
        return grafo.totalEntrante(monederoId);
    }

    public ListaEnlazada<String> destinosAlcanzables(String monederoId) {
        return grafo.alcanzablesDesde(monederoId);
    }

    public GrafoDirigido getGrafo() {
        return grafo;
    }

    // ==========================================
    // NUEVAS CONSULTAS ENTRE CLIENTES
    // ==========================================

    // Transferencias enviadas por un cliente.
    public ListaEnlazada<Arista> transferenciasEntreClientes(
            String clienteId) {

        return grafoClientes.aristasDesde(clienteId);
    }

    // Dinero transferido de un cliente a otro.
    public BigDecimal totalTransferidoEntreClientes(
            String clienteOrigenId,
            String clienteDestinoId) {

        return grafoClientes.pesoEntre(
                clienteOrigenId,
                clienteDestinoId
        );
    }

    // Total enviado a otros clientes.
    public BigDecimal totalEnviadoAClientes(String clienteId) {
        return grafoClientes.totalSaliente(clienteId);
    }

    // Total recibido desde otros clientes.
    public BigDecimal totalRecibidoDeClientes(String clienteId) {
        return grafoClientes.totalEntrante(clienteId);
    }

    // Clientes alcanzables mediante transferencias.
    public ListaEnlazada<String> clientesAlcanzables(
            String clienteId) {

        return grafoClientes.alcanzablesDesde(clienteId);
    }

    public GrafoDirigido getGrafoClientes() {
        return grafoClientes;
    }
}
