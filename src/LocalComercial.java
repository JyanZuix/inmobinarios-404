/**
 * Local comercial: hija de Propiedad (RF-01 / RF-14).
 * El tipo ("Local comercial") lo fija la propia clase.
 */
public class LocalComercial extends Propiedad {

    // Constante estática compartida por todos los locales (RF-14).
    private static final double TASA_IVA = 0.19;

    public LocalComercial(String direccion, double area, double valorBase, String modo, Propietario propietario) {
        super(direccion, area, valorBase, "Local comercial", modo, propietario);
    }

    // Regla propia de RF-14: el local exige dos meses de canon más el IVA correspondiente.
    @Override
    public double calcularDepositoGarantia() {
        return 2 * valorBase * (1 + TASA_IVA);
    }

    // No tiene campos propios que guardar: usa el comportamiento por defecto de Propiedad.
}
