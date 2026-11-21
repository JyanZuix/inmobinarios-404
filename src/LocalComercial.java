public class LocalComercial extends Propiedad {
    private static final double TASA_IVA = 0.19;

    public LocalComercial(String direccion, double area, double valorBase, String tipo, String modo,
                          Propietario propietario) {
        super(direccion, area, valorBase, tipo, modo, propietario);
    }

    @Override
    public double calcularDepositoGarantia() {
        throw new UnsupportedOperationException("Pendiente: RF-14");
    }
}
