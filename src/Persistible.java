/**
 * Interfaz Persistible (RNF-01).
 * Todo objeto que deba sobrevivir entre ejecuciones sabe guardarse en un archivo plano.
 * Cada clase decide qué campos escribe. La carga no está aquí: el sistema se recrea entero al arrancar con
 * SistemaInmobiliario.cargarTodo(), que crea los objetos desde los archivos y reconecta sus relaciones.
 */
public interface Persistible {

    /** Escribe (o actualiza) el registro de este objeto en su archivo plano. */
    void guardar();
}
