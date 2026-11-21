import java.time.LocalDate;
import java.util.List;

public class Pago implements Persistible, Notificable {
    private int idPago;
    private LocalDate fechaLimite;
    private LocalDate fechaPago;
    private String periodoCubierto;
    private double montoPagado;
    private String estadoPago;
    private Mora mora;

    // fechaPago es null mientras el pago siga pendiente
    public Pago(int idPago, LocalDate fechaLimite, LocalDate fechaPago, String periodoCubierto,
                double montoPagado, String estadoPago) {
        this.idPago = idPago;
        this.fechaLimite = fechaLimite;
        this.fechaPago = fechaPago;
        this.periodoCubierto = periodoCubierto;
        this.montoPagado = montoPagado;
        this.estadoPago = estadoPago;
    }

    public void registrarPago() {
        // Pendiente: RF-05
    }

    public boolean validarPeriodo() {
        throw new UnsupportedOperationException("Pendiente: RF-05");
    }

    public double calcularMora() {
        throw new UnsupportedOperationException("Pendiente: RF-06");
    }

    public boolean verificarFechaLimite() {
        throw new UnsupportedOperationException("Pendiente: RF-06");
    }

    @Override
    public List<Notificacion> generarNotificaciones() {
        throw new UnsupportedOperationException("Pendiente: RF-16");
    }

    @Override
    public void guardar() {
        // Pendiente: RNF-01
    }

    @Override
    public void cargar() {
        // Pendiente: RNF-01
    }

    public int getIdPago() {
        return idPago;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public String getPeriodoCubierto() {
        return periodoCubierto;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    private static class Mora {
        private int diasAtraso;
        private double tasaMora;
        private double valorInteres;

        private Mora(int diasAtraso, double tasaMora) {
            this.diasAtraso = diasAtraso;
            this.tasaMora = tasaMora;
        }

        public double calcularValor() {
            throw new UnsupportedOperationException("Pendiente: RF-06");
        }
    }
}
