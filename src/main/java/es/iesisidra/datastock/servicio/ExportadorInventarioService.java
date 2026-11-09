package es.iesisidra.datastock.servicio;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.iesisidra.datastock.modelo.Producto;

public class ExportadorInventarioService {

    private final Inventario inventario;

    public ExportadorInventarioService(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Recupera una lista de productos desde un fichero serializado. Primero
     * comprueba que la ruta sea un fichero válido y después lee el objeto
     * guardado con ObjectInputStream.
     *
     * Antes de sustituir el inventario actual, comprueba que el objeto leído
     * sea una lista y que todos sus elementos sean Producto. Así evita dejar el
     * inventario a medio sustituir si el contenido no es válido.
     */
    public void exportarAXml(String rutaFichero, boolean sobrescribir) {
        if (inventario.getProductos().isEmpty()) {
            System.out.println("No hay productos para exportar.");
        } else {
            Path ruta = Paths.get(rutaFichero);

            if (Files.exists(ruta) && !sobrescribir) {
                System.out.println("Exportación cancelada.");
            } else if (Files.exists(ruta) && !Files.isRegularFile(ruta)) {
                System.out.println("Error: La ruta no es un fichero válido.");
            } else {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document documento = builder.newDocument();

                    Element raiz = documento.createElement("catalogo");
                    documento.appendChild(raiz);

                    for (Producto p : inventario.getProductos()) {
                        Element elementoProducto = documento.createElement("producto");

                        raiz.appendChild(elementoProducto);

                        Element codigo = documento.createElement("codigo");
                        codigo.setTextContent(p.getCodigo());
                        elementoProducto.appendChild(codigo);

                        Element nombre = documento.createElement("nombre");
                        nombre.setTextContent(p.getNombre());
                        elementoProducto.appendChild(nombre);

                        Element precio = documento.createElement("precio");
                        precio.setTextContent(p.getPrecio().toString());
                        elementoProducto.appendChild(precio);

                        Element stock = documento.createElement("stock");
                        stock.setTextContent(String.valueOf(p.getStock()));
                        elementoProducto.appendChild(stock);

                        Element categoriaId = documento.createElement("categoriaId");
                        categoriaId.setTextContent(
                                String.valueOf(p.getCategoriaId()));
                        elementoProducto.appendChild(categoriaId);

                        Element proveedorId = documento.createElement("proveedorId");
                        proveedorId.setTextContent(
                                String.valueOf(p.getProveedorId()));
                        elementoProducto.appendChild(proveedorId);
                    }

                    // Transformer convierte el documento DOM en texto XML y lo escribe en el
                    // fichero.
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();

                    Transformer transformer = transformerFactory.newTransformer();

                    transformer.setOutputProperty(
                            OutputKeys.INDENT, "yes");

                    transformer.setOutputProperty(
                            OutputKeys.ENCODING, "UTF-8");

                    DOMSource origen = new DOMSource(documento);
                    StreamResult destino = new StreamResult(ruta.toFile());

                    transformer.transform(origen, destino);

                    System.out.println(
                            "Exportación XML realizada correctamente.");

                } catch (Exception e) {
                    System.out.println(
                            "Error al exportar XML: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Exporta el inventario a un fichero JSON usando Gson. Se crea un objeto
     * contenedor con la propiedad "productos" para que el resultado tenga la
     * misma estructura que espera el importador JSON.
     *
     * El parámetro sobrescribir indica si se permite reemplazar un fichero
     * existente; la confirmación se solicita previamente desde MenuPrincipal.
     */
    /**
     * Exporta los productos del inventario a un fichero JSON.
     */
    public void exportarAJson(String rutaFichero, boolean sobrescribir) {
        if (inventario.getProductos().isEmpty()) {
            System.out.println("No hay productos para exportar.");
        } else {
            Path ruta = Paths.get(rutaFichero);

            if (Files.exists(ruta) && !sobrescribir) {
                System.out.println("Exportación cancelada.");
            } else if (Files.exists(ruta) && !Files.isRegularFile(ruta)) {
                System.out.println("Error: La ruta no es un fichero válido.");
            } else {
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                // Crea la estructura {"productos": [...]} que se guardará en el JSON.
                Map<String, List<Producto>> contenedor = Map.of("productos", inventario.getProductos());

                try (BufferedWriter writer = Files.newBufferedWriter(
                        ruta,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE)) {

                    gson.toJson(contenedor, writer);
                    System.out.println("Exportación JSON realizada correctamente.");

                } catch (IOException e) {
                    System.out.println("Error al exportar JSON: " + e.getMessage());
                }
            }
        }
    }
}
