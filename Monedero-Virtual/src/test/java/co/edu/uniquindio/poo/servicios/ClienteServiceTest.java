package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.RelojSimulado;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ClienteServiceTest {

    private ClienteService crearServicio() {
        return new ClienteService(new RelojSimulado(LocalDateTime.of(2026, 11, 4, 10, 0)));
    }

    @Test
    void registrarClienteLeAsignaIdFechaYMonederoPrincipal() {
        ClienteService servicio = crearServicio();

        Cliente ana = servicio.registrarCliente("Ana", "ana@correo.com", "1234");

        assertEquals("C1", ana.getId());
        assertEquals(LocalDateTime.of(2026, 11, 4, 10, 0), ana.getFechaRegistro());
        ListaEnlazada<Monedero> monederos = servicio.monederosDe("C1");
        assertEquals(1, monederos.getTamano());
        assertEquals(TipoMonedero.PRINCIPAL, monederos.obtenerValorNodo(0).getTipo());
    }

    @Test
    void noSePermiteRepetirElCorreo() {
        ClienteService servicio = crearServicio();
        servicio.registrarCliente("Ana", "ana@correo.com", "1234");

        try {
            servicio.registrarCliente("Otra Ana", "ANA@correo.com", "9999");
            fail("Debio lanzar MonederoException");
        } catch (MonederoException e) {
            // correcto: el correo ya esta registrado
        }
        assertEquals(1, servicio.listarClientes().getTamano());
    }

    @Test
    void noSePermiteRegistrarSinNombre() {
        ClienteService servicio = crearServicio();

        try {
            servicio.registrarCliente("  ", "ana@correo.com", "1234");
            fail("Debio lanzar MonederoException");
        } catch (MonederoException e) {
            // correcto: el nombre es obligatorio
        }
    }

    @Test
    void buscarUnClienteInexistenteLanzaError() {
        ClienteService servicio = crearServicio();

        try {
            servicio.obtenerCliente("C99");
            fail("Debio lanzar MonederoException");
        } catch (MonederoException e) {
            // correcto
        }
    }

    @Test
    void crearMonederoAgregaUnoNuevo() {
        ClienteService servicio = crearServicio();
        servicio.registrarCliente("Ana", "ana@correo.com", "1234");

        Monedero ahorros = servicio.crearMonedero("C1", "Ahorros", TipoMonedero.AHORROS);

        assertEquals("M2", ahorros.getId());
        assertEquals(2, servicio.monederosDe("C1").getTamano());
        assertEquals("Ahorros", servicio.obtenerMonedero("M2").getNombre());
    }

    @Test
    void noSePermiteRepetirElNombreDeUnMonederoDelMismoCliente() {
        ClienteService servicio = crearServicio();
        servicio.registrarCliente("Ana", "ana@correo.com", "1234");

        try {
            servicio.crearMonedero("C1", "principal", TipoMonedero.OTRO);
            fail("Debio lanzar MonederoException");
        } catch (MonederoException e) {
            // correcto: ya existe "Principal"
        }
    }

    @Test
    void noSePuedeSuperarElMaximoDeMonederos() {
        ClienteService servicio = crearServicio();
        servicio.registrarCliente("Ana", "ana@correo.com", "1234");
        for (int i = 1; i <= 4; i++) {
            servicio.crearMonedero("C1", "Extra" + i, TipoMonedero.OTRO);
        }

        try {
            servicio.crearMonedero("C1", "Sobrante", TipoMonedero.OTRO);
            fail("Debio lanzar MonederoException");
        } catch (MonederoException e) {
            // correcto: ya tiene 5 monederos
        }
        assertEquals(5, servicio.monederosDe("C1").getTamano());
    }

    @Test
    void saldoTotalSumaLosMonederosDelCliente() {
        ClienteService servicio = crearServicio();
        servicio.registrarCliente("Ana", "ana@correo.com", "1234");
        servicio.crearMonedero("C1", "Ahorros", TipoMonedero.AHORROS);
        servicio.obtenerMonedero("M1").acreditar(100);
        servicio.obtenerMonedero("M2").acreditar(50.5);

        assertEquals(150.5, servicio.saldoTotal("C1"), 0.001);
    }
}
