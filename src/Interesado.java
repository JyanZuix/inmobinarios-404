import java.util.ArrayList;
import java.util.List;

/**
 * Interesado en un inmueble. Nombre, contacto y seguimientos vienen de Persona (RF-11 y RF-12 vienen después).
 */
public class Interesado extends Persona {

    public Interesado(String nombre, String contacto) {
        super(nombre, contacto);
    }

    @Override
    public List<Notificacion> generarNotificaciones() {
        // AQUÍ VA RF-18: aviso de seguimientos pendientes.
        return new ArrayList<>();
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
