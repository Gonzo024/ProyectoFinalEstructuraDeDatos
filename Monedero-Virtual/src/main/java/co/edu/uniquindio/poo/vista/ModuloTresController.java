
package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Notificacion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.servicios.ClienteService;
import co.edu.uniquindio.poo.servicios.NotificacionService;
import co.edu.uniquindio.poo.servicios.ProgramacionService;
import co.edu.uniquindio.poo.servicios.ProgramacionNotificacionService;
import co.edu.uniquindio.poo.servicios.ReporteGastosClienteService;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ModuloTresController {

    // SERVICIOS COMPARTIDOS

    private final RelojSimulado reloj = new RelojSimulado();

    private final ClienteService clienteService =
            new ClienteService(reloj);

    private final NotificacionService notificacionService =
            new NotificacionService(reloj);

    private final ProgramacionService programacionService =
            new ProgramacionService(reloj);

    // Conecta programaciones con notificaciones.
    private final ProgramacionNotificacionService
            programacionNotificacionService =
            new ProgramacionNotificacionService(
                    programacionService,
                    notificacionService
            );

    private final ReporteGastosClienteService reporteService =
            new ReporteGastosClienteService(clienteService);

    private final ObservableList<TransaccionProgramada>
            programacionesVisibles =
            FXCollections.observableArrayList();

    private boolean datosDemostracionCargados = false;
    private int contadorProgramaciones = 0;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // CAMPOS DE PROGRAMACIONES

    @FXML private TextField txtClienteProgramacion;
    @FXML private TextField txtMonederoProgramacion;
    @FXML private TextField txtMontoProgramacion;
    @FXML private TextField txtClienteDestino;
    @FXML private TextField txtMonederoDestino;
    @FXML private TextField txtHoraProgramacion;

    @FXML private DatePicker fechaProgramacion;

    @FXML private ComboBox<TipoTransaccion> comboTipo;
    @FXML private ComboBox<String> comboFrecuencia;

    @FXML private Label lblEstadoProgramaciones;

    @FXML private TableView<TransaccionProgramada> tablaProgramaciones;
    @FXML private TableColumn<TransaccionProgramada, String> colProgId;
    @FXML private TableColumn<TransaccionProgramada, String> colProgTipo;
    @FXML private TableColumn<TransaccionProgramada, String> colProgMonto;
    @FXML private TableColumn<TransaccionProgramada, String> colProgFecha;
    @FXML private TableColumn<TransaccionProgramada, String> colProgFrecuencia;

    // CAMPOS DE NOTIFICACIONES

    @FXML private TextField txtClienteNotificacion;
    @FXML private TableView<Notificacion> tablaNotificaciones;
    @FXML private TableColumn<Notificacion, String> columnaId;
    @FXML private TableColumn<Notificacion, String> columnaTipo;
    @FXML private TableColumn<Notificacion, String> columnaMensaje;
    @FXML private TableColumn<Notificacion, String> columnaLeida;
    @FXML private Label lblCantidadNoLeidas;

    // CAMPOS DE ANALISIS DE GASTOS

    @FXML private TextField txtClienteGastos;
    @FXML private TextField txtAnioGastos;
    @FXML private TextField txtMesGastos;
    @FXML private Label lblTotalGastos;
    @FXML private Label lblCategoriaMayor;
    @FXML private Label lblEstadoGastos;

    @FXML private TableView<CategoriaGasto> tablaGastos;
    @FXML private TableColumn<CategoriaGasto, String> columnaCategoriaGastos;
    @FXML private TableColumn<CategoriaGasto, String> columnaTotalGastos;

    private String clienteReporteActual;
    private int anioReporteActual;
    private int mesReporteActual;

    // INICIALIZACION

    @FXML
    public void initialize() {

        configurarProgramaciones();
        configurarNotificaciones();
        configurarGastos();

        LocalDate hoy = reloj.hoy();

        txtAnioGastos.setText(String.valueOf(hoy.getYear()));
        txtMesGastos.setText(String.valueOf(hoy.getMonthValue()));

        fechaProgramacion.setValue(hoy.plusDays(1));
        txtHoraProgramacion.setText("10:00");

        actualizarEstadoProgramaciones();
    }

    // -------------------------------------------------
    // PROGRAMACIONES
    // -------------------------------------------------

    private void configurarProgramaciones() {

        comboTipo.setItems(FXCollections.observableArrayList(
                TipoTransaccion.DEPOSITO,
                TipoTransaccion.PAGO,
                TipoTransaccion.TRANSFERENCIA,
                TipoTransaccion.TRANSFERENCIA_INTERNA
        ));

        comboFrecuencia.setItems(FXCollections.observableArrayList(
                "UNA_VEZ", "DIARIA", "SEMANAL", "MENSUAL"
        ));

        comboTipo.setValue(TipoTransaccion.PAGO);
        comboFrecuencia.setValue("UNA_VEZ");

        colProgId.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getId()));

        colProgTipo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getTipo().toString()));

        colProgMonto.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        formatearDinero(datos.getValue().getMonto())));

        colProgFecha.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getFechaProgramada()
                                .format(formatoFecha)));

        colProgFrecuencia.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getFrecuencia()));

        tablaProgramaciones.setItems(programacionesVisibles);

        tablaProgramaciones.setPlaceholder(
                new Label("No hay programaciones registradas"));
    }

    @FXML
    private void crearProgramacion() {

        try {

            String clienteId =
                    txtClienteProgramacion.getText().trim();

            String monederoId =
                    txtMonederoProgramacion.getText().trim();

            if (clienteId.isEmpty() || monederoId.isEmpty()) {
                throw new IllegalArgumentException(
                        "Escribe el ID del cliente y del monedero");
            }

            clienteService.obtenerCliente(clienteId);

            Monedero monedero =
                    clienteService.obtenerMonedero(monederoId);

            if (!monedero.getClienteId().equals(clienteId)) {
                throw new IllegalArgumentException(
                        "El monedero no pertenece al cliente");
            }

            // Convertimos el monto y validamos errores.
            String textoMonto =
                    txtMontoProgramacion.getText()
                            .trim().replace(",", ".");

            if (textoMonto.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debes escribir un monto");
            }

            BigDecimal monto;

            try {
                monto = new BigDecimal(textoMonto);
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException(
                        "El monto debe ser un numero valido");
            }

            if (!Dinero.esPositivo(monto)) {
                throw new IllegalArgumentException(
                        "El monto debe ser mayor que cero");
            }

            monto = Dinero.redondear(monto);

            LocalDate fecha = fechaProgramacion.getValue();

            if (fecha == null) {
                throw new IllegalArgumentException(
                        "Selecciona una fecha");
            }

            LocalTime hora;

            try {
                hora = LocalTime.parse(
                        txtHoraProgramacion.getText().trim());
            } catch (DateTimeParseException error) {
                throw new IllegalArgumentException(
                        "La hora debe tener el formato HH:mm");
            }

            LocalDateTime fechaCompleta =
                    LocalDateTime.of(fecha, hora);

            if (!fechaCompleta.isAfter(reloj.ahora())) {
                throw new IllegalArgumentException(
                        "La fecha y hora deben ser futuras");
            }

            TipoTransaccion tipo = comboTipo.getValue();
            String frecuencia = comboFrecuencia.getValue();

            if (tipo == null || frecuencia == null) {
                throw new IllegalArgumentException(
                        "Selecciona el tipo y la frecuencia");
            }

            String clienteOrigen = clienteId;
            String monederoOrigen = monederoId;

            String clienteDestino = null;
            String monederoDestino = null;

            if (tipo == TipoTransaccion.DEPOSITO) {

                monederoOrigen = null;
                clienteDestino = clienteId;
                monederoDestino = monederoId;

            } else if (tipo == TipoTransaccion.TRANSFERENCIA
                    || tipo == TipoTransaccion.TRANSFERENCIA_INTERNA) {

                clienteDestino =
                        txtClienteDestino.getText().trim();

                monederoDestino =
                        txtMonederoDestino.getText().trim();

                if (clienteDestino.isEmpty()
                        || monederoDestino.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Indica cliente y monedero destino");
                }

                clienteService.obtenerCliente(clienteDestino);

                Monedero destino =
                        clienteService.obtenerMonedero(monederoDestino);

                if (!destino.getClienteId().equals(clienteDestino)) {
                    throw new IllegalArgumentException(
                            "El monedero destino no pertenece al cliente destino");
                }

                if (monederoDestino.equals(monederoId)) {
                    throw new IllegalArgumentException(
                            "El origen y destino deben ser diferentes");
                }

                if (tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                        && !clienteDestino.equals(clienteId)) {
                    throw new IllegalArgumentException(
                            "La transferencia interna debe ser del mismo cliente");
                }

                if (tipo == TipoTransaccion.TRANSFERENCIA
                        && clienteDestino.equals(clienteId)) {
                    throw new IllegalArgumentException(
                            "Usa transferencia interna para el mismo cliente");
                }
            }

            String id = "P" + (contadorProgramaciones + 1);

            TransaccionProgramada nueva =
                    new TransaccionProgramada(
                            id,
                            tipo,
                            clienteOrigen,
                            monederoOrigen,
                            clienteDestino,
                            monederoDestino,
                            monto,
                            fechaCompleta,
                            frecuencia
                    );

            // NUEVA INTEGRACION:
            // Guarda en la cola y genera una notificacion.
            programacionNotificacionService.programarYNotificar(nueva);

            contadorProgramaciones++;

            programacionesVisibles.add(nueva);
            programacionesVisibles.sort(
                    TransaccionProgramada::compareTo);

            actualizarEstadoProgramaciones();

            // Si estamos viendo avisos de este cliente,
            // refrescamos su tabla tambien.
            actualizarNotificacionesSiCorresponde(clienteId);

            mostrarMensaje(
                    "Programacion creada",
                    "Se registro " + id
                            + " para el "
                            + fechaCompleta.format(formatoFecha)
                            + ". Se genero una notificacion. "
                            + "El dinero aun no se ha movido.");

        } catch (RuntimeException error) {

            mostrarMensaje(
                    "No se pudo programar",
                    mensajeError(error));
        }
    }

    @FXML
    private void cancelarProgramacion() {

        TransaccionProgramada seleccionada =
                tablaProgramaciones.getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {
            mostrarMensaje(
                    "Seleccion",
                    "Selecciona una programacion de la tabla");
            return;
        }

        // NUEVA INTEGRACION:
        // Cancela en el servicio y genera el aviso.
        boolean cancelada =
                programacionNotificacionService.cancelarYNotificar(
                        seleccionada.getId());

        if (cancelada) {

            programacionesVisibles.remove(seleccionada);
            actualizarEstadoProgramaciones();

            actualizarNotificacionesSiCorresponde(
                    seleccionada.getClienteOrigenId());

            mostrarMensaje(
                    "Programacion cancelada",
                    "Se cancelo " + seleccionada.getId()
                            + " y se genero una notificacion.");

        } else {

            mostrarMensaje(
                    "Error",
                    "La programacion ya no esta pendiente");
        }
    }

    @FXML
    private void consultarSiguienteProgramacion() {

        TransaccionProgramada siguiente =
                programacionService.consultarSiguiente();

        if (siguiente == null) {
            mostrarMensaje(
                    "Sin programaciones",
                    "No hay operaciones pendientes");
            return;
        }

        tablaProgramaciones.getSelectionModel().select(siguiente);
        tablaProgramaciones.scrollTo(siguiente);

        mostrarMensaje(
                "Proxima programacion",
                siguiente.getId() + " - "
                        + siguiente.getFechaProgramada()
                                .format(formatoFecha));
    }

    private void actualizarEstadoProgramaciones() {

        lblEstadoProgramaciones.setText(
                "Programaciones pendientes: "
                        + programacionService.cantidadPendientes());
    }

    // -------------------------------------------------
    // NOTIFICACIONES
    // -------------------------------------------------

    private void configurarNotificaciones() {

        columnaId.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getId()));

        columnaTipo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getTipo().toString()));

        columnaMensaje.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getMensaje()));

        columnaLeida.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().isLeida() ? "Si" : "No"));

        tablaNotificaciones.setPlaceholder(
                new Label("No hay notificaciones para mostrar"));
    }

    @FXML
    private void consultarNotificaciones() {

        String clienteId =
                txtClienteNotificacion.getText().trim();

        if (clienteId.isEmpty()) {
            mostrarMensaje(
                    "Validacion",
                    "Debes escribir el ID del cliente");
            return;
        }

        notificacionService.entregarNotificaciones();
        actualizarTablaNotificaciones(clienteId);
    }

    @FXML
    private void crearNotificacionPrueba() {

        String clienteId =
                txtClienteNotificacion.getText().trim();

        if (clienteId.isEmpty()) {
            mostrarMensaje(
                    "Validacion",
                    "Debes escribir el ID del cliente");
            return;
        }

        notificacionService.crearNotificacion(
                clienteId,
                TipoNotificacion.TRANSACCION,
                "Esta es una notificacion de prueba");

        notificacionService.entregarNotificaciones();
        actualizarTablaNotificaciones(clienteId);

        mostrarMensaje(
                "Notificacion creada",
                "Se creo una notificacion para " + clienteId);
    }

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
                    "Primero selecciona una notificacion");
            return;
        }

        if (!seleccionada.getClienteId().equals(clienteId)) {
            mostrarMensaje(
                    "Validacion",
                    "Consulta primero las notificaciones del cliente");
            return;
        }

        boolean marcada =
                notificacionService.marcarComoLeida(
                        seleccionada.getId());

        if (marcada) {

            actualizarTablaNotificaciones(clienteId);

            mostrarMensaje(
                    "Correcto",
                    "Notificacion marcada como leida");

        } else {

            mostrarMensaje(
                    "Error",
                    "No se encontro la notificacion");
        }
    }

    private void actualizarTablaNotificaciones(
            String clienteId) {

        ObservableList<Notificacion> lista =
                FXCollections.observableArrayList();

        for (Notificacion notificacion :
                notificacionService.consultarNotificaciones(
                        clienteId)) {
            lista.add(notificacion);
        }

        tablaNotificaciones.setItems(lista);
        tablaNotificaciones.refresh();

        int noLeidas =
                notificacionService.contarNoLeidas(clienteId);

        lblCantidadNoLeidas.setText(
                "Notificaciones sin leer: " + noLeidas);
    }

    // Actualiza avisos solo si la tabla muestra ese cliente.
    private void actualizarNotificacionesSiCorresponde(
            String clienteId) {

        String clienteConsultado =
                txtClienteNotificacion.getText().trim();

        if (clienteId.equals(clienteConsultado)) {
            actualizarTablaNotificaciones(clienteId);
        }
    }

    // -------------------------------------------------
    // ANALISIS DE GASTOS
    // -------------------------------------------------

    private void configurarGastos() {

        columnaCategoriaGastos.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getNombre()));

        columnaTotalGastos.setCellValueFactory(datos -> {

            if (clienteReporteActual == null) {
                return new ReadOnlyStringWrapper("$0.00");
            }

            BigDecimal total =
                    reporteService.calcularCategoriaMensual(
                            clienteReporteActual,
                            datos.getValue(),
                            anioReporteActual,
                            mesReporteActual);

            return new ReadOnlyStringWrapper(
                    formatearDinero(total));
        });

        tablaGastos.setPlaceholder(
                new Label("Consulta los gastos para ver resultados"));
    }

    @FXML
    private void consultarGastos() {

        String clienteId =
                txtClienteGastos.getText().trim();

        try {

            if (clienteId.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debes escribir el ID del cliente");
            }

            int anio;
            int mes;

            try {

                anio = Integer.parseInt(
                        txtAnioGastos.getText().trim());

                mes = Integer.parseInt(
                        txtMesGastos.getText().trim());

            } catch (NumberFormatException error) {
                throw new IllegalArgumentException(
                        "El anio y el mes deben ser numeros enteros");
            }

            if (anio < 1 || anio > 9999
                    || mes < 1 || mes > 12) {
                throw new IllegalArgumentException(
                        "Ingresa un anio valido y un mes entre 1 y 12");
            }

            clienteService.obtenerCliente(clienteId);

            BigDecimal total =
                    reporteService.calcularTotalMensual(
                            clienteId, anio, mes);

            CategoriaGasto categoriaMayor = null;
            BigDecimal mayorMonto = Dinero.CERO;

            ObservableList<CategoriaGasto> categorias =
                    FXCollections.observableArrayList();

            for (CategoriaGasto categoria :
                    CategoriaGasto.values()) {

                BigDecimal gasto =
                        reporteService.calcularCategoriaMensual(
                                clienteId, categoria, anio, mes);

                if (gasto.signum() > 0) {

                    categorias.add(categoria);

                    if (gasto.compareTo(mayorMonto) > 0) {
                        mayorMonto = gasto;
                        categoriaMayor = categoria;
                    }
                }
            }

            clienteReporteActual = clienteId;
            anioReporteActual = anio;
            mesReporteActual = mes;

            tablaGastos.setItems(categorias);
            tablaGastos.refresh();

            lblTotalGastos.setText(
                    "Total gastado: " + formatearDinero(total));

            if (categoriaMayor == null) {

                lblCategoriaMayor.setText(
                        "Categoria con mayor gasto: Ninguna");

                lblEstadoGastos.setText(
                        "No se encontraron gastos para este periodo");

            } else {

                lblCategoriaMayor.setText(
                        "Categoria con mayor gasto: "
                                + categoriaMayor.getNombre());

                lblEstadoGastos.setText(
                        "Reporte generado para " + clienteId);
            }

        } catch (RuntimeException error) {

            limpiarReporte();

            mostrarMensaje(
                    "Error al consultar",
                    mensajeError(error));
        }
    }

    private void limpiarReporte() {

        clienteReporteActual = null;
        tablaGastos.getItems().clear();

        lblTotalGastos.setText("Total gastado: $0.00");

        lblCategoriaMayor.setText(
                "Categoria con mayor gasto: Ninguna");

        lblEstadoGastos.setText("Sin consulta");
    }

    // -------------------------------------------------
    // DATOS DE DEMOSTRACION
    // -------------------------------------------------

    @FXML
    private void cargarDatosDemostracion() {

        if (datosDemostracionCargados) {

            txtClienteGastos.setText("C1");
            consultarGastos();

            mostrarMensaje(
                    "Datos de prueba",
                    "Los datos ya estaban cargados");
            return;
        }

        Cliente cliente = clienteService.registrarCliente(
                "Cliente demostracion",
                "demo@monedero.local",
                "1234");

        Monedero principal =
                clienteService.monederosDe(
                        cliente.getId()).obtenerValorNodo(0);

        Monedero ahorros = clienteService.crearMonedero(
                cliente.getId(),
                "Ahorros",
                TipoMonedero.AHORROS);

        LocalDateTime fecha1 = reloj.ahora().minusDays(3);
        LocalDateTime fecha2 = reloj.ahora().minusDays(2);
        LocalDateTime fecha3 = reloj.ahora().minusDays(1);

        Transaccion pago1 = Transaccion.builder(
                "DEMO-T1",
                TipoTransaccion.PAGO,
                Dinero.de(80000),
                fecha1)
                .origen(cliente.getId(), principal.getId())
                .categoria(CategoriaGasto.ALIMENTACION)
                .construir();

        Transaccion pago2 = Transaccion.builder(
                "DEMO-T2",
                TipoTransaccion.PAGO,
                Dinero.de(20000),
                fecha2)
                .origen(cliente.getId(), principal.getId())
                .categoria(CategoriaGasto.TRANSPORTE)
                .construir();

        Transaccion pago3 = Transaccion.builder(
                "DEMO-T3",
                TipoTransaccion.PAGO,
                Dinero.de(30000),
                fecha3)
                .origen(cliente.getId(), ahorros.getId())
                .categoria(CategoriaGasto.ALIMENTACION)
                .construir();

        Transaccion deposito = Transaccion.builder(
                "DEMO-T4",
                TipoTransaccion.DEPOSITO,
                Dinero.de(100000),
                fecha3)
                .destino(cliente.getId(), ahorros.getId())
                .construir();

        principal.getHistorial().agregarFinal(pago1);
        principal.getHistorial().agregarFinal(pago2);
        ahorros.getHistorial().agregarFinal(pago3);
        ahorros.getHistorial().agregarFinal(deposito);

        datosDemostracionCargados = true;

        txtClienteGastos.setText(cliente.getId());
        txtAnioGastos.setText(
                String.valueOf(reloj.hoy().getYear()));
        txtMesGastos.setText(
                String.valueOf(reloj.hoy().getMonthValue()));

        txtClienteProgramacion.setText(cliente.getId());
        txtMonederoProgramacion.setText(principal.getId());

        consultarGastos();

        mostrarMensaje(
                "Datos cargados",
                "Cliente " + cliente.getId()
                        + " creado. Monederos: "
                        + principal.getId() + " y "
                        + ahorros.getId()
                        + ". Puedes usarlos en Programaciones.");
    }

    // -------------------------------------------------
    // METODOS AUXILIARES
    // -------------------------------------------------

    private String formatearDinero(BigDecimal monto) {
        return "$" + monto.setScale(2).toPlainString();
    }

    private String mensajeError(RuntimeException error) {

        if (error instanceof NumberFormatException) {
            return "Debes ingresar un numero valido";
        }

        if (error instanceof DateTimeParseException) {
            return "La hora debe tener el formato HH:mm";
        }

        if (error instanceof IllegalArgumentException
                && error.getMessage() != null) {
            return error.getMessage();
        }

        return "No fue posible completar la operacion";
    }

    private void mostrarMensaje(
            String titulo,
            String mensaje) {

        ButtonType aceptar = new ButtonType(
                "Aceptar",
                ButtonBar.ButtonData.OK_DONE);

        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION,
                mensaje,
                aceptar);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
