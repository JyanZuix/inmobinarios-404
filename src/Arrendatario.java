public class Arrendatario extends Cliente {

    public Arrendatario(String documento, String nombre, String contacto) {
        super(documento, nombre, contacto);
    }

    @Override
    public String describir() {
        throw new UnsupportedOperationException("Pendiente: RF-02");
    }
}
