import java.util.List;

/**
 * Interfaz Notificable (RF-16 a RF-19).
 * Todo objeto que pueda requerir la atención del agente sabe generar sus avisos.
 */
public interface Notificable {

    List<Notificacion> generarNotificaciones();
}
