import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Contrato de arriendo (RF-03): solo este tipo de contrato tiene canon, incremento anual y pagos.
 */
public class ContratoArriendo extends Contrato {

    private double canonActual;
    private double porcentajeIncremento;          // por ejemplo 0.05 = 5 % de incremento anual
    private LocalDate ultimaFechaActualizacion;   // arranca en la fecha de inicio del contrato

    // Composición: los pagos pertenecen al contrato y no existen sin él.
    private final List<Pago> pagos = new ArrayList<>();

    public ContratoArriendo(Propiedad propiedad, Arrendatario cliente, LocalDate fechaInicio, LocalDate fechaFin,
                            double porcentajeIncremento) {
        super(propiedad, cliente, fechaInicio, fechaFin);
        validarModo(propiedad);
        validarPorcentaje(porcentajeIncremento);
        // El canon inicial es el valor base de la propiedad.
        this.canonActual = propiedad.getValorBase();
        this.porcentajeIncremento = porcentajeIncremento;
        this.ultimaFechaActualizacion = fechaInicio;
    }

    // Reconstruye un contrato ya existente (al cargarlo desde el archivo): el canon puede haber subido.
    public ContratoArriendo(int numeroContrato, Propiedad propiedad, Arrendatario cliente,
                            LocalDate fechaInicio, LocalDate fechaFin, String estado,
                            double canonActual, double porcentajeIncremento, LocalDate ultimaFechaActualizacion) {
        super(numeroContrato, propiedad, cliente, fechaInicio, fechaFin, estado);
        validarModo(propiedad);
        validarPorcentaje(porcentajeIncremento);
        if (!(canonActual > 0) || Double.isInfinite(canonActual)) {
            throw new IllegalArgumentException("El canon debe ser mayor a cero (se recibió: " + canonActual + ").");
        }
        if (ultimaFechaActualizacion == null || ultimaFechaActualizacion.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La última fecha de actualización no puede ser anterior al inicio del contrato.");
        }
        this.canonActual = canonActual;
        this.porcentajeIncremento = porcentajeIncremento;
        this.ultimaFechaActualizacion = ultimaFechaActualizacion;
    }

    /**
     * RF-07: aplica todos los incrementos anuales que se hayan cumplido hasta la fecha dada.
     * Si pasó más de un año sin actualizar, aplica uno por cada aniversario (el incremento es compuesto).
     * Cuando exista RF-05, registrar un pago llamará a este método con la fecha del pago, para que el canon
     * siempre esté al día sin que el agente tenga que calcularlo.
     */
    public void aplicarIncrementoAnual(LocalDate fecha) {
        if (estado == null) {
            throw new IllegalStateException("El contrato aún no ha sido creado: regístrelo con GestorContratos.agregarContrato() antes de "
                    + "aplicar el incremento.");
        }
        if (!ACTIVO.equals(estado)) {
            throw new IllegalStateException("Solo se puede aplicar el incremento a un contrato activo (estado actual: "
                    + estado + ").");
        }
        if (fecha == null || fecha.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha (" + fecha + ") no puede ser anterior al inicio del contrato ("
                    + fechaInicio + ").");
        }
        while (verificarAniversario(fecha)) {
            canonActual = canonActual * (1 + porcentajeIncremento);
            ultimaFechaActualizacion = ultimaFechaActualizacion.plusYears(1);
        }
    }

    /**
     * RF-07: true si a la fecha dada ya se cumplieron 12 meses desde la última actualización del canon.
     * Un aniversario que coincide con el fin del contrato no cuenta, porque el contrato ya termina.
     */
    public boolean verificarAniversario(LocalDate fecha) {
        LocalDate siguienteAniversario = ultimaFechaActualizacion.plusYears(1);
        return !siguienteAniversario.isAfter(fecha) && siguienteAniversario.isBefore(fechaFin);
    }

    /**
     * RF-08: historial de pagos ordenado por fecha de pago, incluidos los que tuvieron mora.
     * Los pagos pendientes (sin fecha de pago) van al final, ordenados por su fecha límite.
     * Devuelve una copia: ordenarla o modificarla no cambia los pagos del contrato.
     */
    public List<Pago> listarPagos() {
        List<Pago> ordenados = new ArrayList<>(pagos);
        ordenados.sort(Comparator.comparing(Pago::getFechaPago, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Pago::getFechaLimite));
        return ordenados;
    }

    /**
     * Agrega un pago al contrato (mínimo de RF-05, necesario para RF-08).
     * Pendiente de RF-05: registrarPago() y validarPeriodo().
     */
    public void agregarPago(Pago pago) {
        if (estado == null) {
            throw new IllegalStateException("El contrato aún no ha sido creado: regístrelo con GestorContratos.agregarContrato() antes de "
                    + "agregar pagos.");
        }
        if (!ACTIVO.equals(estado)) {
            throw new IllegalStateException("Solo se pueden agregar pagos a un contrato activo (estado actual: "
                    + estado + ").");
        }
        if (pago == null) {
            throw new IllegalArgumentException("No se puede agregar un pago nulo.");
        }
        for (Pago existente : pagos) {
            if (existente.getIdPago() == pago.getIdPago()) {
                throw new IllegalArgumentException("El contrato " + numeroContrato + " ya tiene un pago con el id "
                        + pago.getIdPago() + ".");
            }
        }
        pago.asignarContrato(numeroContrato); // el pago sabe a qué contrato pertenece (lo necesita para guardarse)
        pagos.add(pago);
    }

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

    /** Guarda el contrato y, como los pagos son parte de él (composición), también cada uno de sus pagos. */
    @Override
    public void guardar() {
        super.guardar();
        for (Pago pago : pagos) {
            pago.guardar();
        }
    }

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

    // El canon sale del valor base de la propiedad, así que la propiedad debe estar en modo Arriendo.
    private static void validarModo(Propiedad propiedad) {
        if (!Propiedad.MODO_ARRIENDO.equals(propiedad.getModo())) {
            throw new IllegalArgumentException("No se puede crear un contrato de arriendo: la propiedad '"
                    + propiedad.getDireccion() + "' está en modo " + propiedad.getModo() + ".");
        }
    }

    private static void validarPorcentaje(double porcentajeIncremento) {
        if (!(porcentajeIncremento >= 0) || Double.isInfinite(porcentajeIncremento)) {
            throw new IllegalArgumentException("El porcentaje de incremento no puede ser negativo (se recibió: "
                    + porcentajeIncremento + ").");
        }
    }
}
