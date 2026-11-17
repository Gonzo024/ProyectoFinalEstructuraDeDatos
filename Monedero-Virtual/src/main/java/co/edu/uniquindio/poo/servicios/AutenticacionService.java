package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.util.Constantes;

/**
 * Inicio de sesion del administrador y de los clientes.
 * <p>
 * - Administrador: usuario y contrasena de Constantes.
 * - Cliente: su correo y su PIN. El correo se busca en la tabla hash de ClienteService: O(1).
 */
public class AutenticacionService {

    private static final String MENSAJE_INCORRECTO = "Usuario o contrasena incorrectos";

    private final ClienteService clienteService;

    public AutenticacionService(ClienteService clienteService) {
        if (clienteService == null) {
            throw new IllegalArgumentException("El servicio de clientes es obligatorio");
        }
        this.clienteService = clienteService;
    }

    public boolean esAdministrador(String usuario, String clave) {
        return usuario != null && clave != null
                && usuario.trim().equalsIgnoreCase(Constantes.ADMIN_USUARIO)
                && clave.equals(Constantes.ADMIN_CLAVE);
    }

    /**
     * Valida el correo y el PIN de un cliente.
     *
     * @return el cliente si los datos son correctos
     * @throws MonederoException si el correo no existe o el PIN no coincide
     */
    public Cliente autenticarCliente(String correo, String pin) {
        Cliente cliente = clienteService.obtenerClientePorCorreo(correo);
        // Mismo mensaje en los dos casos: no se revela si el correo existe.
        if (cliente == null || pin == null || !cliente.pinCorrecto(pin)) {
            throw new MonederoException(MENSAJE_INCORRECTO);
        }
        if (!cliente.isActivo()) {
            throw new MonederoException(
                    "Tu cuenta esta desactivada. Comunicate con el administrador");
        }
        return cliente;
    }

    /**
     * Inicia la sesion: primero revisa si es el administrador y si no, busca al cliente.
     */
    public void iniciarSesion(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            throw new MonederoException("Escribe el usuario (o correo) y la contrasena (o PIN)");
        }
        if (esAdministrador(usuario, clave)) {
            Sesion.iniciarComoAdministrador();
            return;
        }
        Sesion.iniciarComoCliente(autenticarCliente(usuario, clave));
    }

    /** Crea la cuenta de un cliente nuevo e inicia su sesion. */
    public Cliente registrarEIniciarSesion(String nombre, String correo, String pin) {
        Cliente cliente = clienteService.registrarCliente(nombre, correo, pin);
        Sesion.iniciarComoCliente(cliente);
        return cliente;
    }
}
