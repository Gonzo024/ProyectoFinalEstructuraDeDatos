
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;

public class ReporteGastosClienteService {

    private ClienteService clienteService;
    private AnalisisGastosService analisisService;

    public ReporteGastosClienteService(
            ClienteService clienteService) {

        if (clienteService == null) {
            throw new IllegalArgumentException(
                "El servicio de clientes es obligatorio"
            );
        }

        this.clienteService = clienteService;
        this.analisisService = new AnalisisGastosService();
    }

    // Reune los pagos de todos los monederos de un cliente.
    public ListaEnlazada<Transaccion> obtenerHistorialGastos(
            String clienteId) {

        ListaEnlazada<Transaccion> resultado =
            new ListaEnlazada<>();

        ListaEnlazada<String> idsAgregados =
            new ListaEnlazada<>();

        // ClienteService valida que el cliente exista.
        ListaEnlazada<Monedero> monederos =
            clienteService.monederosDe(clienteId);

        for (Monedero monedero : monederos) {

            for (Transaccion transaccion :
                    monedero.getHistorial()) {

                if (transaccion == null) {
                    continue;
                }

                boolean esPago =
                    transaccion.getTipo()
                    == TipoTransaccion.PAGO;

                boolean estaCompletada =
                    transaccion.estaCompletada();

                boolean esDelCliente =
                    clienteId.equals(
                        transaccion.getClienteOrigenId()
                    );

                boolean esDelMonedero =
                    monedero.getId().equals(
                        transaccion.getMonederoOrigenId()
                    );

                boolean noEstaRepetida =
                    !contieneId(
                        idsAgregados,
                        transaccion.getId()
                    );

                if (esPago && estaCompletada
                        && esDelCliente && esDelMonedero
                        && noEstaRepetida) {

                    resultado.agregarFinal(transaccion);
                    idsAgregados.agregarFinal(
                        transaccion.getId()
                    );
                }
            }
        }

        return resultado;
    }

    // Comprueba si ya agregamos una transaccion.
    private boolean contieneId(
            ListaEnlazada<String> ids,
            String id) {

        for (String actual : ids) {
            if (actual.equals(id)) {
                return true;
            }
        }

        return false;
    }

    // Total gastado por el cliente.
    public double calcularTotalCliente(String clienteId) {

        ListaEnlazada<Transaccion> historial =
            obtenerHistorialGastos(clienteId);

        return analisisService.calcularTotalGastos(
            historial
        );
    }

    // Total gastado durante un mes.
    public double calcularTotalMensual(
            String clienteId,
            int anio,
            int mes) {

        ListaEnlazada<Transaccion> historial =
            obtenerHistorialGastos(clienteId);

        return analisisService.calcularGastosDelMes(
            historial, anio, mes
        );
    }

    // Categoria donde el cliente gasto mas.
    public CategoriaGasto obtenerCategoriaMayor(
            String clienteId) {

        ListaEnlazada<Transaccion> historial =
            obtenerHistorialGastos(clienteId);

        return analisisService.obtenerCategoriaMayorGasto(
            historial
        );
    }

    // Gastos de una categoria durante un mes.
    public double calcularCategoriaMensual(
            String clienteId,
            CategoriaGasto categoria,
            int anio,
            int mes) {

        ListaEnlazada<Transaccion> historial =
            obtenerHistorialGastos(clienteId);

        return analisisService.calcularGastosPorCategoriaYMes(
            historial, categoria, anio, mes
        );
    }
}
