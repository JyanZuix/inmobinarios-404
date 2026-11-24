import java.time.LocalDate;
import java.time.YearMonth;
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
        validarDuracion(fechaInicio, fechaFin);
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
        if (ACTIVO.equals(estado)) {
            // Un contrato terminado antes de tiempo (RF-10) puede haber durado menos de un mes.
            validarDuracion(fechaInicio, fechaFin);
        }
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
     * Registrar un pago (RF-05) llama a este método con la fecha del pago, para que el canon
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
     * RF-05: registra el pago mensual del periodo dado ("2026-11") en la fecha dada.
     * Solo se puede si el contrato está activo; el pago queda asociado a la fecha y al periodo que cubre.
     * Antes de cobrar se aplica el incremento anual (RF-07) para que el canon esté al día.
     * RNF-02: la fecha de pago no puede ser anterior al inicio del contrato.
     */
    public Pago registrarPago(String periodo, LocalDate fechaPago) {
        if (!ACTIVO.equals(estado)) {
            throw new IllegalStateException("Solo se pueden registrar pagos de un contrato activo (estado actual: "
                    + estado + ").");
        }
        if (fechaPago == null || fechaPago.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de pago (" + fechaPago + ") no puede ser anterior al inicio "
                    + "del contrato (" + fechaInicio + ").");
        }
        YearMonth mes;
        try {
            mes = YearMonth.parse(periodo);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("El periodo debe tener el formato AAAA-MM (se recibió: '" + periodo + "').");
        }
        // Se crean las cuotas que falten hasta ese mes (permite pagar por adelantado).
        LocalDate hasta = mes.atEndOfMonth().isAfter(fechaPago) ? mes.atEndOfMonth() : fechaPago;
        generarCuotasHasta(hasta);
        Pago pago = buscarPagoDelPeriodo(periodo);
        if (pago == null) {
            throw new IllegalArgumentException("El contrato " + numeroContrato + " no tiene cuota para el periodo "
                    + periodo + " (va del " + fechaInicio + " al " + fechaFin + ").");
        }
        aplicarIncrementoAnual(fechaPago);
        pago.registrarPago(fechaPago, canonActual);
        return pago;
    }

    /** Agrega un pago ya armado al contrato (lo usa RF-08 para el historial). */
    public void agregarPago(Pago pago) {
        if (estado == null) {
            throw new IllegalStateException("El contrato aún no ha sido creado: regístrelo con GestorContratos.agregarContrato() antes de "
                    + "agregar pagos.");
        }
        if (!ACTIVO.equals(estado)) {
            throw new IllegalStateException("Solo se pueden agregar pagos a un contrato activo (estado actual: "
                    + estado + ").");
        }
        incorporarPago(pago);
    }

    /**
     * RNF-01: agrega un pago que ya existía, al recrear el sistema desde archivo. No exige contrato activo,
     * porque un contrato finalizado también conserva sus pagos. Default: solo lo llama GestorContratos.
     */
    void cargarPago(Pago pago) {
        incorporarPago(pago);
    }

    // Lo común a agregar y cargar: pago no nulo, id no repetido dentro del contrato, y enlazarlo.
    private void incorporarPago(Pago pago) {
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

    // ---- Cuotas y avisos de pago (RF-16) ----

    /**
     * RF-16: crea las cuotas mensuales pendientes que falten hasta la fecha dada. La cuota k vence el mismo
     * día del inicio, k meses después (contrato que empieza el 21 → cuotas el 21 de cada mes), mientras sea
     * antes del fin del contrato. Si ya existe un pago para ese periodo (registrado o pendiente), no se repite.
     */
    @Override
    public void generarCuotasHasta(LocalDate fecha) {
        if (!ACTIVO.equals(estado)) {
            return; // un contrato sin crear o finalizado no genera cuotas nuevas
        }
        for (int k = 0; ; k++) {
            LocalDate limite = fechaInicio.plusMonths(k); // desde el inicio (no encadenado) para no correr el día
            if (!limite.isBefore(fechaFin) || limite.isAfter(fecha)) {
                break;
            }
            String periodo = YearMonth.from(limite).toString(); // "2026-11"
            if (!tienePagoDelPeriodo(periodo)) {
                incorporarPago(new Pago(siguienteIdPago(), limite, null, periodo, 0, Pago.PENDIENTE));
            }
        }
    }

    /**
     * RF-16: los avisos del contrato (RF-17, en la clase padre) más los de cada uno de sus pagos.
     * Un contrato finalizado ya no avisa sus pagos (igual que RF-09 y RF-19, que solo miran contratos activos).
     */
    @Override
    public List<Notificacion> generarNotificaciones(LocalDate hoy) {
        List<Notificacion> avisos = super.generarNotificaciones(hoy);
        if (ACTIVO.equals(estado)) {
            for (Pago pago : pagos) {
                avisos.addAll(pago.generarNotificaciones(hoy)); // composición: el gestor no conoce los pagos
            }
        }
        return avisos;
    }

    /**
     * RF-10: al terminar, se quitan las cuotas pendientes que vencen en la fecha de terminación o después,
     * porque cubren meses en los que ya no hay arriendo. Las cuotas anteriores se conservan (se siguen debiendo)
     * y también los pagos ya registrados. Las cuotas quitadas se borran también del archivo.
     */
    @Override
    protected void alTerminar(LocalDate fecha) {
        pagos.removeIf(pago -> {
            boolean sobra = pago.estaPendiente() && !pago.getFechaLimite().isBefore(fecha);
            if (sobra) {
                pago.eliminarDelArchivo();
            }
            return sobra;
        });
    }

    /** RF-19: el pago en mora más antiguo (reutiliza Pago.estaEnMora de RF-16), o null si está al día. */
    @Override
    public LocalDate moraDesde(LocalDate hoy) {
        LocalDate masAntigua = null;
        for (Pago pago : pagos) {
            if (pago.estaEnMora(hoy) && (masAntigua == null || pago.getFechaLimite().isBefore(masAntigua))) {
                masAntigua = pago.getFechaLimite();
            }
        }
        return masAntigua;
    }

    private boolean tienePagoDelPeriodo(String periodo) {
        return buscarPagoDelPeriodo(periodo) != null;
    }

    private Pago buscarPagoDelPeriodo(String periodo) {
        for (Pago pago : pagos) {
            if (pago.getPeriodoCubierto().equals(periodo)) {
                return pago;
            }
        }
        return null;
    }

    private int siguienteIdPago() {
        int mayor = 0;
        for (Pago pago : pagos) {
            mayor = Math.max(mayor, pago.getIdPago());
        }
        return mayor + 1;
    }

    // Monto provisional: canon por los meses completos del contrato (siempre es 1 o más, ver validarDuracion).
    @Override
    public double calcularMontoFinal() {
        long meses = Math.max(1, ChronoUnit.MONTHS.between(fechaInicio, fechaFin));
        return canonActual * meses;
    }

    @Override
    protected String estadoPropiedadAlCrear() {
        return Propiedad.ARRENDADA;
    }

    @Override
    public String toString() {
        return super.toString() + " | canon actual: " + Utilidades.moneda(canonActual) + " | incremento anual: "
                + Utilidades.porcentaje(porcentajeIncremento);
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

    // RF-03 / RNF-02: un arriendo dura mínimo un mes (el canon es mensual; un contrato de días no tiene sentido).
    private static void validarDuracion(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin.isBefore(fechaInicio.plusMonths(1))) {
            throw new IllegalArgumentException("Un contrato de arriendo debe durar mínimo un mes: del " + fechaInicio
                    + " al " + fechaFin + " no alcanza (la fecha de fin debe ser el " + fechaInicio.plusMonths(1)
                    + " o después).");
        }
    }

    private static void validarPorcentaje(double porcentajeIncremento) {
        if (!(porcentajeIncremento >= 0) || Double.isInfinite(porcentajeIncremento)) {
            throw new IllegalArgumentException("El porcentaje de incremento no puede ser negativo (se recibió: "
                    + porcentajeIncremento + ").");
        }
    }
}
