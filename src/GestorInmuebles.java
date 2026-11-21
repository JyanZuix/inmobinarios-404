import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor del inventario de inmuebles del agente.
 * Es quien sabe qué propiedades están registradas, por eso valida aquí que no haya direcciones repetidas.
 */
public class GestorInmuebles {

    private final List<Propiedad> inmuebles = new ArrayList<>();
    private final List<Notificacion> notificaciones = new ArrayList<>();

    /**
     * RF-01: registra una propiedad en el inventario.
     * No se permiten dos propiedades con la misma dirección (se ignoran mayúsculas y espacios sobrantes).
     * Si el registro es exitoso, completa el enlace inverso: la propiedad queda en la lista de su propietario.
     */
    public void agregarPropiedad(Propiedad propiedad) {
        if (propiedad == null) {
            throw new IllegalArgumentException("No se puede registrar una propiedad nula.");
        }
        for (Propiedad existente : inmuebles) {
            if (existente.getDireccion().equalsIgnoreCase(propiedad.getDireccion())) {
                throw new IllegalArgumentException("Ya existe una propiedad registrada con la dirección '"
                        + propiedad.getDireccion() + "'. No se permiten dos propiedades con la misma dirección.");
            }
        }
        Propietario dueno = propiedad.getPropietario();
        if (!dueno.getPropiedades().contains(propiedad)) {
            dueno.agregarPropiedad(propiedad);
        }
        inmuebles.add(propiedad);
        Propiedad.contarRegistro(); // suma al contador estático de propiedades registradas
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
        return Collections.unmodifiableList(inmuebles);
    }

    public List<Notificacion> getNotificaciones() {
        return Collections.unmodifiableList(notificaciones);
    }
}
