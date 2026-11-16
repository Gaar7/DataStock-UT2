
package es.iesisidra.datastock.servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Lee un catálogo de productos desde un fichero JSON y valida sus datos.
 *
 * El componente no accede al inventario ni interactúa con la consola.
 * Devuelve los productos válidos y las incidencias encontradas.
 */
public class LectorProductosJson {

    private final Gson gson;

    /**
     * Crea un lector que utiliza Gson para convertir el JSON
     * en objetos Java.
     */
    public LectorProductosJson() {
        this.gson = new Gson();
    }

    /**
     * Lee y valida los productos contenidos en un fichero JSON.
     *
     * Se espera un objeto raíz con una propiedad "productos".
     * Los productos que no superen las validaciones se omiten
     * y se describen en las incidencias del resultado.
     *
     * @param ruta ruta del fichero JSON.
     * @return productos válidos e incidencias detectadas.
     * @throws IllegalArgumentException si la ruta es null.
     * @throws NoSuchFileException si el fichero no existe.
     * @throws IOException si no se puede leer el fichero.
     * @throws JsonParseException si el JSON es incorrecto
     *         o no tiene la estructura esperada.
     */
    public ResultadoLectura leerProductos(Path ruta) throws IOException {

        if (ruta == null) {
            throw new IllegalArgumentException(
                    "La ruta del fichero JSON no puede ser null.");
        }

        if (!Files.exists(ruta)) {
            throw new NoSuchFileException(ruta.toString());
        }

        if (!Files.isRegularFile(ruta) || !Files.isReadable(ruta)) {
            throw new IOException(
                    "La ruta no corresponde a un fichero regular y legible: "
                    + ruta);
        }

        ContenedorJson contenedor;

        // El lector se cierra automáticamente al finalizar la lectura.
        try (BufferedReader reader = Files.newBufferedReader(ruta)) {
            contenedor = gson.fromJson(reader, ContenedorJson.class);
        }

        // Comprueba que existe la estructura raíz esperada.
        if (contenedor == null || contenedor.productos == null) {
            throw new JsonParseException(
                    "El JSON no tiene la estructura esperada: "
                    + "falta la propiedad 'productos'.");
        }

        List<Producto> productosValidos = new ArrayList<>();
        List<String> incidencias = new ArrayList<>();

        // Se valida cada producto de forma independiente.
        for (int indice = 0;
                indice < contenedor.productos.size(); indice++) {

            Producto producto = contenedor.productos.get(indice);
            String motivo = validarProducto(producto);

            if (motivo == null) {

                // Normalizamos el código eliminando espacios exteriores.
                producto.setCodigo(producto.getCodigo().trim());

                productosValidos.add(producto);

            } else {

                String codigo = obtenerCodigoParaIncidencia(producto);

                incidencias.add(
                        "Producto " + codigo
                        + " (posición " + (indice + 1) + "): "
                        + motivo);
            }
        }

        return new ResultadoLectura(productosValidos, incidencias);
    }

    /**
     * Comprueba las reglas de validación de producto utilizadas
     * durante la importación JSON de UT1.
     *
     * @param producto producto que se va a comprobar.
     * @return null si es válido o el motivo del rechazo.
     */
    private String validarProducto(Producto producto) {

        String motivo = null;

        if (producto == null) {

            motivo = "el elemento no contiene un producto";

        } else {

            String codigo = producto.getCodigo() != null
                    ? producto.getCodigo().trim()
                    : "";

            if (codigo.isEmpty() || codigo.length() > 12) {

                motivo = "el código es obligatorio y máximo de 12 caracteres";

            } else if (producto.getNombre() == null
                    || producto.getNombre().trim().length() < 3
                    || producto.getNombre().length() > 100) {

                motivo = "el nombre debe tener entre 3 y 100 caracteres";

            } else if (producto.getPrecio() == null
                    || producto.getPrecio().signum() <= 0) {

                motivo = "el precio debe ser mayor que 0";

            } else if (producto.getStock() < 0) {

                motivo = "el stock no puede ser negativo";
            }
        }

        return motivo;
    }

    /**
     * Obtiene un identificador legible para una incidencia.
     *
     * @param producto producto rechazado.
     * @return su código o el texto "desconocido".
     */
    private String obtenerCodigoParaIncidencia(Producto producto) {

        String codigo = "";

        if (producto != null && producto.getCodigo() != null) {
            codigo = producto.getCodigo().trim();
        }

        if (codigo.isEmpty()) {
            codigo = "desconocido";
        }

        return codigo;
    }

    /**
     * Representa el resultado de la lectura.
     *
     * @param productosValidos productos que han superado las validaciones.
     * @param incidencias motivos por los que se han rechazado productos.
     */
    public record ResultadoLectura(
            List<Producto> productosValidos,
            List<String> incidencias) {

        /**
         * Crea un resultado con listas no modificables desde el exterior.
         */
        public ResultadoLectura {
            productosValidos = List.copyOf(productosValidos);
            incidencias = List.copyOf(incidencias);
        }
    }

    /**
     * Clase auxiliar que representa la estructura del catálogo JSON.
     */
    private static class ContenedorJson {
        private List<Producto> productos;
    }
}
