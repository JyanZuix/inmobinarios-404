import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos comunes de Cliente e Interesado: nombre, contacto y fechas de seguimiento (RF-12 / RF-19).
 */
public abstract class Persona implements Persistible, Notificable {
    protected String nombre;
    protected String contacto;
    protected List<LocalDate> fechasSeguimiento = new ArrayList<>();

    public Persona(String nombre, String contacto) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (contacto == null || contacto.isBlank()) {
            throw new IllegalArgumentException("El contacto es obligatorio.");
        }
        this.nombre = nombre.trim();
        this.contacto = contacto.trim();
    }

    public void actualizarContacto(String nuevoContacto) {
        if (nuevoContacto == null || nuevoContacto.isBlank()) {
            throw new IllegalArgumentException("El contacto no puede quedar vacío.");
        }
        this.contacto = nuevoContacto.trim();
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
