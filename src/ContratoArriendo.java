import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrato de arriendo (RF-03): solo este tipo de contrato tiene canon, incremento anual y pagos.
 */
public class ContratoArriendo extends Contrato {

    private double canonActual;
    private double porcentajeIncremento;          // por ejemplo 0.05 = 5 % de incremento anual
    private LocalDate ultimaFechaActualizacion;   // arranca en la fecha de inicio del contrato

    // Composición: los pagos pertenecen al contrato y no existen sin él.
    private final List<Pago> pagos = new ArrayList<>(); // AQUÍ VA RF-05 (registrar pagos) y RF-08 (historial)

    public ContratoArriendo(Propiedad propiedad, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin,
                            double porcentajeIncremento) {
        super(propiedad, cliente, fechaInicio, fechaFin);
        // El canon inicial es el valor base de la propiedad, así que la propiedad debe estar en modo Arriendo.
        if (!Propiedad.MODO_ARRIENDO.equals(propiedad.getModo())) {
            throw new IllegalArgumentException("No se puede crear un contrato de arriendo: la propiedad '"
                    + propiedad.getDireccion() + "' está en modo " + propiedad.getModo() + ".");
        }
        if (!(porcentajeIncremento >= 0) || Double.isInfinite(porcentajeIncremento)) {
            throw new IllegalArgumentException("El porcentaje de incremento no puede ser negativo (se recibió: "
                    + porcentajeIncremento + ").");
        }
        this.canonActual = propiedad.getValorBase();
        this.porcentajeIncremento = porcentajeIncremento;
        this.ultimaFechaActualizacion = fechaInicio;
    }

    // AQUÍ VAN RF-07 (aplicarIncrementoAnual, verificarAniversario) y RF-08 (listarPagos).

    // Monto provisional: canon por los meses completos del contrato (mínimo 1). Se afina con RF-07.
    @Override
    public double calcularMontoFinal() {
        long meses = Math.max(1, ChronoUnit.MONTHS.between(fechaInicio, fechaFin));
        return canonActual * meses;
    }

    @Override
    protected String estadoPropiedadAlCrear() {
        return Propiedad.ARRENDADA;
    }

    public double getCanonActual() { return canonActual; }
    public double getPorcentajeIncremento() { return porcentajeIncremento; }
    public LocalDate getUltimaFechaActualizacion() { return ultimaFechaActualizacion; }

    // ---- Persistencia: campos propios de esta hija ----
    @Override
    protected String[] datosPropios() {
        return new String[]{
                String.valueOf(canonActual),
                String.valueOf(porcentajeIncremento),
                ultimaFechaActualizacion.toString()
        };
    }

    @Override
    protected void aplicarDatosPropios(String[] datos) {
        this.canonActual = Double.parseDouble(datos[0]);
        this.porcentajeIncremento = Double.parseDouble(datos[1]);
        this.ultimaFechaActualizacion = LocalDate.parse(datos[2]);
    }
}
