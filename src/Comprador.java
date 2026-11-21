/**
 * Comprador: cliente que compra un inmueble (se usa en los contratos de venta).
 */
public class Comprador extends Cliente {

    private boolean requiereFinanciamiento;

    public Comprador(String documento, String nombre, String contacto, boolean requiereFinanciamiento) {
        super(documento, nombre, contacto);
        this.requiereFinanciamiento = requiereFinanciamiento;
    }

    @Override
    public String describir() {
        return "Comprador " + nombre + " (documento " + documento + ")"
                + (requiereFinanciamiento ? ", requiere financiamiento" : ", sin financiamiento");
    }

    public boolean isRequiereFinanciamiento() {
        return requiereFinanciamiento;
    }

    // ---- Persistencia: campo propio de esta hija ----
    @Override
    protected String[] datosPropios() {
        return new String[]{String.valueOf(requiereFinanciamiento)};
    }

    @Override
    protected void aplicarDatosPropios(String[] datos) {
        this.requiereFinanciamiento = Boolean.parseBoolean(datos[0]);
    }
}
