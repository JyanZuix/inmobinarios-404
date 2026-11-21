import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Datos comunes de Cliente e Interesado: nombre, contacto y fechas de seguimiento (RF-12 / RF-19).
 */
public abstract class Persona implements Persistible, Notificable {
    protected String nombre;
    protected String contacto;
    // Privada: solo cambia con registrarSeguimiento(); las hijas la leen con consultarSeguimientos().
    private final List<LocalDate> fechasSeguimiento = new ArrayList<>();

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

    /**
     * RF-12: registra una fecha de seguimiento (cita o contacto pendiente con esta persona).
     * No se rechazan fechas pasadas, porque al cargar desde archivo (RNF-01) se deben poder reconstruir.
     */
    public void registrarSeguimiento(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de seguimiento es obligatoria.");
        }
        if (fechasSeguimiento.contains(fecha)) {
            throw new IllegalArgumentException(nombre + " ya tiene un seguimiento registrado para el " + fecha + ".");
        }
        fechasSeguimiento.add(fecha);
    }

    /**
     * RF-12: devuelve las citas pendientes (en la fecha dada o después), de la más próxima a la más lejana.
     * Devuelve una lista nueva: modificarla no cambia los seguimientos de la persona.
     */
    public List<LocalDate> consultarSeguimientos(LocalDate desde) {
        if (desde == null) {
            throw new IllegalArgumentException("La fecha desde la que se consultan los seguimientos es obligatoria.");
        }
        List<LocalDate> pendientes = new ArrayList<>();
        for (LocalDate fecha : fechasSeguimiento) {
            if (!fecha.isBefore(desde)) {
                pendientes.add(fecha);
            }
        }
        Collections.sort(pendientes);
        return pendientes;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContacto() {
        return contacto;
    }
}
