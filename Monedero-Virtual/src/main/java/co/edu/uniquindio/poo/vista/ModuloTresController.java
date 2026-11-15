
package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Notificacion;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;

import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;

import co.edu.uniquindio.poo.servicios.ClienteService;
import co.edu.uniquindio.poo.servicios.ContextoMonedero;
import co.edu.uniquindio.poo.servicios.ContextoSesion;
import co.edu.uniquindio.poo.servicios.NotificacionService;
import co.edu.uniquindio.poo.servicios.ProcesadorProgramacionesService;
import co.edu.uniquindio.poo.servicios.ProgramacionService;
import co.edu.uniquindio.poo.servicios.ProgramacionNotificacionService;
import co.edu.uniquindio.poo.servicios.ReporteGastosClienteService;
import co.edu.uniquindio.poo.servicios.TransaccionService;

import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ModuloTresController {

    // ==================================================
    // SERVICIOS COMPARTIDOS CON EL MODULO UNO
    // ==================================================

    // Los servicios ya no se crean por separado.
    // Ambos modulos utilizan el mismo contexto.
    private final ContextoMonedero contexto =
            ContextoSesion.obtener();

    private final RelojSimulado reloj =
            contexto.getReloj();

    private final ClienteService clienteService =
            contexto.getClienteService();

    private final TransaccionService transaccionService =
            contexto.getTransaccionService();

    private final NotificacionService notificacionService =
            contexto.getNotificacionService();

    private final ProgramacionService programacionService =
            contexto.getProgramacionService();

    private final ProgramacionNotificacionService
            programacionNotificacionService =
            contexto.getProgramacionNotificacionService();

    private final ReporteGastosClienteService reporteService =
            contexto.getReporteService();

    private final ProcesadorProgramacionesService procesador =
            contexto.getProcesador();

    // ==================================================
    // VARIABLES
    // ==================================================

    private final ObservableList<TransaccionProgramada>
            programacionesVisibles =
            FXCollections.observableArrayList();

    private boolean datosDemostracionCargados = false;
    private int contadorProgramaciones = 0;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Label lblRelojSimulado;

    private String clienteReporteActual;
    private int anioReporteActual;
    private int mesReporteActual;

    // ==================================================
    // CAMPOS DE PROGRAMACIONES
    // ==================================================

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

    @FXML private TableView<TransaccionProgramada>
            tablaProgramaciones;

    @FXML private TableColumn<TransaccionProgramada, String>
            colProgId;

    @FXML private TableColumn<TransaccionProgramada, String>
            colProgTipo;

    @FXML private TableColumn<TransaccionProgramada, String>
            colProgMonto;

    @FXML private TableColumn<TransaccionProgramada, String>
            colProgFecha;

    @FXML private TableColumn<TransaccionProgramada, String>
            colProgFrecuencia;

    // ==================================================
    // CAMPOS DE NOTIFICACIONES
    // ==================================================

    @FXML private TextField txtClienteNotificacion;
    @FXML private TableView<Notificacion> tablaNotificaciones;

    @FXML private TableColumn<Notificacion, String> columnaId;
    @FXML private TableColumn<Notificacion, String> columnaTipo;
    @FXML private TableColumn<Notificacion, String> columnaMensaje;
    @FXML private TableColumn<Notificacion, String> columnaLeida;

    @FXML private Label lblCantidadNoLeidas;

    // ==================================================
    // CAMPOS DE ANALISIS DE GASTOS
    // ==================================================

    @FXML private TextField txtClienteGastos;
    @FXML private TextField txtAnioGastos;
    @FXML private TextField txtMesGastos;

    @FXML private Label lblTotalGastos;
    @FXML private Label lblCategoriaMayor;
    @FXML private Label lblEstadoGastos;

    @FXML private TableView<CategoriaGasto> tablaGastos;

    @FXML private TableColumn<CategoriaGasto, String>
            columnaCategoriaGastos;

    @FXML private TableColumn<CategoriaGasto, String>
            columnaTotalGastos;

    // ==================================================
    // INICIALIZACION
    // ==================================================

    @FXML
    public void initialize() {

        configurarProgramaciones();
        configurarNotificaciones();
        configurarGastos();

        LocalDate hoy = reloj.hoy();

        txtAnioGastos.setText(
                String.valueOf(hoy.getYear())
        );

        txtMesGastos.setText(
                String.valueOf(hoy.getMonthValue())
        );

        fechaProgramacion.setValue(hoy.plusDays(1));
        txtHoraProgramacion.setText("10:00");

        crearControlesSimulacion();
        actualizarEstadoProgramaciones();
    }

    // ==================================================
    // PROGRAMACIONES
    // ==================================================

    private void configurarProgramaciones() {

        comboTipo.setItems(
                FXCollections.observableArrayList(
                        TipoTransaccion.DEPOSITO,
                        TipoTransaccion.PAGO,
                        TipoTransaccion.TRANSFERENCIA,
                        TipoTransaccion.TRANSFERENCIA_INTERNA
                )
        );

        comboFrecuencia.setItems(
                FXCollections.observableArrayList(
                        "UNA_VEZ", "DIARIA", "SEMANAL", "MENSUAL"
                )
        );

        comboTipo.setValue(TipoTransaccion.PAGO);
        comboFrecuencia.setValue("UNA_VEZ");

        colProgId.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getId()
                )
        );

        colProgTipo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getTipo().toString()
                )
        );

        colProgMonto.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        formatearDinero(datos.getValue().getMonto())
                )
        );

        colProgFecha.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getFechaProgramada()
                                .format(formatoFecha)
                )
        );

        colProgFrecuencia.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getFrecuencia()
                )
        );

        tablaProgramaciones.setItems(programacionesVisibles);

        tablaProgramaciones.setPlaceholder(
                new Label("No hay programaciones registradas")
        );
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
                        "Escribe el ID del cliente y del monedero"
                );
            }

            clienteService.obtenerCliente(clienteId);

            Monedero monedero =
                    clienteService.obtenerMonedero(monederoId);

            if (!monedero.getClienteId().equals(clienteId)) {
                throw new IllegalArgumentException(
                        "El monedero no pertenece al cliente"
                );
            }

            String textoMonto =
                    txtMontoProgramacion.getText()
                            .trim().replace(",", ".");

            if (textoMonto.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debes escribir un monto"
                );
            }

            BigDecimal monto;

            try {
                monto = new BigDecimal(textoMonto);
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException(
                        "El monto debe ser un numero valido"
                );
            }

            if (!Dinero.esPositivo(monto)) {
                throw new IllegalArgumentException(
                        "El monto debe ser mayor que cero"
                );
            }

            monto = Dinero.redondear(monto);

            LocalDate fecha = fechaProgramacion.getValue();

            if (fecha == null) {
                throw new IllegalArgumentException(
                        "Selecciona una fecha"
                );
            }

            LocalTime hora;

            try {
                hora = LocalTime.parse(
                        txtHoraProgramacion.getText().trim()
                );
            } catch (DateTimeParseException error) {
                throw new IllegalArgumentException(
                        "La hora debe tener el formato HH:mm"
                );
            }

            LocalDateTime fechaCompleta =
                    LocalDateTime.of(fecha, hora);

            if (!fechaCompleta.isAfter(reloj.ahora())) {
                throw new IllegalArgumentException(
                        "La fecha y hora deben ser futuras"
                );
            }

            TipoTransaccion tipo = comboTipo.getValue();
            String frecuencia = comboFrecuencia.getValue();

            if (tipo == null || frecuencia == null) {
                throw new IllegalArgumentException(
                        "Selecciona el tipo y la frecuencia"
                );
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
                            "Indica cliente y monedero destino"
                    );
                }

                clienteService.obtenerCliente(clienteDestino);

                Monedero destino =
                        clienteService.obtenerMonedero(monederoDestino);

                if (!destino.getClienteId().equals(clienteDestino)) {
                    throw new IllegalArgumentException(
                            "El monedero destino no pertenece al cliente destino"
                    );
                }

                if (monederoDestino.equals(monederoId)) {
                    throw new IllegalArgumentException(
                            "El origen y destino deben ser diferentes"
                    );
                }

                if (tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                        && !clienteDestino.equals(clienteId)) {
                    throw new IllegalArgumentException(
                            "La transferencia interna debe ser del mismo cliente"
                    );
                }

                if (tipo == TipoTransaccion.TRANSFERENCIA
                        && clienteDestino.equals(clienteId)) {
                    throw new IllegalArgumentException(
                            "Usa transferencia interna para el mismo cliente"
                    );
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

            programacionNotificacionService.programarYNotificar(nueva);

            contadorProgramaciones++;

            programacionesVisibles.add(nueva);
            programacionesVisibles.sort(
                    TransaccionProgramada::compareTo
            );

            actualizarEstadoProgramaciones();
            actualizarNotificacionesSiCorresponde(clienteId);

            mostrarMensaje(
                    "Programacion creada",
                    "Se registro " + id
                            + " para el "
                            + fechaCompleta.format(formatoFecha)
                            + ". Se genero una notificacion. "
                            + "El dinero aun no se ha movido."
            );

        } catch (RuntimeException error) {
            mostrarMensaje(
                    "No se pudo programar",
                    mensajeError(error)
            );
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
                    "Selecciona una programacion de la tabla"
            );
            return;
        }

        boolean cancelada =
                programacionNotificacionService.cancelarYNotificar(
                        seleccionada.getId()
                );

        if (cancelada) {

            programacionesVisibles.remove(seleccionada);
            actualizarEstadoProgramaciones();

            actualizarNotificacionesSiCorresponde(
                    seleccionada.getClienteOrigenId()
            );

            mostrarMensaje(
                    "Programacion cancelada",
                    "Se cancelo " + seleccionada.getId()
                            + " y se genero una notificacion."
            );

        } else {
            mostrarMensaje(
                    "Error",
                    "La programacion ya no esta pendiente"
            );
        }
    }

    @FXML
    private void consultarSiguienteProgramacion() {

        TransaccionProgramada siguiente =
                programacionService.consultarSiguiente();

        if (siguiente == null) {
            mostrarMensaje(
                    "Sin programaciones",
                    "No hay operaciones pendientes"
            );
            return;
        }

        tablaProgramaciones.getSelectionModel().select(siguiente);
        tablaProgramaciones.scrollTo(siguiente);

        mostrarMensaje(
                "Proxima programacion",
                siguiente.getId() + " - "
                        + siguiente.getFechaProgramada()
                                .format(formatoFecha)
        );
    }

    // ==================================================
    // EJECUCION DE PROGRAMACIONES
    // ==================================================

    private void crearControlesSimulacion() {

        VBox panel =
                (VBox) tablaProgramaciones.getParent();

        Label titulo = new Label(
                "Ejecucion de programaciones"
        );

        titulo.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14px;"
        );

        lblRelojSimulado = new Label();

        Button btnAvanzar = new Button(
                "Avanzar al siguiente vencimiento"
        );

        Button btnProcesar = new Button(
                "Ejecutar programaciones vencidas"
        );

        btnAvanzar.setOnAction(evento ->
                avanzarRelojAlSiguienteVencimiento()
        );

        btnProcesar.setOnAction(evento ->
                procesarVencimientosDePrueba()
        );

        HBox botones = new HBox(
                10, btnAvanzar, btnProcesar
        );

        Label advertencia = new Label(
                "Las operaciones modifican saldos reales "
                        + "de esta sesion de prueba. "
                        + "El reloj se avanza manualmente."
        );

        advertencia.setWrapText(true);
        advertencia.setStyle(
                "-fx-text-fill: #777777; -fx-font-size: 12px;"
        );

        int posicion =
                panel.getChildren().indexOf(tablaProgramaciones);

        panel.getChildren().add(posicion++, titulo);
        panel.getChildren().add(posicion++, lblRelojSimulado);
        panel.getChildren().add(posicion++, botones);
        panel.getChildren().add(posicion, advertencia);

        actualizarRelojVisual();
    }

    private void avanzarRelojAlSiguienteVencimiento() {

        TransaccionProgramada siguiente =
                programacionService.consultarSiguiente();

        if (siguiente == null) {
            mostrarMensaje(
                    "Sin programaciones",
                    "Primero debes crear una programacion."
            );
            return;
        }

        LocalDateTime fecha =
                siguiente.getFechaProgramada();

        if (fecha.isAfter(reloj.ahora())) {
            reloj.fijar(fecha);
        }

        actualizarRelojVisual();

        mostrarMensaje(
                "Reloj actualizado",
                "El reloj simulado ahora marca "
                        + reloj.ahora().format(formatoFecha)
                        + ". Ya puedes ejecutar las vencidas."
        );
    }

    private void procesarVencimientosDePrueba() {

        if (!programacionService.hayTransaccionesVencidas()) {
            mostrarMensaje(
                    "Sin vencimientos",
                    "No hay operaciones vencidas."
            );
            return;
        }

        int procesadas = procesador.procesarVencidas();

        programacionesVisibles.removeIf(
                programacion -> !programacion.isActiva()
        );

        programacionesVisibles.sort(
                TransaccionProgramada::compareTo
        );

        tablaProgramaciones.refresh();

        actualizarEstadoProgramaciones();
        actualizarRelojVisual();

        String clienteConsultado =
                txtClienteNotificacion.getText().trim();

        if (!clienteConsultado.isEmpty()) {
            actualizarTablaNotificaciones(clienteConsultado);
        }

        if (clienteReporteActual != null) {
            consultarGastos();
        }

        mostrarMensaje(
                "Procesamiento completado",
                "Se procesaron " + procesadas
                        + " programaciones vencidas. "
                        + "Consulta Notificaciones para comprobar "
                        + "cuales se ejecutaron y cuales fallaron. "
                        + "Los movimientos exitosos tambien aparecen "
                        + "en el historial del modulo Mi monedero."
        );
    }

    private void actualizarRelojVisual() {

        lblRelojSimulado.setText(
                "Fecha simulada: "
                        + reloj.ahora().format(formatoFecha)
        );
    }

    private void actualizarEstadoProgramaciones() {

        lblEstadoProgramaciones.setText(
                "Programaciones pendientes: "
                        + programacionService.cantidadPendientes()
        );
    }

    // ==================================================
    // NOTIFICACIONES
    // ==================================================

    private void configurarNotificaciones() {

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
        actualizarTablaNotificaciones(clienteId);
    }

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

        // El cliente debe existir en el sistema compartido.
        try {
            clienteService.obtenerCliente(clienteId);

            notificacionService.crearNotificacion(
                    clienteId,
                    TipoNotificacion.TRANSACCION,
                    "Esta es una notificacion de prueba"
            );

            notificacionService.entregarNotificaciones();
            actualizarTablaNotificaciones(clienteId);

            mostrarMensaje(
                    "Notificacion creada",
                    "Se creo una notificacion para " + clienteId
            );

        } catch (RuntimeException error) {
            mostrarMensaje(
                    "No se pudo crear",
                    mensajeError(error)
            );
        }
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
            actualizarTablaNotificaciones(clienteId);

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

    private void actualizarTablaNotificaciones(
            String clienteId) {

        ObservableList<Notificacion> lista =
                FXCollections.observableArrayList();

        for (Notificacion notificacion :
                notificacionService.consultarNotificaciones(clienteId)) {
            lista.add(notificacion);
        }

        tablaNotificaciones.setItems(lista);
        tablaNotificaciones.refresh();

        int noLeidas =
                notificacionService.contarNoLeidas(clienteId);

        lblCantidadNoLeidas.setText(
                "Notificaciones sin leer: " + noLeidas
        );
    }

    private void actualizarNotificacionesSiCorresponde(
            String clienteId) {

        String clienteConsultado =
                txtClienteNotificacion.getText().trim();

        if (clienteId.equals(clienteConsultado)) {
            actualizarTablaNotificaciones(clienteId);
        }
    }

    // ==================================================
    // ANALISIS DE GASTOS
    // ==================================================

    private void configurarGastos() {

        columnaCategoriaGastos.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getNombre()
                )
        );

        columnaTotalGastos.setCellValueFactory(datos -> {

            if (clienteReporteActual == null) {
                return new ReadOnlyStringWrapper("$0.00");
            }

            BigDecimal total =
                    reporteService.calcularCategoriaMensual(
                            clienteReporteActual,
                            datos.getValue(),
                            anioReporteActual,
                            mesReporteActual
                    );

            return new ReadOnlyStringWrapper(
                    formatearDinero(total)
            );
        });

        tablaGastos.setPlaceholder(
                new Label("Consulta los gastos para ver resultados")
        );
    }

    @FXML
    private void consultarGastos() {

        String clienteId =
                txtClienteGastos.getText().trim();

        try {

            if (clienteId.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debes escribir el ID del cliente"
                );
            }

            int anio;
            int mes;

            try {
                anio = Integer.parseInt(
                        txtAnioGastos.getText().trim()
                );

                mes = Integer.parseInt(
                        txtMesGastos.getText().trim()
                );
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException(
                        "El anio y el mes deben ser numeros enteros"
                );
            }

            if (anio < 1 || anio > 9999
                    || mes < 1 || mes > 12) {
                throw new IllegalArgumentException(
                        "Ingresa un anio valido y un mes entre 1 y 12"
                );
            }

            clienteService.obtenerCliente(clienteId);

            BigDecimal total =
                    reporteService.calcularTotalMensual(
                            clienteId, anio, mes
                    );

            CategoriaGasto categoriaMayor = null;
            BigDecimal mayorMonto = Dinero.CERO;

            ObservableList<CategoriaGasto> categorias =
                    FXCollections.observableArrayList();

            for (CategoriaGasto categoria :
                    CategoriaGasto.values()) {

                BigDecimal gasto =
                        reporteService.calcularCategoriaMensual(
                                clienteId,
                                categoria,
                                anio,
                                mes
                        );

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
                    "Total gastado: " + formatearDinero(total)
            );

            if (categoriaMayor == null) {

                lblCategoriaMayor.setText(
                        "Categoria con mayor gasto: Ninguna"
                );

                lblEstadoGastos.setText(
                        "No se encontraron gastos para este periodo"
                );

            } else {

                lblCategoriaMayor.setText(
                        "Categoria con mayor gasto: "
                                + categoriaMayor.getNombre()
                );

                lblEstadoGastos.setText(
                        "Reporte generado para " + clienteId
                );
            }

        } catch (RuntimeException error) {

            limpiarReporte();

            mostrarMensaje(
                    "Error al consultar",
                    mensajeError(error)
            );
        }
    }

    private void limpiarReporte() {

        clienteReporteActual = null;
        tablaGastos.getItems().clear();

        lblTotalGastos.setText("Total gastado: $0.00");

        lblCategoriaMayor.setText(
                "Categoria con mayor gasto: Ninguna"
        );

        lblEstadoGastos.setText("Sin consulta");
    }

    // ==================================================
    // DATOS DE DEMOSTRACION
    // ==================================================

    @FXML
    private void cargarDatosDemostracion() {

        if (datosDemostracionCargados) {
            mostrarMensaje(
                    "Datos de prueba",
                    "Los datos de demostracion ya estaban cargados."
            );
            return;
        }

        try {

            // Si ya existe este correo, no creamos otro
            // cliente ni duplicamos los depositos.
            for (Cliente existente : clienteService.listarClientes()) {

                if (existente.getCorreo().equalsIgnoreCase(
                        "demo@monedero.local")) {

                    throw new IllegalArgumentException(
                            "Ya existe el cliente de demostracion. "
                                    + "Puedes utilizar sus monederos."
                    );
                }
            }

            Cliente cliente =
                    clienteService.registrarCliente(
                            "Cliente demostracion",
                            "demo@monedero.local",
                            "1234"
                    );

            Monedero principal =
                    clienteService.monederosDe(cliente.getId())
                            .obtenerValorNodo(0);

            Monedero ahorros =
                    clienteService.crearMonedero(
                            cliente.getId(),
                            "Ahorros",
                            TipoMonedero.AHORROS
                    );

            // Primero ingresamos el dinero realmente.
            // El servicio guarda cada deposito en el historial.
            transaccionService.depositar(
                    principal.getId(),
                    Dinero.de(200000),
                    "Saldo inicial de demostracion"
            );

            transaccionService.depositar(
                    ahorros.getId(),
                    Dinero.de(50000),
                    "Saldo inicial de ahorros"
            );

            // Los gastos tambien deben ser movimientos
            // reales; asi el saldo y el historial coinciden.
            transaccionService.pagar(
                    principal.getId(),
                    Dinero.de(80000),
                    CategoriaGasto.ALIMENTACION,
                    "Compra de alimentos de demostracion"
            );

            transaccionService.pagar(
                    principal.getId(),
                    Dinero.de(20000),
                    CategoriaGasto.TRANSPORTE,
                    "Transporte de demostracion"
            );

            transaccionService.pagar(
                    ahorros.getId(),
                    Dinero.de(30000),
                    CategoriaGasto.ALIMENTACION,
                    "Segundo gasto de alimentos"
            );

            datosDemostracionCargados = true;

            txtClienteProgramacion.setText(cliente.getId());
            txtMonederoProgramacion.setText(principal.getId());

            txtClienteGastos.setText(cliente.getId());

            txtAnioGastos.setText(
                    String.valueOf(reloj.hoy().getYear())
            );

            txtMesGastos.setText(
                    String.valueOf(reloj.hoy().getMonthValue())
            );

            consultarGastos();

            mostrarMensaje(
                    "Datos cargados",
                    "Se creo el cliente " + cliente.getId()
                            + " con monederos "
                            + principal.getId() + " y "
                            + ahorros.getId() + ". "
                            + "Saldo principal: "
                            + formatearDinero(principal.getSaldo())
                            + ". Saldo ahorros: "
                            + formatearDinero(ahorros.getSaldo())
                            + ". Los gastos se registraron "
                            + "como movimientos financieros reales."
            );

        } catch (RuntimeException error) {
            mostrarMensaje(
                    "No se pudieron cargar los datos",
                    mensajeError(error)
            );
        }
    }

    // ==================================================
    // METODOS AUXILIARES
    // ==================================================

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

        if (error.getMessage() != null
                && !error.getMessage().isBlank()) {
            return error.getMessage();
        }

        return "No fue posible completar la operacion";
    }

    private void mostrarMensaje(
            String titulo,
            String mensaje) {

        ButtonType aceptar = new ButtonType(
                "Aceptar",
                ButtonBar.ButtonData.OK_DONE
        );

        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION,
                "",
                aceptar
        );

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);

        Label contenido = new Label(mensaje);
        contenido.setWrapText(true);
        contenido.setMaxWidth(520);

        alerta.getDialogPane().setContent(contenido);
        alerta.getDialogPane().setPrefWidth(580);

        alerta.showAndWait();
    }
}
