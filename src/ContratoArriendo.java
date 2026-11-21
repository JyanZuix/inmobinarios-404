import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratoArriendo extends Contrato {
    private double canonActual;
    private double porcentajeIncremento;
    private LocalDate ultimaFechaActualizacion;
    private List<Pago> pagos = new ArrayList<>();

    public ContratoArriendo(Propiedad propiedad, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin,
                            double canonActual, double porcentajeIncremento) {
        super(propiedad, cliente, fechaInicio, fechaFin);
        this.canonActual = canonActual;
        this.porcentajeIncremento = porcentajeIncremento;
        this.ultimaFechaActualizacion = fechaInicio;
    }

    // Reconstruye un contrato ya existente (al cargarlo desde el archivo)
    public ContratoArriendo(int numeroContrato, Propiedad propiedad, Cliente cliente,
                            LocalDate fechaInicio, LocalDate fechaFin, String estado,
                            double canonActual, double porcentajeIncremento, LocalDate ultimaFechaActualizacion) {
        super(numeroContrato, propiedad, cliente, fechaInicio, fechaFin, estado);
        this.canonActual = canonActual;
        this.porcentajeIncremento = porcentajeIncremento;
        this.ultimaFechaActualizacion = ultimaFechaActualizacion;
    }

    public void aplicarIncrementoAnual() {
        // Pendiente: RF-07
    }

    public boolean verificarAniversario() {
        throw new UnsupportedOperationException("Pendiente: RF-07");
    }

    public List<Pago> listarPagos() {
        throw new UnsupportedOperationException("Pendiente: RF-08");
    }

    public void agregarPago(Pago pago) {
        // Pendiente: RF-05
    }

    @Override
    public double calcularMontoFinal() {
        throw new UnsupportedOperationException("Pendiente: RF-03");
    }

    public double getCanonActual() {
        return canonActual;
    }

    public double getPorcentajeIncremento() {
        return porcentajeIncremento;
    }

    public LocalDate getUltimaFechaActualizacion() {
        return ultimaFechaActualizacion;
    }
}
