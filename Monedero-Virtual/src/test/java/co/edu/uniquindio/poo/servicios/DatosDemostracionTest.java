package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoBeneficio;
import co.edu.uniquindio.poo.util.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica que los datos de demostracion de la sustentacion quedan como se espera
 * y que son coherentes (integridad correcta en todos los monederos).
 */
class DatosDemostracionTest {

    private ContextoMonedero contexto;
    private ClienteService clientes;

    @BeforeEach
    void setUp() {
        contexto = new ContextoMonedero();
        DatosDemostracion.cargar(contexto);
        clientes = contexto.getClienteService();
    }

    @Test
    void creaCuatroClientesConLosIdsDeLaGuia() {
        assertEquals(4, clientes.listarClientes().getTamano());
        assertEquals("Ana Torres", clientes.obtenerCliente("C1").getNombre());
        assertEquals("Beto Ramirez", clientes.obtenerCliente("C2").getNombre());
        assertEquals("Carla Gomez", clientes.obtenerCliente("C3").getNombre());
        assertEquals("David Lopez", clientes.obtenerCliente("C4").getNombre());
        assertEquals("C1", clientes.obtenerMonedero("M3").getClienteId());
        assertEquals("C4", clientes.obtenerMonedero("M7").getClienteId());
    }

    @Test
    void hayUnClienteEnCadaRango() {
        assertEquals(Rango.PLATINO, clientes.obtenerCliente("C1").getRango());
        assertEquals(Rango.ORO, clientes.obtenerCliente("C2").getRango());
        assertEquals(Rango.PLATA, clientes.obtenerCliente("C3").getRango());
        assertEquals(Rango.BRONCE, clientes.obtenerCliente("C4").getRango());
        assertTrue(contexto.getRangoService().estaBalanceado());
    }

    @Test
    void losBeneficiosCanjeadosQuedanVigentes() {
        BeneficioService beneficios = contexto.getBeneficioService();
        assertTrue(beneficios.tieneVigente("C1", TipoBeneficio.DESCUENTO_TRANSFERENCIAS));
        assertTrue(beneficios.tieneVigente("C2", TipoBeneficio.RETIROS_SIN_CARGO));
    }

    @Test
    void losGrafosTienenLasTransferencias() {
        MultimonederoService grafos = contexto.getMultimonederoService();
        assertEquals(0, grafos.totalTransferidoEntreClientes("C1", "C2").compareTo(Dinero.de(30_000)));
        assertEquals(0, grafos.totalTransferidoEntreClientes("C3", "C1").compareTo(Dinero.de(10_000)));
        assertEquals(0, grafos.totalMovido("M1", "M2").compareTo(Dinero.de(100_000)));
    }

    @Test
    void elMonederoDeGastosDeDavidQuedaCercaDelSaldoBajo() {
        assertEquals(0, clientes.obtenerMonedero("M7").getSaldo().compareTo(Dinero.de(150)));
    }

    @Test
    void laIntegridadEsCorrectaEnTodosLosClientes() {
        TransaccionService transacciones = contexto.getTransaccionService();
        for (String id : new String[]{"C1", "C2", "C3", "C4"}) {
            assertTrue(transacciones.verificarIntegridadCliente(id), "integridad de " + id);
        }
    }
}
