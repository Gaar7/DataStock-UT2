package es.iesisidra.datastock.servicio;

import java.io.File;
import java.math.BigDecimal;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.iesisidra.datastock.modelo.Producto;

public class ImportadorXmlService {

    private final Inventario inventario;

    public ImportadorXmlService(Inventario inventario) {
        this.inventario = inventario;
    }

    /* Método importarDesdeXml que:
     * - Compruebe que el fichero existe
     * - Lo lea mediante DOM
     * - Obtener los productos del documento
     * - Validar cada producto
     * - Añadir los válidos a produtos
     * - Mostrar el informe de importación
     */
    /**
     * Importa productos desde un fichero XML utilizando DOM.
     */
    /**
     * Importa productos desde un fichero XML mediante DOM. DOM carga el
     * documento XML como una estructura de nodos que podemos recorrer para
     * obtener los campos de cada producto.
     *
     * Cada producto se convierte en un objeto Producto, se validan sus datos y,
     * si es correcto, se añade a la lista del inventario.
     */
    public void importarDesdeXml(String rutaFichero) {
        File fichero = new File(rutaFichero);

        if (!fichero.exists() || !fichero.isFile()) {
            System.out.println("Error: El fichero no existe o no es válido.");
            return;
        }

        Document documento;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Protección frente a entidades externas
            factory.setFeature(
                    XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            documento = builder.parse(fichero);
            documento.getDocumentElement().normalize();

        } catch (Exception e) {
            System.out.println("Error al leer el XML: " + e.getMessage());
            return;
        }

        // Obtiene todos los elementos llamados 'producto' que cuelgan del nodo raíz.
        NodeList listaProductos = documento.getDocumentElement()
                .getElementsByTagName("producto");

        int encontrados = listaProductos.getLength();
        int importados = 0;
        int rechazados = 0;

        StringBuilder incidencias = new StringBuilder();

        // Recorre los nodos de producto encontrados en el documento XML.
        for (int i = 0; i < encontrados; i++) {
            Element elemento = (Element) listaProductos.item(i);

            String codigo = "";
            try {
                codigo = elemento.getElementsByTagName("codigo")
                        .item(0).getTextContent().trim();

                String nombre = elemento.getElementsByTagName("nombre")
                        .item(0).getTextContent().trim();

                BigDecimal precio = new BigDecimal(
                        elemento.getElementsByTagName("precio")
                                .item(0).getTextContent().trim());

                int stock = Integer.parseInt(
                        elemento.getElementsByTagName("stock")
                                .item(0).getTextContent().trim());

                int categoriaId = Integer.parseInt(
                        elemento.getElementsByTagName("categoriaId")
                                .item(0).getTextContent().trim());

                int proveedorId = Integer.parseInt(
                        elemento.getElementsByTagName("proveedorId")
                                .item(0).getTextContent().trim());

                Producto p = new Producto(
                        codigo, nombre, precio, stock,
                        categoriaId, proveedorId);

                // Validación de los datos
                String motivo = "";

                if (codigo.isEmpty() || codigo.length() > 12) {
                    motivo = "el código es obligatorio y máximo de 12 caracteres";
                } else if (nombre.length() < 3 || nombre.length() > 100) {
                    motivo = "el nombre debe tener entre 3 y 100 caracteres";
                } else if (precio.signum() <= 0) {
                    motivo = "el precio debe ser mayor que 0";
                } else if (stock < 0) {
                    motivo = "el stock no puede ser negativo";
                } else {

                    boolean duplicado = false;

                    for (Producto existente : inventario.getProductos()) {
                        if (existente.getCodigo() != null
                                && existente.getCodigo().trim()
                                        .equalsIgnoreCase(codigo)) {
                            duplicado = true;
                        }
                    }

                    if (duplicado) {
                        motivo = "código duplicado";
                    }
                }

                if (motivo.isEmpty()) {
                    inventario.getProductos().add(p);
                    importados++;
                } else {
                    rechazados++;
                    incidencias.append(" - Producto ")
                            .append(codigo.isEmpty() ? "desconocido" : codigo)
                            .append(": ").append(motivo).append("\n");
                }

            } catch (Exception e) {
                rechazados++;
                incidencias.append(" - Producto ")
                        .append(codigo.isEmpty() ? "desconocido" : codigo)
                        .append(": datos incompletos o formato numérico incorrecto\n");
            }
        }

        System.out.println("\n===== IMPORTAR XML =====");
        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Importados: " + importados
                + " | Rechazados: " + rechazados);

        if (rechazados > 0) {
            System.out.println("INCIDENCIAS:\n" + incidencias);
        }

        System.out.println("Importación finalizada.");
    }

}
