package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.estructuras.arbol.ArbolAVL;
import co.edu.uniquindio.poo.estructuras.lista.ListaEnlazada;
import co.edu.uniquindio.poo.modelo.ClavePuntos;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.enums.Rango;
import co.edu.uniquindio.poo.modelo.enums.TipoNotificacion;

/**
 * Clasifica a los clientes en Bronce, Plata, Oro y Platino.
 * <p>
 * Usa un ArbolAVL con clave (puntos acumulados, clienteId) y valor clienteId.
 * Como los puntos casi siempre suben, un ABB simple degeneraría en lista;
 * el AVL mantiene la altura en O(log n).
 * <p>
 * Un árbol de búsqueda no permite cambiar la clave de un nodo en su sitio:
 * para actualizar se elimina la clave vieja y se inserta la nueva.
 */
public class RangoService {

    private final ArbolAVL<ClavePuntos, String> arbol = new ArbolAVL<>();
    private final ClienteService clienteService;
    private final NotificacionService notificacionService;

    public RangoService(ClienteService clienteService, NotificacionService notificacionService) {
        if (clienteService == null || notificacionService == null) {
            throw new IllegalArgumentException("Los servicios son obligatorios");
        }
        this.clienteService = clienteService;
        this.notificacionService = notificacionService;
    }

    /**
     * Mueve al cliente dentro del árbol y actualiza su rango. O(log n).
     *
     * @param claveAnterior clave que tenía en el árbol (null si es la primera vez)
     * @param claveNueva    clave con sus puntos acumulados actuales
     * @return el rango que quedó
     */
    public Rango actualizar(String clienteId, ClavePuntos claveAnterior, ClavePuntos claveNueva) {
        if (claveAnterior != null) {
            arbol.eliminar(claveAnterior);
        }
        arbol.insertar(claveNueva, clienteId);

        Cliente cliente = clienteService.obtenerCliente(clienteId);
        Rango anterior = cliente.getRango();
        Rango nuevo = Rango.desdePuntos(claveNueva.getPuntos());

        if (nuevo != anterior) {
            cliente.setRango(nuevo);
            String accion = nuevo.ordinal() > anterior.ordinal() ? "Subiste" : "Bajaste";
            notificacionService.crearNotificacion(clienteId, TipoNotificacion.CAMBIO_RANGO,
                    accion + " de " + anterior.getNombre() + " a " + nuevo.getNombre());
        }
        return nuevo;
    }

    /**
     * Ids de los clientes de un rango, de menos a más puntos.
     * Usa la búsqueda por intervalo del árbol: O(log n + k).
     */
    public ListaEnlazada<String> clientesDeRango(Rango rango) {
        return arbol.rango(
                ClavePuntos.desde(rango.getPuntosMinimos()),
                ClavePuntos.hasta(rango.getPuntosMaximos()));
    }

    public int contarClientes(Rango rango) {
        return clientesDeRango(rango).getTamano();
    }

    public int cantidadClientes() {
        return arbol.tamanio();
    }

    /** Para mostrar en la interfaz y en las pruebas que el árbol sigue balanceado. */
    public int altura() {
        return arbol.altura();
    }

    public boolean estaBalanceado() {
        return arbol.estaBalanceado();
    }
}