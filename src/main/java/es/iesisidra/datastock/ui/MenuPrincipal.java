
package es.iesisidra.datastock.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Optional;

import com.google.gson.JsonParseException;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ConsultaInventarioService;
import es.iesisidra.datastock.servicio.ConsultaProductoJdbc;
import es.iesisidra.datastock.servicio.ExportadorInventarioService;
import es.iesisidra.datastock.servicio.ImportadorXmlService;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.LectorProductosJson;
import es.iesisidra.datastock.servicio.SerializacionService;
import es.iesisidra.datastock.servicio.ServicioCategoria;
import es.iesisidra.datastock.servicio.ServicioConexionBD;
import es.iesisidra.datastock.servicio.ServicioConsultaProducto;
import es.iesisidra.datastock.servicio.ServicioMovimientoStock;
import es.iesisidra.datastock.servicio.ServicioProducto;
import es.iesisidra.datastock.servicio.ServicioProveedor;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Gestiona la presentación y la navegación de los menús de DataStock.
 *
 * La clase solicita los datos al usuario, delega las operaciones en los
 * componentes correspondientes y muestra los resultados por consola.
 * La lectura JSON se realiza mediante LectorProductosJson y la
 * búsqueda JDBC por código mediante ConsultaProductoJdbc.
 */
public class MenuPrincipal {

    /** Inventario compartido por los distintos menús de la aplicación. */
    private final Inventario inventario;

    /** Componente encargado de leer y validar productos desde JSON. */
    private final LectorProductosJson lectorProductosJson;

    /** Componente encargado de consultar productos en MySQL por código. */
    private final ConsultaProductoJdbc consultaProductoJdbc;

    private final ImportadorXmlService importadorXml;
    private final ConsultaInventarioService consultaService;
    private final ExportadorInventarioService exportadorService;
    private final SerializacionService serializacionService;

    /**
     * Construye el menú principal y prepara los componentes que utilizará.
     *
     * @param inventario inventario compartido durante la ejecución.
     */
    public MenuPrincipal(Inventario inventario) {
        this.inventario = inventario;
        this.lectorProductosJson = new LectorProductosJson();
        this.consultaProductoJdbc = new ConsultaProductoJdbc();
        this.importadorXml = new ImportadorXmlService(inventario);
        this.consultaService = new ConsultaInventarioService(inventario);
        this.exportadorService = new ExportadorInventarioService(inventario);
        this.serializacionService = new SerializacionService(inventario);
    }

    /**
     * Muestra el menú principal y ejecuta la opción seleccionada hasta que el
     * usuario elige salir.
     */
    public void iniciar() {
        int opcion;

        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            mostrarCabeceraUT2();
            System.out.println("7. Comprobar conexión y mostrar info de la BD");
            System.out.println("8. Gestión de Categorías");
            System.out.println("9. Gestión de Proveedores");
            System.out.println("10. Mantenimiento de Productos (CRUD)");
            System.out.println("11. Consultas y Búsquedas Avanzadas en BD");
            System.out.println("12. Registrar Movimiento de Stock");
            System.out.println("0. Salir");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 12);

            switch (opcion) {
                case 1 -> {
                    String ruta = LectorConsola.leerTexto(
                            "Introduce la ruta a explorar: ");
                    ExploradorFicheros.explorarRuta(ruta);
                    LectorConsola.pausar();
                }
                case 2 -> {
                    importarProductosJson();
                    LectorConsola.pausar();
                }
                case 3 -> {
                    String rutaXml = LectorConsola.leerTexto(
                            "Introduce la ruta del XML: ");
                    importadorXml.importarDesdeXml(rutaXml);
                    LectorConsola.pausar();
                }
                case 4 -> menuConsultas();
                case 5 -> menuExportaciones();
                case 6 -> menuSerializacion();
                case 7 -> {
                    ServicioConexionBD.comprobarConexionInformacion();
                    LectorConsola.pausar();
                }
                case 8 -> menuCategorias();
                case 9 -> menuProveedores();
                case 10 -> menuProductos();
                case 11 -> menuConsultasBd();
                case 12 -> {
                    ServicioMovimientoStock.registrarMovimientoStock();
                    LectorConsola.pausar();
                }
                case 0 -> System.out.println(
                        ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    /**
     * Solicita un fichero JSON, delega su lectura y validación y añade al
     * inventario los productos válidos que devuelve el componente.
     *
     * El método también presenta por consola el resumen y las incidencias.
     */
    private void importarProductosJson() {
        String rutaTexto = LectorConsola.leerTexto(
                "Introduce la ruta del JSON: ");

        try {
            Path ruta = Paths.get(rutaTexto);
            LectorProductosJson.ResultadoLectura resultado =
                    lectorProductosJson.leerProductos(ruta);

            int importados = resultado.productosValidos().size();
            int rechazados = resultado.incidencias().size();
            int encontrados = importados + rechazados;

            // El componente devuelve productos válidos, pero no modifica el inventario.
            inventario.getProductos().addAll(resultado.productosValidos());

            System.out.println("\n===== IMPORTAR JSON =====");
            System.out.println("Productos encontrados: " + encontrados);
            System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);

            if (!resultado.incidencias().isEmpty()) {
                System.out.println(ColoresConsola.pintar(
                        "[AVISO] Algunos productos no cumplen las validaciones:",
                        ColoresConsola.AMARILLO));

                for (String incidencia : resultado.incidencias()) {
                    System.out.println(" - " + incidencia);
                }
            }

            System.out.println(ColoresConsola.pintar(
                    "[OK] Fichero JSON leído e importación finalizada.",
                    ColoresConsola.VERDE));

        } catch (NoSuchFileException e) {
            System.out.println(ColoresConsola.pintar(
                    "[ERROR] No se encuentra el fichero: " + e.getFile(),
                    ColoresConsola.ROJO));

        } catch (JsonParseException e) {
            System.out.println(ColoresConsola.pintar(
                    "[ERROR] El fichero JSON está mal formado o tiene una estructura incorrecta: "
                            + e.getMessage(),
                    ColoresConsola.ROJO));

        } catch (InvalidPathException e) {
            System.out.println(ColoresConsola.pintar(
                    "[ERROR] La ruta introducida no es válida: " + e.getInput(),
                    ColoresConsola.ROJO));

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar(
                    "[ERROR] No se pudo leer el fichero JSON: " + e.getMessage(),
                    ColoresConsola.ROJO));
        }
    }

    /** Muestra la cabecera de la aplicación y el número de productos en memoria. */
    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar(
                "\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar(
                "         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar(
                "                 FASE 1 - UT1", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar(
                "================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar(
                "Productos en memoria: " + inventario.getProductos().size(),
                ColoresConsola.GRIS));
    }

    /** Muestra la cabecera de la sección de base de datos y el número de productos en memoria. */
    private void mostrarCabeceraUT2() {
        System.out.println(ColoresConsola.pintar(
                "\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar(
                "         DATASTOCK - CONEXIÓN A BASES DE DATOS", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar(
                "                 FASE 2 - UT2", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar(
                "================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar(
                "Productos en memoria: " + inventario.getProductos().size(),
                ColoresConsola.GRIS));
    }

    /** Muestra por consola los datos principales de un producto. */
    private void mostrarProducto(Producto producto) {
        System.out.println("--------------------------------");
        System.out.println("Código: " + producto.getCodigo());
        System.out.println("Nombre: " + producto.getNombre());
        System.out.println("Precio: " + producto.getPrecio() + " €");
        System.out.println("Stock: " + producto.getStock());
        System.out.println("Categoría: " + producto.getCategoriaId());
        System.out.println("Proveedor: " + producto.getProveedorId());
    }

    /** Gestiona las consultas sobre el inventario que está cargado en memoria. */
    private void menuConsultas() {
        int opcion;

        do {
            System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opción: ", 0, 3);

            switch (opcion) {
                case 1 -> {
                    if (inventario.getProductos().isEmpty()) {
                        System.out.println("No hay productos en el inventario.");
                    } else {
                        System.out.println("\n===== LISTADO DE PRODUCTOS =====");
                        for (Producto producto : consultaService.obtenerProductosOrdenados()) {
                            mostrarProducto(producto);
                        }
                    }
                }
                case 2 -> {
                    String codigo = LectorConsola.leerTexto(
                            "Introduce el código del producto: ");
                    Producto producto = consultaService.buscarPorCodigo(codigo);

                    if (producto != null) {
                        System.out.println("\n===== PRODUCTO ENCONTRADO =====");
                        mostrarProducto(producto);
                    } else {
                        System.out.println(
                                "No existe ningún producto con ese código.");
                    }
                }
                case 3 -> {
                    String nombre = LectorConsola.leerTexto(
                            "Introduce el texto a buscar en el nombre: ");

                    if (nombre.trim().isEmpty()) {
                        System.out.println(
                                "Debes introducir un texto para buscar.");
                    } else {
                        var encontrados = consultaService.buscarPorNombre(nombre);

                        if (encontrados.isEmpty()) {
                            System.out.println("No se encontraron productos.");
                        } else {
                            System.out.println("\n===== PRODUCTOS ENCONTRADOS =====");
                            for (Producto producto : encontrados) {
                                mostrarProducto(producto);
                            }
                            System.out.println("Total encontrados: " + encontrados.size());
                        }
                    }
                }
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    /** Muestra las opciones de exportación a JSON y XML. */
    private void menuExportaciones() {
        int opcion;

        do {
            System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opción: ", 0, 2);

            switch (opcion) {
                case 1 -> gestionarExportacion(true);
                case 2 -> gestionarExportacion(false);
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> throw new IllegalStateException("Opción inesperada");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    /**
     * Solicita la ruta de destino y gestiona la exportación al formato indicado.
     *
     * @param json true para exportar a JSON; false para exportar a XML.
     */
    private void gestionarExportacion(boolean json) {
        if (inventario.getProductos().isEmpty()) {
            System.out.println("No hay productos para exportar.");
        } else {
            String formato = json ? "JSON" : "XML";
            String rutaTexto = LectorConsola.leerTexto(
                    "Introduce la ruta de destino del " + formato + ": ");

            try {
                Path ruta = Paths.get(rutaTexto);
                boolean sobrescribir = false;

                if (Files.exists(ruta)) {
                    sobrescribir = LectorConsola.confirmar(
                            "El fichero ya existe. ¿Deseas sobrescribirlo?");
                }

                if (!Files.exists(ruta) || sobrescribir) {
                    if (json) {
                        exportadorService.exportarAJson(rutaTexto, sobrescribir);
                    } else {
                        exportadorService.exportarAXml(rutaTexto, sobrescribir);
                    }
                } else {
                    System.out.println("Exportación cancelada.");
                }
            } catch (Exception e) {
                System.out.println("Error en la ruta de destino: " + e.getMessage());
            }
        }
    }

    /** Solicita la ruta y guarda el inventario en un fichero serializado. */
    private void gestionarGuardado() {
        String ruta = LectorConsola.leerTexto(
                "Introduce la ruta del fichero .dat: ");

        try {
            Path destino = Paths.get(ruta);
            boolean sobrescribir = false;

            if (Files.exists(destino)) {
                sobrescribir = LectorConsola.confirmar(
                        "El fichero ya existe. ¿Deseas sobrescribirlo?");
            }

            if (!Files.exists(destino) || sobrescribir) {
                serializacionService.guardarInventario(ruta);
            } else {
                System.out.println("Guardado cancelado.");
            }
        } catch (Exception e) {
            System.out.println("Error en la ruta: " + e.getMessage());
        }
    }

    /** Gestiona la confirmación antes de sustituir el inventario actual. */
    private void gestionarRecuperacion() {
        if (inventario.getProductos().isEmpty()) {
            recuperarDesdeFichero();
        } else {
            boolean confirmar = LectorConsola.confirmar(
                    "Se sustituirá el inventario actual. ¿Continuar?");

            if (confirmar) {
                recuperarDesdeFichero();
            } else {
                System.out.println("Recuperación cancelada.");
            }
        }
    }

    /** Solicita el fichero serializado y delega la recuperación del inventario. */
    private void recuperarDesdeFichero() {
        String ruta = LectorConsola.leerTexto(
                "Introduce la ruta del fichero .dat: ");

        try {
            serializacionService.recuperarInventario(ruta);
        } catch (Exception e) {
            System.out.println("Error en la ruta: " + e.getMessage());
        }
    }

    /** Muestra el submenú para guardar y recuperar el inventario. */
    private void menuSerializacion() {
        int opcion;

        do {
            System.out.println("----------------------------------------------");
            System.out.println("GUARDAR Y RECUPERAR INVENTARIO");
            System.out.println("----------------------------------------------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            System.out.println("----------------------------------------------");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opción: ", 0, 2);

            switch (opcion) {
                case 1 -> gestionarGuardado();
                case 2 -> gestionarRecuperacion();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> throw new IllegalStateException("Opción inesperada");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    /** Muestra un aviso para una funcionalidad pendiente de desarrollo. */
    private void pendiente(String requisito) {
        System.out.println(ColoresConsola.pintar(
                "[PENDIENTE] " + requisito, ColoresConsola.AMARILLO));
        System.out.println("Esta funcionalidad debe desarrollarla el alumnado.");
        LectorConsola.pausar();
    }

    /** Muestra el submenú de gestión de categorías. */
    private void menuCategorias() {
        int opcion;

        do {
            System.out.println("\n---------- GESTIÓN DE CATEGORÍAS ----------");
            System.out.println("1. Dar de alta categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar categoría");
            System.out.println("4. Eliminar categoría");
            System.out.println("0. Volver al menú principal");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1 -> ServicioCategoria.darDeAltaCategoria();
                case 2 -> ServicioCategoria.listarCategorias();
                case 3 -> ServicioCategoria.modificarCategoria();
                case 4 -> ServicioCategoria.eliminarCategoria();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> throw new IllegalStateException("Opción inesperada");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    /** Muestra el submenú de gestión de proveedores. */
    private void menuProveedores() {
        int opcion;

        do {
            System.out.println("\n---------- GESTIÓN DE PROVEEDORES ----------");
            System.out.println("1. Dar de alta proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar proveedor");
            System.out.println("4. Eliminar proveedor");
            System.out.println("0. Volver al menú principal");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1 -> ServicioProveedor.darDeAltaProveedor();
                case 2 -> ServicioProveedor.listarProveedores();
                case 3 -> ServicioProveedor.modificarProveedor();
                case 4 -> ServicioProveedor.eliminarProveedor();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> throw new IllegalStateException("Opción inesperada");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    /** Muestra el submenú de mantenimiento de productos. */
    private void menuProductos() {
        int opcion;

        do {
            System.out.println("\n---------- MANTENIMIENTO DE PRODUCTOS ----------");
            System.out.println("1. Dar de alta producto");
            System.out.println("2. Consultar producto por código");
            System.out.println("3. Modificar producto");
            System.out.println("4. Eliminar producto");
            System.out.println("0. Volver al menú principal");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1 -> ServicioProducto.darDeAltaProducto();
                case 2 -> ServicioProducto.consultarProductoPorCodigo();
                case 3 -> ServicioProducto.modificarProducto();
                case 4 -> ServicioProducto.eliminarProducto();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> throw new IllegalStateException("Opción inesperada");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    /**
     * Solicita un código y presenta el resultado de la consulta JDBC.
     * La consulta y el cierre de los recursos pertenecen a
     * ConsultaProductoJdbc; este método gestiona la interacción con el
     * usuario y muestra la ficha recuperada.
     */
    private void buscarProductoPorCodigoEnBd() {
        System.out.println("\n=== BÚSQUEDA EXACTA POR CÓDIGO ===");
        String codigo = LectorConsola.leerCadenaNoVacia(
                "Introduce el código exacto del producto: ");

        try {
            Optional<ConsultaProductoJdbc.ResultadoConsulta> resultado =
                    consultaProductoJdbc.buscarPorCodigo(codigo);

            if (resultado.isPresent()) {
                ConsultaProductoJdbc.ResultadoConsulta ficha = resultado.get();
                Producto producto = ficha.producto();

                System.out.println(ColoresConsola.pintar(
                        "\n[OK] Producto encontrado:", ColoresConsola.VERDE));
                System.out.println("Código    : " + producto.getCodigo());
                System.out.println("Nombre    : " + producto.getNombre());
                System.out.println("Precio    : " + producto.getPrecio() + " EUR");
                System.out.println("Stock     : " + producto.getStock());
                System.out.println("Categoría : " + ficha.nombreCategoria());
                System.out.println("Proveedor : " + ficha.nombreProveedor());
            } else {
                System.out.println(ColoresConsola.pintar(
                        "[AVISO] No se ha encontrado ningún producto con el código "
                                + codigo.trim().toUpperCase() + ".",
                        ColoresConsola.AMARILLO));
            }
        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar(
                    "[ERROR] No se pudo realizar la búsqueda del producto en MySQL: "
                            + e.getMessage(),
                    ColoresConsola.ROJO));
        }
    }

    /** Muestra las consultas disponibles sobre la base de datos MySQL. */
    private void menuConsultasBd() {
        int opcion;

        do {
            System.out.println("\n---------- CONSULTAS Y BÚSQUEDAS EN BD ----------");
            System.out.println("1. Listar todos los productos (con JOIN)");
            System.out.println("2. Buscar producto por código exacto");
            System.out.println("3. Buscar productos por nombre parcial (LIKE)");
            System.out.println("4. Consultar productos con stock inferior a umbral");
            System.out.println("0. Volver al menú principal");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1 -> ServicioConsultaProducto.listarTodosLosProductos();
                case 2 -> buscarProductoPorCodigoEnBd();
                case 3 -> ServicioConsultaProducto.buscarProductosPorNombreParcial();
                case 4 -> ServicioConsultaProducto.consultarProductosStockInferior();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> throw new IllegalStateException("Opción inesperada");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }
}