package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.util.RelojSimulado;

public class ContextoMonedero {

    // Un solo reloj para todos los modulos.
    private final RelojSimulado reloj;

    // Servicios que manejan clientes y dinero.
    private final ClienteService clienteService;
    private final TransaccionService transaccionService;
    private final MultimonederoService multimonederoService;

    // Servicios del modulo de programaciones.
    private final NotificacionService notificacionService;
    private final ProgramacionService programacionService;
    private final ProgramacionNotificacionService
            programacionNotificacionService;

    private final ReporteGastosClienteService reporteService;
    private final EjecutorFinancieroProgramado ejecutorFinanciero;
    private final ProcesadorProgramacionesService procesador;

    // Integrante 2: puntos, rangos y beneficios.
    private final RangoService rangoService;
    private final PuntosService puntosService;
    private final BeneficioService beneficioService;

    // Constructor normal para la aplicacion.
    public ContextoMonedero() {
        this(new RelojSimulado());
    }

    // Constructor con reloj recibido.
    // Nos permite usar fechas fijas en las pruebas.
    public ContextoMonedero(RelojSimulado reloj) {

        if (reloj == null) {
            throw new IllegalArgumentException(
                    "El reloj es obligatorio"
            );
        }

        this.reloj = reloj;

        // Todos comparten los mismos clientes.
        clienteService = new ClienteService(reloj);

        transaccionService = new TransaccionService(
                clienteService,
                reloj
        );

        // El grafo escucha todas las transferencias
        // internas realizadas en la aplicacion.
        multimonederoService = new MultimonederoService(
                clienteService
        );

        transaccionService.agregarListener(
                multimonederoService
        );

        // Servicios de programaciones.
        notificacionService = new NotificacionService(reloj);

        // Integrante 2: puntos, rangos y beneficios.
        // El orden importa: cada servicio necesita a los anteriores.
        rangoService = new RangoService(
                clienteService,
                notificacionService
        );

        puntosService = new PuntosService(
                clienteService,
                rangoService,
                notificacionService,
                reloj
        );

        // PuntosService escucha cada transaccion (patron Observador).
        transaccionService.agregarListener(puntosService);

        beneficioService = new BeneficioService(
                puntosService,
                transaccionService,
                clienteService,
                notificacionService,
                reloj
        );

        // TransaccionService pregunta por descuentos a traves de la interfaz.
        transaccionService.setProveedorDescuentos(beneficioService);

        programacionService = new ProgramacionService(reloj);

        programacionNotificacionService =
                new ProgramacionNotificacionService(
                        programacionService,
                        notificacionService
                );

        reporteService = new ReporteGastosClienteService(
                clienteService
        );

        // Ejecutor de dinero real.
        ejecutorFinanciero = new EjecutorFinancieroProgramado(
                clienteService,
                transaccionService
        );

        // Procesamiento de operaciones vencidas.
        procesador = new ProcesadorProgramacionesService(
                programacionService,
                notificacionService,
                ejecutorFinanciero
        );
    }

    public RelojSimulado getReloj() {
        return reloj;
    }

    public ClienteService getClienteService() {
        return clienteService;
    }

    public TransaccionService getTransaccionService() {
        return transaccionService;
    }

    public MultimonederoService getMultimonederoService() {
        return multimonederoService;
    }

    public NotificacionService getNotificacionService() {
        return notificacionService;
    }

    public ProgramacionService getProgramacionService() {
        return programacionService;
    }

    public ProgramacionNotificacionService
    getProgramacionNotificacionService() {
        return programacionNotificacionService;
    }

    public ReporteGastosClienteService getReporteService() {
        return reporteService;
    }

    public EjecutorFinancieroProgramado getEjecutorFinanciero() {
        return ejecutorFinanciero;
    }

    public ProcesadorProgramacionesService getProcesador() {
        return procesador;
    }

    // Integrante 2

    public RangoService getRangoService() {
        return rangoService;
    }

    public PuntosService getPuntosService() {
        return puntosService;
    }

    public BeneficioService getBeneficioService() {
        return beneficioService;
    }
}