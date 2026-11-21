/**
 * Arrendatario: cliente que arrienda un inmueble (RF-02).
 */
public class Arrendatario extends Cliente {

    public Arrendatario(String documento, String nombre, String contacto) {
        super(documento, nombre, contacto);
    }

    @Override
    public String describir() {
        return "Arrendatario " + nombre + " (documento " + documento + ")";
    }
}
