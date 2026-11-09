package es.iesisidra.datastock.servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import es.iesisidra.datastock.modelo.Producto;

public class ImportadorJsonService {

    // Clase auxiliar que representa la estructura del JSON de entrada.
    // El fichero espera un objeto principal con una propiedad llamada
    // "productos", que contiene la lista de artículos.
    // Clase auxiliar interna para Gson (correctamente aislada)
    private final Inventario inventario;

    public ImportadorJsonService(Inventario inventario) {
        this.inventario = inventario;
    }

    private static class ContenedorJson {

        private List<Producto> productos;

        public List<Producto> getProductos() {
            return productos;
        }
    }

    /**
     * Exporta los productos del inventario a un fichero XML utilizando DOM.
     * Construye un documento en memoria con un nodo raíz "catalogo" y un nodo
     * "producto" por cada artículo, con sus campos como elementos hijos.
     *
     * El parámetro sobrescribir indica si el usuario ha autorizado reemplazar
     * un fichero que ya existe. MenuPrincipal solicita esa confirmación.
     */
    /**
     * Exporta los productos del inventario a un fichero XML.
     */
    public void importarDesdeJson(String rutaFichero) {
        Path ruta = Paths.get(rutaFichero);

        if (!Files.exists(ruta) || !Files.isRegularFile(ruta)) {
            System.out.println("Error: El fichero no existe o no es un fichero válido.");
            return;
        }
        // Gson se encarga de transformar el contenido JSON en objetos Java.
        Gson gson = new Gson();
        ContenedorJson contenedor;

        try (BufferedReader reader = Files.newBufferedReader(ruta)) {
            contenedor = gson.fromJson(reader, ContenedorJson.class);
            if (contenedor == null || contenedor.getProductos() == null) {
                System.out.println(
                        "Error: El fichero JSON no tiene la estructura correcta (falta la clave 'productos').");
                return;
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: El fichero JSON está mal formado y no se puede interpretar[cite: 16].");
            return;
        } catch (IOException e) {
            System.out.println("Error de E/S al leer el fichero: " + e.getMessage());
            return;
        }
        // Contadores para elaborar el resumen que se muestra al terminar la
        // importación.
        int encontrados = contenedor.getProductos().size();
        int importados = 0;
        int rechazados = 0;
        StringBuilder incidencias = new StringBuilder();

        // Se procesa cada producto del fichero por separado.
        for (Producto p : contenedor.getProductos()) {
            String codigo = p.getCodigo() != null ? p.getCodigo().trim() : "";

            boolean esValido = true;
            String motivo = "";

            if (codigo.isEmpty() || codigo.length() > 12) {
                esValido = false;
                motivo = "el código es obligatorio y máximo de 12 caracteres";
            } else if (p.getNombre() == null || p.getNombre().trim().length() < 3 || p.getNombre().length() > 100) {
                esValido = false;
                motivo = "el nombre debe tener entre 3 y 100 caracteres";
            } else if (p.getPrecio() == null || p.getPrecio().signum() <= 0) {
                esValido = false;
                motivo = "el precio debe ser mayor que 0";
            } else if (p.getStock() < 0) {
                esValido = false;
                motivo = "el stock no puede ser negativo";
            } else {

                // Comprueba si el código ya aparece en el inventario actual.
                boolean duplicado = false;

                for (Producto existente : inventario.getProductos()) {
                    if (existente.getCodigo() != null
                            && existente.getCodigo().trim()
                                    .equalsIgnoreCase(codigo)) {
                        duplicado = true;
                    }
                }

            }

            if (esValido) {
                p.setCodigo(codigo);
                inventario.getProductos().add(p);
                importados++;
            } else {
                rechazados++;
                incidencias.append(" - Producto ").append(codigo.isEmpty() ? "desconocido" : codigo).append(": ")
                        .append(motivo).append("\n");
            }
        }

        System.out.println("\n===== IMPORTAR JSON =====");
        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);
        if (rechazados > 0) {
            System.out.println("INCIDENCIAS:\n" + incidencias.toString());
        }
        System.out.println("Importación finalizada.");
    }

}
