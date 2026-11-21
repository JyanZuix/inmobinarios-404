/**
 * Interfaz Persistible (RNF-01).
 * Todo objeto que deba sobrevivir entre ejecuciones sabe guardarse y cargarse
 * en un archivo plano. Cada clase decide cómo escribir y leer sus propios datos.
 */
public interface Persistible {

    /** Escribe (o actualiza) el registro de este objeto en su archivo plano. */
    void guardar();

    /** Vuelve a leer del archivo plano el registro de este objeto (identificado por su clave). */
    void cargar();
}
