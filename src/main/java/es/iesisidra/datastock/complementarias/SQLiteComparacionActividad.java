package es.iesisidra.datastock.complementarias;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Clase de actividad para la comparativa y demostración de SQLite frente a MySQL en DataStock.
 */
public class SQLiteComparacionActividad {

    public static void ejecutarComparacion() {
        String url = "jdbc:sqlite::memory:"; // Base de datos SQLite temporal en memoria
        System.out.println("\n[SQLITE] Iniciando prueba de comparación SQLite (Memoria)...");

        try (Connection conexion = DriverManager.getConnection(url)) {
            if (conexion != null) {
                System.out.println("[SQLITE] Conexión establecida con éxito con SQLite en memoria.");

                // 1. Crear tabla de prueba
                String sqlCrear = "CREATE TABLE productos_test (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL, " +
                        "stock INTEGER NOT NULL)";
                try (Statement stmt = conexion.createStatement()) {
                    stmt.execute(sqlCrear);
                    System.out.println("[SQLITE] Tabla 'productos_test' creada correctamente.");
                }

                // 2. Insertar datos
                String sqlInsert = "INSERT INTO productos_test (nombre, stock) VALUES (?, ?)";
                try (PreparedStatement pstmt = conexion.prepareStatement(sqlInsert)) {
                    pstmt.setString(1, "Producto SQLite Demo");
                    pstmt.setInt(2, 50);
                    pstmt.executeUpdate();
                    System.out.println("[SQLITE] Datos de prueba insertados.");
                }

                // 3. Consultar datos
                String sqlSelect = "SELECT id, nombre, stock FROM productos_test";
                try (Statement stmt = conexion.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlSelect)) {
                    System.out.println("[SQLITE] Resultados de la consulta:");
                    while (rs.next()) {
                        System.out.println("  - ID: " + rs.getInt("id") + 
                                           ", Nombre: " + rs.getString("nombre") + 
                                           ", Stock: " + rs.getInt("stock"));
                    }
                }
                System.out.println("[SQLITE] Prueba completada con éxito.\n");
            }
        } catch (Exception e) {
            System.err.println("[SQLITE] Error durante la ejecución de SQLite: " + e.getMessage());
        }
    }
}