
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;

public class AnalisisGastosService {

    public double calcularTotalGastos(
            ListaEnlazada<Transaccion> historial) {

        double total = 0;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)) {
                total += transaccion.getMonto();
            }
        }

        return Dinero.redondear(total);
    }

    public double calcularGastosPorCategoria(
            ListaEnlazada<Transaccion> historial,
            CategoriaGasto categoria) {

        double total = 0;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)
                    && transaccion.getCategoria() == categoria) {

                total += transaccion.getMonto();
            }
        }

        return Dinero.redondear(total);
    }

    public CategoriaGasto obtenerCategoriaMayorGasto(
            ListaEnlazada<Transaccion> historial) {

        CategoriaGasto categoriaMayor = null;
        double mayorGasto = 0;

        for (CategoriaGasto categoria : CategoriaGasto.values()) {

            double total = calcularGastosPorCategoria(
                historial, categoria
            );

            if (total > mayorGasto) {
                mayorGasto = total;
                categoriaMayor = categoria;
            }
        }

        return categoriaMayor;
    }

    public int contarPagos(
            ListaEnlazada<Transaccion> historial) {

        int cantidad = 0;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private boolean esGastoValido(Transaccion transaccion) {

        return transaccion != null
                && transaccion.getTipo() == TipoTransaccion.PAGO
                && transaccion.estaCompletada();
    }
}
