
package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.modelo.Notificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.servicios.NotificacionService;
import co.edu.uniquindio.poo.util.RelojSimulado;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ModuloTresController {

    private final NotificacionService notificacionService =
            new NotificacionService(new RelojSimulado());

    @FXML
    private TextField txtClienteNotificacion;

    @FXML
    private TableView<Notificacion> tablaNotificaciones;

    @FXML
    private TableColumn<Notificacion, String> columnaId;

    @FXML
    private TableColumn<Notificacion, String> columnaTipo;

    @FXML
    private TableColumn<Notificacion, String> columnaMensaje;

    @FXML
    private TableColumn<Notificacion, String> columnaLeida;

    @FXML
    private Label lblCantidadNoLeidas;

    @FXML
    public void initialize() {

        columnaId.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getId()
                )
        );

        columnaTipo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getTipo().toString()
                )
        );

        columnaMensaje.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getMensaje()
                )
        );

        columnaLeida.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().isLeida() ? "Si" : "No"
                )
        );

        tablaNotificaciones.setPlaceholder(
                new Label("No hay notificaciones para mostrar")
        );
    }

    // Consulta las notificaciones de un cliente.
    @FXML
    private void consultarNotificaciones() {

        String clienteId =
                txtClienteNotificacion.getText().trim();

        if (clienteId.isEmpty()) {
            mostrarMensaje(
                    "Validacion",
                    "Debes escribir el ID del cliente"
            );
            return;
        }

        notificacionService.entregarNotificaciones();

        actualizarTabla(clienteId);
    }

    // Crea una notificacion de prueba.
    @FXML
    private void crearNotificacionPrueba() {

        String clienteId =
                txtClienteNotificacion.getText().trim();

        if (clienteId.isEmpty()) {
            mostrarMensaje(
                    "Validacion",
                    "Debes escribir el ID del cliente"
            );
            return;
        }

        notificacionService.crearNotificacion(
                clienteId,
                TipoNotificacion.TRANSACCION,
                "Esta es una notificacion de prueba"
        );

        notificacionService.entregarNotificaciones();

        actualizarTabla(clienteId);

        mostrarMensaje(
                "Notificacion creada",
                "Se creo una notificacion para " + clienteId
        );
    }

    // Marca como leida la notificacion seleccionada.
    @FXML
    private void marcarNotificacionLeida() {

        String clienteId =
                txtClienteNotificacion.getText().trim();

        Notificacion seleccionada =
                tablaNotificaciones.getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {
            mostrarMensaje(
                    "Seleccion",
                    "Primero selecciona una notificacion"
            );
            return;
        }

        if (!seleccionada.getClienteId().equals(clienteId)) {
            mostrarMensaje(
                    "Validacion",
                    "Consulta primero las notificaciones del cliente"
            );
            return;
        }

        boolean marcada =
                notificacionService.marcarComoLeida(
                        seleccionada.getId()
                );

        if (marcada) {

            // Actualizamos los datos del cliente.
            actualizarTabla(clienteId);

            // Forzamos a JavaFX a volver a dibujar las celdas.
            tablaNotificaciones.refresh();

            mostrarMensaje(
                    "Correcto",
                    "Notificacion marcada como leida"
            );

        } else {

            mostrarMensaje(
                    "Error",
                    "No se encontro la notificacion"
            );
        }
    }

    // Actualiza la tabla y el contador de no leidas.
    private void actualizarTabla(String clienteId) {

        var lista = FXCollections
                .<Notificacion>observableArrayList();

        for (Notificacion notificacion :
                notificacionService.consultarNotificaciones(
                        clienteId
                )) {

            lista.add(notificacion);
        }

        tablaNotificaciones.setItems(lista);

        // Evita mostrar datos visuales desactualizados.
        tablaNotificaciones.refresh();

        int noLeidas =
                notificacionService.contarNoLeidas(clienteId);

        lblCantidadNoLeidas.setText(
                "Notificaciones sin leer: " + noLeidas
        );
    }

    private void mostrarMensaje(
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION
        );

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
