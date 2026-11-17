package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.servicios.Sesion;
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
 * <p>
 * Antes de mostrar un módulo se llama a aplicarSesion() de su controlador, para que se ajuste
 * a quien inició sesión: el administrador ve todo; un cliente solo lo suyo.
 */
public class PrincipalController {

    @FXML
    private BorderPane raizInicio;

    @FXML
    private Label lblSesion;

    private Parent moduloUno;
    private Parent moduloDos;
    private Parent moduloTres;

    private PantallaConSesion controladorUno;
    private PantallaConSesion controladorDos;
    private PantallaConSesion controladorTres;

    /** Controlador del último módulo cargado (lo llena cargarModulo). */
    private PantallaConSesion controladorCargado;

    /** La escena de la ventana. Se guarda porque, mientras se ve un módulo, raizInicio no está en ella. */
    private Scene escena;

    /** Lo llama el Navegador cada vez que alguien inicia sesión. */
    public void alIniciarSesion() {
        lblSesion.setText("Sesion: " + Sesion.descripcion());
    }

    @FXML
    private void cerrarSesion() {
        Sesion.cerrar();
        Navegador.mostrarLogin();
    }

    @FXML
    private void abrirModuloUno() {
        if (moduloUno == null) {
            moduloUno = cargarModulo("/fxml/ModuloUno.fxml", "Mi monedero");
            controladorUno = controladorCargado;
        }
        mostrar(moduloUno, controladorUno);
    }

    @FXML
    private void abrirModuloDos() {
        if (moduloDos == null) {
            moduloDos = cargarModulo("/fxml/ModuloDos.fxml", "Puntos y rangos");
            controladorDos = controladorCargado;
        }
        mostrar(moduloDos, controladorDos);
    }

    @FXML
    private void abrirModuloTres() {
        if (moduloTres == null) {
            moduloTres = cargarModulo("/fxml/ModuloTres.fxml", "Planeacion y analisis");
            controladorTres = controladorCargado;
        }
        mostrar(moduloTres, controladorTres);
    }

    /** Vuelve a mostrar la pantalla de inicio en la misma ventana. */
    private void volverAlInicio() {
        escena.setRoot(raizInicio);
    }

    private void mostrar(Parent pantalla, PantallaConSesion controlador) {
        if (pantalla == null) {
            return;
        }
        if (escena == null) {
            escena = raizInicio.getScene();
        }
        try {
            if (controlador != null) {
                controlador.aplicarSesion();
            }
        } catch (RuntimeException e) {
            mostrarError("No se pudo preparar el modulo para esta sesion", e);
        }
        escena.setRoot(pantalla);
    }

    /**
     * Carga el FXML del módulo y lo pone debajo de una barra con el botón "Volver al inicio".
     * Así ningún módulo tiene que agregar el botón en su propio FXML.
     */
    private Parent cargarModulo(String rutaFxml, String nombreModulo) {
        controladorCargado = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent contenido = loader.load();

            Object controlador = loader.getController();
            if (controlador instanceof PantallaConSesion conSesion) {
                controladorCargado = conSesion;
            }

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
            mostrarError("No se pudo abrir el modulo " + nombreModulo, e);
            return null;
        }
    }

    private void mostrarError(String encabezado, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(String.valueOf(e.getMessage()));
        alerta.showAndWait();
    }
}
