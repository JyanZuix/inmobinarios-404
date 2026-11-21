public class Comprador extends Cliente {
    private boolean requiereFinanciamiento;

    public Comprador(String documento, String nombre, String contacto, boolean requiereFinanciamiento) {
        super(documento, nombre, contacto);
        this.requiereFinanciamiento = requiereFinanciamiento;
    }

    @Override
    public String describir() {
        throw new UnsupportedOperationException("Pendiente: RF-02");
    }

    public boolean isRequiereFinanciamiento() {
        return requiereFinanciamiento;
    }
}
