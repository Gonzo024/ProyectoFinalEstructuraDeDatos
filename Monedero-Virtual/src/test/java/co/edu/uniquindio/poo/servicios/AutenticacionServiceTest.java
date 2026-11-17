package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del inicio de sesion: administrador, clientes, datos incorrectos y registro.
 */
class AutenticacionServiceTest {

    private ClienteService clientes;
    private AutenticacionService autenticacion;
    private Cliente ana;

    @BeforeEach
    void setUp() {
        ContextoMonedero contexto = new ContextoMonedero();
        clientes = contexto.getClienteService();
        autenticacion = new AutenticacionService(clientes);
        ana = clientes.registrarCliente("Ana", "ana@prueba.com", "1234");
        Sesion.cerrar();
    }

    @AfterEach
    void cerrarSesion() {
        Sesion.cerrar(); // la sesion es global: no debe pasar de una prueba a otra
    }

    @Test
    void elAdministradorEntraConSuUsuarioYContrasena() {
        autenticacion.iniciarSesion("admin", "admin123");

        assertTrue(Sesion.esAdministrador());
        assertFalse(Sesion.esCliente());
        assertNull(Sesion.getCliente());
    }

    @Test
    void unClienteEntraConSuCorreoYPin() {
        autenticacion.iniciarSesion("ana@prueba.com", "1234");

        assertTrue(Sesion.esCliente());
        assertEquals(ana, Sesion.getCliente());
        assertEquals("CLIENTE:" + ana.getId(), Sesion.identificador());
    }

    @Test
    void elCorreoNoDistingueMayusculas() {
        autenticacion.iniciarSesion("  ANA@Prueba.com ", "1234");

        assertEquals(ana, Sesion.getCliente());
    }

    @Test
    void pinIncorrectoNoIniciaSesion() {
        assertThrows(MonederoException.class,
                () -> autenticacion.iniciarSesion("ana@prueba.com", "9999"));
        assertFalse(Sesion.estaIniciada());
    }

    @Test
    void correoInexistenteNoIniciaSesion() {
        assertThrows(MonederoException.class,
                () -> autenticacion.iniciarSesion("nadie@prueba.com", "1234"));
        assertFalse(Sesion.estaIniciada());
    }

    @Test
    void contrasenaDeAdministradorIncorrectaNoIniciaSesion() {
        assertThrows(MonederoException.class,
                () -> autenticacion.iniciarSesion("admin", "1234"));
        assertFalse(Sesion.estaIniciada());
    }

    @Test
    void camposVaciosNoIniciaSesion() {
        assertThrows(MonederoException.class,
                () -> autenticacion.iniciarSesion("", ""));
        assertFalse(Sesion.estaIniciada());
    }

    @Test
    void crearCuentaRegistraAlClienteEIniciaSuSesion() {
        Cliente nuevo = autenticacion.registrarEIniciarSesion("Beto", "beto@prueba.com", "5678");

        assertTrue(Sesion.esCliente());
        assertEquals(nuevo, Sesion.getCliente());
        assertEquals(1, clientes.monederosDe(nuevo.getId()).getTamano()); // su monedero Principal
    }

    @Test
    void crearCuentaConCorreoRepetidoFallaYNoIniciaSesion() {
        assertThrows(MonederoException.class,
                () -> autenticacion.registrarEIniciarSesion("Otra Ana", "ana@prueba.com", "1111"));
        assertFalse(Sesion.estaIniciada());
    }

    @Test
    void cerrarSesionLaDejaVacia() {
        autenticacion.iniciarSesion("ana@prueba.com", "1234");
        Sesion.cerrar();

        assertFalse(Sesion.estaIniciada());
        assertEquals("", Sesion.identificador());
    }

    @Test
    void buscarClientePorCorreo() {
        assertEquals(ana, clientes.obtenerClientePorCorreo("ana@prueba.com"));
        assertEquals(ana, clientes.obtenerClientePorCorreo("ANA@PRUEBA.COM"));
        assertNull(clientes.obtenerClientePorCorreo("nadie@prueba.com"));
        assertNull(clientes.obtenerClientePorCorreo(""));
    }
}
