import java.util.ArrayList;
import java.util.List;

public class GestorInmuebles {
    private List<Propiedad> inmuebles = new ArrayList<>();
    private List<Notificacion> notificaciones = new ArrayList<>();

    public void agregarPropiedad(Propiedad propiedad) {
        // Pendiente: RF-01 (también registra la propiedad en su propietario con agregarPropiedad)
    }

    public List<Propiedad> listarDisponibles(String modalidad) {
        throw new UnsupportedOperationException("Pendiente: RF-13");
    }

    public void recolectarNotificaciones() {
        // Pendiente: RF-18
    }

    public List<Notificacion> listarNotificacionesPendientes() {
        throw new UnsupportedOperationException("Pendiente: RF-20");
    }

    public List<Propiedad> getInmuebles() {
        return List.copyOf(inmuebles);
    }

    public List<Notificacion> getNotificaciones() {
        return List.copyOf(notificaciones);
    }
}
