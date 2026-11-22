import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilidad interna de persistencia (RNF-01). NO es una clase del dominio ni aparece en el UML:
 * solo evita repetir el código de lectura/escritura de archivos planos en Propiedad, Cliente y Contrato.
 *
 * Formato: un registro por línea, campos separados por "|". El primer campo es siempre la CLAVE
 * del registro (dirección, documento, número de contrato...). Los archivos viven en la carpeta "datos/".
 *
 * Es de acceso "default" (sin public): solo la usan las clases del mismo paquete.
 */
final class ArchivoPlano {

    private static final Path CARPETA = Paths.get("datos");

    private ArchivoPlano() {
        // Clase de utilidades: no se instancia.
    }

    /** Guarda el registro; si ya existe uno con la misma clave (campos[0]) lo reemplaza, si no, lo agrega. */
    static void guardarRegistro(String archivo, String[] campos) {
        try {
            Files.createDirectories(CARPETA);
            Path ruta = CARPETA.resolve(archivo);
            List<String> lineas = Files.exists(ruta)
                    ? new ArrayList<>(Files.readAllLines(ruta, StandardCharsets.UTF_8))
                    : new ArrayList<>();

            String claveEscapada = escapar(campos[0]);
            String nueva = unir(campos);
            boolean reemplazada = false;
            for (int i = 0; i < lineas.size(); i++) {
                if (claveDe(lineas.get(i)).equals(claveEscapada)) {
                    lineas.set(i, nueva);
                    reemplazada = true;
                    break;
                }
            }
            if (!reemplazada) {
                lineas.add(nueva);
            }
            Files.write(ruta, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar en el archivo '" + archivo + "': " + e.getMessage(), e);
        }
    }

    /** Devuelve los campos del registro con esa clave, o null si no existe. */
    static String[] buscarRegistro(String archivo, String clave) {
        Path ruta = CARPETA.resolve(archivo);
        if (!Files.exists(ruta)) {
            return null;
        }
        try {
            String claveEscapada = escapar(clave);
            for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
                if (claveDe(linea).equals(claveEscapada)) {
                    return separar(linea);
                }
            }
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo '" + archivo + "': " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve todos los registros del archivo, en el orden en que están (lista vacía si el archivo no existe).
     * Lo usan los gestores para recrear el sistema al arrancar.
     */
    static List<String[]> leerTodos(String archivo) {
        Path ruta = CARPETA.resolve(archivo);
        List<String[]> registros = new ArrayList<>();
        if (!Files.exists(ruta)) {
            return registros;
        }
        try {
            for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) {
                    registros.add(separar(linea));
                }
            }
            return registros;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo '" + archivo + "': " + e.getMessage(), e);
        }
    }

    /** Junta dos arreglos de campos (los comunes de la clase padre + los propios de cada hija). */
    static String[] concatenar(String[] a, String[] b) {
        String[] resultado = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, resultado, a.length, b.length);
        return resultado;
    }

    // ---------- Auxiliares privados ----------

    private static String unir(String[] campos) {
        String[] escapados = new String[campos.length];
        for (int i = 0; i < campos.length; i++) {
            escapados[i] = escapar(campos[i]);
        }
        return String.join("|", escapados);
    }

    // Parte una línea en sus campos y deshace el escape de cada uno.
    private static String[] separar(String linea) {
        String[] partes = linea.split("\\|", -1);
        for (int i = 0; i < partes.length; i++) {
            partes[i] = desescapar(partes[i]);
        }
        return partes;
    }

    private static String claveDe(String linea) {
        int posicion = linea.indexOf('|');
        return posicion < 0 ? linea : linea.substring(0, posicion);
    }

    // Si un texto trae "|" o saltos de línea, se escapan para no romper el formato del archivo.
    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String desescapar(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '\\' && i + 1 < texto.length()) {
                char siguiente = texto.charAt(++i);
                switch (siguiente) {
                    case 'p':
                        resultado.append('|');
                        break;
                    case 'n':
                        resultado.append('\n');
                        break;
                    case 'r':
                        resultado.append('\r');
                        break;
                    default:
                        resultado.append(siguiente);
                }
            } else {
                resultado.append(c);
            }
        }
        return resultado.toString();
    }
}
