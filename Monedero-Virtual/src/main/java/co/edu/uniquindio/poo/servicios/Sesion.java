package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.Cliente;

/**
 * Quien esta usando la aplicacion en este momento.
 * <p>
 * - ADMINISTRADOR: ve y opera sobre todos los clientes (como el operador del sistema).
 * - CLIENTE: solo ve y opera sobre sus propios monederos.
 * <p>
 * Es unica para toda la aplicacion (igual que ContextoSesion): las pantallas la consultan
 * para saber que mostrar y que permitir.
 */
public final class Sesion {

    public enum Rol {
        ADMINISTRADOR,
        CLIENTE
    }

    private static Rol rol;
    private static Cliente cliente;

    private Sesion() {
        // Solo tiene metodos estaticos.
    }

    public static void iniciarComoAdministrador() {
        rol = Rol.ADMINISTRADOR;
        cliente = null;
    }

    public static void iniciarComoCliente(Cliente quien) {
        if (quien == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        rol = Rol.CLIENTE;
        cliente = quien;
    }

    public static void cerrar() {
        rol = null;
        cliente = null;
    }

    public static boolean estaIniciada() {
        return rol != null;
    }

    public static boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public static boolean esCliente() {
        return rol == Rol.CLIENTE;
    }

    /** El cliente que inicio sesion, o null si es el administrador o no hay sesion. */
    public static Cliente getCliente() {
        return cliente;
    }

    public static Rol getRol() {
        return rol;
    }

    /**
     * Texto que cambia cuando cambia la persona conectada.
     * Las pantallas lo usan para saber si deben limpiarse al volver a abrirse.
     */
    public static String identificador() {
        if (rol == null) {
            return "";
        }
        return rol == Rol.CLIENTE ? "CLIENTE:" + cliente.getId() : "ADMINISTRADOR";
    }

    /** Para mostrar en la pantalla de inicio. */
    public static String descripcion() {
        if (esAdministrador()) {
            return "Administrador";
        }
        if (esCliente()) {
            return cliente.getNombre() + " (" + cliente.getId() + ")";
        }
        return "Sin sesion";
    }
}
