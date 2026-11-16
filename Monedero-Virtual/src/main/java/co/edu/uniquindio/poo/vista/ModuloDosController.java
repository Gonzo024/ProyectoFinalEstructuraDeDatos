package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.BeneficioActivo;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.RegistroPuntos;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoBeneficio;
import co.edu.uniquindio.poo.servicios.BeneficioService;
import co.edu.uniquindio.poo.servicios.ClienteService;
import co.edu.uniquindio.poo.servicios.ContextoMonedero;
import co.edu.uniquindio.poo.servicios.ContextoSesion;
import co.edu.uniquindio.poo.servicios.PuntosService;
import co.edu.uniquindio.poo.servicios.RangoService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Modulo 2: puntos, rangos y canjes. Usa el mismo contexto que los modulos 1 y 3,
 * asi que ve los mismos clientes, monederos y transacciones.
 * <p>
 * El controlador no calcula nada: solo pregunta a PuntosService, RangoService y
 * BeneficioService y muestra la respuesta. Las estructuras propias (ListaEnlazada)
 * se pasan a ObservableList solo aqui, en el borde de la interfaz.
 */
public class ModuloDosController {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ContextoMonedero contexto = ContextoSesion.obtener();
    private final ClienteService clienteService = contexto.getClienteService();
    private final PuntosService puntosService = contexto.getPuntosService();
    private final RangoService rangoService = contexto.getRangoService();
    private final BeneficioService beneficioService = contexto.getBeneficioService();

    @FXML private VBox raiz;
    @FXML private ComboBox<Cliente> comboCliente;

    // Pestana "Mis puntos"
    @FXML private Label lblDisponibles;
    @FXML private Label lblAcumulados;
    @FXML private Label lblRango;
    @FXML private Label lblSiguienteRango;
    @FXML private TableView<MovimientoPuntos> tablaHistorial;
    @FXML private TableColumn<MovimientoPuntos, String> colFecha;
    @FXML private TableColumn<MovimientoPuntos, String> colTipo;
    @FXML private TableColumn<MovimientoPuntos, String> colPuntos;
    @FXML private TableColumn<MovimientoPuntos, String> colDescripcion;

    // Pestana "Rangos y clasificacion"
    @FXML private TableView<Rango> tablaRangos;
    @FXML private TableColumn<Rango, String> colRangoNombre;
    @FXML private TableColumn<Rango, String> colRangoPuntos;
    @FXML private TableColumn<Rango, String> colRangoBeneficios;
    @FXML private TableColumn<Rango, String> colRangoClientes;
    @FXML private Label lblArboles;
    @FXML private TableView<RegistroPuntos> tablaClasificacion;
    @FXML private TableColumn<RegistroPuntos, String> colPuesto;
    @FXML private TableColumn<RegistroPuntos, String> colClasCliente;
    @FXML private TableColumn<RegistroPuntos, String> colClasAcumulados;
    @FXML private TableColumn<RegistroPuntos, String> colClasDisponibles;
    @FXML private TableColumn<RegistroPuntos, String> colClasRango;

    // Pestana "Canjes"
    @FXML private Label lblPuntosCanje;
    @FXML private TableView<TipoBeneficio> tablaCatalogo;
    @FXML private TableColumn<TipoBeneficio, String> colBeneficio;
    @FXML private TableColumn<TipoBeneficio, String> colCosto;
    @FXML private TableColumn<TipoBeneficio, String> colVigencia;
    @FXML private TableColumn<TipoBeneficio, String> colDetalle;
    @FXML private TableColumn<TipoBeneficio, String> colEstado;
    @FXML private ComboBox<Monedero> comboMonederoBono;
    @FXML private Label lblMensajeCanje;
    @FXML private TableView<BeneficioActivo> tablaVigentes;
    @FXML private TableColumn<BeneficioActivo, String> colVigenteBeneficio;
    @FXML private TableColumn<BeneficioActivo, String> colVigenteDesde;
    @FXML private TableColumn<BeneficioActivo, String> colVigenteHasta;

    // =========================================================== inicio

    @FXML
    public void initialize() {
        configurarMisPuntos();
        configurarRangos();
        configurarCanjes();

        // Cada vez que se despliega la lista se revisa si hay clientes nuevos.
        comboCliente.setOnShowing(evento -> cargarClientes());

        // El modulo se carga una sola vez y se guarda. Este aviso llega cada vez que la
        // pantalla vuelve a mostrarse, y sirve para traer lo que cambio en los modulos 1 y 3.
        raiz.sceneProperty().addListener((observable, anterior, nueva) -> {
            if (nueva != null) {
                refrescarTodo();
            }
        });

        refrescarTodo();
    }

    private void configurarMisPuntos() {
        colFecha.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getFecha().format(FORMATO)));
        colTipo.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getTipo().getNombre()));
        colPuntos.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getPuntosTexto()));
        colDescripcion.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getDescripcion()));
    }

    private void configurarRangos() {
        colRangoNombre.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getNombre()));
        colRangoPuntos.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(intervalo(d.getValue())));
        colRangoBeneficios.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().descripcionBeneficios()));
        // Busqueda por intervalo en el ArbolAVL: O(log n + k).
        colRangoClientes.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(String.valueOf(rangoService.contarClientes(d.getValue()))));
        tablaRangos.setItems(FXCollections.observableArrayList(Rango.values()));

        colPuesto.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(String.valueOf(
                        tablaClasificacion.getItems().indexOf(d.getValue()) + 1)));
        colClasCliente.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(
                        clienteService.obtenerCliente(d.getValue().getClienteId()).getNombre()));
        colClasAcumulados.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(String.valueOf(d.getValue().getPuntosAcumulados())));
        colClasDisponibles.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(String.valueOf(d.getValue().getPuntosDisponibles())));
        colClasRango.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getRango().getNombre()));
    }

    private void configurarCanjes() {
        colBeneficio.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getNombre()));
        colCosto.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getCostoPuntos() + " puntos"));
        colVigencia.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().esTemporal()
                        ? d.getValue().getDiasVigencia() + " dias"
                        : "Inmediato"));
        colDetalle.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getDescripcion()));
        colEstado.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(estadoBeneficio(d.getValue())));
        tablaCatalogo.setItems(FXCollections.observableArrayList(beneficioService.catalogo()));

        colVigenteBeneficio.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getTipo().getNombre()));
        colVigenteDesde.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getInicio().format(FORMATO)));
        colVigenteHasta.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().getFin().format(FORMATO)));
    }

    // =========================================================== cliente

    /** Vuelve a leer los clientes solo si se registraron nuevos (los clientes nunca se borran). */
    private void cargarClientes() {
        ListaEnlazada<Cliente> registrados = clienteService.listarClientes();
        if (registrados.getTamano() == comboCliente.getItems().size()) {
            return;
        }
        Cliente seleccionado = comboCliente.getValue();
        ObservableList<Cliente> clientes = FXCollections.observableArrayList();
        for (Cliente cliente : registrados) {
            clientes.add(cliente);
        }
        comboCliente.setItems(clientes);
        comboCliente.setValue(seleccionado);
    }

    /** Boton "Actualizar" y cada vez que la pantalla vuelve a mostrarse. */
    @FXML
    private void refrescarTodo() {
        cargarClientes();
        if (comboCliente.getValue() == null && !comboCliente.getItems().isEmpty()) {
            comboCliente.setValue(comboCliente.getItems().get(0));
        }
        actualizar();
    }

    /** Se llama al elegir un cliente: vuelve a pintar las tres pestanas. */
    @FXML
    private void actualizar() {
        lblMensajeCanje.setText("");
        actualizarRangos();

        Cliente cliente = comboCliente.getValue();
        if (cliente == null) {
            limpiarDatosCliente();
            return;
        }
        actualizarMisPuntos(cliente);
        actualizarCanjes(cliente);
    }

    // =========================================================== mis puntos

    private void actualizarMisPuntos(Cliente cliente) {
        RegistroPuntos registro = puntosService.obtenerRegistro(cliente.getId()); // TablaHash: O(1)

        lblDisponibles.setText(String.valueOf(registro.getPuntosDisponibles()));
        lblAcumulados.setText(String.valueOf(registro.getPuntosAcumulados()));

        Rango rango = registro.getRango();
        lblRango.setText(rango.getNombre());
        lblRango.setStyle("-fx-text-fill: " + rango.getColorHex() + ";");

        Rango siguiente = rango.siguiente();
        lblSiguienteRango.setText(siguiente == null
                ? "Ya estas en el rango maximo"
                : "Te faltan " + (siguiente.getPuntosMinimos() - registro.getPuntosAcumulados())
                + " puntos para " + siguiente.getNombre());

        ObservableList<MovimientoPuntos> filas = FXCollections.observableArrayList();
        for (MovimientoPuntos movimiento : registro.getHistorial()) {
            filas.add(0, movimiento); // el mas reciente arriba
        }
        tablaHistorial.setItems(filas);
    }

    // =========================================================== rangos y clasificacion

    private void actualizarRangos() {
        // Los clientes que aun no tienen movimientos entran a los arboles con 0 puntos (Bronce).
        puntosService.sincronizarClientes();

        // Recorrido inorden inverso del ABB: ya sale de mayor a menor.
        int totalClientes = clienteService.listarClientes().getTamano();
        ObservableList<RegistroPuntos> filas = FXCollections.observableArrayList();
        for (RegistroPuntos registro : puntosService.clasificacion(totalClientes)) {
            filas.add(registro);
        }
        tablaClasificacion.setItems(filas);
        tablaClasificacion.refresh();

        tablaRangos.refresh(); // vuelve a contar los clientes de cada rango

        lblArboles.setText("Arbol AVL de rangos: " + rangoService.cantidadClientes()
                + " clientes, altura " + rangoService.altura()
                + (rangoService.estaBalanceado() ? " (balanceado)" : " (NO balanceado)")
                + "   |   ABB de clasificacion: altura " + puntosService.alturaRanking());
    }

    private String intervalo(Rango rango) {
        if (rango.getPuntosMaximos() == Long.MAX_VALUE) {
            return "Mas de " + (rango.getPuntosMinimos() - 1);
        }
        return rango.getPuntosMinimos() + " a " + rango.getPuntosMaximos();
    }

    // =========================================================== canjes

    private void actualizarCanjes(Cliente cliente) {
        long disponibles = puntosService.consultarPuntosDisponibles(cliente.getId());
        lblPuntosCanje.setText(cliente.getNombre() + " tiene " + disponibles
                + " puntos disponibles. Selecciona un beneficio de la tabla y pulsa Canjear.");

        tablaCatalogo.refresh(); // la columna "Estado" depende del cliente elegido

        Monedero elegido = comboMonederoBono.getValue();
        ObservableList<Monedero> monederos = FXCollections.observableArrayList();
        for (Monedero monedero : clienteService.monederosDe(cliente.getId())) {
            monederos.add(monedero);
        }
        comboMonederoBono.setItems(monederos);
        if (elegido != null && monederos.contains(elegido)) {
            comboMonederoBono.setValue(elegido);
        } else if (!monederos.isEmpty()) {
            comboMonederoBono.setValue(monederos.get(0));
        } else {
            comboMonederoBono.setValue(null);
        }

        ObservableList<BeneficioActivo> vigentes = FXCollections.observableArrayList();
        for (BeneficioActivo beneficio : beneficioService.beneficiosVigentes(cliente.getId())) {
            vigentes.add(beneficio);
        }
        tablaVigentes.setItems(vigentes);
    }

    /** Texto de la columna "Estado" del catalogo para el cliente elegido. */
    private String estadoBeneficio(TipoBeneficio tipo) {
        Cliente cliente = comboCliente.getValue();
        if (cliente == null) {
            return "-";
        }
        if (tipo.esTemporal() && beneficioService.tieneVigente(cliente.getId(), tipo)) {
            return "Ya lo tienes activo";
        }
        long faltan = tipo.getCostoPuntos()
                - puntosService.consultarPuntosDisponibles(cliente.getId());
        return faltan > 0 ? "Te faltan " + faltan + " puntos" : "Disponible";
    }

    @FXML
    private void canjear() {
        Cliente cliente = comboCliente.getValue();
        if (cliente == null) {
            mostrarMensajeCanje("Primero elige un cliente", false);
            return;
        }
        TipoBeneficio tipo = tablaCatalogo.getSelectionModel().getSelectedItem();
        if (tipo == null) {
            mostrarMensajeCanje("Selecciona un beneficio de la tabla", false);
            return;
        }

        // El monedero solo se usa para el bono de saldo.
        Monedero monedero = comboMonederoBono.getValue();
        String monederoId = tipo == TipoBeneficio.BONO_SALDO && monedero != null
                ? monedero.getId()
                : null;

        try {
            // BeneficioService valida todo antes de descontar puntos.
            beneficioService.canjear(cliente.getId(), tipo, monederoId);
            actualizar();
            mostrarMensajeCanje("Canje realizado: " + tipo.getNombre(), true);
        } catch (RuntimeException error) {
            // Puntos insuficientes, beneficio ya activo, monedero ajeno...
            mostrarMensajeCanje(error.getMessage() == null
                    ? "No fue posible realizar el canje"
                    : error.getMessage(), false);
        }
    }

    private void mostrarMensajeCanje(String texto, boolean exito) {
        lblMensajeCanje.getStyleClass().removeAll("mensaje-exito", "mensaje-error");
        lblMensajeCanje.getStyleClass().add(exito ? "mensaje-exito" : "mensaje-error");
        lblMensajeCanje.setText(texto);
    }

    // =========================================================== sin cliente

    private void limpiarDatosCliente() {
        lblDisponibles.setText("-");
        lblAcumulados.setText("-");
        lblRango.setText("-");
        lblRango.setStyle("");
        lblSiguienteRango.setText("");
        tablaHistorial.getItems().clear();

        lblPuntosCanje.setText("Registra un cliente en el modulo 1 y elige uno arriba.");
        tablaCatalogo.refresh();
        comboMonederoBono.getItems().clear();
        tablaVigentes.getItems().clear();
    }
}
