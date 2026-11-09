package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Clase de servicio encargada de ejecutar las consultas y búsquedas avanzadas
 * sobre los productos almacenados en MySQL, empleando JOINs, LIKE y filtros por umbral
 * (Requisito RF-JDBC-04).
 */
public class ServicioConsultaProducto {

    /**
     * Lista todos los productos almacenados en la base de datos ordenados por código,
     * incorporando los nombres de sus respectivas categorías y proveedores mediante JOIN.
     */
    public static void listarTodosLosProductos() {
        System.out.println("\n=== LISTADO COMPLETO DE PRODUCTOS EN MYSQL ===");

        // Consulta SQL con JOIN para relacionar producto con categoria y proveedor
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, c.nombre AS categoria_nombre, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "ORDER BY p.codigo ASC";

        // Uso de try-with-resources para asegurar el cierre automático de conexión, statement y resultset
        try (Connection con = ConexionBD.conectar();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hayRegistros = false;
            int totalProductos = 0;

            System.out.printf("%-12s %-30s %-10s %-8s %-25s %-25s%n", "CÓDIGO", "NOMBRE", "PRECIO", "STOCK", "CATEGORÍA", "PROVEEDOR");
            System.out.println("------------------------------------------------------------------------------------------------------------------------");

            // Recorrido secuencial del ResultSet mediante el cursor interno
            while (rs.next()) {
                hayRegistros = true;
                totalProductos++;
                
                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");
                String categoria = rs.getString("categoria_nombre");
                String proveedor = rs.getString("proveedor_nombre");

                System.out.printf("%-12s %-30s %-10.2f %-8d %-25s %-25s%n", codigo, nombre, precio, stock, categoria, proveedor);
            }

            if (!hayRegistros) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] No hay productos almacenados en MySQL." + ColoresConsola.RESET);
            } else {
                System.out.println("------------------------------------------------------------------------------------------------------------------------");
                System.out.println("Total de productos encontrados: " + totalProductos);
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] No se pudo recuperar el listado de productos de la base de datos." + ColoresConsola.RESET);
        }
    }

    /**
     * Busca un producto por su código exacto tras normalizarlo a mayúsculas.
     */
    public static void buscarProductoPorCodigoExacto() {
        System.out.println("\n=== BÚSQUEDA EXACTA POR CÓDIGO ===");

        String codigo = LectorConsola.leerCadenaNoVacia("Introduce el código exacto del producto: ").trim().toUpperCase();

        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, c.nombre AS categoria_nombre, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "WHERE p.codigo = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, codigo);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n[OK] Producto encontrado:");
                    System.out.println("Código    : " + rs.getString("codigo"));
                    System.out.println("Nombre    : " + rs.getString("nombre"));
                    System.out.println("Precio    : " + rs.getBigDecimal("precio") + " EUR");
                    System.out.println("Stock     : " + rs.getInt("stock"));
                    System.out.println("Categoría : " + rs.getString("categoria_nombre"));
                    System.out.println("Proveedor : " + rs.getString("proveedor_nombre"));
                } else {
                    System.out.println(ColoresConsola.AMARILLO + "[AVISO] No se ha encontrado ningún producto con el código " + codigo + "." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] Error al realizar la búsqueda por código." + ColoresConsola.RESET);
        }
    }

    /**
     * Busca productos cuyo nombre contenga un fragmento de texto introducido por el usuario,
     * utilizando el operador LIKE con parámetros preparados de manera segura[cite: 13, 16].
     */
    public static void buscarProductosPorNombreParcial() {
        System.out.println("\n=== BÚSQUEDA PARCIAL POR NOMBRE ===");

        String filtroNombre = LectorConsola.leerCadenaNoVacia("Introduce el texto o fragmento a buscar: ").trim();

        // Se construye el patrón con comodines '%' para buscar coincidencias parciales
        String patronLike = "%" + filtroNombre + "%";

        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, c.nombre AS categoria_nombre, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "WHERE p.nombre LIKE ? " +
                     "ORDER BY p.codigo ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, patronLike);

            try (ResultSet rs = pst.executeQuery()) {
                boolean hayCoincidencias = false;
                int contador = 0;

                System.out.printf("%-12s %-30s %-10s %-8s %-25s %-25s%n", "CÓDIGO", "NOMBRE", "PRECIO", "STOCK", "CATEGORÍA", "PROVEEDOR");
                System.out.println("------------------------------------------------------------------------------------------------------------------------");

                while (rs.next()) {
                    hayCoincidencias = true;
                    contador++;

                    System.out.printf("%-12s %-30s %-10.2f %-8d %-25s %-25s%n",
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            rs.getString("categoria_nombre"),
                            rs.getString("proveedor_nombre")
                    );
                }

                if (!hayCoincidencias) {
                    System.out.println(ColoresConsola.AMARILLO + "[AVISO] No se encontraron productos que coincidan con '" + filtroNombre + "'." + ColoresConsola.RESET);
                } else {
                    System.out.println("------------------------------------------------------------------------------------------------------------------------");
                    System.out.println("Coincidencias encontradas: " + contador);
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] Error al ejecutar la búsqueda por nombre." + ColoresConsola.RESET);
        }
    }

    /**
     * Consulta y muestra los productos cuyo stock sea estrictamente menor a un umbral indicado,
     * ordenados primero por stock ascendente y después por código[cite: 13, 16].
     */
    public static void consultarProductosStockInferior() {
        System.out.println("\n=== CONSULTA DE PRODUCTOS CON STOCK INFERIOR A UN UMBRAL ===");

        int umbral = LectorConsola.leerEnteroPositivo("Introduce el umbral de stock (número entero >= 0): ");

        // Consulta SQL aplicando la condición estricta stock < umbral y ordenación secundaria
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, c.nombre AS categoria_nombre, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "WHERE p.stock < ? " +
                     "ORDER BY p.stock ASC, p.codigo ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, umbral);

            try (ResultSet rs = pst.executeQuery()) {
                boolean hayRegistros = false;
                int contador = 0;

                System.out.printf("%-12s %-30s %-10s %-8s %-25s %-25s%n", "CÓDIGO", "NOMBRE", "PRECIO", "STOCK", "CATEGORÍA", "PROVEEDOR");
                System.out.println("------------------------------------------------------------------------------------------------------------------------");

                while (rs.next()) {
                    hayRegistros = true;
                    contador++;

                    System.out.printf("%-12s %-30s %-10.2f %-8d %-25s %-25s%n",
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            rs.getString("categoria_nombre"),
                            rs.getString("proveedor_nombre")
                    );
                }

                if (!hayRegistros) {
                    System.out.println(ColoresConsola.AMARILLO + "[AVISO] No hay ningún producto con stock estrictamente menor a " + umbral + "." + ColoresConsola.RESET);
                } else {
                    System.out.println("------------------------------------------------------------------------------------------------------------------------");
                    System.out.println("Productos con stock bajo el umbral: " + contador);
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] Error al consultar los productos por umbral de stock." + ColoresConsola.RESET);
        }
    }
}
