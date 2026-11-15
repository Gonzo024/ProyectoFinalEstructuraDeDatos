
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

        // Comprobamos que el monedero de origen
        // realmente pertenezca al cliente indicado.
        Monedero origen = clientes.obtenerMonedero(
                programacion.getMonederoOrigenId()
        );

        if (!origen.getClienteId().equals(
                programacion.getClienteOrigenId())) {
            return false;
        }

        TipoTransaccion tipo = programacion.getTipo();

        String descripcion = "Programacion "
                + programacion.getId();

        if (tipo == TipoTransaccion.DEPOSITO) {

            transacciones.depositar(
                    origen.getId(),
                    programacion.getMonto(),
                    descripcion
            );

            return true;
        }

        if (tipo == TipoTransaccion.PAGO) {

            // El modelo de programaciones aun no guarda
            // la categoria elegida para el gasto.
            transacciones.pagar(
                    origen.getId(),
                    programacion.getMonto(),
                    CategoriaGasto.OTROS,
                    descripcion
            );

            return true;
        }

        if (tipo == TipoTransaccion.TRANSFERENCIA
                || tipo == TipoTransaccion.TRANSFERENCIA_INTERNA) {

            Monedero destino = clientes.obtenerMonedero(
                    programacion.getMonederoDestinoId()
            );

            // Evita transferir a un monedero diferente
            // del cliente destino indicado.
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

            } else {

                transacciones.transferirEntreMisMonederos(
                        origen.getId(),
                        destino.getId(),
                        programacion.getMonto(),
                        descripcion
                );
            }

            return true;
        }

        return false;
    }
}
