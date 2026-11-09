package es.iesisidra.datastock.util;

import java.util.Scanner;

/**
 * Utilidad compartida para solicitar datos por consola.
 * <p>Utiliza un único {@link Scanner} para toda la aplicación. Se leen líneas
 * completas y se convierten cuando procede, evitando conflictos entre
 * {@code nextInt()} y {@code nextLine()}.</p>
 */
public final class LectorConsola {
    private static final Scanner TECLADO = new Scanner(System.in);
    private LectorConsola() { }

    /** Lee una línea; permite una cadena vacía. */
    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!TECLADO.hasNextLine()) {
            // Entrada cerrada (por ejemplo, ejecución no interactiva).
            System.out.println();
            System.exit(0);
        }
        return TECLADO.nextLine();
    }

    /** Lee un entero; repite la pregunta ante una entrada no válida. */
    public static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).trim();
            try { return Integer.parseInt(texto); }
            catch (NumberFormatException ex) {
                System.out.println(ColoresConsola.pintar("Introduce un número entero válido.", ColoresConsola.AMARILLO));
            }
        }
    }

    /** Lee una opción comprendida entre los límites indicados. */
    public static int leerOpcion(String mensaje, int minimo, int maximo) {
        while (true) {
            int opcion = leerEntero(mensaje);
            if (opcion >= minimo && opcion <= maximo) return opcion;
            System.out.println(ColoresConsola.pintar("Opción fuera de rango.", ColoresConsola.AMARILLO));
        }
    }

    /** Solicita confirmación con S/N y devuelve true únicamente si se elige S. */
    public static boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerTexto(mensaje + " (S/N): ").trim();
            if (respuesta.equalsIgnoreCase("S")) return true;
            if (respuesta.equalsIgnoreCase("N")) return false;
            System.out.println("Responde S o N.");
        }
    }

    public static int leerEnteroPositivo(String mensaje) {
        int valor = -1;
        
        // Bucle do-while: se ejecuta al menos una vez y se repite hasta que el valor sea válido.
        // Por qué se usa: garantiza que el programa no avance hasta obtener un dato correcto.
        do {
            System.out.print(mensaje);
            try {
                // Lee la línea completa y la convierte a entero para evitar problemas con el buffer de Scanner
                String entrada = TECLADO.nextLine();
                valor = Integer.parseInt(entrada.trim());
                
                if (valor <= 0) {
                    System.err.println("[ERROR] El valor debe ser un número entero positivo mayor que cero.");
                }
            } catch (NumberFormatException e) {
                // Captura el error si el usuario introduce letras o símbolos no numéricos
                System.err.println("[ERROR] Formato incorrecto. Debe introducir un número entero válido.");
            }
        } while (valor <= 0);
        
        return valor;
    }

    /**
     * Lee una cadena de texto por consola asegurándose de que no esté vacía
     * ni compuesta únicamente por espacios en blanco.
     * 
     * @param mensaje Mensaje informativo que se muestra al usuario.
     * @return La cadena de texto validada.
     */
    public static String leerCadenaNoVacia(String mensaje) {
        String texto = "";
        
        // Bucle do-while para obligar al usuario a introducir contenido real
        do {
            System.out.print(mensaje);
            if (TECLADO.hasNextLine()) {
                texto = TECLADO.nextLine().trim();
            }
            
            if (texto.isEmpty()) {
                System.err.println("[ERROR] El campo no puede estar vacío.");
            }
        } while (texto.isEmpty());
        
        return texto;
    }

    public static String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return TECLADO.nextLine().trim();
    }

    /** Detiene el menú hasta que el usuario pulse Intro. */
    public static void pausar() { leerTexto("Pulsa Intro para continuar..."); }
}
