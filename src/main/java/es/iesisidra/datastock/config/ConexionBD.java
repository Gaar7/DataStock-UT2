package es.iesisidra.datastock.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase utilitaria encargada de gestionar y proporcionar las conexiones JDBC
 * a la base de datos MySQL de DataStock.
 */

public class ConexionBD {

    // Objeto para almacenar las propiedades de configuración cargadas desde el fichero externo
    private static final Properties props = new Properties();

    // Bloque estático: se ejecuta una sola vez al cargar la clase en memoria para leer las credenciales
    static{
        // Por qué está aquí: aislar la lectura del fichero de configuración evita repetir código
        // y garantiza que las propiedades estén listas antes de intentar conectar.
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("database.properties")){
            if (input == null){
                throw new RuntimeException("No se ha encontrado el fichero database.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException ("Error al cargar la configuración" + e);
        }
    }

    /**
     * Establece y devuelve una nueva conexión con la base de datos MySQL.
     * 
     * @return Objeto Connection activo.
     * @throws SQLException Si ocurre un error al conectar (credenciales erróneas, servidor apagado, etc.).
     */
    public static Connection conectar() throws SQLException{
        // Qué hace esta línea: utiliza el DriverManager de JDBC para abrir un canal de comunicación
        // con el SGBD utilizando la URL, el usuario y la contraseña definidos en las propiedades.
        // Por qué se usa: es el método estándar en JDBC para obtener la sesión activa con la base de datos.
        return DriverManager.getConnection(
            props.getProperty("jdbc.url"),
            props.getProperty("jdbc.user"),
            props.getProperty("jdbc.password")
        );
    }
}
