
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class AnalisisGastosService {

    // Calcula el total de todos los gastos realizados
    public BigDecimal calcularTotalGastos(
            ListaEnlazada<Transaccion> historial) {

        BigDecimal total = Dinero.CERO;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)) {
                total = total.add(transaccion.getMonto());
            }
        }

        return Dinero.redondear(total);
    }

    // Calcula los gastos de una categoria especifica
    public BigDecimal calcularGastosPorCategoria(
            ListaEnlazada<Transaccion> historial,
            CategoriaGasto categoria) {

        BigDecimal total = Dinero.CERO;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)
                    && transaccion.getCategoria() == categoria) {

                total = total.add(transaccion.getMonto());
            }
        }

        return Dinero.redondear(total);
    }

    // Busca la categoria donde se ha gastado mas dinero
    public CategoriaGasto obtenerCategoriaMayorGasto(
            ListaEnlazada<Transaccion> historial) {

        CategoriaGasto categoriaMayor = null;
        BigDecimal mayorGasto = Dinero.CERO;

        for (CategoriaGasto categoria : CategoriaGasto.values()) {

            BigDecimal total = calcularGastosPorCategoria(
                historial, categoria
            );

            if (total.compareTo(mayorGasto) > 0) {
                mayorGasto = total;
                categoriaMayor = categoria;
            }
        }

        return categoriaMayor;
    }

    // Cuenta la cantidad de pagos completados
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

    // Calcula los gastos de un mes y un año determinados
    public BigDecimal calcularGastosDelMes(
            ListaEnlazada<Transaccion> historial,
            int anio,
            int mes) {

        YearMonth periodo = YearMonth.of(anio, mes);
        BigDecimal total = Dinero.CERO;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)
                    && transaccion.getFecha() != null) {

                YearMonth fechaTransaccion = YearMonth.from(
                    transaccion.getFecha()
                );

                if (fechaTransaccion.equals(periodo)) {
                    total = total.add(transaccion.getMonto());
                }
            }
        }

        return Dinero.redondear(total);
    }

    // Calcula gastos de una categoria en un mes especifico
    public BigDecimal calcularGastosPorCategoriaYMes(
            ListaEnlazada<Transaccion> historial,
            CategoriaGasto categoria,
            int anio,
            int mes) {

        YearMonth periodo = YearMonth.of(anio, mes);
        BigDecimal total = Dinero.CERO;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)
                    && transaccion.getFecha() != null
                    && transaccion.getCategoria() == categoria) {

                YearMonth fechaTransaccion = YearMonth.from(
                    transaccion.getFecha()
                );

                if (fechaTransaccion.equals(periodo)) {
                    total = total.add(transaccion.getMonto());
                }
            }
        }

        return Dinero.redondear(total);
    }

    // Calcula gastos entre dos fechas, incluyendo ambos dias
    public BigDecimal calcularGastosEntreFechas(
            ListaEnlazada<Transaccion> historial,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (fechaInicio == null || fechaFin == null
                || fechaInicio.isAfter(fechaFin)) {

            throw new IllegalArgumentException(
                "El rango de fechas no es valido"
            );
        }

        BigDecimal total = Dinero.CERO;

        for (Transaccion transaccion : historial) {

            if (esGastoValido(transaccion)
                    && transaccion.getFecha() != null) {

                LocalDate fecha = transaccion.getFecha()
                    .toLocalDate();

                boolean despuesDelInicio =
                    !fecha.isBefore(fechaInicio);

                boolean antesDelFin =
                    !fecha.isAfter(fechaFin);

                if (despuesDelInicio && antesDelFin) {
                    total = total.add(transaccion.getMonto());
                }
            }
        }

        return Dinero.redondear(total);
    }

    // Comprueba si una transaccion debe contarse como gasto
    private boolean esGastoValido(Transaccion transaccion) {

        return transaccion != null
                && transaccion.getTipo() == TipoTransaccion.PAGO
                && transaccion.estaCompletada();
    }
}
