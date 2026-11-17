
package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.estructuras.grafo.Arista;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;

import co.edu.uniquindio.poo.servicios.ClienteService;
import co.edu.uniquindio.poo.servicios.ContextoMonedero;
import co.edu.uniquindio.poo.servicios.ContextoSesion;
import co.edu.uniquindio.poo.servicios.MultimonederoService;
import co.edu.uniquindio.poo.servicios.TransaccionService;
import co.edu.uniquindio.poo.servicios.Sesion;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.Node;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ModuloUnoController implements PantallaConSesion {

    // ==================================================
    // NOMBRES DE LAS OPERACIONES
    // ==================================================

    private static final String DEPOSITAR = "Depositar";
    private static final String RETIRAR = "Retirar";
    private static final String PAGAR = "Pagar / gasto";
    private static final String TRANSFERIR =
            "Transferir a otro cliente";
    private static final String ENTRE_MIS_MONEDEROS =
            "Entre mis monederos";

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ==================================================
    // SERVICIOS COMPARTIDOS
    // ==================================================

    // Antes este controlador creaba un ClienteService,
    // un TransaccionService y un reloj independientes.
    //
    // Ahora todos se obtienen del contexto comun,
    // que tambien utilizara el modulo 3.
    private final ContextoMonedero contexto =
            ContextoSesion.obtener();

    private final ClienteService clienteService =
            contexto.getClienteService();

    private final TransaccionService transaccionService =
            contexto.getTransaccionService();

    private final MultimonederoService multimonederoService =
            contexto.getMultimonederoService();

    // ==================================================
    // CAMPOS DE CLIENTES Y MONEDEROS
    // ==================================================

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

    // ==================================================
    // CAMPOS DE OPERACIONES
    // ==================================================

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

    // ==================================================
    // CAMPOS DE HISTORIAL
    // ==================================================

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

    // ==================================================
    // CAMPOS DE MULTIMONEDERO
    // ==================================================

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

    // ==================================================
    // INICIALIZACION
    // ==================================================

    @FXML
    public void initialize() {

        // El listener del grafo ya se registra
        // una sola vez en ContextoMonedero.
        //
        // No debemos volver a agregarlo aqui,
        // porque recibiria cada transferencia
        // dos veces y duplicaria las cantidades.

        cmbTipoMonedero.setItems(
                FXCollections.observableArrayList(
                        TipoMonedero.values()
                )
        );

        cmbCategoria.setItems(
                FXCollections.observableArrayList(
                        CategoriaGasto.values()
                )
        );

        cmbOperacion.setItems(
                FXCollections.observableArrayList(
                        DEPOSITAR,
                        RETIRAR,
                        PAGAR,
                        TRANSFERIR,
                        ENTRE_MIS_MONEDEROS
                )
        );

        configurarTablaMonederos();
        configurarTablaHistorial();
        configurarTablaAristas();
        configurarTablaClientes();
    }

    // ==================================================
    // CONFIGURACION DE TABLAS
    // ==================================================

    private void configurarTablaMonederos() {

        columnaIdMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getId()
                )
        );

        columnaNombreMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getNombre()
                )
        );

        columnaTipoMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getTipo().toString()
                )
        );

        columnaSaldoMonedero.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "$" + datos.getValue().getSaldo()
                )
        );
    }

    private void configurarTablaHistorial() {

        columnaIdTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getId()
                )
        );

        columnaTipoTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getTipo().toString()
                )
        );

        columnaMontoTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "$" + datos.getValue().getMonto()
                )
        );

        columnaComisionTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "$" + datos.getValue().getComision()
                )
        );

        columnaFechaTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .getFecha()
                                .format(FORMATO_FECHA)
                )
        );

        columnaEstadoTransaccion.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado().toString()
                )
        );

        columnaCategoriaTransaccion.setCellValueFactory(datos -> {

            CategoriaGasto categoria =
                    datos.getValue().getCategoria();

            return new ReadOnlyStringWrapper(
                    categoria == null
                            ? "-"
                            : categoria.toString()
            );
        });
    }

    private void configurarTablaAristas() {

        columnaDesde.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        nombreMonedero(
                                datos.getValue().getOrigen()
                        )
                )
        );

        columnaHacia.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        nombreMonedero(
                                datos.getValue().getDestino()
                        )
                )
        );

        columnaTotalMovido.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "$" + datos.getValue().getPeso()
                )
        );

        columnaVeces.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        String.valueOf(
                                datos.getValue().getCantidad()
                        )
                )
        );
    }

    // ==================================================
    // CLIENTES Y MONEDEROS
    // ==================================================

    @FXML
    private void registrarCliente() {

        try {

            Cliente cliente =
                    clienteService.registrarCliente(
                            txtNombre.getText(),
                            txtCorreo.getText(),
                            txtPin.getText()
                    );

            txtNombre.clear();
            txtCorreo.clear();
            txtPin.clear();

            txtClienteMonederos.setText(
                    cliente.getId()
            );

            consultarMonederos();

            mostrarInformacion(
                    "Cliente registrado",
                    "Se registro a "
                            + cliente.getNombre()
                            + " con el ID "
                            + cliente.getId()
            );

        } catch (RuntimeException error) {

            mostrarError(error.getMessage());
        }
    }

    @FXML
    private void crearMonedero() {

        try {

            if (cmbTipoMonedero.getValue() == null) {

                mostrarError(
                        "Selecciona el tipo de monedero"
                );

                return;
            }

            Monedero monedero =
                    clienteService.crearMonedero(
                            textoDe(txtClienteMonederos),
                            txtNombreMonedero.getText(),
                            cmbTipoMonedero.getValue()
                    );

            txtNombreMonedero.clear();

            consultarMonederos();

            mostrarInformacion(
                    "Monedero creado",
                    "Se creo el monedero "
                            + monedero
                            + " con el ID "
                            + monedero.getId()
            );

        } catch (RuntimeException error) {

            mostrarError(error.getMessage());
        }
    }

    @FXML
    private void consultarMonederos() {

        try {

            String clienteId =
                    textoDe(txtClienteMonederos);

            ObservableList<Monedero> filas =
                    FXCollections.observableArrayList();

            for (Monedero monedero :
                    clienteService.monederosDe(clienteId)) {

                filas.add(monedero);
            }

            tablaMonederos.setItems(filas);
            tablaMonederos.refresh();

            lblSaldoTotal.setText(
                    "Saldo total: $"
                            + clienteService.saldoTotal(
                                    clienteId
                            )
            );

        } catch (RuntimeException error) {

            mostrarError(error.getMessage());
        }
    }

    // ==================================================
    // OPERACIONES FINANCIERAS
    // ==================================================

    @FXML
    private void realizarOperacion() {

        String operacion =
                cmbOperacion.getValue();

        if (operacion == null) {

            mostrarError("Selecciona una operacion");

            return;
        }

        try {

            String origen =
                    textoDe(txtMonederoOrigen);

            BigDecimal monto =
                    leerMonto();

            String descripcion =
                    txtDescripcion.getText();

            Transaccion transaccion;

            if (operacion.equals(DEPOSITAR)) {

                transaccion = transaccionService.depositar(
                        origen,
                        monto,
                        descripcion
                );

            } else if (operacion.equals(RETIRAR)) {

                transaccion = transaccionService.retirar(
                        origen,
                        monto,
                        descripcion
                );

            } else if (operacion.equals(PAGAR)) {

                transaccion = transaccionService.pagar(
                        origen,
                        monto,
                        cmbCategoria.getValue(),
                        descripcion
                );

            } else if (operacion.equals(TRANSFERIR)) {

                transaccion = transaccionService.transferir(
                        origen,
                        textoDe(txtMonederoDestino),
                        monto,
                        descripcion
                );

            } else {

                transaccion =
                        transaccionService.transferirEntreMisMonederos(
                                origen,
                                textoDe(txtMonederoDestino),
                                monto,
                                descripcion
                        );
            }

            lblResultadoOperacion.setText(
                    "Operacion "
                            + transaccion.getId()
                            + " realizada: "
                            + transaccion.getTipo()
                            + " por $"
                            + transaccion.getMonto()
                            + " (comision $"
                            + transaccion.getComision()
                            + "). Saldo de "
                            + origen
                            + ": $"
                            + transaccionService.consultarSaldo(
                                    origen
                            )
            );

            txtMonto.clear();
            txtDescripcion.clear();

        } catch (RuntimeException error) {

            lblResultadoOperacion.setText(
                    "No se pudo realizar: "
                            + error.getMessage()
            );

            mostrarError(error.getMessage());
        }
    }

    // ==================================================
    // HISTORIAL
    // ==================================================

    @FXML
    private void consultarHistorial() {

        try {

            String monederoId =
                    textoDe(txtMonederoHistorial);

            ObservableList<Transaccion> filas =
                    FXCollections.observableArrayList();

            for (Transaccion transaccion :
                    transaccionService.consultarHistorial(
                            monederoId
                    )) {

                filas.add(transaccion);
            }

            tablaHistorial.setItems(filas);
            tablaHistorial.refresh();

            lblSaldoHistorial.setText(
                    "Saldo: $"
                            + transaccionService.consultarSaldo(
                                    monederoId
                            )
            );

            lblEstadoHistorial.setText(
                    "Movimientos encontrados: "
                            + filas.size()
            );

        } catch (RuntimeException error) {

            mostrarError(error.getMessage());
        }
    }

    @FXML
    private void revertirUltima() {

        try {

            Transaccion transaccion =
                    transaccionService.revertirUltima(
                            textoDe(txtMonederoHistorial)
                    );

            consultarHistorial();

            lblEstadoHistorial.setText(
                    "Se revirtio la operacion "
                            + transaccion.getId()
                            + " ("
                            + transaccion.getTipo()
                            + ")"
            );

        } catch (RuntimeException error) {

            mostrarError(error.getMessage());
        }
    }

    @FXML
    private void verificarIntegridad() {

        try {

            boolean correcta =
                    transaccionService.verificarIntegridad(
                            textoDe(txtMonederoHistorial)
                    );

            if (correcta) {

                lblEstadoHistorial.setText(
                        "Integridad correcta: "
                                + "el saldo coincide con el historial"
                );

            } else {

                lblEstadoHistorial.setText(
                        "ALERTA: el saldo NO coincide "
                                + "con el historial"
                );
            }

        } catch (RuntimeException error) {

            mostrarError(error.getMessage());
        }
    }

    // ==================================================
    // MULTIMONEDERO
    // ==================================================

    @FXML
    private void consultarMultimonedero() {

        try {

            ObservableList<Arista> filas =
                    FXCollections.observableArrayList();

            for (Arista arista :
                    multimonederoService.movimientosDelCliente(
                            textoDe(txtClienteMultimonedero)
                    )) {

                filas.add(arista);
            }

            tablaAristas.setItems(filas);
            tablaAristas.refresh();

        } catch (RuntimeException error) {

            mostrarError(error.getMessage());
        }
    }

    // ==================================================
    // ADMINISTRAR CLIENTES (solo administrador)
    // ==================================================

    @FXML
    private TabPane tabsModulo;

    @FXML
    private Tab tabAdministrarClientes;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> colClienteId;

    @FXML
    private TableColumn<Cliente, String> colClienteNombre;

    @FXML
    private TableColumn<Cliente, String> colClienteCorreo;

    @FXML
    private TableColumn<Cliente, String> colClienteRango;

    @FXML
    private TableColumn<Cliente, String> colClienteSaldo;

    @FXML
    private TableColumn<Cliente, String> colClienteEstado;

    @FXML
    private TextField txtEditarNombre;

    @FXML
    private TextField txtEditarCorreo;

    @FXML
    private PasswordField txtEditarPin;

    @FXML
    private Button btnCambiarEstado;

    @FXML
    private Label lblMensajeAdmin;

    private void configurarTablaClientes() {

        colClienteId.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getId()));

        colClienteNombre.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getNombre()));

        colClienteCorreo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getCorreo()));

        colClienteRango.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getRango().getNombre()));

        colClienteSaldo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper("$" + clienteService.saldoTotal(
                        datos.getValue().getId())));

        colClienteEstado.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().isActivo()
                        ? "Activa" : "Desactivada"));

        tablaClientes.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) ->
                        mostrarClienteSeleccionado(seleccionado));

        // Al entrar a la pestana se recargan los clientes (pueden haberse
        // registrado nuevos desde el login o desde la pestana Clientes).
        tabAdministrarClientes.setOnSelectionChanged(evento -> {
            if (tabAdministrarClientes.isSelected()) {
                cargarTablaClientes();
            }
        });
    }

    private void cargarTablaClientes() {

        Cliente seleccionado =
                tablaClientes.getSelectionModel().getSelectedItem();

        ObservableList<Cliente> filas =
                FXCollections.observableArrayList();

        for (Cliente cliente : clienteService.listarClientes()) {
            filas.add(cliente);
        }

        tablaClientes.setItems(filas);
        tablaClientes.refresh();

        if (seleccionado != null) {
            tablaClientes.getSelectionModel().select(seleccionado);
        }
    }

    private void mostrarClienteSeleccionado(Cliente cliente) {

        txtEditarPin.clear();

        if (cliente == null) {
            txtEditarNombre.clear();
            txtEditarCorreo.clear();
            btnCambiarEstado.setText("Desactivar cuenta");
            return;
        }

        txtEditarNombre.setText(cliente.getNombre());
        txtEditarCorreo.setText(cliente.getCorreo());
        btnCambiarEstado.setText(cliente.isActivo()
                ? "Desactivar cuenta" : "Reactivar cuenta");
    }

    @FXML
    private void actualizarCliente() {

        Cliente cliente =
                tablaClientes.getSelectionModel().getSelectedItem();

        if (cliente == null) {
            mostrarMensajeAdmin("Selecciona un cliente de la tabla", false);
            return;
        }

        try {
            clienteService.actualizarCliente(
                    cliente.getId(),
                    txtEditarNombre.getText(),
                    txtEditarCorreo.getText(),
                    txtEditarPin.getText()
            );

            cargarTablaClientes();
            mostrarClienteSeleccionado(cliente);
            mostrarMensajeAdmin("Se actualizaron los datos de "
                    + cliente.getNombre(), true);

        } catch (RuntimeException error) {
            mostrarMensajeAdmin(error.getMessage(), false);
        }
    }

    @FXML
    private void cambiarEstadoCliente() {

        Cliente cliente =
                tablaClientes.getSelectionModel().getSelectedItem();

        if (cliente == null) {
            mostrarMensajeAdmin("Selecciona un cliente de la tabla", false);
            return;
        }

        try {
            if (cliente.isActivo()) {

                Alert confirmacion = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "La cuenta de " + cliente.getNombre()
                                + " quedara desactivada: no podra iniciar sesion"
                                + " ni hacer operaciones. Su historial se conserva"
                                + " y la puedes reactivar despues.",
                        ButtonType.OK, ButtonType.CANCEL);
                confirmacion.setTitle("Desactivar cuenta");
                confirmacion.setHeaderText("Desactivar a " + cliente.getNombre() + "?");

                if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                        != ButtonType.OK) {
                    return;
                }

                clienteService.desactivarCliente(cliente.getId());
                mostrarMensajeAdmin("Se desactivo la cuenta de "
                        + cliente.getNombre(), true);

            } else {

                clienteService.reactivarCliente(cliente.getId());
                mostrarMensajeAdmin("Se reactivo la cuenta de "
                        + cliente.getNombre(), true);
            }

            cargarTablaClientes();
            mostrarClienteSeleccionado(cliente);

        } catch (RuntimeException error) {
            mostrarMensajeAdmin(error.getMessage(), false);
        }
    }

    /** Lleva a la pestana Clientes con los monederos del cliente seleccionado. */
    @FXML
    private void verMonederosDelSeleccionado() {

        Cliente cliente =
                tablaClientes.getSelectionModel().getSelectedItem();

        if (cliente == null) {
            mostrarMensajeAdmin("Selecciona un cliente de la tabla", false);
            return;
        }

        txtClienteMonederos.setText(cliente.getId());
        consultarMonederos();
        tabsModulo.getSelectionModel().selectFirst();
    }

    private void mostrarMensajeAdmin(String texto, boolean exito) {

        lblMensajeAdmin.getStyleClass().removeAll("mensaje-exito", "mensaje-error");
        lblMensajeAdmin.getStyleClass().add(exito ? "mensaje-exito" : "mensaje-error");
        lblMensajeAdmin.setText(texto == null || texto.isBlank()
                ? "No fue posible completar la operacion" : texto);
    }

    // ==================================================
    // SESION (administrador o cliente)
    // ==================================================

    /** Quien estaba conectado la ultima vez que se preparo la pantalla. */
    private String sesionAplicada = "";

    /**
     * Ajusta la pantalla a quien inicio sesion. La llama PrincipalController cada vez
     * que se abre el modulo.
     * - Administrador: la pantalla funciona como siempre.
     * - Cliente: se oculta "Registrar cliente", su ID queda fijo y solo puede operar
     *   con sus propios monederos.
     */
    @Override
    public void aplicarSesion() {

        boolean esCliente = Sesion.esCliente();

        // Si sigue la misma persona, solo se actualizan sus datos.
        if (Sesion.identificador().equals(sesionAplicada)) {
            if (esCliente) {
                consultarMonederos();
            } else {
                cargarTablaClientes();
            }
            return;
        }
        sesionAplicada = Sesion.identificador();

        // Cambio de persona: no debe quedar nada de la anterior en pantalla.
        limpiarPantalla();

        Node tarjetaRegistro = txtNombre.getParent();
        tarjetaRegistro.setVisible(!esCliente);
        tarjetaRegistro.setManaged(!esCliente);

        txtClienteMonederos.setDisable(esCliente);
        txtClienteMultimonedero.setDisable(esCliente);

        // La pestana "Administrar clientes" solo existe para el administrador.
        if (esCliente) {
            tabsModulo.getTabs().remove(tabAdministrarClientes);
        } else {
            if (!tabsModulo.getTabs().contains(tabAdministrarClientes)) {
                tabsModulo.getTabs().add(tabAdministrarClientes);
            }
            cargarTablaClientes();
        }
        tabsModulo.getSelectionModel().selectFirst();

        if (esCliente) {
            String clienteId = Sesion.getCliente().getId();
            String principal = clienteService.monederosDe(clienteId)
                    .obtenerValorNodo(0).getId();

            txtClienteMonederos.setText(clienteId);
            txtClienteMultimonedero.setText(clienteId);
            txtMonederoOrigen.setText(principal);
            txtMonederoHistorial.setText(principal);

            consultarMonederos();
            consultarHistorial();
            consultarMultimonedero();
        }
    }

    private void limpiarPantalla() {

        txtNombre.clear();
        txtCorreo.clear();
        txtPin.clear();
        txtClienteMonederos.clear();
        txtNombreMonedero.clear();
        txtMonederoOrigen.clear();
        txtMonederoDestino.clear();
        txtMonto.clear();
        txtDescripcion.clear();
        txtMonederoHistorial.clear();
        txtClienteMultimonedero.clear();

        tablaMonederos.setItems(FXCollections.observableArrayList());
        tablaHistorial.setItems(FXCollections.observableArrayList());
        tablaAristas.setItems(FXCollections.observableArrayList());

        lblSaldoTotal.setText("$0.00");
        lblSaldoHistorial.setText("$0.00");
        lblEstadoHistorial.setText("Sin consulta");
        lblResultadoOperacion.setText("Aun no se ha realizado ninguna operacion.");

        tablaClientes.getSelectionModel().clearSelection();
        txtEditarNombre.clear();
        txtEditarCorreo.clear();
        txtEditarPin.clear();
        lblMensajeAdmin.setText("");
    }

    /** Un cliente solo puede operar y consultar sus propios monederos. */
    private void validarMonederoPropio(String monederoId) {

        Monedero monedero = clienteService.obtenerMonedero(monederoId);

        if (!monedero.getClienteId().equals(Sesion.getCliente().getId())) {
            throw new IllegalArgumentException(
                    "El monedero " + monederoId + " no es tuyo. "
                            + "Usa uno de tus monederos (los ves en la pestana Clientes)."
            );
        }
    }

    // ==================================================
    // METODOS AUXILIARES
    // ==================================================

    private String textoDe(TextField campo) {

        String texto = campo.getText();

        if (texto == null || texto.isBlank()) {

            throw new IllegalArgumentException(
                    "Completa el campo: "
                            + campo.getPromptText()
            );
        }

        String valor = texto.trim().toUpperCase();

        // Monedero de origen (operaciones) y monedero del historial:
        // un cliente solo puede usar los suyos.
        if (Sesion.esCliente()
                && (campo == txtMonederoOrigen || campo == txtMonederoHistorial)) {
            validarMonederoPropio(valor);
        }

        return valor;
    }

    private BigDecimal leerMonto() {

        String texto = txtMonto.getText();

        if (texto == null || texto.isBlank()) {

            throw new IllegalArgumentException(
                    "Escribe el monto"
            );
        }

        try {

            return new BigDecimal(
                    texto.trim().replace(",", ".")
            );

        } catch (NumberFormatException error) {

            throw new IllegalArgumentException(
                    "El monto debe ser un numero, "
                            + "por ejemplo 1500.50"
            );
        }
    }

    private String nombreMonedero(
            String monederoId) {

        return clienteService
                .obtenerMonedero(monederoId)
                .getNombre()
                + " ("
                + monederoId
                + ")";
    }

    private void mostrarError(String mensaje) {

        Alert alerta =
                new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInformacion(
            String titulo,
            String mensaje) {

        Alert alerta =
                new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
