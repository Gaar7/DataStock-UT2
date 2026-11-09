package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Clase de servicio encargada de gestionar el mantenimiento completo de los
 * proveedores,
 * incluyendo validaciones de correo opcional, unicidad y control de integridad
 * referencial
 * (Requisito RF-JDBC-02).
 */

public class ServicioProveedor {

    /**
     * Da de alta un nuevo proveedor solicitando ID, nombre obligatorio y correo
     * electrónico opcional.
     */

    public static void darDeAltaProveedor() {
        System.out.println("\n=== ALTA DE PROVEEDOR ===");

        int id = LectorConsola.leerEnteroPositivo("Introduce el ID del proveedor (entero positivo): ");
        String nombre = LectorConsola.leerCadenaNoVacia("Introduce el nombre del proveedor (máximo 100 caracteres): ")
                .trim();

        if (nombre.length() > 100) {
            System.out.println(ColoresConsola.ROJO
                    + "[ERROR] El nombre del proveedor no puede superar los 100 caracteres." + ColoresConsola.RESET);
        }

        System.out.println("Introduce el correo electrónico (opcional, pulse Intro para dejar vacío): ");
        // Se lee la línea de texto; si se deja en blanco, se tratará como null
        String emailInput = LectorConsola
                .leerLinea("Introduce el correo electrónico (opcional, pulse Intro para dejar vacío): ");
        String email = emailInput.isEmpty() ? null : emailInput;

        if (email != null) {
            if (email.length() > 120) {
                System.out.println(ColoresConsola.ROJO
                        + "[ERROR] El correo electrónico no puede superar los 120 caracteres." + ColoresConsola.RESET);
            }
            // Validación básica de formato de correo asegurando que contenga '@' y '.'
            if (!email.contains("@") || !email.contains(".")) {
                System.out.println(
                        ColoresConsola.ROJO + "[ERROR] El formato del correo es erroneo." + ColoresConsola.ROJO);
            }
        }
        String sqlInsert = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";
        // Uso de try-with-resources para garantizar el cierre seguro de los recursos
        // JDBC de la conexión y sentencia
        try (Connection con = ConexionBD.conectar();
                PreparedStatement pst = con.prepareStatement(sqlInsert)) {

            pst.setInt(1, id);
            pst.setString(2, nombre);

            // Gestión de parámetros nulos: si el email es null, se usa setNull para
            // indicarlo explícitamente a SQL
            if (email == null) {
                pst.setNull(3, Types.VARCHAR);
            } else {
                pst.setString(3, email);
            }

            int filasAfectadas = pst.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println(
                        ColoresConsola.VERDE + "[OK] Proveedor registrado correctamente." + ColoresConsola.RESET);
            }
        } catch (SQLException e) {
            // Captura de errores de SQL por duplicidad de clave primaria (ID) o de correo
            // electrónico único
            System.err.println(ColoresConsola.ROJO
                    + "[ERROR] No se pudo registrar el proveedor. El ID o el correo electrónico ya están registrados."
                    + ColoresConsola.RESET);
        }
    }

    public static void listarProveedores() {
        System.out.println("\n||||LISTADO DE PROVEEDORES||||\n");

        String sqlSelect = "SELECT id, nombre, email FROM proveedor ORDER BY id ASC";

        try (Connection con = ConexionBD.conectar();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sqlSelect)) {

            boolean hayRegistros = false;
            System.out.println("%-10s %-40s %-30s%n\", \"ID\", \"NOMBRE\", \"EMAIL");
            System.out.println("------------------------------------------------------------------------------\n");

            // Recorrido secuencial del ResultSet mediante el cursor interno
            while (rs.next()) {
                hayRegistros = true;
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String email = rs.getString("email");
                // Si el correo es nulo en la base de datos, se representa visualmente con un
                // guion
                String emailMostrar = (email == null || email.isEmpty()) ? "-" : email;
                System.out.printf("%-10d %-40s %-30s%n", id, nombre, emailMostrar);
            }

            if (!hayRegistros) {
                System.out.println(ColoresConsola.AMARILLO
                        + "[AVISO] No hay proveedores registrados en la base de datos." + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] Error al consultar los proveedores en la base de datos."
                    + ColoresConsola.RESET);
        }
    }

    /**
     * Permite modificar el nombre y/o el correo electrónico de un proveedor
     * existente.
     * Permite conservar valores pulsando Intro o eliminar el correo escribiendo un
     * guion (-).
     */
    public static void modificarProveedor() {
        System.out.println("\n||||MODIFICAR PROVEEDOR||||\n");

        int id = LectorConsola.leerEnteroPositivo("Introduce el ID del proveedor a modificar: ");
        String sqlSelectId = "SELECT nombre, email FROM proveedor WHERE id = ?";
        String sqlUpdate = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
                PreparedStatement pstSelect = con.prepareStatement(sqlSelectId)) {

            pstSelect.setInt(1, id);
            String nombreActual = "";
            String emailActual = "";

            try (ResultSet rs = pstSelect.executeQuery()) {
                if (!rs.next()) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] El proveedor con ID " + id + " no existe."
                            + ColoresConsola.RESET);
                    return;
                }
                nombreActual = rs.getString("nombre");
                emailActual = rs.getString("email");

                System.out.println("Nombre actual: " + nombreActual);
                System.out.println("Correo actual: " + (emailActual == null ? "-" : emailActual));
            }

            String nuevoNombreInput = LectorConsola
                    .leerLinea("Introduce el nuevo nombre (pulse Intro para conservar el actual): ");
            String nuevoNombre = nuevoNombreInput.isEmpty() ? nombreActual : nuevoNombreInput;

            if (nuevoNombre.length() > 100) {
                System.err.println(ColoresConsola.ROJO + "[ERROR] El nombre no puede superar los 100 caracteres."
                        + ColoresConsola.RESET);
                return;
            }

            String nuevoEmailInput = LectorConsola
                    .leerLinea("Introduce el nuevo correo (pulse Intro para conservar, escriba '-' para eliminar): ");
            String nuevoEmail = emailActual;

            if (nuevoEmailInput.equals("-")) {
                nuevoEmail = null; // Mecanismo para dejar el campo a NULL
            } else if (!nuevoEmailInput.isEmpty()) {
                if (nuevoEmailInput.length() > 120) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] El correo no puede superar los 120 caracteres."
                            + ColoresConsola.RESET);
                    return;
                }
                if (!nuevoEmailInput.contains("@") || !nuevoEmailInput.contains(".")) {
                    System.err.println(
                            ColoresConsola.ROJO + "[ERROR] Formato de correo no válido." + ColoresConsola.RESET);
                    return;
                }
                nuevoEmail = nuevoEmailInput;
            }

            String confirmar = LectorConsola.leerLinea("¿Confirmas la modificación? (S/N): ");
            
            if (!confirmar.equalsIgnoreCase("S")) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] Modificación cancelada por el usuario."
                        + ColoresConsola.RESET);
                return;
            }

            try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate)) {
                pstUpdate.setString(1, nuevoNombre);

                if (nuevoEmail == null) {
                    pstUpdate.setNull(2, Types.VARCHAR);
                } else {
                    pstUpdate.setString(2, nuevoEmail);
                }

                pstUpdate.setInt(3, id);

                int filas = pstUpdate.executeUpdate();
                if (filas > 0) {
                    System.out.println(
                            ColoresConsola.VERDE + "[OK] Proveedor modificado correctamente." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.err.println(ColoresConsola.ROJO
                    + "[ERROR] No se pudo modificar el proveedor. El correo electrónico puede estar repetido."
                    + ColoresConsola.RESET);
        }
    }

    /**
     * Elimina un proveedor por su ID, controlando las restricciones de integridad
     * referencial.
     */
    public static void eliminarProveedor() {
        System.out.println("\n=== ELIMINAR PROVEEDOR ===");

        int id = LectorConsola.leerEnteroPositivo("Introduce el ID del proveedor a eliminar: ");

        String sqlSelect = "SELECT id, nombre, email FROM proveedor WHERE id = ?";
        String sqlDelete = "DELETE FROM proveedor WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
                PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {

            pstSelect.setInt(1, id);
            try (ResultSet rs = pstSelect.executeQuery()) {
                if (!rs.next()) {
                    System.err.println(ColoresConsola.ROJO + "[ERROR] El proveedor con ID " + id + " no existe."
                            + ColoresConsola.RESET);
                    return;
                }
                String emailMostrado = rs.getString("email") == null ? "-" : rs.getString("email");
                System.out.println("Datos del proveedor -> ID: " + rs.getInt("id") + ", Nombre: "
                        + rs.getString("nombre") + ", Email: " + emailMostrado);
            }

            String confirmar = LectorConsola.leerLinea("¿Confirmas la eliminación definitiva? (S/N): ");
            if (!confirmar.equalsIgnoreCase("S")) {
                System.out.println(ColoresConsola.AMARILLO + "[AVISO] Eliminación cancelada." + ColoresConsola.RESET);
                return;
            }

            try (PreparedStatement pstDelete = con.prepareStatement(sqlDelete)) {
                pstDelete.setInt(1, id);
                int filas = pstDelete.executeUpdate();

                if (filas > 0) {
                    System.out.println(
                            ColoresConsola.VERDE + "[OK] Proveedor eliminado correctamente." + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            // Captura de excepción por clave foránea si el proveedor está siendo
            // referenciado por algún producto
            System.err.println(ColoresConsola.ROJO
                    + "[ERROR] No se puede eliminar el proveedor porque tiene productos asociados (Integridad Referencial)."
                    + ColoresConsola.RESET);
        }
    }
}
