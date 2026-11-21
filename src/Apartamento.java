/**
 * Apartamento: hija de Propiedad (RF-01 / RF-14).
 * El tipo ("Apartamento") lo fija la propia clase, así nadie puede crear un apartamento con otro tipo.
 */
public class Apartamento extends Propiedad {
<<<<<<< HEAD
    private int numHabitaciones;

    public Apartamento(String direccion, double area, double valorBase, String tipo, String modo,
                       Propietario propietario, int numHabitaciones) {
        super(direccion, area, valorBase, tipo, modo, propietario);
        this.numHabitaciones = numHabitaciones;
    }

    @Override
    public double calcularDepositoGarantia() {
        throw new UnsupportedOperationException("Pendiente: RF-14");
=======

    private int numHabitaciones;

    public Apartamento(String direccion, double area, double valorBase, String modo, int numHabitaciones) {
        super(direccion, area, valorBase, "Apartamento", modo);
        validarHabitaciones(numHabitaciones);
        this.numHabitaciones = numHabitaciones;
    }

    // Regla propia de RF-14: el apartamento exige un mes de canon como depósito de garantía.
    @Override
    public double calcularDepositoGarantia() {
        return valorBase;
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }

    public int getNumHabitaciones() {
        return numHabitaciones;
    }
<<<<<<< HEAD
=======

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
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
}
