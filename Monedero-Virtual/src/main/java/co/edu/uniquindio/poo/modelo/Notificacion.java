
package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;
import java.time.LocalDateTime;

public class Notificacion {

    private String id;
    private String clienteId;
    private TipoNotificacion tipo;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;

    public Notificacion(String id, String clienteId,
                        TipoNotificacion tipo, String mensaje,
                        LocalDateTime fecha) {

        this.id = id;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leida = false;
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void marcarComoLeida() {
        leida = true;
    }

    @Override
    public String toString() {
        return tipo.getNombre() + ": " + mensaje;
    }
}
