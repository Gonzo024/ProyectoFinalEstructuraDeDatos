
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.cola.ColaPrioridad;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.util.RelojSimulado;

public class ProgramacionService {

    private ColaPrioridad<TransaccionProgramada> pendientes;
    private RelojSimulado reloj;

    public ProgramacionService(RelojSimulado reloj) {
        this.reloj = reloj;
        this.pendientes = new ColaPrioridad<>();
    }

    public void programar(TransaccionProgramada transaccion) {

        if (transaccion == null) {
            throw new IllegalArgumentException(
                "La transaccion no puede ser nula"
            );
        }

        if (!transaccion.isActiva()) {
            throw new IllegalArgumentException(
                "La transaccion esta cancelada"
            );
        }

        if (!transaccion.getFechaProgramada().isAfter(reloj.ahora())) {
            throw new IllegalArgumentException(
                "La fecha debe ser futura"
            );
        }

        pendientes.insertar(transaccion);
    }

    public boolean hayTransaccionesVencidas() {

        if (pendientes.estaVacia()) {
            return false;
        }

        TransaccionProgramada siguiente = pendientes.primero();

        return !siguiente.getFechaProgramada().isAfter(
            reloj.ahora()
        );
    }

    public TransaccionProgramada retirarSiguienteVencida() {

        if (!hayTransaccionesVencidas()) {
            return null;
        }

        return pendientes.extraer();
    }

    public void reprogramar(TransaccionProgramada transaccion) {

        if (transaccion == null || !transaccion.isActiva()) {
            return;
        }

        transaccion.actualizarFecha();

        if (transaccion.isActiva()) {

            while (!transaccion.getFechaProgramada()
                    .isAfter(reloj.ahora())) {
                transaccion.actualizarFecha();
            }

            pendientes.insertar(transaccion);
        }
    }

    public TransaccionProgramada consultarSiguiente() {

        if (pendientes.estaVacia()) {
            return null;
        }

        return pendientes.primero();
    }

    public int cantidadPendientes() {
        return pendientes.tamanio();
    }


public boolean cancelarTransaccion(String id) {

    if (id == null || id.isBlank()) {
        return false;
    }

    ColaPrioridad<TransaccionProgramada> auxiliar =
        new ColaPrioridad<>();

    boolean encontrada = false;

    while (!pendientes.estaVacia()) {

        TransaccionProgramada actual =
            pendientes.extraer();

        if (actual.getId().equals(id)) {

            actual.cancelar();
            encontrada = true;

        } else {

            auxiliar.insertar(actual);
        }
    }

    pendientes = auxiliar;

    return encontrada;
}

}
