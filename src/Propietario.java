import java.util.ArrayList;
<<<<<<< HEAD
import java.util.List;

public class Propietario implements Persistible {
    private String documento;
    private String nombre;
    private String contacto;
    private List<Propiedad> propiedades = new ArrayList<>();

    public Propietario(String documento, String nombre, String contacto) {
        this.documento = documento;
        this.nombre = nombre;
        this.contacto = contacto;
    }

    public void actualizarContacto(String contacto) {
        // Pendiente
    }

    public void agregarPropiedad(Propiedad propiedad) {
        // Pendiente: RF-01
    }

    @Override
    public void guardar() {
        // Pendiente: RNF-01
=======
import java.util.Collections;
import java.util.List;

/**
 * Propietario del inmueble (el agente gestiona inmuebles de varios dueños).
 * Una propiedad se relaciona con otras SOLO a través de su dueño: un propietario tiene 0..* propiedades
 * y cada propiedad pertenece a un único propietario.
 * Solo está lo mínimo para RF-01; el resto queda para sus requerimientos.
 */
public class Propietario implements Persistible {

    private String documento;
    private String nombre;
    private String contacto;
    private final List<Propiedad> propiedades = new ArrayList<>();

    public Propietario(String documento, String nombre, String contacto) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento del propietario es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del propietario es obligatorio.");
        }
        if (contacto == null || contacto.isBlank()) {
            throw new IllegalArgumentException("El contacto del propietario es obligatorio.");
        }
        this.documento = documento.trim();
        this.nombre = nombre.trim();
        this.contacto = contacto.trim();
    }

    /** Asigna una propiedad a este propietario (y a la propiedad le deja a este propietario como dueño). */
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

    public void actualizarContacto(String nuevoContacto) {
        if (nuevoContacto == null || nuevoContacto.isBlank()) {
            throw new IllegalArgumentException("El contacto del propietario no puede quedar vacío.");
        }
        this.contacto = nuevoContacto.trim();
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
    public List<Propiedad> getPropiedades() { return Collections.unmodifiableList(propiedades); }

    // ---- Persistencia (RNF-01) ----
    @Override
    public void guardar() {
        // AQUÍ VA la persistencia de Propietario (no hace parte de RF-01 a RF-03).
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }

    @Override
    public void cargar() {
<<<<<<< HEAD
        // Pendiente: RNF-01
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public List<Propiedad> getPropiedades() {
        return List.copyOf(propiedades);
=======
        // AQUÍ VA la lectura de Propietario desde archivo.
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }
}
