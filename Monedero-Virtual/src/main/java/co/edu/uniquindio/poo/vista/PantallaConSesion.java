package co.edu.uniquindio.poo.vista;

/**
 * La implementan los controladores de los modulos.
 * PrincipalController llama a aplicarSesion() cada vez que abre el modulo, para que
 * la pantalla se ajuste a quien inicio sesion (administrador o cliente).
 */
public interface PantallaConSesion {

    void aplicarSesion();
}
