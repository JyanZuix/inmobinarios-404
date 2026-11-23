/**
 * Apartamento: hija de Propiedad (RF-01 / RF-14).
 * El tipo ("Apartamento") lo fija la propia clase, así nadie puede crear un apartamento con otro tipo.
 */
public class Apartamento extends Propiedad {

    private int numHabitaciones;

    public Apartamento(String direccion, double area, double valorBase, String modo,
                       Propietario propietario, int numHabitaciones) {
        super(direccion, area, valorBase, "Apartamento", modo, propietario);
        validarHabitaciones(numHabitaciones);
        this.numHabitaciones = numHabitaciones;
    }

    // Regla propia de RF-14: el apartamento exige un mes de canon como depósito de garantía.
    @Override
    public double calcularDepositoGarantia() {
        return valorBase;
    }

    public int getNumHabitaciones() {
        return numHabitaciones;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + numHabitaciones + (numHabitaciones == 1 ? " habitación" : " habitaciones");
    }

    // ---- Persistencia: campo propio de esta hija ----
    @Override
    protected String[] datosPropios() {
        return new String[]{String.valueOf(numHabitaciones)};
    }

    @Override
    protected void aplicarDatosPropios(String[] datos) {
        int habitaciones = Integer.parseInt(datos[0]);
        validarHabitaciones(habitaciones);
        this.numHabitaciones = habitaciones;
    }

    private static void validarHabitaciones(int habitaciones) {
        if (habitaciones < 1) {
            throw new IllegalArgumentException("El apartamento debe tener al menos 1 habitación (se recibió: "
                    + habitaciones + ").");
        }
    }
}
