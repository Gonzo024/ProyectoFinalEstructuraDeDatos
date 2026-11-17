package co.edu.uniquindio.poo.servicios;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.enums.CategoriaGasto;
import co.edu.uniquindio.poo.modelo.enums.TipoBeneficio;
import co.edu.uniquindio.poo.modelo.enums.TipoMonedero;
import co.edu.uniquindio.poo.util.Dinero;

/**
 * Datos de demostracion para la sustentacion.
 * <p>
 * Se cargan una sola vez al abrir la aplicacion (ver ContextoSesion). Las pruebas
 * unitarias crean su propio ContextoMonedero, asi que no se ven afectadas.
 * <p>
 * Todo se crea con los servicios reales (depositos, pagos, transferencias, canjes),
 * no se escriben saldos ni puntos a mano. Por eso los puntos, los rangos, los grafos,
 * el historial y las notificaciones quedan coherentes y la verificacion de integridad
 * da correcta.
 * <p>
 * Resultado (los IDs salen en este orden):
 * <ul>
 *   <li>C1 Ana Torres - Platino. Monederos M1 Principal, M2 Ahorros, M3 Gastos diarios.</li>
 *   <li>C2 Beto Ramirez - Oro. Monedero M4 Principal.</li>
 *   <li>C3 Carla Gomez - Plata. Monedero M5 Principal.</li>
 *   <li>C4 David Lopez - Bronce. Monederos M6 Principal y M7 Gastos diarios (saldo 150,
 *       cerca del limite de saldo bajo).</li>
 * </ul>
 * Todos los clientes tienen el PIN 1234.
 */
public final class DatosDemostracion {

    private static final String PIN = "1234";

    private DatosDemostracion() {
        // Solo tiene un metodo estatico.
    }

    public static void cargar(ContextoMonedero contexto) {
        ClienteService clientes = contexto.getClienteService();
        TransaccionService transacciones = contexto.getTransaccionService();
        BeneficioService beneficios = contexto.getBeneficioService();

        // ------------------------------------------------ clientes y monederos
        // El orden importa: asi los IDs quedan C1..C4 y M1..M7.

        Cliente ana = clientes.registrarCliente("Ana Torres", "ana@demo.com", PIN);
        String anaPrincipal = principal(clientes, ana);
        String anaAhorros = clientes.crearMonedero(ana.getId(), "Ahorros", TipoMonedero.AHORROS).getId();
        String anaGastos = clientes.crearMonedero(ana.getId(), "Gastos diarios", TipoMonedero.GASTOS_DIARIOS).getId();

        Cliente beto = clientes.registrarCliente("Beto Ramirez", "beto@demo.com", PIN);
        String betoPrincipal = principal(clientes, beto);

        Cliente carla = clientes.registrarCliente("Carla Gomez", "carla@demo.com", PIN);
        String carlaPrincipal = principal(clientes, carla);

        Cliente david = clientes.registrarCliente("David Lopez", "david@demo.com", PIN);
        String davidPrincipal = principal(clientes, david);
        String davidGastos = clientes.crearMonedero(david.getId(), "Gastos diarios", TipoMonedero.GASTOS_DIARIOS).getId();

        // ------------------------------------------------ depositos iniciales (dan puntos)

        transacciones.depositar(anaPrincipal, Dinero.de(520_000), "Salario");          // 5200 pts -> Platino
        transacciones.depositar(betoPrincipal, Dinero.de(150_000), "Salario");         // 1500 pts -> Oro
        transacciones.depositar(carlaPrincipal, Dinero.de(50_000), "Mesada");          // 500 pts  -> Bronce
        transacciones.depositar(davidPrincipal, Dinero.de(20_000), "Ahorro inicial");  // 200 pts  -> Bronce

        // ------------------------------------------------ multimonedero (grafo entre monederos)

        transacciones.transferirEntreMisMonederos(anaPrincipal, anaAhorros, Dinero.de(100_000), "Ahorro del mes");
        transacciones.transferirEntreMisMonederos(anaPrincipal, anaGastos, Dinero.de(50_000), "Gastos de la semana");
        transacciones.transferirEntreMisMonederos(anaAhorros, anaGastos, Dinero.de(10_000), "Refuerzo de gastos");
        transacciones.transferirEntreMisMonederos(davidPrincipal, davidGastos, Dinero.de(150), "Para el bus");

        // ------------------------------------------------ canjes (antes de transferir, para ver el descuento)

        beneficios.canjear(ana.getId(), TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);
        beneficios.canjear(beto.getId(), TipoBeneficio.RETIROS_SIN_CARGO, null);

        // ------------------------------------------------ transferencias entre clientes (grafo de clientes)
        // Ana -> Beto -> Carla -> Ana forma un ciclo; David solo recibe.

        transacciones.transferir(anaPrincipal, betoPrincipal, Dinero.de(30_000), "Pago de arriendo compartido");
        transacciones.transferir(betoPrincipal, carlaPrincipal, Dinero.de(20_000), "Prestamo");
        transacciones.transferir(carlaPrincipal, anaPrincipal, Dinero.de(10_000), "Devolucion");   // Carla -> Plata
        transacciones.transferir(anaPrincipal, davidPrincipal, Dinero.de(15_000), "Regalo");
        transacciones.transferir(betoPrincipal, davidPrincipal, Dinero.de(5_000), "Almuerzo");

        // ------------------------------------------------ pagos por categoria (reporte de gastos)

        transacciones.pagar(anaGastos, Dinero.de(12_000), CategoriaGasto.ALIMENTACION, "Mercado");
        transacciones.pagar(anaGastos, Dinero.de(5_000), CategoriaGasto.TRANSPORTE, "Taxi");
        transacciones.pagar(anaGastos, Dinero.de(8_000), CategoriaGasto.ALIMENTACION, "Restaurante");
        transacciones.pagar(anaPrincipal, Dinero.de(80_000), CategoriaGasto.VIVIENDA, "Arriendo");
        transacciones.pagar(anaPrincipal, Dinero.de(20_000), CategoriaGasto.ENTRETENIMIENTO, "Cine y comida");

        transacciones.pagar(betoPrincipal, Dinero.de(30_000), CategoriaGasto.SERVICIOS, "Luz y agua");
        transacciones.pagar(betoPrincipal, Dinero.de(10_000), CategoriaGasto.EDUCACION, "Curso en linea");

        transacciones.pagar(carlaPrincipal, Dinero.de(5_000), CategoriaGasto.ALIMENTACION, "Almuerzo");

        transacciones.pagar(davidPrincipal, Dinero.de(8_000), CategoriaGasto.SALUD, "Farmacia");

        // ------------------------------------------------ retiros

        transacciones.retirar(betoPrincipal, Dinero.de(10_000), "Cajero");   // sin comision por el canje
        transacciones.retirar(anaAhorros, Dinero.de(5_000), "Cajero");

        // Asegura que todos aparezcan en la clasificacion de puntos.
        contexto.getPuntosService().sincronizarClientes();
    }

    private static String principal(ClienteService clientes, Cliente cliente) {
        Monedero monedero = clientes.monederosDe(cliente.getId()).obtenerValorNodo(0);
        return monedero.getId();
    }
}
