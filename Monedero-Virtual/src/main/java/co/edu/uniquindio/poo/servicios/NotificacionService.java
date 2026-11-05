
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.cola.Cola;
import co.edu.uniquindio.poo.estructuras.lista.ListaCircular;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.Notificacion;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.poo.util.RelojSimulado;

public class NotificacionService {

    private Cola<Notificacion> pendientes;
    private ListaCircular<Notificacion> notificaciones;
    private RelojSimulado reloj;
    private int contador;

    public NotificacionService(RelojSimulado reloj) {
        this.reloj = reloj;
        this.pendientes = new Cola<>();
        this.notificaciones = new ListaCircular<>();
        this.contador = 0;
    }

    public void crearNotificacion(String clienteId,
                                  TipoNotificacion tipo,
                                  String mensaje) {

        if (clienteId == null || clienteId.isBlank()
                || tipo == null || mensaje == null
                || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                "Los datos de la notificacion son obligatorios"
            );
        }

        contador++;

        Notificacion nueva = new Notificacion(
            "N" + contador,
            clienteId,
            tipo,
            mensaje,
            reloj.ahora()
        );

        pendientes.encolar(nueva);
    }

    public void entregarNotificaciones() {

        while (!pendientes.estaVacia()) {

            Notificacion actual = pendientes.desencolar();

            notificaciones.agregar(actual);
        }
    }

    public ListaEnlazada<Notificacion> consultarNotificaciones(
            String clienteId) {

        ListaEnlazada<Notificacion> resultado =
            new ListaEnlazada<>();

        for (int i = 0; i < notificaciones.tamanio(); i++) {

            Notificacion actual = notificaciones.obtener(i);

            if (actual.getClienteId().equals(clienteId)) {
                resultado.agregarFinal(actual);
            }
        }

        return resultado;
    }

    public int contarNoLeidas(String clienteId) {

        int cantidad = 0;

        for (int i = 0; i < notificaciones.tamanio(); i++) {

            Notificacion actual = notificaciones.obtener(i);

            if (actual.getClienteId().equals(clienteId)
                    && !actual.isLeida()) {
                cantidad++;
            }
        }

        return cantidad;
    }

    public boolean marcarComoLeida(String id) {

        for (int i = 0; i < notificaciones.tamanio(); i++) {

            Notificacion actual = notificaciones.obtener(i);

            if (actual.getId().equals(id)) {
                actual.marcarComoLeida();
                return true;
            }
        }

        return false;
    }

    public int cantidadPendientes() {
        return pendientes.tamanio();
    }
}

