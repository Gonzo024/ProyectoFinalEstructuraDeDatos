
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.util.RelojSimulado;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReporteGastosClienteServiceTest {

    @Test
    void analizarGastosDeVariosMonederos() {

        RelojSimulado reloj = new RelojSimulado(
            LocalDateTime.of(2026, 11, 5, 10, 0)
        );

        ClienteService clientes =
            new ClienteService(reloj);

        Cliente ana = clientes.registrarCliente(
            "Ana", "ana@correo.com", "1234"
        );

        Monedero principal = clientes.obtenerMonedero(
            "M1"
        );

        Monedero ahorros = clientes.crearMonedero(
            ana.getId(),
            "Ahorros",
            TipoMonedero.AHORROS
        );

        LocalDateTime fecha =
            LocalDateTime.of(2026, 11, 5, 10, 0);

        Transaccion comida = Transaccion.builder(
            "T1", TipoTransaccion.PAGO, 50000, fecha
        )
        .origen(ana.getId(), principal.getId())
        .categoria(CategoriaGasto.ALIMENTACION)
        .construir();

        Transaccion transporte = Transaccion.builder(
            "T2", TipoTransaccion.PAGO, 20000, fecha
        )
        .origen(ana.getId(), principal.getId())
        .categoria(CategoriaGasto.TRANSPORTE)
        .construir();

        Transaccion almuerzo = Transaccion.builder(
            "T3", TipoTransaccion.PAGO, 30000, fecha
        )
        .origen(ana.getId(), ahorros.getId())
        .categoria(CategoriaGasto.ALIMENTACION)
        .construir();

        Transaccion deposito = Transaccion.builder(
            "T4", TipoTransaccion.DEPOSITO, 100000, fecha
        )
        .destino(ana.getId(), ahorros.getId())
        .construir();

        principal.getHistorial().agregarFinal(comida);
        principal.getHistorial().agregarFinal(transporte);

        ahorros.getHistorial().agregarFinal(almuerzo);
        ahorros.getHistorial().agregarFinal(deposito);

        // Repetimos T1 para comprobar que no se cuente dos veces.
        principal.getHistorial().agregarFinal(comida);

        ReporteGastosClienteService reporte =
            new ReporteGastosClienteService(clientes);

        assertEquals(
            100000,
            reporte.calcularTotalCliente(ana.getId()),
            0.001
        );

        assertEquals(
            100000,
            reporte.calcularTotalMensual(
                ana.getId(), 2026, 11
            ),
            0.001
        );

        assertEquals(
            80000,
            reporte.calcularCategoriaMensual(
                ana.getId(),
                CategoriaGasto.ALIMENTACION,
                2026, 11
            ),
            0.001
        );

        assertEquals(
            CategoriaGasto.ALIMENTACION,
            reporte.obtenerCategoriaMayor(ana.getId())
        );

        assertEquals(
            3,
            reporte.obtenerHistorialGastos(
                ana.getId()
            ).getTamano()
        );
    }

    @Test
    void clienteSinGastosTieneTotalCero() {

        ClienteService clientes = new ClienteService(
            new RelojSimulado()
        );

        Cliente ana = clientes.registrarCliente(
            "Ana", "ana2@correo.com", "1234"
        );

        ReporteGastosClienteService reporte =
            new ReporteGastosClienteService(clientes);

        assertEquals(
            0,
            reporte.calcularTotalCliente(ana.getId()),
            0.001
        );

        assertNull(
            reporte.obtenerCategoriaMayor(ana.getId())
        );
    }
}
