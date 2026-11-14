package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.estructuras.grafo.Arista;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.servicios.ClienteService;
import co.edu.uniquindio.poo.servicios.MultimonederoService;
import co.edu.uniquindio.poo.servicios.TransaccionService;
import co.edu.uniquindio.poo.util.RelojSimulado;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ModuloUnoController {

    // Nombres de las operaciones que aparecen en el ComboBox.
    private static final String DEPOSITAR = "Depositar";
    private static final String RETIRAR = "Retirar";
    private static final String PAGAR = "Pagar / gasto";
    private static final String TRANSFERIR = "Transferir a otro cliente";
    private static final String ENTRE_MIS_MONEDEROS = "Entre mis monederos";

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Servicios que utilizara nuestra interfaz.
    private final RelojSimulado reloj = new RelojSimulado();

    private final ClienteService clienteService =
            new ClienteService(reloj);

    private final TransaccionService transaccionService =
            new TransaccionService(clienteService, reloj);

    private final MultimonederoService multimonederoService =
            new MultimonederoService(clienteService);


    // CAMPOS DE CLIENTES Y MONEDEROS

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtCorreo;

    @FXML
    private PasswordField txtPin;

    @FXML
    private TextField txtClienteMonederos;

    @FXML
    private TextField txtNombreMonedero;

    @FXML
    private ComboBox<TipoMonedero> cmbTipoMonedero;

    @FXML
    private Label lblSaldoTotal;

    @FXML
    private TableView<Monedero> tablaMonederos;

    @FXML
    private TableColumn<Monedero, String> columnaIdMonedero;

    @FXML
    private TableColumn<Monedero, String> columnaNombreMonedero;

    @FXML
    private TableColumn<Monedero, String> columnaTipoMonedero;

    @FXML
    private TableColumn<Monedero, String> columnaSaldoMonedero;


    // CAMPOS DE OPERACIONES


    @FXML
    private ComboBox<String> cmbOperacion;

    @FXML
    private TextField txtMonederoOrigen;

    @FXML
    private TextField txtMonederoDestino;

    @FXML
    private TextField txtMonto;

    @FXML
    private ComboBox<CategoriaGasto> cmbCategoria;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private Label lblResultadoOperacion;


    // CAMPOS DE HISTORIAL


    @FXML
    private TextField txtMonederoHistorial;

    @FXML
    private Label lblSaldoHistorial;

    @FXML
    private Label lblEstadoHistorial;

    @FXML
    private TableView<Transaccion> tablaHistorial;

    @FXML
    private TableColumn<Transaccion, String> columnaIdTransaccion;

    @FXML
    private TableColumn<Transaccion, String> columnaTipoTransaccion;

    @FXML
    private TableColumn<Transaccion, String> columnaMontoTransaccion;

    @FXML
    private TableColumn<Transaccion, String> columnaComisionTransaccion;

    @FXML
    private TableColumn<Transaccion, String> columnaFechaTransaccion;

    @FXML
    private TableColumn<Transaccion, String> columnaEstadoTransaccion;

    @FXML
    private TableColumn<Transaccion, String> columnaCategoriaTransaccion;

    // -------------------------------------------------
    // CAMPOS DE MULTIMONEDERO
    // -------------------------------------------------

    @FXML
    private TextField txtClienteMultimonedero;

    @FXML
    private TableView<Arista> tablaAristas;

    @FXML
    private TableColumn<Arista, String> columnaDesde;

    @FXML
    private TableColumn<Arista, String> columnaHacia;

    @FXML
    private TableColumn<Arista, String> columnaTotalMovido;

    @FXML
    private TableColumn<Arista, String> columnaVeces;

    // INICIALIZACION


    @FXML
    public void initialize() {

        // El multimonedero escucha a TransaccionService para llenar el grafo.
        transaccionService.agregarListener(multimonederoService);

        cmbTipoMonedero.setItems(
                FXCollections.observableArrayList(TipoMonedero.values())
        );

        cmbCategoria.setItems(
                FXCollections.observableArrayList(CategoriaGasto.values())
        );

        cmbOperacion.setItems(
                FXCollections.observableArrayList(
                        DEPOSITAR, RETIRAR, PAGAR, TRANSFERIR, ENTRE_MIS_MONEDEROS
                )
        );

        configurarTablaMonederos();
        configurarTablaHistorial();
        configurarTablaAristas();
    }

    private void configurarTablaMonederos() {

        columnaIdMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getId())
        );

        columnaNombreMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getNombre())
        );

        columnaTipoMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getTipo().toString())
        );

        columnaSaldoMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper("$" + datos.getValue().getSaldo())
        );
    }

    private void configurarTablaHistorial() {

        columnaIdTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getId())
        );

        columnaTipoTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getTipo().toString())
        );

        columnaMontoTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper("$" + datos.getValue().getMonto())
        );

        columnaComisionTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper("$" + datos.getValue().getComision())
        );

        columnaFechaTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getFecha().format(FORMATO_FECHA)
                )
        );

        columnaEstadoTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getEstado().toString())
        );

        columnaCategoriaTransaccion.setCellValueFactory(datos -> {
            CategoriaGasto categoria = datos.getValue().getCategoria();
            return new ReadOnlyStringWrapper(
                    categoria == null ? "-" : categoria.toString()
            );
        });
    }

    private void configurarTablaAristas() {

        columnaDesde.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        nombreMonedero(datos.getValue().getOrigen())
                )
        );

        columnaHacia.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        nombreMonedero(datos.getValue().getDestino())
                )
        );

        columnaTotalMovido.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper("$" + datos.getValue().getPeso())
        );

        columnaVeces.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        String.valueOf(datos.getValue().getCantidad())
                )
        );
    }


    // ACCIONES DE CLIENTES Y MONEDEROS


    @FXML
    private void registrarCliente() {

        try {
            Cliente cliente = clienteService.registrarCliente(
                    txtNombre.getText(),
                    txtCorreo.getText(),
                    txtPin.getText()
            );

            txtNombre.clear();
            txtCorreo.clear();
            txtPin.clear();

            // Mostramos de una vez sus monederos (ya tiene el Principal).
            txtClienteMonederos.setText(cliente.getId());
            consultarMonederos();

            mostrarInformacion(
                    "Cliente registrado",
                    "Se registro a " + cliente.getNombre()
                            + " con el ID " + cliente.getId()
            );

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void crearMonedero() {

        try {
            if (cmbTipoMonedero.getValue() == null) {
                mostrarError("Selecciona el tipo de monedero");
                return;
            }

            Monedero monedero = clienteService.crearMonedero(
                    textoDe(txtClienteMonederos),
                    txtNombreMonedero.getText(),
                    cmbTipoMonedero.getValue()
            );

            txtNombreMonedero.clear();
            consultarMonederos();

            mostrarInformacion(
                    "Monedero creado",
                    "Se creo el monedero " + monedero
                            + " con el ID " + monedero.getId()
            );

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void consultarMonederos() {

        try {
            String clienteId = textoDe(txtClienteMonederos);

            ObservableList<Monedero> filas = FXCollections.observableArrayList();

            for (Monedero monedero : clienteService.monederosDe(clienteId)) {
                filas.add(monedero);
            }

            tablaMonederos.setItems(filas);
            tablaMonederos.refresh();

            lblSaldoTotal.setText(
                    "Saldo total: $" + clienteService.saldoTotal(clienteId)
            );

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }


    // ACCIONES DE OPERACIONES


    @FXML
    private void realizarOperacion() {

        String operacion = cmbOperacion.getValue();

        if (operacion == null) {
            mostrarError("Selecciona una operacion");
            return;
        }

        try {
            String origen = textoDe(txtMonederoOrigen);
            BigDecimal monto = leerMonto();
            String descripcion = txtDescripcion.getText();

            Transaccion t;

            if (operacion.equals(DEPOSITAR)) {
                t = transaccionService.depositar(origen, monto, descripcion);

            } else if (operacion.equals(RETIRAR)) {
                t = transaccionService.retirar(origen, monto, descripcion);

            } else if (operacion.equals(PAGAR)) {
                t = transaccionService.pagar(
                        origen, monto, cmbCategoria.getValue(), descripcion
                );

            } else if (operacion.equals(TRANSFERIR)) {
                t = transaccionService.transferir(
                        origen, textoDe(txtMonederoDestino), monto, descripcion
                );

            } else {
                t = transaccionService.transferirEntreMisMonederos(
                        origen, textoDe(txtMonederoDestino), monto, descripcion
                );
            }

            lblResultadoOperacion.setText(
                    "Operacion " + t.getId() + " realizada: " + t.getTipo()
                            + " por $" + t.getMonto()
                            + " (comision $" + t.getComision() + "). "
                            + "Saldo de " + origen + ": $"
                            + transaccionService.consultarSaldo(origen)
            );

            txtMonto.clear();
            txtDescripcion.clear();

        } catch (RuntimeException e) {
            // Incluye saldo insuficiente: la operacion queda como FALLIDA en el historial.
            lblResultadoOperacion.setText("No se pudo realizar: " + e.getMessage());
            mostrarError(e.getMessage());
        }
    }

    // -------------------------------------------------
    // ACCIONES DE HISTORIAL
    // -------------------------------------------------

    @FXML
    private void consultarHistorial() {

        try {
            String monederoId = textoDe(txtMonederoHistorial);

            ObservableList<Transaccion> filas = FXCollections.observableArrayList();

            for (Transaccion t : transaccionService.consultarHistorial(monederoId)) {
                filas.add(t);
            }

            tablaHistorial.setItems(filas);
            tablaHistorial.refresh();

            lblSaldoHistorial.setText(
                    "Saldo: $" + transaccionService.consultarSaldo(monederoId)
            );

            lblEstadoHistorial.setText(
                    "Movimientos encontrados: " + filas.size()
            );

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void revertirUltima() {

        try {
            Transaccion t = transaccionService.revertirUltima(
                    textoDe(txtMonederoHistorial)
            );

            consultarHistorial();

            lblEstadoHistorial.setText(
                    "Se revirtio la operacion " + t.getId() + " (" + t.getTipo() + ")"
            );

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void verificarIntegridad() {

        try {
            boolean correcta = transaccionService.verificarIntegridad(
                    textoDe(txtMonederoHistorial)
            );

            if (correcta) {
                lblEstadoHistorial.setText(
                        "Integridad correcta: el saldo coincide con el historial"
                );
            } else {
                lblEstadoHistorial.setText(
                        "ALERTA: el saldo NO coincide con el historial"
                );
            }

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    // -------------------------------------------------
    // ACCIONES DE MULTIMONEDERO
    // -------------------------------------------------

    @FXML
    private void consultarMultimonedero() {

        try {
            ObservableList<Arista> filas = FXCollections.observableArrayList();

            for (Arista arista : multimonederoService.movimientosDelCliente(
                    textoDe(txtClienteMultimonedero))) {
                filas.add(arista);
            }

            tablaAristas.setItems(filas);
            tablaAristas.refresh();

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    // -------------------------------------------------
    // AYUDAS
    // -------------------------------------------------

    /** Texto del campo sin espacios; los IDs se pasan a mayuscula (m1 -> M1). */
    private String textoDe(TextField campo) {
        String texto = campo.getText();
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Completa el campo: " + campo.getPromptText());
        }
        return texto.trim().toUpperCase();
    }

    /** Convierte el texto del monto a BigDecimal (acepta coma o punto decimal). */
    private BigDecimal leerMonto() {
        String texto = txtMonto.getText();
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Escribe el monto");
        }
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El monto debe ser un numero, por ejemplo 1500.50");
        }
    }

    /** "Ahorros (M2)" en lugar de solo "M2". */
    private String nombreMonedero(String monederoId) {
        return clienteService.obtenerMonedero(monederoId).getNombre()
                + " (" + monederoId + ")";
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
