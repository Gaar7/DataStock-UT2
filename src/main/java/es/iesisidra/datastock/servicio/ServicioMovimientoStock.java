package es.iesisidra.datastock.servicio;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Clase de servicio encargada de gestionar los movimientos de stock
 * invocando procedimientos almacenados en la base de datos MySQL (Requisito RF-JDBC-05).
 */
public class ServicioMovimientoStock {

    /**
     * Registra un movimiento de stock (entrada o salida) para un producto existente,
     * invocando un procedimiento almacenado en el SGBD mediante un CallableStatement.
     */
    public static void registrarMovimientoStock() {
        System.out.println("\n=== REGISTRAR MOVIMIENTO DE STOCK ===");

        // 1. Solicitud y normalización del código del producto
        String codigo = LectorConsola.leerCadenaNoVacia("Introduce el código del producto: ").trim().toUpperCase();

        // 2. Solicitud y validación del tipo de movimiento (E = Entrada, S = Salida)
        String tipo;
        while (true) {
            tipo = LectorConsola.leerCadenaNoVacia("Introduce el tipo de movimiento ('E' para Entrada, 'S' para Salida): ").trim().toUpperCase();
            if (tipo.equals("E") || tipo.equals("S")) {
                break;
            }
            System.out.println(ColoresConsola.ROJO + "[ERROR] Tipo de movimiento no válido. Debe introducir 'E' o 'S'." + ColoresConsola.RESET);
        }

        // 3. Solicitud de la cantidad (entero positivo)
        int cantidad = LectorConsola.leerEnteroPositivo("Introduce la cantidad (mayor que 0): ");

        // 4. Solicitud de observaciones opcionales
        String observaciones = LectorConsola.leerLinea("Introduce observaciones (opcional, pulse Intro para dejar vacío): ");
        if (observaciones.isEmpty()) {
            observaciones = null;
        }

        // Sintaxis JDBC para invocar un procedimiento almacenado mediante llaves y la palabra clave CALL
        // Por qué se usa CallableStatement: es la interfaz estándar en JDBC diseñada específicamente 
        // para ejecutar rutinas almacenadas en el SGBD de manera eficiente y segura.
        String sqlCallable = "{CALL sp_registrar_movimiento(?, ?, ?, ?)}";

        // Uso de try-with-resources para garantizar el cierre automático y seguro de la conexión y el CallableStatement
        try (Connection con = ConexionBD.conectar();
             CallableStatement cstmt = con.prepareCall(sqlCallable)) {

            // Asignación secuencial de los parámetros de entrada requeridos por el procedimiento almacenado
            cstmt.setString(1, codigo);
            cstmt.setString(2, tipo);
            cstmt.setInt(3, cantidad);
            
            if (observaciones == null) {
                cstmt.setNull(4, Types.VARCHAR);
            } else {
                cstmt.setString(4, observaciones);
            }

            // Ejecución de la llamada al procedimiento almacenado en el SGBD
            cstmt.execute();

            System.out.println(ColoresConsola.VERDE + "[OK] Movimiento de stock registrado correctamente en el sistema." + ColoresConsola.RESET);

        } catch (SQLException e) {
            // Captura de errores devueltos por el SGBD (por ejemplo, stock insuficiente para una salida o código de producto inexistente)
            System.out.println(ColoresConsola.ROJO + "[ERROR] No se pudo registrar el movimiento de stock en la base de datos." + ColoresConsola.RESET);
            System.out.println("[DETALLE SQL] " + e.getMessage());
        }
    }
}