package co.edu.uniquindio.poo.vista;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Controla la pantalla de inicio y la navegación entre módulos.
 * <p>
 * Todo pasa en UNA sola ventana: al abrir un módulo se cambia el contenido de la ventana
 * por el del módulo, con una barra arriba que tiene el botón "Volver al inicio".
 * <p>
 * Cada módulo se carga una sola vez y se guarda (moduloUno, moduloDos, moduloTres). Así, si sales y
 * vuelves a entrar, los clientes y operaciones que registraste siguen ahí.
 */
public class PrincipalController {

    @FXML
    private BorderPane raizInicio;

    private Parent moduloUno;
    private Parent moduloDos;
    private Parent moduloTres;

    /** La escena de la ventana. Se guarda porque, mientras se ve un módulo, raizInicio no está en ella. */
    private Scene escena;

    @FXML
    private void abrirModuloUno() {
        if (moduloUno == null) {
            moduloUno = cargarModulo("/fxml/ModuloUno.fxml", "Mi monedero");
        }
        mostrar(moduloUno);
    }

    @FXML
    private void abrirModuloDos() {
        if (moduloDos == null) {
            moduloDos = cargarModulo("/fxml/ModuloDos.fxml", "Puntos y rangos");
        }
        mostrar(moduloDos);
    }

    @FXML
    private void abrirModuloTres() {
        if (moduloTres == null) {
            moduloTres = cargarModulo("/fxml/ModuloTres.fxml", "Planeacion y analisis");
        }
        mostrar(moduloTres);
    }

    /** Vuelve a mostrar la pantalla de inicio en la misma ventana. */
    private void volverAlInicio() {
        escena.setRoot(raizInicio);
    }

    private void mostrar(Parent pantalla) {
        if (pantalla == null) {
            return;
        }
        if (escena == null) {
            escena = raizInicio.getScene();
        }
        escena.setRoot(pantalla);
    }

    /**
     * Carga el FXML del módulo y lo pone debajo de una barra con el botón "Volver al inicio".
     * Así ningún módulo tiene que agregar el botón en su propio FXML.
     */
    private Parent cargarModulo(String rutaFxml, String nombreModulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent contenido = loader.load();

            Button volver = new Button("←  Volver al inicio");
            volver.getStyleClass().add("boton-volver");
            volver.setOnAction(evento -> volverAlInicio());

            Region separador = new Region();
            separador.getStyleClass().add("barra-separador");

            Label titulo = new Label(nombreModulo);
            titulo.getStyleClass().add("barra-titulo");

            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            Label marca = new Label("Monedero Virtual");
            marca.getStyleClass().add("barra-marca");

            HBox barra = new HBox(volver, separador, titulo, espacio, marca);
            barra.getStyleClass().add("barra-navegacion");

            BorderPane pantalla = new BorderPane();
            pantalla.setTop(barra);
            pantalla.setCenter(contenido);
            pantalla.getStylesheets().add(
                    getClass().getResource("/css/monedero.css").toExternalForm()
            );
            return pantalla;

        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText("No se pudo abrir el modulo " + nombreModulo);
            alerta.setContentText(String.valueOf(e.getMessage()));
            alerta.showAndWait();
            return null;
        }
    }
}
