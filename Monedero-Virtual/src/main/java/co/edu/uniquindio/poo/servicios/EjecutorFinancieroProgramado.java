
package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoTransaccion;

public class EjecutorFinancieroProgramado
        implements ProcesadorProgramacionesService.EjecutorOperacion {

    private final ClienteService clientes;
    private final TransaccionService transacciones;

    public EjecutorFinancieroProgramado(
            ClienteService clientes,
            TransaccionService transacciones) {

        if (clientes == null || transacciones == null) {
            throw new IllegalArgumentException(
                    "Los servicios son obligatorios"
            );
        }

        this.clientes = clientes;
        this.transacciones = transacciones;
    }

    @Override
    public boolean ejecutar(TransaccionProgramada programacion) {

        if (programacion == null || !programacion.isActiva()) {
            return false;
        }

        TipoTransaccion tipo = programacion.getTipo();
        String descripcion = "Programacion " + programacion.getId();

        // DEPOSITO:
        // En JavaFX el monedero se guarda como destino,
        // porque el dinero entra a ese monedero.
        if (tipo == TipoTransaccion.DEPOSITO) {

            String monederoId =
                    programacion.getMonederoDestinoId();

            // Compatibilidad con programaciones anteriores
            // que guardaban el monedero como origen.
            if (monederoId == null || monederoId.isBlank()) {
                monederoId = programacion.getMonederoOrigenId();
            }

            if (monederoId == null || monederoId.isBlank()) {
                return false;
            }

            Monedero destino = clientes.obtenerMonedero(
                    monederoId
            );

            // El deposito solo puede ir al monedero
            // perteneciente al cliente de la programacion.
            if (!destino.getClienteId().equals(
                    programacion.getClienteOrigenId())) {
                return false;
            }

            if (programacion.getClienteDestinoId() != null
                    && !destino.getClienteId().equals(
                            programacion.getClienteDestinoId())) {
                return false;
            }

            transacciones.depositar(
                    destino.getId(),
                    programacion.getMonto(),
                    descripcion
            );

            return true;
        }

        // Las demas operaciones necesitan un origen.
        String monederoOrigenId =
                programacion.getMonederoOrigenId();

        if (monederoOrigenId == null
                || monederoOrigenId.isBlank()) {
            return false;
        }

        Monedero origen = clientes.obtenerMonedero(
                monederoOrigenId
        );

        if (!origen.getClienteId().equals(
                programacion.getClienteOrigenId())) {
            return false;
        }

        // PAGO
        if (tipo == TipoTransaccion.PAGO) {

            // La programacion no guarda categoria;
            // mientras tanto usamos OTROS.
            transacciones.pagar(
                    origen.getId(),
                    programacion.getMonto(),
                    CategoriaGasto.OTROS,
                    descripcion
            );

            return true;
        }

        // TRANSFERENCIAS
        if (tipo == TipoTransaccion.TRANSFERENCIA
                || tipo == TipoTransaccion.TRANSFERENCIA_INTERNA) {

            String monederoDestinoId =
                    programacion.getMonederoDestinoId();

            if (monederoDestinoId == null
                    || monederoDestinoId.isBlank()) {
                return false;
            }

            Monedero destino = clientes.obtenerMonedero(
                    monederoDestinoId
            );

            if (!destino.getClienteId().equals(
                    programacion.getClienteDestinoId())) {
                return false;
            }

            if (tipo == TipoTransaccion.TRANSFERENCIA) {

                transacciones.transferir(
                        origen.getId(),
                        destino.getId(),
                        programacion.getMonto(),
                        descripcion
                );

                return true;
            }

            transacciones.transferirEntreMisMonederos(
                    origen.getId(),
                    destino.getId(),
                    programacion.getMonto(),
                    descripcion
            );

            return true;
        }

        return false;
    }
}
