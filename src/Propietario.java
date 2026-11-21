import java.util.ArrayList;
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
    }

    @Override
    public void cargar() {
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
    }
}
