import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Propietario del inmueble (el agente gestiona inmuebles de varios dueños).
 * Una propiedad se relaciona con otras SOLO a través de su dueño: un propietario tiene 0..* propiedades
 * y cada propiedad pertenece a un único propietario.
 * Solo está lo mínimo para RF-01; el resto queda para sus requerimientos.
 */
public class Propietario implements Persistible {

    private static final String ARCHIVO = "propietarios.txt";

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
    // Sus propiedades no se guardan aquí: cada propiedad guarda el documento de su dueño y el enlace se
    // reconstruye al cargar las propiedades.
    @Override
    public void guardar() {
        ArchivoPlano.guardarRegistro(ARCHIVO, new String[]{documento, nombre, contacto});
    }

    @Override
    public void cargar() {
        String[] registro = ArchivoPlano.buscarRegistro(ARCHIVO, documento);
        if (registro == null) {
            throw new IllegalStateException("No hay ningún propietario guardado con el documento " + documento + ".");
        }
        if (registro.length < 3 || registro[1].isBlank() || registro[2].isBlank()) {
            throw new IllegalStateException("El registro guardado del propietario " + documento + " está dañado.");
        }
        this.nombre = registro[1];
        this.contacto = registro[2];
    }
}
