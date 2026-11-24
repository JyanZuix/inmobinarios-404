import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Utilidades internas compartidas (igual que ArchivoPlano, NO es una clase del dominio ni aparece en el UML):
 * reglas de formato que usan varias clases y que así se escriben una sola vez.
 *
 * Es de acceso "default" (sin public): solo la usan las clases del mismo paquete.
 */
final class Utilidades {

    private static final int MIN_DIGITOS_DOCUMENTO = 5;
    private static final int MAX_DIGITOS_DOCUMENTO = 12;

    private Utilidades() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * RF-01 / RF-02: deja el documento solo con sus números, para que "52.111.222", "52 111 222" y "52111222"
     * sean el mismo documento. Lanza una excepción si queda vacío, si trae letras o si tiene una longitud rara.
     *
     * @param documento texto tal como lo escribió el operador
     * @param quien     a quién pertenece ("cliente", "propietario"), para que el mensaje sea claro
     */
    static String normalizarDocumento(String documento, String quien) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento del " + quien + " es obligatorio.");
        }
        String limpio = limpiarDocumento(documento); // se aceptan puntos, espacios y guiones
        if (!limpio.matches("\\d+")) {
            throw new IllegalArgumentException("El documento del " + quien + " solo puede tener números (se recibió: '"
                    + documento.trim() + "').");
        }
        if (limpio.length() < MIN_DIGITOS_DOCUMENTO || limpio.length() > MAX_DIGITOS_DOCUMENTO) {
            throw new IllegalArgumentException("El documento del " + quien + " debe tener entre " + MIN_DIGITOS_DOCUMENTO
                    + " y " + MAX_DIGITOS_DOCUMENTO + " dígitos (se recibió: '" + documento.trim() + "').");
        }
        return limpio;
    }

    /** Quita puntos, espacios y guiones del documento, sin validarlo. La usan también las búsquedas. */
    static String limpiarDocumento(String documento) {
        return documento == null ? null : documento.trim().replaceAll("[.\\s-]", "");
    }

    /**
     * RF-02: un contacto válido es un teléfono (7 a 10 dígitos; se permiten espacios, guiones, paréntesis y el
     * prefijo +57) o un correo con la forma nombre@dominio.algo. Devuelve el contacto sin espacios sobrantes.
     */
    static String validarContacto(String contacto) {
        if (contacto == null || contacto.isBlank()) {
            throw new IllegalArgumentException("El contacto es obligatorio.");
        }
        String limpio = contacto.trim();
        if (esCorreo(limpio) || esTelefono(limpio)) {
            return limpio;
        }
        throw new IllegalArgumentException("El contacto '" + limpio + "' no es válido: escriba un teléfono de 7 a 10 "
                + "dígitos o un correo (por ejemplo nombre@correo.com).");
    }

    /**
     * RF-11: true si dos contactos son el mismo aunque estén escritos distinto: sin importar mayúsculas, espacios,
     * guiones, paréntesis ni el prefijo +57 ("312 555 0000" = "3125550000" = "+57 312-555-0000").
     */
    static boolean mismoContacto(String a, String b) {
        return a != null && b != null && claveContacto(a).equals(claveContacto(b));
    }

    /** Muestra un monto como lo leería una persona: 30000000.0 → "$30.000.000". */
    static String moneda(double valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(); // los separadores se fijan a mano
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return "$" + new DecimalFormat("#,##0", simbolos).format(valor);
    }

    /** Muestra un porcentaje guardado como fracción: 0.05 → "5 %", 0.075 → "7,5 %". */
    static String porcentaje(double fraccion) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat("0.##", simbolos).format(fraccion * 100) + " %";
    }

    // ---------- Auxiliares privados ----------

    private static String claveContacto(String contacto) {
        String limpio = contacto.trim().toLowerCase().replaceAll("[\\s()-]", "");
        return limpio.startsWith("+57") ? limpio.substring(3) : limpio;
    }

    private static boolean esCorreo(String texto) {
        return texto.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    private static boolean esTelefono(String texto) {
        String digitos = texto.replaceAll("[\\s()-]", "");
        if (digitos.startsWith("+57")) {
            digitos = digitos.substring(3);
        }
        return digitos.matches("\\d{7,10}");
    }
}
