package es.iesisidra.datastock.servicio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

public class SerializacionService {

    private final Inventario inventario;

    public SerializacionService(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Guarda el inventario en un fichero binario mediante serialización Java.
     * Recibe la ruta del fichero .dat desde MenuPrincipal.
     *
     * Comprueba que haya productos y que la ruta no apunte a un directorio.
     * ObjectOutputStream escribe una copia de la lista en el fichero.
     */
    public void guardarInventario(String rutaFichero) {
        Path ruta = Paths.get(rutaFichero);

        if (inventario.getProductos().isEmpty()) {
            System.out.println("No hay productos para guardar.");
        } else if (Files.exists(ruta) && !Files.isRegularFile(ruta)) {
            System.out.println("La ruta no es un fichero válido.");
        } else {
            try (ObjectOutputStream salida = new ObjectOutputStream(
                    new FileOutputStream(ruta.toFile()))) {

                salida.writeObject(new ArrayList<>(inventario.getProductos()));

                System.out.println("Inventario guardado correctamente.");

            } catch (IOException e) {
                System.out.println("Error al guardar: " + e.getMessage());
            }
        }

    }

    /**
     * Recupera una lista de productos desde un fichero serializado. Primero
     * comprueba que la ruta sea un fichero válido y después lee el objeto
     * guardado con ObjectInputStream.
     *
     * Antes de sustituir el inventario actual, comprueba que el objeto leído
     * sea una lista y que todos sus elementos sean Producto. Así evita dejar el
     * inventario a medio sustituir si el contenido no es válido.
     */
    public void recuperarInventario(String rutaFichero) {
        Path ruta = Paths.get(rutaFichero);

        if (!Files.isRegularFile(ruta)) {
            System.out.println("El fichero no existe o no es válido.");
        } else {
            try (ObjectInputStream entrada = new ObjectInputStream(
                    new FileInputStream(ruta.toFile()))) {

                Object objeto = entrada.readObject();

                if (objeto instanceof List<?>) {
                    List<?> listaLeida = (List<?>) objeto;
                    List<Producto> productosRecuperados = new ArrayList<>();

                    boolean listaValida = true;

                    for (Object elemento : listaLeida) {
                        if (elemento instanceof Producto) {
                            productosRecuperados.add((Producto) elemento);
                        } else {
                            listaValida = false;
                        }
                    }

                    if (listaValida) {
                        inventario.getProductos().clear();
                        inventario.getProductos().addAll(productosRecuperados);

                        System.out.println(
                                "Inventario recuperado correctamente. Productos: " + inventario.getProductos());
                    } else {
                        System.out.println(
                                "El fichero contiene datos que no son productos.");
                    }
                } else {
                    System.out.println(
                            "El fichero no contiene una lista de productos.");
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println(
                        "Error al recuperar el inventario: " + e.getMessage());
            }
        }
    }

}
