package es.iesisidra.datastock.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Representa un producto del catálogo de DataStock.
 *
 * Esta clase pertenece al paquete modelo porque contiene los datos de cada
 * producto, sin encargarse de mostrar menús ni de gestionar ficheros.
 *
 * En UT1 los productos se guardan en memoria dentro de un Inventario.
 * Al implementar Serializable, también pueden guardarse en un fichero
 * binario y recuperarse posteriormente.
 */
public class Producto implements Serializable {
    /*
     * Identificador de versión de la clase para la serialización.
     * Permite comprobar que la versión de la clase utilizada para leer
     * un objeto es compatible con la que se utilizó para guardarlo.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    // Código identificador del producto. Debe ser único en el inventario.
    private String codigo;
    // Nombre o descripción comercial del producto.
    private String nombre;
    /*
     * Precio del producto.
     * Se utiliza BigDecimal en lugar de double para representar importes
     * monetarios con mayor precisión y evitar errores de redondeo binario.
     */
    private BigDecimal precio;
    // Número de unidades disponibles del producto.
    private int stock;
    // Identificador de la categoría a la que pertenece el producto.
    private int categoriaId;

    private int proveedorId;

    /**
     * Constructor vacío.
     *
     * Permite crear un Producto sin indicar inicialmente sus datos.
     * Es útil para bibliotecas de intercambio de datos como Gson,
     * que pueden crear objetos y rellenar después sus atributos.
     */
    public Producto() {
    }

    /** Construye un producto con los campos previstos en el enunciado. */
    /**
     * Constructor completo.
     *
     * Recibe todos los datos necesarios para crear un producto y los asigna
     * a los atributos del objeto mediante la referencia this.
     *
     * @param codigo      código identificador del producto.
     * @param nombre      nombre comercial del producto.
     * @param precio      precio del producto.
     * @param stock       unidades disponibles.
     * @param categoriaId identificador de la categoría.
     * @param proveedorId identificador del proveedor.
     */
    public Producto(String codigo, String nombre, BigDecimal precio, int stock,
            int categoriaId, int proveedorId) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.proveedorId = proveedorId;
    }

    /** @return código único del producto (máximo 12 caracteres). */
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /** @return nombre comercial del artículo. */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** @return precio monetario, representado con BigDecimal. */
    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    /** @return unidades disponibles actualmente. */
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /** @return identificador de la categoría. */
    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    /** @return identificador del proveedor. */
    public int getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(int proveedorId) {
        this.proveedorId = proveedorId;
    }

    /**
     * Representación textual del producto.
     * Sobreescribe a toString() para que a la hora de imprimir un objeto Producto
     * se pueda consultar el contenido de sus atributos.
     * 
     * @return cadena con los datos del producto.
     */
    @Override
    public String toString() {
        return "Producto{codigo='" + codigo + "', nombre='" + nombre + "', precio=" + precio
                + ", stock=" + stock + ", categoriaId=" + categoriaId
                + ", proveedorId=" + proveedorId + "}";
    }
}
