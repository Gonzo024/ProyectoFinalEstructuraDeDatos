package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.excepciones.TransaccionInvalidaException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de lo que hace el administrador con los clientes:
 * actualizar sus datos y desactivar o reactivar su cuenta.
 */
class AdministracionClientesTest {

    private ClienteService clientes;
    private TransaccionService transacciones;
    private AutenticacionService autenticacion;
    private Cliente ana;
    private Cliente beto;
    private String monederoAna;
    private String monederoBeto;

    @BeforeEach
    void setUp() {
        ContextoMonedero contexto = new ContextoMonedero();
        clientes = contexto.getClienteService();
        transacciones = contexto.getTransaccionService();
        autenticacion = new AutenticacionService(clientes);

        ana = clientes.registrarCliente("Ana", "ana@prueba.com", "1234");
        beto = clientes.registrarCliente("Beto", "beto@prueba.com", "5678");
        monederoAna = clientes.monederosDe(ana.getId()).obtenerValorNodo(0).getId();
        monederoBeto = clientes.monederosDe(beto.getId()).obtenerValorNodo(0).getId();
        transacciones.depositar(monederoAna, Dinero.de(1000), "inicial");
        Sesion.cerrar();
    }

    @AfterEach
    void cerrarSesion() {
        Sesion.cerrar();
    }

    // ============================================================ actualizar

    @Test
    void actualizarCambiaNombreCorreoYPin() {
        clientes.actualizarCliente(ana.getId(), "Ana Maria", "anamaria@prueba.com", "4321");

        assertEquals("Ana Maria", ana.getNombre());
        assertEquals("anamaria@prueba.com", ana.getCorreo());
        // Entra con el correo y el PIN nuevos...
        assertEquals(ana, autenticacion.autenticarCliente("anamaria@prueba.com", "4321"));
        // ...y ya no con los viejos.
        assertThrows(MonederoException.class,
                () -> autenticacion.autenticarCliente("ana@prueba.com", "1234"));
        assertNull(clientes.obtenerClientePorCorreo("ana@prueba.com"));
    }

    @Test
    void pinVacioNoCambiaElPin() {
        clientes.actualizarCliente(ana.getId(), "Ana", "ana@prueba.com", "");

        assertEquals(ana, autenticacion.autenticarCliente("ana@prueba.com", "1234"));
    }

    @Test
    void noSePuedeUsarElCorreoDeOtroCliente() {
        assertThrows(MonederoException.class,
                () -> clientes.actualizarCliente(ana.getId(), "Ana", "beto@prueba.com", ""));

        assertEquals("ana@prueba.com", ana.getCorreo());
        assertEquals(beto, clientes.obtenerClientePorCorreo("beto@prueba.com"));
    }

    @Test
    void nombreVacioNoSeAcepta() {
        assertThrows(MonederoException.class,
                () -> clientes.actualizarCliente(ana.getId(), " ", "ana@prueba.com", ""));
        assertEquals("Ana", ana.getNombre());
    }

    // ============================================================ desactivar

    @Test
    void unaCuentaDesactivadaNoPuedeIniciarSesion() {
        clientes.desactivarCliente(ana.getId());

        assertThrows(MonederoException.class,
                () -> autenticacion.iniciarSesion("ana@prueba.com", "1234"));
        assertFalse(Sesion.estaIniciada());
    }

    @Test
    void unaCuentaDesactivadaNoPuedeHacerNiRecibirOperaciones() {
        clientes.desactivarCliente(ana.getId());

        assertThrows(TransaccionInvalidaException.class,
                () -> transacciones.depositar(monederoAna, Dinero.de(100), "x"));
        assertThrows(TransaccionInvalidaException.class,
                () -> transacciones.retirar(monederoAna, Dinero.de(100), "x"));
        assertThrows(TransaccionInvalidaException.class,
                () -> transacciones.transferir(monederoBeto, monederoAna, Dinero.de(10), "x"));
        assertThrows(MonederoException.class,
                () -> clientes.crearMonedero(ana.getId(), "Ahorros", TipoMonedero.AHORROS));

        // No se movio dinero.
        assertEquals(0, transacciones.consultarSaldo(monederoAna).compareTo(Dinero.de(1000)));
    }

    @Test
    void desactivarConservaElHistorialYLaIntegridad() {
        transacciones.transferir(monederoAna, monederoBeto, Dinero.de(100), "antes");
        clientes.desactivarCliente(ana.getId());

        assertEquals(2, transacciones.consultarHistorial(monederoAna).getTamano());
        assertTrue(transacciones.verificarIntegridadCliente(ana.getId()));
        assertTrue(transacciones.verificarIntegridadCliente(beto.getId()));
    }

    @Test
    void lasCuentasDesactivadasNoSalenEnLaListaDeActivos() {
        clientes.desactivarCliente(ana.getId());

        assertEquals(1, clientes.listarClientesActivos().getTamano());
        assertEquals(beto, clientes.listarClientesActivos().obtenerValorNodo(0));
        assertEquals(2, clientes.listarClientes().getTamano()); // no se borro
    }

    @Test
    void reactivarDevuelveElAcceso() {
        clientes.desactivarCliente(ana.getId());
        clientes.reactivarCliente(ana.getId());

        autenticacion.iniciarSesion("ana@prueba.com", "1234");
        assertEquals(ana, Sesion.getCliente());
        transacciones.depositar(monederoAna, Dinero.de(100), "de nuevo");
    }

    @Test
    void noSePuedeDesactivarDosVecesNiReactivarUnaCuentaActiva() {
        assertThrows(MonederoException.class, () -> clientes.reactivarCliente(ana.getId()));
        clientes.desactivarCliente(ana.getId());
        assertThrows(MonederoException.class, () -> clientes.desactivarCliente(ana.getId()));
    }
}
