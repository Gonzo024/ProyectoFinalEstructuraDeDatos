package co.edu.uniquindio.poo.vista;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ModuloUnoApp extends Application {

    @Override
    public void start(Stage ventana) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ModuloUno.fxml")
        );

        Scene escena = new Scene(loader.load(), 950, 650);

        ventana.setTitle("Monedero Virtual - Clientes y operaciones");
        ventana.setScene(escena);
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
