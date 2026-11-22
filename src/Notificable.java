import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz Notificable (RF-16 a RF-19).
 * Todo objeto que pueda requerir la atención del agente sabe generar sus avisos.
 * Recibe la fecha de referencia ("hoy") para que los avisos se puedan calcular y demostrar con fechas fijas.
 */
public interface Notificable {

    List<Notificacion> generarNotificaciones(LocalDate hoy);
}
