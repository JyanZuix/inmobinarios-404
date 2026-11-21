public class Apartamento extends Propiedad {
    private int numHabitaciones;

    public Apartamento(String direccion, double area, double valorBase, String tipo, String modo,
                       Propietario propietario, int numHabitaciones) {
        super(direccion, area, valorBase, tipo, modo, propietario);
        this.numHabitaciones = numHabitaciones;
    }

    @Override
    public double calcularDepositoGarantia() {
        throw new UnsupportedOperationException("Pendiente: RF-14");
    }

    public int getNumHabitaciones() {
        return numHabitaciones;
    }
}
