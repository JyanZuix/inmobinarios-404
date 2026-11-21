import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor del inventario de inmuebles del agente.
 * Es quien sabe qué propiedades están registradas, por eso valida aquí que no haya direcciones repetidas.
 */
public class GestorInmuebles {

    private final List<Propiedad> inmuebles = new ArrayList<>();

    // AQUÍ VAN las notificaciones del gestor (RF-18, RF-20): notificaciones : List<Notificacion>

    /**
     * RF-01: registra una propiedad en el inventario.
     * No se permiten dos propiedades con la misma dirección (se ignoran mayúsculas y espacios sobrantes).
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
        inmuebles.add(propiedad);
        Propiedad.contarRegistro(); // suma al contador estático de propiedades registradas
    }

    // AQUÍ VA RF-13: listarDisponibles(modalidad).

    public List<Propiedad> getInmuebles() {
        return Collections.unmodifiableList(inmuebles);
    }
}
