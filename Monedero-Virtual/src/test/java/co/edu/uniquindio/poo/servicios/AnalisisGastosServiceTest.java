
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AnalisisGastosServiceTest {

    @Test
    void calcularGastosCorrectamente() {

        ListaEnlazada<Transaccion> historial =
            new ListaEnlazada<>();

        LocalDateTime fecha = LocalDateTime.of(
            2026, 11, 5, 10, 0
        );

        historial.agregarFinal(
            Transaccion.builder(
                "T001", TipoTransaccion.PAGO, 80000, fecha
            )
            .categoria(CategoriaGasto.ALIMENTACION)
            .origen("C001", "M001")
            .construir()
        );

        historial.agregarFinal(
            Transaccion.builder(
                "T002", TipoTransaccion.PAGO, 20000, fecha
            )
            .categoria(CategoriaGasto.TRANSPORTE)
            .origen("C001", "M001")
            .construir()
        );

        historial.agregarFinal(
            Transaccion.builder(
                "T003", TipoTransaccion.PAGO, 30000, fecha
            )
            .categoria(CategoriaGasto.ALIMENTACION)
            .origen("C001", "M001")
            .construir()
        );

        // Este deposito NO debe contarse como gasto.
        historial.agregarFinal(
            Transaccion.builder(
                "T004", TipoTransaccion.DEPOSITO, 500000, fecha
            )
            .destino("C001", "M001")
            .construir()
        );

        AnalisisGastosService servicio =
            new AnalisisGastosService();

        assertEquals(
            130000,
            servicio.calcularTotalGastos(historial),
            0.001
        );

        assertEquals(
            110000,
            servicio.calcularGastosPorCategoria(
                historial,
                CategoriaGasto.ALIMENTACION
            ),
            0.001
        );

        assertEquals(
            CategoriaGasto.ALIMENTACION,
            servicio.obtenerCategoriaMayorGasto(historial)
        );

        assertEquals(
            3,
            servicio.contarPagos(historial)
        );
    }
}
