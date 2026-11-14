package co.edu.uniquindio.poo.vista;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación: abre la pantalla de inicio (Principal.fxml).
 * Desde ahí se entra a cada módulo y se vuelve con el botón "Volver al inicio".
 */
public class PrincipalApp extends Application {

    @Override
    public void start(Stage ventana) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Principal.fxml")
        );

        Scene escena = new Scene(loader.load(), 1180, 740);

        ventana.setTitle("Monedero Virtual");
        ventana.setScene(escena);

        // Tamaño mínimo para que las tarjetas y tablas no se desordenen al achicar la ventana.
        ventana.setMinWidth(1050);
        ventana.setMinHeight(680);

        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}