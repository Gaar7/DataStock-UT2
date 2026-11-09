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
 * Clase de servicio encargada de gestionar todas las operaciones CRUD y reglas
 * de negocio asociadas a las categorías de productos (Requisito RF-JDBC-02).
 */
public class ServicioCategoria {

    /**
     * Da de alta una nueva categoría en la base de datos tras validar sus datos.
     */
    public static void darDeAltaCategoria() {
        System.out.println("\n=== ALTA DE CATEGORÍA ===");
        
        int id = LectorConsola.leerEnteroPositivo("Introduce el ID de la categoría (entero positivo): ");
        String nombre = LectorConsola.leerCadenaNoVacia("Introduce el nombre de la categoría (máximo 60 caracteres): ");
        
        // Normalización de datos: se eliminan espacios sobrantes al principio y al final
        nombre = nombre.trim();
        
        if (nombre.length() > 60) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] El nombre no puede superar los 60 caracteres." + ColoresConsola.RESET);
            return;
        }

        String sqlInsert = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";

        // Uso de try-with-resources para asegurar el cierre automático de la conexión y la sentencia preparada
        try (Connection con = ConexionBD.conectar();
             PreparedStatement pst = con.prepareStatement(sqlInsert)) {

            // Asignación de parámetros al PreparedStatement para prevenir inyección SQL
            pst.setInt(1, id);
            pst.setString(2, nombre);

            int filasAfectadas = pst.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println(ColoresConsola.VERDE + "[OK] Categoría registrada correctamente." + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            // Captura de excepciones específicas de SQL (por ejemplo, duplicidad de clave primaria o nombre único)
            System.err.println(ColoresConsola.ROJO + "[ERROR] No se pudo registrar la categoría. El ID o el nombre ya existen." + ColoresConsola.RESET);
        }
    }

    /**
     * Consulta y muestra por consola todas las categorías ordenadas por ID ascendente.
     */
    public static void listarCategorias() {
        System.out.println("\n=== LISTADO DE CATEGORÍAS ===");
        
        String sqlSelect = "SELECT id, nombre FROM categoria ORDER BY id ASC";

        try (Connection con = ConexionBD.conectar();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sqlSelect)) {

            boolean hayRegistros = false;
            System.out.printf("%-10s %-40s%n", "ID", "NOMBRE");
            System.out.println("--------------------------------------------------");

            // Recorrido del ResultSet mediante el cursor interno
            while (rs.next()) {
                hayRegistros = true;
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                System.out.printf("%-10d %-40s%n", id, nombre);
            }

            if (!hayRegistros) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] No hay categorías registradas." + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] Error al consultar las categorías en la base de datos." + ColoresConsola.RESET);
        }
    }

    /**
     * Permite modificar el nombre de una categoría existente buscando por su ID.
     */
    public static void modificarCategoria() {
        System.out.println("\n=== MODIFICAR CATEGORÍA ===");
        
        int id = LectorConsola.leerEnteroPositivo("Introduce el ID de la categoría a modificar: ");
        
        String sqlSelectId = "SELECT nombre FROM categoria WHERE id = ?";
        String sqlUpdate = "UPDATE categoria SET nombre = ? WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pstSelect = con.prepareStatement(sqlSelectId)) {

            pstSelect.setInt(1, id);
            try (ResultSet rs = pstSelect.executeQuery()) {
                if (!rs.next()) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] La categoría con ID " + id + " no existe." + ColoresConsola.RESET);
                    return;
                }
                System.out.println("Nombre actual: " + rs.getString("nombre"));
            }

            String nuevoNombre = LectorConsola.leerCadenaNoVacia("Introduce el nuevo nombre: ").trim();
            
            if (nuevoNombre.length() > 60) {
                System.err.println(ColoresConsola.ROJO + "[ERROR] El nombre no puede superar los 60 caracteres." + ColoresConsola.RESET);
                return;
            }

            String confirmar = LectorConsola.leerCadenaNoVacia("¿Confirmas la modificación? (S/N): ");
            if (!confirmar.equalsIgnoreCase("S")) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] Modificación cancelada por el usuario." + ColoresConsola.RESET);
                return;
            }

            try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate)) {
                pstUpdate.setString(1, nuevoNombre);
                pstUpdate.setInt(2, id);
                
                int filas = pstUpdate.executeUpdate();
                if (filas > 0) {
                    System.out.println(ColoresConsola.VERDE + "[OK] Categoría modificada correctamente." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.err.println(ColoresConsola.ROJO + "[ERROR] No se pudo modificar la categoría. El nombre puede estar repetido." + ColoresConsola.RESET);
        }
    }

    /**
     * Elimina una categoría por su ID, respetando las restricciones de integridad referencial.
     */
    public static void eliminarCategoria() {
        System.out.println("\n=== ELIMINAR CATEGORÍA ===");
        
        int id = LectorConsola.leerEnteroPositivo("Introduce el ID de la categoría a eliminar: ");
        
        String sqlSelect = "SELECT id, nombre FROM categoria WHERE id = ?";
        String sqlDelete = "DELETE FROM categoria WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {

            pstSelect.setInt(1, id);
            try (ResultSet rs = pstSelect.executeQuery()) {
                if (!rs.next()) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] La categoría con ID " + id + " no existe." + ColoresConsola.RESET);
                    return;
                }
                System.out.println("Datos de la categoría -> ID: " + rs.getInt("id") + ", Nombre: " + rs.getString("nombre"));
            }

            String confirmar = LectorConsola.leerCadenaNoVacia("¿Confirmas la eliminación definitiva? (S/N): ");
            if (!confirmar.equalsIgnoreCase("S")) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] Eliminación cancelada." + ColoresConsola.RESET);
                return;
            }

            try (PreparedStatement pstDelete = con.prepareStatement(sqlDelete)) {
                pstDelete.setInt(1, id);
                int filas = pstDelete.executeUpdate();
                
                if (filas > 0) {
                    System.out.println(ColoresConsola.VERDE + "[OK] Categoría eliminada correctamente." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            // Captura el bloqueo por clave foránea si existen productos asociados a esta categoría
            System.err.println(ColoresConsola.ROJO + "[ERROR] No se puede eliminar la categoría porque tiene productos asociados (Integridad Referencial)." + ColoresConsola.RESET);
        }
    }
}