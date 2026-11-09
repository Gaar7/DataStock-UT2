package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Estado común del inventario durante la ejecución de UT1.
 */
/**
 * Esta clase se encarga de gestionar el inventario durante la ejecución.
 * Mantiene la lista común de productos y ofrece los métodos para importar,
 * consultar, exportar, guardar y recuperar esos productos.
 *
 * Se relaciona con Producto porque la lista almacena objetos de esa clase.
 * MenuPrincipal utiliza los métodos públicos de Inventario cuando el usuario
 * elige una opción en los menús.
 */
public class Inventario {

    // Lista que contiene los productos cargados en memoria.
    // Es final para que la referencia a la lista no se sustituya, aunque
    // sí podemos añadir, eliminar o consultar sus elementos.
    private final List<Producto> productos = new ArrayList<>();

    /**
     * Devuelve la lista de productos del inventario. MenuPrincipal la utiliza,
     * por ejemplo, para mostrar cuántos productos hay y para comprobar si el
     * inventario está vacío.
     */
    public List<Producto> getProductos() {
        return productos;
    }
}
