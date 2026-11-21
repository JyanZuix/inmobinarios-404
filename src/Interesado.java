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
    }

    @Override
    public void guardar() {
        // AQUÍ VA la persistencia de Interesado.
    }

    @Override
    public void cargar() {
        // AQUÍ VA la lectura de Interesado desde archivo.
    }
}
