/**
 * Arrendatario: cliente que arrienda un inmueble (RF-02).
 */
public class Arrendatario extends Cliente {

    public Arrendatario(String documento, String nombre, String contacto) {
        super(documento, nombre, contacto);
    }

    @Override
    public String describir() {
<<<<<<< HEAD
        throw new UnsupportedOperationException("Pendiente: RF-02");
=======
        return "Arrendatario " + nombre + " (documento " + documento + ")";
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }
}
