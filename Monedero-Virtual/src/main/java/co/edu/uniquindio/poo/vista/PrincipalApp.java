package co.edu.uniquindio.poo.vista;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación: abre la pantalla de inicio de sesión (Login.fxml).
 * Al entrar se muestra la pantalla de inicio (Principal.fxml), y desde ahí cada módulo.
 */
public class PrincipalApp extends Application {

    @Override
    public void start(Stage ventana) {

        ventana.setTitle("Monedero Virtual");
        ventana.setScene(Navegador.crearEscena(1180, 740));

        // Tamaño mínimo para que las tarjetas y tablas no se desordenen al achicar la ventana.
        ventana.setMinWidth(1050);
        ventana.setMinHeight(680);

        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
