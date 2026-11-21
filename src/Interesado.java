<<<<<<< HEAD
import java.util.List;

public class Interesado extends Persona {

    public Interesado(String nombre, String contacto) {
        super(nombre, contacto);
    }

    @Override
    public List<Notificacion> generarNotificaciones() {
        throw new UnsupportedOperationException("Pendiente: RF-18");
=======
import java.util.ArrayList;
import java.util.List;

/**
 * Interesado en un inmueble. Solo está lo mínimo para que compile (RF-11 y RF-12 vienen después).
 */
public class Interesado implements Persistible, Notificable {

    // AQUÍ VAN nombre, contacto y fechasSeguimiento (RF-11 / RF-12).
    // AQUÍ VAN registrarSeguimiento(fecha) y consultarSeguimientos().

    @Override
    public List<Notificacion> generarNotificaciones() {
        // AQUÍ VA RF-18: aviso de seguimientos pendientes.
        return new ArrayList<>();
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }

    @Override
    public void guardar() {
<<<<<<< HEAD
        // Pendiente: RNF-01
=======
        // AQUÍ VA la persistencia de Interesado.
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }

    @Override
    public void cargar() {
<<<<<<< HEAD
        // Pendiente: RNF-01
=======
        // AQUÍ VA la lectura de Interesado desde archivo.
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }
}
