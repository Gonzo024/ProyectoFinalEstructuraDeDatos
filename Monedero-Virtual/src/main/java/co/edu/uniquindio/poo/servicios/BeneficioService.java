package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.hash.TablaHash;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.excepciones.MonederoException;
import co.edu.uniquindio.poo.modelo.BeneficioActivo;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.enums.TipoBeneficio;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.util.Constantes;
import co.edu.uniquindio.poo.util.RelojSimulado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Canje de puntos por beneficios.
 * TablaHash clienteId → lista de beneficios canjeados.
 * Implementa ProveedorDescuentos para que TransaccionService aplique los descuentos.
 */
public class BeneficioService implements ProveedorDescuentos {

    private final TablaHash<String, ListaEnlazada<BeneficioActivo>> activos = new TablaHash<>();

    private final PuntosService puntosService;
    private final TransaccionService transaccionService;
    private final ClienteService clienteService;
    private final NotificacionService notificacionService;
    private final RelojSimulado reloj;

    public BeneficioService(PuntosService puntosService, TransaccionService transaccionService,
                            ClienteService clienteService, NotificacionService notificacionService,
                            RelojSimulado reloj) {
        if (puntosService == null || transaccionService == null || clienteService == null
                || notificacionService == null || reloj == null) {
            throw new IllegalArgumentException("Los servicios son obligatorios");
        }
        this.puntosService = puntosService;
        this.transaccionService = transaccionService;
        this.clienteService = clienteService;
        this.notificacionService = notificacionService;
        this.reloj = reloj;
    }

    public TipoBeneficio[] catalogo() {
        return TipoBeneficio.values();
    }

    /** Para habilitar o deshabilitar el botón "Canjear" en la interfaz. */
    public boolean puedeCanjear(String clienteId, TipoBeneficio tipo) {
        boolean alcanza = puntosService.obtenerRegistro(clienteId).puedeCanjear(tipo.getCostoPuntos());
        boolean yaActivo = tipo.esTemporal() && tieneVigente(clienteId, tipo);
        return alcanza && !yaActivo;
    }

    /**
     * Canjea un beneficio. Primero valida TODO; después descuenta puntos; al final entrega.
     * Si algo falla en la validación, el cliente no pierde puntos.
     *
     * @param monederoIdBono monedero donde se acredita el bono (solo para BONO_SALDO)
     */
    public void canjear(String clienteId, TipoBeneficio tipo, String monederoIdBono) {
        if (tipo == null) {
            throw new IllegalArgumentException("Elige un beneficio");
        }

        // 1. Validar
        if (tipo == TipoBeneficio.BONO_SALDO) {
            if (monederoIdBono == null) {
                throw new MonederoException("Elige el monedero donde quieres recibir el bono");
            }
            Monedero monedero = clienteService.obtenerMonedero(monederoIdBono);
            if (!monedero.getClienteId().equals(clienteId)) {
                throw new MonederoException("Ese monedero no es tuyo");
            }
        } else if (tieneVigente(clienteId, tipo)) {
            throw new MonederoException("Ya tienes activo: " + tipo.getNombre());
        }

        // 2. Descontar puntos (lanza PuntosInsuficientesException si no alcanzan)
        puntosService.canjear(clienteId, tipo.getCostoPuntos(), "Canje: " + tipo.getNombre());

        // 3. Entregar el beneficio
        if (tipo == TipoBeneficio.BONO_SALDO) {
            transaccionService.acreditarBono(monederoIdBono, Constantes.BONO_SALDO);
        } else {
            LocalDateTime ahora = reloj.ahora();
            listaDe(clienteId).agregarFinal(new BeneficioActivo(
                    clienteId, tipo, ahora, ahora.plusDays(tipo.getDiasVigencia())));
        }

        notificacionService.crearNotificacion(clienteId, TipoNotificacion.BENEFICIO,
                "Canjeaste " + tipo.getCostoPuntos() + " puntos por: " + tipo.getNombre());
    }

    public boolean tieneVigente(String clienteId, TipoBeneficio tipo) {
        ListaEnlazada<BeneficioActivo> lista = activos.obtener(clienteId);
        if (lista == null) {
            return false;
        }
        LocalDateTime ahora = reloj.ahora();
        for (BeneficioActivo beneficio : lista) {
            if (beneficio.getTipo() == tipo && beneficio.estaVigente(ahora)) {
                return true;
            }
        }
        return false;
    }

    public ListaEnlazada<BeneficioActivo> beneficiosVigentes(String clienteId) {
        ListaEnlazada<BeneficioActivo> vigentes = new ListaEnlazada<>();
        ListaEnlazada<BeneficioActivo> lista = activos.obtener(clienteId);
        if (lista == null) {
            return vigentes;
        }
        LocalDateTime ahora = reloj.ahora();
        for (BeneficioActivo beneficio : lista) {
            if (beneficio.estaVigente(ahora)) {
                vigentes.agregarFinal(beneficio);
            }
        }
        return vigentes;
    }

    // =========================================================== ProveedorDescuentos

    @Override
    public BigDecimal descuentoComisionTransferencia(String clienteId) {
        return tieneVigente(clienteId, TipoBeneficio.DESCUENTO_TRANSFERENCIAS)
                ? Constantes.DESCUENTO_BENEFICIO_TRANSFERENCIA
                : BigDecimal.ZERO;
    }

    @Override
    public boolean retiroSinComision(String clienteId) {
        return tieneVigente(clienteId, TipoBeneficio.RETIROS_SIN_CARGO);
    }

    // =========================================================== interno

    private ListaEnlazada<BeneficioActivo> listaDe(String clienteId) {
        ListaEnlazada<BeneficioActivo> lista = activos.obtener(clienteId);
        if (lista == null) {
            lista = new ListaEnlazada<>();
            activos.insertar(clienteId, lista);
        }
        return lista;
    }
}