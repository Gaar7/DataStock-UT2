package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Clase de servicio encargada de gestionar el mantenimiento CRUD completo de los productos,
 * aplicando validaciones de formato, uso de BigDecimal para precios y control estricto 
 * de la integridad referencial con categorías y proveedores (Requisito RF-JDBC-03).
 */
public class ServicioProducto {

    /**
     * Da de alta un nuevo producto tras validar todas sus restricciones de negocio
     * y comprobar la existencia previa de su categoría y proveedor en la base de datos.
     */
    public static void darDeAltaProducto() {
        System.out.println("\n=== ALTA DE PRODUCTO ===");

        // 1. Solicitud y normalización del código de producto
        String codigo = LectorConsola.leerCadenaNoVacia("Introduce el código del producto (1-12 caracteres, A-Z, 0-9 y '-'): ").trim().toUpperCase();
        
        // Validación estricta mediante expresión regular según el script SQL y la guía de prácticas
        if (!codigo.matches("^[A-Z0-9-]{1,12}$")) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] El código no cumple el formato establecido (1-12 caracteres permitidos: letras mayúsculas, dígitos y guiones)." + ColoresConsola.RESET);
            return;
        }

        // 2. Solicitud y validación del nombre del producto
        String nombre = LectorConsola.leerCadenaNoVacia("Introduce el nombre del producto (3-100 caracteres): ").trim();
        if (nombre.length() < 3 || nombre.length() > 100) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] El nombre del producto debe tener una longitud de entre 3 y 100 caracteres." + ColoresConsola.RESET);
            return;
        }

        // 3. Solicitud y validación del precio utilizando BigDecimal
        BigDecimal precio = null;
        try {
            String precioStr = LectorConsola.leerCadenaNoVacia("Introduce el precio del producto (mayor que 0, máximo 2 decimales): ").trim();
            precio = new BigDecimal(precioStr);
            
            // Comprobación de que el precio sea estrictamente positivo (mayor que cero)
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                System.err.println(ColoresConsola.ROJO + "[ERROR] El precio debe ser un valor numérico mayor que cero." + ColoresConsola.RESET);
                return;
            }
            
            // Comprobación de escala (máximo dos decimales admitidos por el tipo DECIMAL(10,2))
            if (precio.scale() > 2) {
                System.err.println(ColoresConsola.ROJO + "[ERROR] El precio no puede tener más de dos cifras decimales." + ColoresConsola.RESET);
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] Formato de precio inválido." + ColoresConsola.RESET);
            return;
        }

        // 4. Solicitud y validación del stock inicial
        int stock = LectorConsola.leerEnteroPositivo("Introduce el stock inicial (entero mayor o igual a 0): ");
        // Nota: LectorConsola exige > 0 por defecto, por lo que si se permite stock 0, 
        // se puede leer como entero general validando que no sea negativo.

        // 5. Solicitud de IDs relacionales
        int categoriaId = LectorConsola.leerEnteroPositivo("Introduce el ID de la categoría asociada: ");
        int proveedorId = LectorConsola.leerEnteroPositivo("Introduce el ID del proveedor asociado: ");

        // Establecimiento de la conexión y validación de claves foráneas antes de insertar
        String sqlVerificarCat = "SELECT id FROM categoria WHERE id = ?";
        String sqlVerificarProv = "SELECT id FROM proveedor WHERE id = ?";
        String sqlInsertProducto = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";

        // Uso de try-with-resources para garantizar el cierre seguro de los recursos JDBC
        try (Connection con = ConexionBD.conectar()) {

            // Verificación de existencia de la categoría (Integridad Referencial)
            try (PreparedStatement pstCat = con.prepareStatement(sqlVerificarCat)) {
                pstCat.setInt(1, categoriaId);
                try (ResultSet rsCat = pstCat.executeQuery()) {
                    if (!rsCat.next()) {
                        System.err.println(ColoresConsola.ROJO + "[ERROR] La categoría con ID " + categoriaId + " no existe en la base de datos." + ColoresConsola.RESET);
                        return;
                    }
                }
            }

            // Verificación de existencia del proveedor (Integridad Referencial)
            try (PreparedStatement pstProv = con.prepareStatement(sqlVerificarProv)) {
                pstProv.setInt(1, proveedorId);
                try (ResultSet rsProv = pstProv.executeQuery()) {
                    if (!rsProv.next()) {
                        System.err.println(ColoresConsola.ROJO + "[ERROR] El proveedor con ID " + proveedorId + " no existe en la base de datos." + ColoresConsola.RESET);
                        return;
                    }
                }
            }

            // Inserción del producto mediante PreparedStatement para prevenir inyección SQL
            try (PreparedStatement pstInsert = con.prepareStatement(sqlInsertProducto)) {
                pstInsert.setString(1, codigo);
                pstInsert.setString(2, nombre);
                pstInsert.setBigDecimal(3, precio); // Asignación segura de importes monetarios con BigDecimal
                pstInsert.setInt(4, stock);
                pstInsert.setInt(5, categoriaId);
                pstInsert.setInt(6, proveedorId);

                int filas = pstInsert.executeUpdate();
                if (filas > 0) {
                    System.out.println(ColoresConsola.VERDE + "[OK] Producto " + codigo + " guardado correctamente en MySQL." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] No se pudo insertar el producto. El código de producto ya existe en la base de datos." + ColoresConsola.RESET);
        }
    }

    /**
     * Consulta un producto específico introduciendo su código exacto,
     * mostrando su ficha completa y resolviendo los nombres mediante relaciones.
     */
    public static void consultarProductoPorCodigo() {
        System.out.println("\n=== CONSULTAR PRODUCTO POR CÓDIGO ===");

        String codigo = LectorConsola.leerCadenaNoVacia("Introduce el código del producto a consultar: ").trim().toUpperCase();

        // Consulta SQL con JOIN para recuperar los nombres descriptivos de categoría y proveedor
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, c.nombre AS cat_nombre, pr.nombre AS prov_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "WHERE p.codigo = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, codigo);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- FICHA DEL PRODUCTO ---");
                    System.out.println("Código: " + rs.getString("codigo"));
                    System.out.println("Nombre: " + rs.getString("nombre"));
                    System.out.println("Precio: " + rs.getBigDecimal("precio") + " EUR");
                    System.out.println("Stock actual: " + rs.getInt("stock"));
                    System.out.println("Categoría: " + rs.getString("cat_nombre"));
                    System.out.println("Proveedor: " + rs.getString("prov_nombre"));
                } else {
                    System.out.println(ColoresConsola.AMARILLO + "[AVISO] No existe ningún producto con el código " + codigo + "." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] Error al consultar el producto en la base de datos." + ColoresConsola.RESET);
        }
    }

    /**
     * Permite modificar los datos de un producto (nombre, precio, categoría y proveedor),
     * permitiendo conservar los valores actuales pulsando Intro. El stock y el código no se modifican directamente.
     */
    public static void modificarProducto() {
        System.out.println("\n=== MODIFICAR PRODUCTO ===");

        String codigo = LectorConsola.leerCadenaNoVacia("Introduce el código del producto a modificar: ").trim().toUpperCase();

        String sqlSelect = "SELECT nombre, precio, categoria_id, proveedor_id FROM producto WHERE codigo = ?";
        String sqlUpdate = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? WHERE codigo = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {

            pstSelect.setString(1, codigo);
            String nombreActual;
            BigDecimal precioActual;
            int catActual;
            int provActual;

            try (ResultSet rs = pstSelect.executeQuery()) {
                if (!rs.next()) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] El producto con código " + codigo + " no existe." + ColoresConsola.RESET);
                    return;
                }
                nombreActual = rs.getString("nombre");
                precioActual = rs.getBigDecimal("precio");
                catActual = rs.getInt("categoria_id");
                provActual = rs.getInt("proveedor_id");

                System.out.println("Valores actuales -> Nombre: " + nombreActual + ", Precio: " + precioActual + ", Cat ID: " + catActual + ", Prov ID: " + provActual);
            }

            // Lectura de nuevos valores opcionales
            String nuevoNombreInput = LectorConsola.leerLinea("Introduce el nuevo nombre (pulse Intro para conservar): ");
            String nuevoNombre = nuevoNombreInput.isEmpty() ? nombreActual : nuevoNombreInput;

            String nuevoPrecioInput = LectorConsola.leerLinea("Introduce el nuevo precio (pulse Intro para conservar): ");
            BigDecimal nuevoPrecio = precioActual;
            if (!nuevoPrecioInput.isEmpty()) {
                try {
                    nuevoPrecio = new BigDecimal(nuevoPrecioInput);
                    if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0 || nuevoPrecio.scale() > 2) {
                        System.err.println(ColoresConsola.ROJO + "[ERROR] Precio inválido. Se conservará el anterior." + ColoresConsola.RESET);
                        nuevoPrecio = precioActual;
                    }
                } catch (NumberFormatException e) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] Formato de precio erróneo. Se conservará el anterior." + ColoresConsola.RESET);
                }
            }

            // Actualización de categoría y proveedor con validación de existencia
            String nuevaCatInput = LectorConsola.leerLinea("Introduce el nuevo ID de categoría (pulse Intro para conservar): ");
            int nuevaCat = nuevaCatInput.isEmpty() ? catActual : Integer.parseInt(nuevaCatInput);

            String nuevoProvInput = LectorConsola.leerLinea("Introduce el nuevo ID de proveedor (pulse Intro para conservar): ");
            int nuevoProv = nuevoProvInput.isEmpty() ? provActual : Integer.parseInt(nuevoProvInput);

            String confirmar = LectorConsola.leerLinea("¿Confirmas la modificación del producto? (S/N): ");
            if (!confirmar.equalsIgnoreCase("S")) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] Modificación cancelada." + ColoresConsola.RESET);
                return;
            }

            try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate)) {
                pstUpdate.setString(1, nuevoNombre);
                pstUpdate.setBigDecimal(2, nuevoPrecio);
                pstUpdate.setInt(3, nuevaCat);
                pstUpdate.setInt(4, nuevoProv);
                pstUpdate.setString(5, codigo);

                int filas = pstUpdate.executeUpdate();
                if (filas > 0) {
                    System.out.println(ColoresConsola.VERDE + "[OK] Producto modificado correctamente." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] No se pudo modificar el producto. Compruebe que los IDs de categoría y proveedor existan." + ColoresConsola.RESET);
        }
    }

    /**
     * Elimina un producto por su código, exigiendo confirmación
     * y respetando las restricciones de integridad referencial del historial de movimientos.
     */
    public static void eliminarProducto() {
        System.out.println("\n=== ELIMINAR PRODUCTO ===");

        String codigo = LectorConsola.leerCadenaNoVacia("Introduce el código del producto a eliminar: ").trim().toUpperCase();

        String sqlSelect = "SELECT codigo, nombre, precio, stock FROM producto WHERE codigo = ?";
        String sqlDelete = "DELETE FROM producto WHERE codigo = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {

            pstSelect.setString(1, codigo);
            try (ResultSet rs = pstSelect.executeQuery()) {
                if (!rs.next()) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] El producto con código " + codigo + " no existe." + ColoresConsola.RESET);
                    return;
                }
                System.out.println("Datos -> Código: " + rs.getString("codigo") + ", Nombre: " + rs.getString("nombre") + ", Stock: " + rs.getInt("stock"));
            }

            String confirmar = LectorConsola.leerLinea("¿Confirmas la eliminación del producto? (S/N): ");
            if (!confirmar.equalsIgnoreCase("S")) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] Eliminación cancelada." + ColoresConsola.RESET);
                return;
            }

            try (PreparedStatement pstDelete = con.prepareStatement(sqlDelete)) {
                pstDelete.setString(1, codigo);
                int filas = pstDelete.executeUpdate();

                if (filas > 0) {
                    System.out.println(ColoresConsola.VERDE + "[OK] Producto eliminado correctamente de MySQL." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            // Captura el bloqueo por clave foránea si el producto posee movimientos registrados en movimiento_stock
            System.err.println(ColoresConsola.ROJO + "[ERROR] No se puede eliminar el producto porque tiene movimientos históricos asociados (Integridad Referencial)." + ColoresConsola.RESET);
        }
    }
}