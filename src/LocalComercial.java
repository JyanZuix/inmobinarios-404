/**
 * Local comercial: hija de Propiedad (RF-01 / RF-14).
 * El tipo ("Local comercial") lo fija la propia clase.
 */
public class LocalComercial extends Propiedad {
<<<<<<< HEAD
    private static final double TASA_IVA = 0.19;

    public LocalComercial(String direccion, double area, double valorBase, String tipo, String modo,
                          Propietario propietario) {
        super(direccion, area, valorBase, tipo, modo, propietario);
    }

    @Override
    public double calcularDepositoGarantia() {
        throw new UnsupportedOperationException("Pendiente: RF-14");
    }
=======

    // Constante estática compartida por todos los locales (RF-14).
    private static final double TASA_IVA = 0.19;

    public LocalComercial(String direccion, double area, double valorBase, String modo) {
        super(direccion, area, valorBase, "Local comercial", modo);
    }

    // Regla propia de RF-14: el local exige dos meses de canon más el IVA correspondiente.
    @Override
    public double calcularDepositoGarantia() {
        return 2 * valorBase * (1 + TASA_IVA);
    }

    // No tiene campos propios que guardar: usa el comportamiento por defecto de Propiedad.
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
}
