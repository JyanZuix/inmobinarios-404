import java.util.List;

public class Interesado extends Persona {

    public Interesado(String nombre, String contacto) {
        super(nombre, contacto);
    }

    @Override
    public List<Notificacion> generarNotificaciones() {
        throw new UnsupportedOperationException("Pendiente: RF-18");
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
