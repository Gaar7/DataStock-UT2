
package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;

/**
 * Componente encargado de consultar un producto en MySQL mediante JDBC.
 *
 * Recibe el código del producto y devuelve sus datos junto con los nombres
 * de su categoría y proveedor. No solicita datos ni muestra resultados por
 * consola; esas tareas corresponden a la interfaz de usuario.
 */
public class ConsultaProductoJdbc {

    private static final String SQL_BUSCAR_POR_CODIGO = """
            SELECT p.codigo,
                           p.nombre,
                           p.precio,
                           p.stock,
                           p.categoria_id,
                           p.proveedor_id,
                           c.nombre AS categoria_nombre,
                           pr.nombre AS proveedor_nombre
                    FROM producto p
                    JOIN categoria c ON p.categoria_id = c.id
                    JOIN proveedor pr ON p.proveedor_id = pr.id
                    WHERE p.codigo = ?
                    """;
    
        /**
     * Busca un producto por su código exacto.
     *
     * El código se limpia de espacios exteriores y se convierte a mayúsculas,
     * manteniendo el comportamiento de la búsqueda que ya existía en UT2.
     * La consulta utiliza PreparedStatement para parametrizar el código.
     *
     * @param codigo código identificador del producto que se desea consultar.
     * @return un resultado con el producto, el nombre de su categoría y el de
     *         su proveedor; Optional.empty() si no existe.
     * @throws IllegalArgumentException si el código es null o está vacío.
     * @throws SQLException si se produce un error al conectar o consultar MySQL.
     */

    public Optional<ResultadoConsulta> buscarPorCodigo(String codigo) throws SQLException{
        if (codigo == null || codigo.isBlank()){
            throw new IllegalArgumentException(
                "El código del producto no puede estar vacío."
            );
        }
        String codigoNormalizado = codigo.trim().toUpperCase(Locale.ROOT);
        ResultadoConsulta resultado = null;
        //Los recursos JDBC se cierran automáticamente
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(SQL_BUSCAR_POR_CODIGO)) {

            sentencia.setString(1, codigoNormalizado);

            try (ResultSet resultadoSQL = sentencia.executeQuery()) {

                if (resultadoSQL.next()) {

                    Producto producto = new Producto(
                            resultadoSQL.getString("codigo"),
                            resultadoSQL.getString("nombre"),
                            resultadoSQL.getBigDecimal("precio"),
                            resultadoSQL.getInt("stock"),
                            resultadoSQL.getInt("categoria_id"),
                            resultadoSQL.getInt("proveedor_id")
                    );

                    resultado = new ResultadoConsulta(
                            producto,
                            resultadoSQL.getString("categoria_nombre"),
                            resultadoSQL.getString("proveedor_nombre")
                    );
                }
            }
        }

        return Optional.ofNullable(resultado);
    }
        /**
     * Datos recuperados para presentar la ficha completa del producto.
     *
     * @param producto objeto de dominio con los datos del producto.
     * @param nombreCategoria nombre de la categoría asociada.
     * @param nombreProveedor nombre del proveedor asociado.
     */
    public record ResultadoConsulta(
            Producto producto,
            String nombreCategoria,
            String nombreProveedor) {
    }
}