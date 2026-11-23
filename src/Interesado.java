import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Interesado en un inmueble. Nombre, contacto y seguimientos vienen de Persona.
 */
public class Interesado extends Persona {
    static final String ARCHIVO = "interesados.txt";

    private final UUID id;
    private final List<Propiedad> inmuebles;

    Interesado(String nombre, String contacto, List<Propiedad> inmuebles) {
        this(UUID.randomUUID(), nombre, contacto, inmuebles);
    }

    Interesado(UUID id, String nombre, String contacto, List<Propiedad> inmuebles) {
        super(nombre, contacto);
        if (inmuebles == null || inmuebles.isEmpty() || inmuebles.stream().anyMatch(p -> p == null)) {
            throw new IllegalArgumentException("El interesado debe estar asociado a mínimo un inmueble.");
        }
        if (new HashSet<>(inmuebles).size() != inmuebles.size()) {
            throw new IllegalArgumentException("No se puede repetir un inmueble para el mismo interesado.");
        }
        this.id = id;
        this.inmuebles = List.copyOf(inmuebles);
    }

    public List<Propiedad> getInmuebles() {
        return inmuebles;
    }

    /** RF-18: avisa cada cita de seguimiento que es hoy o ya pasó. */
    @Override
    public List<Notificacion> generarNotificaciones(LocalDate hoy) {
        return notificacionesDeSeguimiento(hoy, clave());
    }

    @Override
    public void guardar() {
        String[] datos = new String[3 + inmuebles.size()];
        datos[0] = id.toString();
        datos[1] = nombre;
        datos[2] = contacto;
        for (int i = 0; i < inmuebles.size(); i++) {
            datos[3 + i] = inmuebles.get(i).getDireccion();
        }
        ArchivoPlano.guardarRegistro(ARCHIVO, datos);
        guardarSeguimientos(clave());
    }

    @Override
    public void cargar() {
        String[] datos = ArchivoPlano.buscarRegistro(ARCHIVO, id.toString());
        if (datos == null) {
            throw new IllegalStateException("No hay un registro guardado para " + nombre + ".");
        }
        // Las relaciones se reconstruyen en GestorInmuebles.cargarTodo(), igual que las de Propiedad.
        cargarSeguimientosGuardados();
        this.nombre = datos[1];
        this.contacto = datos[2];
    }

    void cargarSeguimientosGuardados() {
        cargarSeguimientos(clave());
    }

    private String clave() {
        return "INTERESADO:" + id;
    }
}
