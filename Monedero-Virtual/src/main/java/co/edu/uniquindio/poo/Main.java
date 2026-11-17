package co.edu.uniquindio.poo;

import co.edu.uniquindio.poo.vista.PrincipalApp;
import javafx.application.Application;

/**
 * Lanzador de la aplicacion.
 * <p>
 * La aplicacion es PrincipalApp. Esta clase existe porque, si se ejecuta directamente
 * una clase que extiende Application, Java exige que JavaFX este en el module path y
 * muestra "JavaFX runtime components are missing". Como Main NO extiende Application,
 * esa revision no se hace y se puede ejecutar con el boton Run de IntelliJ.
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(PrincipalApp.class, args);
    }
}
