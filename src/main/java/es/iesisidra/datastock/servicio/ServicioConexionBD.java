package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;

/**
 * Servicio encargado de gestionar la comprobación de la conexión y la extracción
 * de información estructural de la base de datos MySQL (Requisito RF-JDBC-01).
 */
public class ServicioConexionBD {
    /**
     * Comprueba la conectividad con la base de datos, obtiene metadatos del gestor
     * y realiza un recuento de los productos almacenados en la tabla correspondiente.
     */

    public static void comprobarConexionInformacion(){
        // Consulta SQL sencilla para cumplir con el requisito de contar los productos actuales en la BD
        String sqlCount = "SELECT COUNT (*) AS total FROM producto";

        // Estructura try-with-resources: por qué se usa aquí y qué sirve.
        // Garantiza que los recursos pesados (Connection, PreparedStatement, ResultSet) 
        // se cierren automáticamente al salir del bloque, previniendo fugas de memoria o bloqueos
        try (Connection con = ConexionBD.conectar();
            PreparedStatement pst = con.prepareStatement(sqlCount);
            ResultSet rs = pst.executeQuery()){
        // Obtención de metadatos del sistema gestor de base de datos
        // Por qué se usa DatabaseMetaData: permite consultar en tiempo de ejecución información 
        // estructural del SGBD sin hardcodear valores en el código Java.
        DatabaseMetaData metaData = con.getMetaData();

        String nombreBD = con.getCatalog();
        String nombreGestor = metaData.getDatabaseProductName();
        String versionGestor = metaData.getDatabaseProductVersion();

        //Extracción del resultado del recuento de productos
        int totalProductos = 0;
        if(rs.next()){
            totalProductos = rs.getInt("total");
        }
        // Presentación de resultados correctos en color verde según las convenciones de la práctica
        System.out.println(ColoresConsola.VERDE + "[OK] Conexión establecida correctamente." + ColoresConsola.RESET);
        System.out.println("Base de datos: " + nombreBD);
        System.out.println("Gestor: " + nombreGestor);
        System.out.println("Versión: " + versionGestor);
        System.out.println("Productos almacenados en MySQL: " + totalProductos);
        }catch (SQLException e){
            // Tratamiento de errores controlado: captura excepciones de SQL (servidor apagado, credenciales inválidas, esquema ausente)
            // Por qué está aquí: evita que la aplicación sufra un cierre abrupto y muestra un mensaje amigable en rojo
            System.out.println(ColoresConsola.ROJO + "[ERROR] No se pudo establecer la conexión con MySQL." + ColoresConsola.RESET);
            System.out.println("[DETALLE] Verifique que el servidor esté activo y las credenciales sean correctas.");
        }
    }
}
