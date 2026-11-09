package es.iesisidra.datastock.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ConsultaInventarioService;
import es.iesisidra.datastock.servicio.ExportadorInventarioService;
import es.iesisidra.datastock.servicio.ImportadorJsonService;
import es.iesisidra.datastock.servicio.ImportadorXmlService;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.SerializacionService;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>
 * Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación y
 * el procesamiento en métodos o clases independientes.
 * </p>
 */
/**
 * Clase encargada de la parte visual y de la navegación de DataStock. Muestra
 * los menús, recoge las opciones del usuario y llama a los métodos de
 * Inventario para realizar las operaciones.
 *
 * No contiene la lógica principal de importación, búsqueda o exportación: esa
 * responsabilidad está en Inventario. También utiliza LectorConsola para leer
 * datos y ColoresConsola para dar formato a algunos mensajes.
 */
public class MenuPrincipal {

    // Referencia al inventario compartido por todos los menús.
    // Es final porque se asigna en el constructor y no se reemplaza después.
    private final Inventario inventario;
    private final ImportadorJsonService importadorJson;
    private final ImportadorXmlService importadorXml;
    private final ConsultaInventarioService consultaService;
    private final ExportadorInventarioService exportadorService;
    private final SerializacionService serializacionService;

    // Este es el constructor obligatorio que App.java está buscando
    public MenuPrincipal(Inventario inventario) {
        this.inventario = inventario;

        // Inyectamos el inventario en los servicios
        this.importadorJson = new ImportadorJsonService(inventario);
        this.importadorXml = new ImportadorXmlService(inventario);
        this.consultaService = new ConsultaInventarioService(inventario);
        this.exportadorService = new ExportadorInventarioService(inventario);
        this.serializacionService = new SerializacionService(inventario);
    }

    /**
     * @param inventario colección común de productos de esta ejecución.
     */
    /**
     * Constructor del menú principal. Recibe el inventario que se va a utilizar
     * durante toda la ejecución y lo guarda en el atributo de la clase.
     *
     * De esta forma, todos los submenús trabajan sobre la misma lista de
     * productos y no crean inventarios independientes.
     */
    /**
     * Ejecuta el menú principal hasta que el usuario selecciona salir.
     */
    /**
     * Muestra el menú principal repetidamente hasta que el usuario elige 0.
     * Primero presenta la cabecera y las opciones, después lee una opción
     * válida y ejecuta la acción correspondiente mediante el switch.
     *
     * Las opciones de consulta, exportación y serialización abren sus
     * respectivos submenús. Los break evitan que se ejecute también el
     * siguiente case de este switch.
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
            System.out.println("0. Salir");
            // LectorConsola valida que la opción esté dentro del rango permitido.
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 6);
            switch (opcion) {
                case 1:
                    System.out.println("Introduce la ruta a explorar: ");
                    String ruta = LectorConsola.leerTexto("Introduce la ruta");
                    ExploradorFicheros.explorarRuta(ruta);
                    LectorConsola.pausar();
                    break;
                case 2:
                    System.out.println("Introduce la ruta del JSON: ");
                    String rutaJson = LectorConsola.leerTexto("Introduce la ruta");
                    importadorJson.importarDesdeJson(rutaJson);
                    LectorConsola.pausar();
                    break;
                case 3:
                    System.out.println("Introduce la ruta del XML: ");
                    String rutaXml = LectorConsola.leerTexto("Introduce la ruta: ");
                    importadorXml.importarDesdeXml(rutaXml);
                    LectorConsola.pausar();
                    break;
                case 4:
                    menuConsultas();
                    break;
                case 5:
                    menuExportaciones();
                    break;
                case 6:
                    menuSerializacion();
                    break;
                case 0:
                    System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                    break;
                default:
                    throw new IllegalStateException("Opción inesperada");

            }
        } while (opcion != 0);
    }

    /**
     * Muestra el encabezado de DataStock y el número de productos que hay
     * actualmente en memoria. Obtiene ese número consultando la lista del
     * objeto Inventario que recibió el constructor.
     */
    private void mostrarCabecera() {
        System.out.println(
                ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("                 FASE 1 - UT1", ColoresConsola.CIAN));
        System.out.println(
                ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(),
                ColoresConsola.GRIS));
    }

    /**
     * Muestra por consola todos los datos de un producto. Recibe un objeto
     * Producto y utiliza sus getters para consultar el código, nombre, precio,
     * stock, categoría y proveedor.
     *
     * Este método evita repetir el mismo bloque de impresión en las distintas
     * opciones de búsqueda y listado.
     */
    private void mostrarProducto(es.iesisidra.datastock.modelo.Producto p) {
        System.out.println("--------------------------------");
        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio() + " €");
        System.out.println("Stock: " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaId());
        System.out.println("Proveedor: " + p.getProveedorId());
    }

    /**
     * Gestiona el submenú de consultas. Permite listar todos los productos,
     * buscar uno por código o buscar varios por una parte de su nombre. Las
     * búsquedas se delegan en Inventario y los resultados se muestran con
     * mostrarProducto().
     *
     * El bucle do-while mantiene abierto este submenú hasta elegir 0.
     */
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

                        // Inventario devuelve una copia ordenada por código.
                        for (Producto p : consultaService.obtenerProductosOrdenados()) {
                            mostrarProducto(p);
                        }
                    }
                }

                case 2 -> {
                    String codigo = LectorConsola.leerTexto(
                            "Introduce el código del producto: ");
                    // La búsqueda real se realiza en Inventario.
                    Producto p = consultaService.buscarPorCodigo(codigo);

                    if (p != null) {
                        System.out.println("\n===== PRODUCTO ENCONTRADO =====");
                        mostrarProducto(p);
                    } else {
                        System.out.println("No existe ningún producto con ese código.");
                    }
                }

                case 3 -> {
                    String nombre = LectorConsola.leerTexto(
                            "Introduce el texto a buscar en el nombre: ");

                    if (nombre.trim().isEmpty()) {
                        System.out.println("Debes introducir un texto para buscar.");
                    } else {
                        // Se buscan coincidencias en el nombre del producto.
                        var encontrados = consultaService.buscarPorNombre(nombre);

                        if (encontrados.isEmpty()) {
                            System.out.println("No se encontraron productos.");
                        } else {
                            System.out.println("\n===== PRODUCTOS ENCONTRADOS =====");

                            for (Producto p : encontrados) {
                                mostrarProducto(p);
                            }

                            System.out.println("Total encontrados: "
                                    + encontrados.size());
                        }
                    }
                }

                case 0 -> {
                    System.out.println("Volviendo al menú principal...");
                }
            }

        } while (opcion != 0);
    }

    /**
     * Muestra el submenú de exportación y permite elegir JSON o XML. Según la
     * opción seleccionada, llama a gestionarExportacion() indicando el formato.
     * El submenú se repite hasta elegir 0.
     */
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
                case 1 ->
                    gestionarExportacion(true);
                case 2 ->
                    gestionarExportacion(false);
                case 0 ->
                    System.out.println("Volviendo al menú principal...");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);
    }

    /**
     * Solicita la ruta y gestiona la exportación elegida.
     *
     * @param json true para JSON y false para XML.
     */
    /**
     * Gestiona los pasos comunes antes de exportar a JSON o XML. Comprueba que
     * el inventario no esté vacío, pide la ruta de destino y, si el fichero ya
     * existe, solicita confirmación para sobrescribirlo.
     *
     * El parámetro json determina el formato: true significa JSON y false
     * significa XML. Finalmente delega la escritura en Inventario.
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
                // Solo se permitirá reemplazar el fichero si el usuario confirma.
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
                System.out.println("Error en la ruta de destino: "
                        + e.getMessage());
            }
        }
    }

    /**
     * Pide la ruta donde se guardará el inventario en un fichero .dat. Si el
     * fichero ya existe, pregunta al usuario si desea sobrescribirlo. Si no
     * existe o el usuario confirma, llama a guardarInventario() de la clase
     * Inventario.
     */
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

    /**
     * Controla la recuperación de un inventario desde un fichero .dat. Si el
     * inventario actual está vacío, permite recuperar directamente. Si contiene
     * productos, pide confirmación porque la recuperación sustituirá los datos
     * que hay actualmente en memoria.
     */
    private void gestionarRecuperacion() {
        if (inventario.getProductos().isEmpty()) {
            recuperarDesdeFichero();
        } else {
            boolean confirmar = LectorConsola.confirmar(
                    "Se sustituirá el inventario actual. ¿Continuar?");
            // La recuperación sustituye los productos que ya están en memoria.
            if (confirmar) {
                recuperarDesdeFichero();
            } else {
                System.out.println("Recuperación cancelada.");
            }
        }
    }

    /**
     * Solicita la ruta del fichero .dat que contiene el inventario guardado y
     * delega la lectura y sustitución de los productos en Inventario. El
     * try-catch permite mostrar un mensaje si se produce un error relacionado
     * con la ruta o con la recuperación.
     */
    private void recuperarDesdeFichero() {
        String ruta = LectorConsola.leerTexto(
                "Introduce la ruta del fichero .dat: ");

        try {
            serializacionService.recuperarInventario(ruta);
        } catch (Exception e) {
            System.out.println("Error en la ruta: " + e.getMessage());
        }
    }

    /**
     * Muestra el submenú para guardar o recuperar el inventario. La opción 1
     * llama al método de gestión del guardado y la opción 2 al de recuperación.
     * Se repite hasta que el usuario elige 0.
     */
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
                case 1 ->
                    gestionarGuardado();
                case 2 ->
                    gestionarRecuperacion();
                case 0 ->
                    System.out.println(
                            "Volviendo al menú principal...");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);
    }

    /**
     * Sustituir las llamadas por servicios propios a medida que se implementen
     * los RF.
     */
    /**
     * Método auxiliar para mostrar que una funcionalidad está pendiente. Recibe
     * el nombre del requisito, lo muestra resaltado y pausa la ejecución para
     * que el usuario pueda leer el mensaje.
     *
     * En esta versión se conserva como método auxiliar, aunque las operaciones
     * principales ya se realizan mediante otros métodos.
     */
    private void pendiente(String requisito) {
        System.out.println(ColoresConsola.pintar("[PENDIENTE] " + requisito, ColoresConsola.AMARILLO));
        System.out.println("Esta funcionalidad debe desarrollarla el alumnado.");
        LectorConsola.pausar();
    }
}
