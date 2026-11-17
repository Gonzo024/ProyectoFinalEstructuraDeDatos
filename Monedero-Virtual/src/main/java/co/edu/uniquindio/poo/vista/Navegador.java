package co.edu.uniquindio.poo.vista;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Cambia la ventana entre la pantalla de inicio de sesion y la pantalla de inicio.
 * <p>
 * Las dos pantallas se cargan una sola vez y se guardan. Asi los modulos (y lo que
 * hay en ellos) siguen ahi cuando alguien cierra sesion y otro entra.
 */
public final class Navegador {

    private static Scene escena;

    private static Parent login;
    private static LoginController controladorLogin;

    private static Parent inicio;
    private static PrincipalController controladorInicio;

    private Navegador() {
        // Solo tiene metodos estaticos.
    }

    /** Crea la escena de la ventana con la pantalla de inicio de sesion. */
    public static Scene crearEscena(double ancho, double alto) {
        FXMLLoader loader = cargador("/fxml/Login.fxml");
        try {
            login = loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la pantalla de inicio de sesion", e);
        }
        controladorLogin = loader.getController();
        escena = new Scene(login, ancho, alto);
        return escena;
    }

    /** Despues de iniciar sesion: muestra la pantalla de inicio con los modulos. */
    public static void mostrarInicio() {
        if (inicio == null) {
            FXMLLoader loader = cargador("/fxml/Principal.fxml");
            try {
                inicio = loader.load();
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo cargar la pantalla de inicio", e);
            }
            controladorInicio = loader.getController();
        }
        controladorInicio.alIniciarSesion();
        escena.setRoot(inicio);
    }

    /** Al cerrar sesion: vuelve a la pantalla de inicio de sesion. */
    public static void mostrarLogin() {
        controladorLogin.limpiar();
        escena.setRoot(login);
    }

    private static FXMLLoader cargador(String ruta) {
        return new FXMLLoader(Navegador.class.getResource(ruta));
    }
}
