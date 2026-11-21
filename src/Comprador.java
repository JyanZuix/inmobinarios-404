/**
 * Comprador: cliente que compra un inmueble (se usa en los contratos de venta).
 */
public class Comprador extends Cliente {
<<<<<<< HEAD
=======

>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    private boolean requiereFinanciamiento;

    public Comprador(String documento, String nombre, String contacto, boolean requiereFinanciamiento) {
        super(documento, nombre, contacto);
        this.requiereFinanciamiento = requiereFinanciamiento;
    }

    @Override
    public String describir() {
<<<<<<< HEAD
        throw new UnsupportedOperationException("Pendiente: RF-02");
=======
        return "Comprador " + nombre + " (documento " + documento + ")"
                + (requiereFinanciamiento ? ", requiere financiamiento" : ", sin financiamiento");
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }

    public boolean isRequiereFinanciamiento() {
        return requiereFinanciamiento;
    }
<<<<<<< HEAD
=======

    // ---- Persistencia: campo propio de esta hija ----
    @Override
    protected String[] datosPropios() {
        return new String[]{String.valueOf(requiereFinanciamiento)};
    }

    @Override
    protected void aplicarDatosPropios(String[] datos) {
        this.requiereFinanciamiento = Boolean.parseBoolean(datos[0]);
    }
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
}
