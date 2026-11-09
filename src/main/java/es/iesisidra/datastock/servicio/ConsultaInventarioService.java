package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import es.iesisidra.datastock.modelo.Producto;

public class ConsultaInventarioService {

    private final Inventario inventario;

    public ConsultaInventarioService(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Devuelve una copia de los productos ordenada por código. Se crea una
     * lista nueva para ordenar sin cambiar el orden de la lista original del
     * inventario.
     */
    /**
     * Devuelve una copia de los productos ordenada por código.
     */
    public List<Producto> obtenerProductosOrdenados() {
        List<Producto> ordenados = new ArrayList<>(inventario.getProductos());

        ordenados.sort(
                Comparator.comparing(
                        Producto::getCodigo,
                        String.CASE_INSENSITIVE_ORDER));

        return ordenados;
    }

    /**
     * Busca un producto por su código, sin distinguir mayúsculas y minúsculas.
     * Recorre la lista y compara los códigos después de quitar espacios
     * exteriores. Devuelve el producto encontrado o null si no existe.
     */
    /**
     * Busca un producto por su código.
     */
    public Producto buscarPorCodigo(String codigo) {
        Producto encontrado = null;

        for (Producto p : inventario.getProductos()) {
            if (p.getCodigo() != null
                    && p.getCodigo().trim()
                            .equalsIgnoreCase(codigo.trim())) {
                encontrado = p;
            }
        }

        return encontrado;
    }

    /**
     * Busca productos cuyo nombre contenga el texto indicado. La comparación no
     * distingue mayúsculas y minúsculas. Los resultados se guardan en una lista
     * nueva, por lo que no se modifica el inventario.
     */
    /**
     * Busca productos cuyo nombre contenga el texto indicado.
     */
    public List<Producto> buscarPorNombre(String texto) {
        List<Producto> encontrados = new ArrayList<>();

        String textoBuscado = texto.trim().toLowerCase(Locale.ROOT);

        for (Producto p : inventario.getProductos()) {
            if (p.getNombre() != null
                    && p.getNombre().toLowerCase(Locale.ROOT)
                            .contains(textoBuscado)) {
                encontrados.add(p);
            }
        }

        return encontrados;
    }
}
