import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Persona implements Persistible, Notificable {
    protected String nombre;
    protected String contacto;
    protected List<LocalDate> fechasSeguimiento = new ArrayList<>();

    public Persona(String nombre, String contacto) {
        this.nombre = nombre;
        this.contacto = contacto;
    }

    public void actualizarContacto(String contacto) {
        // Pendiente
    }

    public void registrarSeguimiento(LocalDate fecha) {
        // Pendiente: RF-12, RF-19
    }

    public List<LocalDate> consultarSeguimientos() {
        throw new UnsupportedOperationException("Pendiente: RF-12, RF-19");
    }

    public String getNombre() {
        return nombre;
    }

    public String getContacto() {
        return contacto;
    }
}
