# DataStock - UT2 (Práctica Integradora: Acceso a Datos con JDBC y SQLite)

DataStock es una aplicación de consola en Java desarrollada con Maven para la gestión integral de inventarios. En esta segunda entrega (**UT2**), el proyecto evoluciona para incorporar persistencia relacional mediante **JDBC y MySQL**, manteniendo intactas todas las funcionalidades en memoria de la **UT1** e incorporando una actividad comparativa independiente con **SQLite**.

---

## 1. Requisitos del Sistema y Dependencias

* **Java Development Kit (JDK):** Versión 21 recomendada (compatible con JDK 17+).
* **Apache Maven:** Versión 3.8 o superior.
* **MySQL Server:** Versión 8.0.16 o posterior.
* **Dependencias Maven (`pom.xml`):**
  * **MySQL Connector/J:** Versión `8.3.0` (o superior compatible).
  * **SQLite JDBC Driver (`org.xerial:sqlite-jdbc`):** Versión `3.45.1.0` (para la actividad comparativa).

---

## 2. Configuración sin Secretos

Para evitar exponer credenciales en el repositorio público, la aplicación lee los parámetros de conexión mediante un fichero de propiedades local o mediante variables de entorno (`JDBC_URL`, `JDBC_USER`, `JDBC_PASSWORD`).

1. En la ruta `src/main/resources/`, crea un fichero llamado `database.properties` (este archivo está excluido en el `.gitignore` y **nunca** debe subirse a Git).
2. Utiliza la siguiente estructura basada en la plantilla de ejemplo:

   ```properties
   db.url=jdbc:mysql://localhost:3306/datastock?useSSL=false&serverTimezone=UTC
   db.user=tu_usuario_mysql
   db.password=tu_contrasena_segura
   ```

---

## 3. Instalación del Esquema de Base de Datos (MySQL)

El script oficial se encuentra en la carpeta de referencia:
* **Ruta:** `material_referencia/DataStock_BD_MySQL.sql`

Para desplegar el esquema:
1. Accede a tu gestor MySQL (por ejemplo, MySQL Workbench o consola).
2. Ejecuta el script para crear la base de datos `datastock`, sus cuatro tablas (`categoria`, `proveedor`, `producto`, `movimiento_stock`) y el procedimiento almacenado `registrar_movimiento`:
   ```bash
   mysql -u tu_usuario -p < material_referencia/DataStock_BD_MySQL.sql
   ```
*Nota: Las restricciones de integridad (`PRIMARY KEY`, `FOREIGN KEY`, `CHECK`, `UNIQUE`) definidas en el script original se respetan estrictamente desde Java.*

---

## 4. Compilación y Ejecución de la Aplicación

Desde la raíz del módulo (`DataStock-UT2`):

1. **Compilar el proyecto:**
   ```bash
   mvn clean compile
   ```

2. **Ejecutar la aplicación principal (DataStock):**
   ```bash
   mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.App"
   ```

3. **Ejecutar la actividad independiente de SQLite:**
   ```bash
   mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.complementarias.SQLiteComparacionActividad"
   ```

---

## 5. Estructura de Menús de la Aplicación

La interfaz de consola respeta la continuidad de la UT1 y añade el nuevo menú de gestión JDBC:

* **Menú Principal:**
  1. Explorar ficheros y directorios (UT1)
  2. Importar productos desde JSON (UT1)
  3. Importar productos desde XML (UT1)
  4. Consultar productos en memoria (UT1)
  5. Exportar productos (UT1)
  6. Guardar y recuperar inventario - Serialización (UT1)
  7. **Gestionar base de datos MySQL (UT2)**
  0. Salir

* **Submenú MySQL (Opción 7):**
  1. Comprobar conexión y mostrar información de la BD (`RF-JDBC-01`)
  2. Gestionar categorías (Alta, listar, modificar, baja) (`RF-JDBC-02`)
  3. Gestionar proveedores (Alta, listar, modificar, baja) (`RF-JDBC-03`)
  4. Gestionar productos - CRUD (`RF-JDBC-03`)
  5. Consultar y buscar productos (Listados, código, nombre LIKE, umbral stock) (`RF-JDBC-04`)
  6. Registrar entrada o salida de stock (vía `CallableStatement`) (`RF-JDBC-05`)
  7. Consultar historial de movimientos (`RF-JDBC-06`)
  8. Importar productos de memoria a MySQL (con transacción por lotes) (`RF-JDBC-07`)
  9. Demostración de transacción con commit/rollback (`RF-JDBC-08`)
  0. Volver al menú principal

---

## 6. Arquitectura y Estructura del Código

El código fuente se organiza en paquetes limpios con responsabilidades bien definidas:
* `es.iesisidra.datastock`: Clase principal de arranque (`App.java`).
* `es.iesisidra.datastock.config`: Configuración y gestión de conexiones JDBC (`ConexionBD.java`).
* `es.iesisidra.datastock.modelo`: Clases de entidad (`Categoria`, `Proveedor`, `Producto`, `MovimientoStock`).
* `es.iesisidra.datastock.servicio`: Lógica de negocio, DAOs y transacciones JDBC.
* `es.iesisidra.datastock.ui`: Interfaz de usuario por consola, menús y explorador de ficheros.
* `es.iesisidra.datastock.util`: Utilidades de consola, colores ANSI y lectura segura de datos.
* `es.iesisidra.datastock.complementarias`: Actividades independientes (ej. `SQLiteComparacionActividad.java`).

---

## 7. Diferencias Técnicas: Memoria / SQLite vs MySQL

### Comparativa de Motores de Persistencia

| Característica | MySQL | SQLite | Inventario en Memoria (UT1) |
| :--- | :--- | :--- | :--- |
| **¿Necesita servidor?** | Sí (Servidor independiente) | No (Motor embebido basado en ficheros) | No (Residente en RAM) |
| **Almacenamiento** | Tablas relacionales en servidor | Fichero local único o memoria RAM | Colecciones de objetos Java |
| **Conexión JDBC** | `jdbc:mysql://host:port/db` | `jdbc:sqlite:ruta/fichero.db` | N/A (Estructura interna Java) |
| **Dependencia Maven** | `mysql-connector-j` | `sqlite-jdbc` | Ninguna |
| **Ventajas** | Alta concurrencia, robustez, transacciones ACID completas. | Ligero, sin instalación previa, ideal para pruebas y apps locales. | Acceso inmediato en memoria, sin latencia de red/disco. |
| **Inconvenientes** | Requiere configuración de servidor y permisos. | Bloqueo a nivel de base de datos en escritura masiva. | Volátil (se pierde al cerrar la app si no se serializa). |
| **Caso de uso ideal** | Aplicaciones corporativas y multiusuario. | Aplicaciones de escritorio o móviles embebidas. | Caché temporal o procesamiento rápido de ficheros. |

### Nota sobre Conexiones Cortas y Pools de Conexiones
DataStock utiliza un modelo de conexiones acotadas (conexiones cortas abiertas y cerradas mediante `try-with-resources` en cada operación puntual). Esto es idóneo para aplicaciones de consola monousuario locales ya que evita mantener puertos y recursos de servidor ocupados innecesariamente. 
En entornos web o de alta concurrencia masiva, se emplea un **Pool de Conexiones** (como HikariCP), el cual mantiene un conjunto de conexiones reutilizables pre-creadas para reducir la latencia de apertura y cierre de sockets con la base de datos.