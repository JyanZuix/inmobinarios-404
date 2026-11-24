import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Propietario del inmueble (el agente gestiona inmuebles de varios dueños).
 * Una propiedad se relaciona con otras SOLO a través de su dueño: un propietario tiene 0..* propiedades
 * y cada propiedad pertenece a un único propietario.
 * Se registra junto con sus propiedades (RF-01) y se guarda en propietarios.txt (RNF-01).
 */
public class Propietario implements Persistible {

    private static final String ARCHIVO = "propietarios.txt";

    private String documento;
    private String nombre;
    private String contacto;
    private final List<Propiedad> propiedades = new ArrayList<>();

    public Propietario(String documento, String nombre, String contacto) {
        // "100.200.300" y "100200300" son el mismo documento: se guarda solo con números.
        this.documento = Utilidades.normalizarDocumento(documento, "propietario");
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del propietario es obligatorio.");
        }
        this.nombre = nombre.trim();
        this.contacto = Utilidades.validarContacto(contacto); // teléfono o correo
    }

    /**
     * Agrega una propiedad a la lista de este propietario (enlace inverso de Propiedad.propietario).
     * Normalmente lo llama GestorInmuebles.agregarPropiedad() al registrar la propiedad.
     */
    public void agregarPropiedad(Propiedad propiedad) {
        if (propiedad == null) {
            throw new IllegalArgumentException("No se puede asignar una propiedad nula al propietario.");
        }
        if (propiedades.contains(propiedad)) {
            throw new IllegalStateException("La propiedad '" + propiedad.getDireccion()
                    + "' ya está asignada a " + nombre + ".");
        }
        propiedad.asignarPropietario(this); // lanza excepción si ya tiene otro dueño
        propiedades.add(propiedad);
    }

    // Lo llama GestorInmuebles.quitarPropiedad(): la propiedad quitada deja de estar en la lista del dueño.
    void quitarPropiedad(Propiedad propiedad) {
        propiedades.remove(propiedad);
    }

    public void actualizarContacto(String nuevoContacto) {
        this.contacto = Utilidades.validarContacto(nuevoContacto);
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
    public List<Propiedad> getPropiedades() { return Collections.unmodifiableList(propiedades); }

    // ---- Persistencia (RNF-01) ----
    // Sus propiedades no se guardan aquí: cada propiedad guarda el documento de su dueño y el enlace se
    // reconstruye al cargar las propiedades.
    @Override
    public void guardar() {
        ArchivoPlano.guardarRegistro(ARCHIVO, new String[]{documento, nombre, contacto});
    }
}
