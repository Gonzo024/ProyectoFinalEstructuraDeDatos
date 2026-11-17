
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.grafo.Arista;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GrafoTransferenciasClientesTest {

    private ContextoMonedero contexto;
    private MultimonederoService grafo;
    private TransaccionService transacciones;

    private void preparar() {
        contexto = new ContextoMonedero();

        ClienteService clientes = contexto.getClienteService();
        transacciones = contexto.getTransaccionService();
        grafo = contexto.getMultimonederoService();

        clientes.registrarCliente("Ana", "ana@correo.com", "1234");
        clientes.registrarCliente("Beto", "beto@correo.com", "5678");

        transacciones.depositar(
                "M1", Dinero.de(1000), "Saldo inicial"
        );
    }

    @Test
    void registraTransferenciaEntreClientes() {
        preparar();

        transacciones.transferir(
                "M1", "M2", Dinero.de(200), "Pago a Beto"
        );

        assertEquals(
                Dinero.de(200),
                grafo.totalTransferidoEntreClientes("C1", "C2")
        );
    }

    @Test
    void acumulaVariasTransferencias() {
        preparar();

        transacciones.transferir(
                "M1", "M2", Dinero.de(100), ""
        );

        transacciones.transferir(
                "M1", "M2", Dinero.de(200), ""
        );

        assertEquals(
                Dinero.de(300),
                grafo.totalTransferidoEntreClientes("C1", "C2")
        );

        Arista arista = grafo.getGrafoClientes()
                .buscarArista("C1", "C2");

        assertNotNull(arista);
        assertEquals(2, arista.getCantidad());
    }

    @Test
    void permiteConsultarEnviosYRecepciones() {
        preparar();

        transacciones.transferir(
                "M1", "M2", Dinero.de(150), ""
        );

        assertEquals(
                Dinero.de(150),
                grafo.totalEnviadoAClientes("C1")
        );

        assertEquals(
                Dinero.de(150),
                grafo.totalRecibidoDeClientes("C2")
        );

        assertEquals(
                1,
                grafo.transferenciasEntreClientes("C1").getTamano()
        );
    }

    @Test
    void revertirDescuentaDelGrafo() {
        preparar();

        transacciones.transferir(
                "M1", "M2", Dinero.de(200), ""
        );

        transacciones.revertirUltima("M1");

        assertEquals(
                Dinero.de(0),
                grafo.totalTransferidoEntreClientes("C1", "C2")
        );
    }

    @Test
    void mantieneSeparadosLosDosGrafos() {
        preparar();

        contexto.getClienteService().crearMonedero(
                "C1", "Ahorros", TipoMonedero.AHORROS
        );

        transacciones.transferirEntreMisMonederos(
                "M1", "M3", Dinero.de(100), ""
        );

        transacciones.transferir(
                "M1", "M2", Dinero.de(50), ""
        );

        assertEquals(
                Dinero.de(100),
                grafo.totalMovido("M1", "M3")
        );

        assertEquals(
                Dinero.de(50),
                grafo.totalTransferidoEntreClientes("C1", "C2")
        );
    }
}
