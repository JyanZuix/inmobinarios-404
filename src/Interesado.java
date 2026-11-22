import java.time.LocalDate;
import java.util.List;

/**
 * Interesado en un inmueble. Nombre, contacto y seguimientos vienen de Persona.
 */
public class Interesado extends Persona {

    public Interesado(String nombre, String contacto) {
        super(nombre, contacto);
    }

    /** RF-18: avisa cada cita de seguimiento que es hoy o ya pasó. No tiene documento: se identifica por su contacto. */
    @Override
    public List<Notificacion> generarNotificaciones(LocalDate hoy) {
        return notificacionesDeSeguimiento(hoy, "INTERESADO:" + contacto);
    }

    @Override
    public void guardar() {
        // Pendiente: RNF-01
    }

    @Override
    public void cargar() {
        // Pendiente: RNF-01
    }
}
