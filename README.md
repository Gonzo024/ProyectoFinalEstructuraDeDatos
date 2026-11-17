# Monedero Virtual con sistema de puntos

Proyecto final de **Estructuras de Datos** — Universidad del Quindío.

Aplicación de escritorio en Java con JavaFX que simula un monedero virtual: los clientes
depositan, retiran, transfieren y pagan, manejan varios monederos, programan transacciones
y acumulan puntos que los suben de rango y que pueden canjear por beneficios.

Todas las estructuras de datos fueron implementadas por el grupo, sin usar las colecciones de Java.

## Integrantes

| Integrante | Módulo |
|---|---|
| Isabel Angulo Martinez | 1. Mi monedero: clientes, monederos, operaciones, historial y reversiones |
| Santiago Gonzalez Rincon | 2. Puntos y rangos: puntos, rangos de cliente y canje de beneficios |
| Isabela Arenas Rincon | 3. Planeación y análisis: transacciones programadas, notificaciones y análisis de gastos |

Docente: Robinson Arias Muñoz

## Requisitos

- JDK 24
- Maven (o IntelliJ IDEA, que ya lo incluye)

## Cómo ejecutar

Desde la carpeta `Monedero-Virtual`:

```bash
mvn javafx:run
```

En IntelliJ IDEA: panel **Maven** → `Monedero-Virtual` → `Plugins` → `javafx` → `javafx:run`.

> La aplicación se debe abrir con `javafx:run`. Si se ejecuta `PrincipalApp` directamente con
> el botón de Run, Java muestra el error "JavaFX runtime components are missing".

También se puede abrir con el botón **Run** de IntelliJ sobre la clase `Main`.

Los datos se guardan en memoria: al cerrar la aplicación se borran. Al abrirla se cargan
datos de demostración (cuatro clientes, uno en cada rango).

## Inicio de sesión

| Usuario | Contraseña | Qué puede hacer |
|---|---|---|
| `admin` | `admin123` | Ve y opera sobre todos los clientes, maneja el reloj y ejecuta las programaciones. En el módulo 1, pestaña **Administrar clientes**, actualiza los datos de los clientes y desactiva o reactiva sus cuentas. |
| `ana@demo.com` (Platino) | `1234` | Solo ve y usa sus propios monederos, puntos, programaciones y notificaciones. |
| `beto@demo.com` (Oro) | `1234` | Igual que Ana. |
| `carla@demo.com` (Plata) | `1234` | Igual que Ana. |
| `david@demo.com` (Bronce) | `1234` | Igual que Ana. |

Desde la pantalla de inicio de sesión también se puede crear una cuenta nueva.

Una cuenta desactivada no puede iniciar sesión ni hacer o recibir operaciones, y deja de aparecer
en las listas de clientes. No se borra nada: su historial se conserva y se puede reactivar.

## Cómo ejecutar las pruebas

```bash
mvn test
```

## Estructuras de datos

| Estructura | Para qué se usa |
|---|---|
| Lista enlazada | Historial de transacciones y de puntos |
| Pila | Transacciones reversibles |
| Cola | Notificaciones pendientes |
| Cola de prioridad (heap) | Transacciones programadas, ordenadas por fecha |
| Lista circular | Notificaciones entregadas |
| Árbol binario de búsqueda | Clasificación de clientes por puntos |
| Árbol AVL | Rangos de los clientes (Bronce, Plata, Oro, Platino) |
| Tabla hash | Acceso rápido a clientes, monederos y puntos |
| Grafo dirigido | Movimientos entre los monederos de un cliente |

Además, la verificación de integridad de las transacciones usa algoritmos recursivos.

## Organización del código

```
Monedero-Virtual/src/main/java/co/edu/uniquindio/poo/
├── estructuras/   Estructuras de datos propias
├── modelo/        Cliente, Monedero, Transaccion, RegistroPuntos...
├── servicios/     Reglas de negocio (transacciones, puntos, rangos, programaciones...)
├── vista/         Controladores JavaFX
├── excepciones/   Errores del sistema
└── util/          Constantes, manejo de dinero y reloj simulado

Monedero-Virtual/src/main/resources/   Pantallas (FXML) y estilos (CSS)
Monedero-Virtual/src/test/java/        Pruebas unitarias (JUnit)
```
