package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.servicios.AutenticacionService;
import co.edu.uniquindio.poo.servicios.ContextoSesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Pantalla de inicio de sesion.
 * El administrador entra con su usuario y contrasena; un cliente, con su correo y su PIN.
 * Tambien permite que un cliente nuevo cree su cuenta.
 */
public class LoginController {

    private final AutenticacionService autenticacion =
            new AutenticacionService(ContextoSesion.obtener().getClienteService());

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblMensajeLogin;

    @FXML private TextField txtNombreNuevo;
    @FXML private TextField txtCorreoNuevo;
    @FXML private PasswordField txtPinNuevo;
    @FXML private Label lblMensajeRegistro;

    @FXML
    private void iniciarSesion() {
        try {
            autenticacion.iniciarSesion(txtUsuario.getText(), txtClave.getText());
            Navegador.mostrarInicio();
        } catch (RuntimeException error) {
            mostrarError(lblMensajeLogin, error);
            txtClave.clear();
        }
    }

    @FXML
    private void crearCuenta() {
        try {
            autenticacion.registrarEIniciarSesion(
                    txtNombreNuevo.getText(),
                    txtCorreoNuevo.getText(),
                    txtPinNuevo.getText());
            Navegador.mostrarInicio();
        } catch (RuntimeException error) {
            mostrarError(lblMensajeRegistro, error);
        }
    }

    /** Deja la pantalla vacia (se llama al cerrar sesion). */
    public void limpiar() {
        txtUsuario.clear();
        txtClave.clear();
        txtNombreNuevo.clear();
        txtCorreoNuevo.clear();
        txtPinNuevo.clear();
        lblMensajeLogin.setText("");
        lblMensajeRegistro.setText("");
    }

    private void mostrarError(Label etiqueta, RuntimeException error) {
        String mensaje = error.getMessage();
        etiqueta.setText(mensaje == null || mensaje.isBlank()
                ? "No fue posible completar la operacion"
                : mensaje);
    }
}
