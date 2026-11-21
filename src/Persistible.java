/**
 * Interfaz Persistible (RNF-01).
 * Todo objeto que deba sobrevivir entre ejecuciones sabe guardarse y cargarse
 * en un archivo plano. Cada clase decide cómo escribir y leer sus propios datos.
 */
public interface Persistible {
<<<<<<< HEAD
    void guardar();
=======

    /** Escribe (o actualiza) el registro de este objeto en su archivo plano. */
    void guardar();

    /** Vuelve a leer del archivo plano el registro de este objeto (identificado por su clave). */
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    void cargar();
}
