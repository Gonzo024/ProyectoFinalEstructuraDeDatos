
package co.edu.uniquindio.poo.vista;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Notificacion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.poo.servicios.ClienteService;
import co.edu.uniquindio.poo.servicios.NotificacionService;
import co.edu.uniquindio.poo.servicios.ReporteGastosClienteService;
import co.edu.uniquindio.poo.util.Dinero;
import co.edu.uniquindio.poo.util.RelojSimulado;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ModuloTresController {

    // Servicios que utilizara nuestra interfaz.
    private final RelojSimulado reloj = new RelojSimulado();

    private final NotificacionService notificacionService =
            new NotificacionService(reloj);

    private final ClienteService clienteService =
            new ClienteService(reloj);

    private final ReporteGastosClienteService reporteService =
            new ReporteGastosClienteService(clienteService);

    private boolean datosDemostracionCargados = false;

    // -------------------------------------------------
    // CAMPOS DE NOTIFICACIONES
    // -------------------------------------------------

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

    // -------------------------------------------------
    // CAMPOS DE ANALISIS DE GASTOS
    // -------------------------------------------------

    @FXML
    private TextField txtClienteGastos;

    @FXML
    private TextField txtAnioGastos;

    @FXML
    private TextField txtMesGastos;

    @FXML
    private Label lblTotalGastos;

    @FXML
    private Label lblCategoriaMayor;

    @FXML
    private Label lblEstadoGastos;

    @FXML
    private TableView<CategoriaGasto> tablaGastos;

    @FXML
    private TableColumn<CategoriaGasto, String> columnaCategoriaGastos;

    @FXML
    private TableColumn<CategoriaGasto, String> columnaTotalGastos;

    // -------------------------------------------------
    // INICIALIZACION DE LAS TABLAS
    // -------------------------------------------------

    @FXML
    public void initialize() {

        configurarTablaNotificaciones();
        configurarTablaGastos();

        LocalDate hoy = reloj.hoy();

        txtAnioGastos.setText(
                String.valueOf(hoy.getYear())
        );

        txtMesGastos.setText(
                String.valueOf(hoy.getMonthValue())
        );
    }

    private void configurarTablaNotificaciones() {

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

    private void configurarTablaGastos() {

        columnaCategoriaGastos.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getNombre()
                )
        );

        columnaTotalGastos.setCellValueFactory(datos -> {

            CategoriaGasto categoria = datos.getValue();

            String clienteId = txtClienteGastos
                    .getText().trim();

            int anio = Integer.parseInt(
                    txtAnioGastos.getText().trim()
            );

            int mes = Integer.parseInt(
                    txtMesGastos.getText().trim()
            );

            BigDecimal total =
                    reporteService.calcularCategoriaMensual(
                            clienteId, categoria, anio, mes
                    );

            return new ReadOnlyStringWrapper(
                    formatearDinero(total)
            );
        });

        tablaGastos.setPlaceholder(
                new Label("Consulta los gastos para ver resultados")
        );
    }

    // -------------------------------------------------
    // NOTIFICACIONES
    // -------------------------------------------------

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

    private void actualizarTablaNotificaciones(
            String clienteId) {

        var lista = FXCollections
                .<Notificacion>observableArrayList();

        for (Notificacion notificacion :
                notificacionService.consultarNotificaciones(
                        clienteId
                )) {

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

    // -------------------------------------------------
    // ANALISIS DE GASTOS
    // -------------------------------------------------

    @FXML
    private void consultarGastos() {

        String clienteId =
                txtClienteGastos.getText().trim();

        if (clienteId.isEmpty()) {
            limpiarReporte();
            mostrarMensaje(
                    "Validacion",
                    "Debes escribir el ID del cliente"
            );
            return;
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

            limpiarReporte();
            mostrarMensaje(
                    "Validacion",
                    "El año y el mes deben ser numeros enteros"
            );
            return;
        }

        if (anio < 1 || anio > 9999 || mes < 1 || mes > 12) {

            limpiarReporte();
            mostrarMensaje(
                    "Validacion",
                    "Ingresa un año valido y un mes entre 1 y 12"
            );
            return;
        }

        try {

            // Comprueba que el cliente realmente exista.
            clienteService.obtenerCliente(clienteId);

            BigDecimal total =
                    reporteService.calcularTotalMensual(
                            clienteId, anio, mes
                    );

            lblTotalGastos.setText(
                    "Total gastado: " + formatearDinero(total)
            );

            var categorias = FXCollections
                    .<CategoriaGasto>observableArrayList();

            BigDecimal mayorMonto = Dinero.CERO;
            CategoriaGasto categoriaMayor = null;

            for (CategoriaGasto categoria :
                    CategoriaGasto.values()) {

                BigDecimal gastoCategoria =
                        reporteService.calcularCategoriaMensual(
                                clienteId,
                                categoria,
                                anio,
                                mes
                        );

                if (gastoCategoria.signum() > 0) {

                    categorias.add(categoria);

                    if (gastoCategoria.compareTo(mayorMonto) > 0) {
                        mayorMonto = gastoCategoria;
                        categoriaMayor = categoria;
                    }
                }
            }

            tablaGastos.setItems(categorias);
            tablaGastos.refresh();

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
                    error.getMessage() == null
                            ? "No fue posible consultar los gastos"
                            : error.getMessage()
            );
        }
    }

    private void limpiarReporte() {

        tablaGastos.getItems().clear();

        lblTotalGastos.setText(
                "Total gastado: $0.00"
        );

        lblCategoriaMayor.setText(
                "Categoria con mayor gasto: Ninguna"
        );

        lblEstadoGastos.setText(
                "Sin consulta"
        );
    }

    // -------------------------------------------------
    // DATOS PARA DEMOSTRAR EL FUNCIONAMIENTO
    // -------------------------------------------------

    @FXML
    private void cargarDatosDemostracion() {

        if (datosDemostracionCargados) {

            txtClienteGastos.setText("C1");
            consultarGastos();

            mostrarMensaje(
                    "Datos de prueba",
                    "Los datos ya estaban cargados"
            );
            return;
        }

        // Crea un cliente mediante el servicio real.
        Cliente cliente = clienteService.registrarCliente(
                "Cliente demostracion",
                "demo@monedero.local",
                "1234"
        );

        // El servicio crea automaticamente el monedero principal.
        Monedero principal =
                clienteService.monederosDe(
                        cliente.getId()
                ).obtenerValorNodo(0);

        Monedero ahorros = clienteService.crearMonedero(
                cliente.getId(),
                "Ahorros",
                TipoMonedero.AHORROS
        );

        // Utilizamos fechas del mes actual.
        LocalDateTime fecha1 =
                reloj.ahora().minusDays(3);

        LocalDateTime fecha2 =
                reloj.ahora().minusDays(2);

        LocalDateTime fecha3 =
                reloj.ahora().minusDays(1);

        // Pago de alimentacion: $80.000
        Transaccion pago1 = Transaccion.builder(
                "DEMO-T1",
                TipoTransaccion.PAGO,
                Dinero.de(80000),
                fecha1
        )
                .origen(cliente.getId(), principal.getId())
                .categoria(CategoriaGasto.ALIMENTACION)
                .construir();

        // Pago de transporte: $20.000
        Transaccion pago2 = Transaccion.builder(
                "DEMO-T2",
                TipoTransaccion.PAGO,
                Dinero.de(20000),
                fecha2
        )
                .origen(cliente.getId(), principal.getId())
                .categoria(CategoriaGasto.TRANSPORTE)
                .construir();

        // Pago de alimentacion desde otro monedero: $30.000
        Transaccion pago3 = Transaccion.builder(
                "DEMO-T3",
                TipoTransaccion.PAGO,
                Dinero.de(30000),
                fecha3
        )
                .origen(cliente.getId(), ahorros.getId())
                .categoria(CategoriaGasto.ALIMENTACION)
                .construir();

        // Deposito: no debe contarse como gasto.
        Transaccion deposito = Transaccion.builder(
                "DEMO-T4",
                TipoTransaccion.DEPOSITO,
                Dinero.de(100000),
                fecha3
        )
                .destino(cliente.getId(), ahorros.getId())
                .construir();

        // Solo agregamos movimientos de prueba al historial.
        // No modificamos saldos ni ejecutamos operaciones reales.
        principal.getHistorial().agregarFinal(pago1);
        principal.getHistorial().agregarFinal(pago2);

        ahorros.getHistorial().agregarFinal(pago3);
        ahorros.getHistorial().agregarFinal(deposito);

        datosDemostracionCargados = true;

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
                "Se crearon movimientos de demostracion "
                        + "para el cliente " + cliente.getId()
        );
    }

    // -------------------------------------------------
    // METODOS AUXILIARES
    // -------------------------------------------------

    private String formatearDinero(BigDecimal monto) {

        return "$" + monto.setScale(2).toPlainString();
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
