import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Pago implements Persistible, Notificable {
    private static final String ARCHIVO = "pagos.txt";
    private static final int DIAS_AVISO_PREVIO = 7; // RF-16: avisar los pagos que vencen en 7 días o menos
    public static final String PENDIENTE = "pendiente";
    public static final String PAGADO = "pagado";
    public static final String PAGADO_CON_MORA = "pagado con mora";
    // RF-06: interés de mora por cada día de atraso, sobre el canon (0,1 % diario ≈ 3 % al mes).
    public static final double TASA_MORA_DIARIA = 0.001;

    private int numeroContrato; // contrato al que pertenece; 0 mientras no se haya agregado a uno
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
        // RNF-02: datos coherentes antes de crear el pago.
        if (idPago < 1) {
            throw new IllegalArgumentException("El id del pago debe ser mayor a cero (se recibió: " + idPago + ").");
        }
        if (fechaLimite == null) {
            throw new IllegalArgumentException("La fecha límite del pago es obligatoria.");
        }
        if (periodoCubierto == null || !periodoCubierto.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("El periodo cubierto debe tener el formato AAAA-MM (se recibió: "
                    + periodoCubierto + ").");
        }
        if (!(montoPagado >= 0) || Double.isInfinite(montoPagado)) {
            throw new IllegalArgumentException("El monto pagado no puede ser negativo (se recibió: " + montoPagado + ").");
        }
        boolean pagado = PAGADO.equals(estadoPago) || PAGADO_CON_MORA.equals(estadoPago); // RF-05 / RF-06
        if (!pagado && !PENDIENTE.equals(estadoPago)) {
            throw new IllegalArgumentException("El estado del pago debe ser '" + PENDIENTE + "', '" + PAGADO + "' o '"
                    + PAGADO_CON_MORA + "' (se recibió: " + estadoPago + ").");
        }
        if (pagado && fechaPago == null) {
            throw new IllegalArgumentException("Un pago '" + estadoPago + "' debe tener fecha de pago.");
        }
        if (PENDIENTE.equals(estadoPago) && fechaPago != null) {
            throw new IllegalArgumentException("Un pago '" + PENDIENTE + "' todavía no tiene fecha de pago.");
        }
        this.idPago = idPago;
        this.fechaLimite = fechaLimite;
        this.fechaPago = fechaPago;
        this.periodoCubierto = periodoCubierto;
        this.montoPagado = montoPagado;
        this.estadoPago = estadoPago;
    }

    /**
     * RF-05: registra el pago de esta cuota en la fecha dada, por el valor del canon.
     * Queda asociado a la fecha de pago y al periodo que cubre (periodoCubierto).
     * Default: solo lo llama ContratoArriendo.registrarPago(), que antes verifica que el contrato esté activo.
     */
    void registrarPago(LocalDate fechaPago, double canon) {
        if (!estaPendiente()) {
            throw new IllegalStateException("El pago del periodo " + periodoCubierto + " ya fue registrado el "
                    + this.fechaPago + ".");
        }
        if (fechaPago == null) {
            throw new IllegalArgumentException("La fecha de pago es obligatoria.");
        }
        if (!(canon > 0) || Double.isInfinite(canon)) {
            throw new IllegalArgumentException("El canon debe ser mayor a cero (se recibió: " + canon + ").");
        }
        if (!validarPeriodo()) {
            throw new IllegalStateException("El periodo '" + periodoCubierto + "' no corresponde a la fecha límite "
                    + fechaLimite + ".");
        }
        this.fechaPago = fechaPago;
        if (verificarFechaLimite()) {
            // RF-06: pagó tarde → interés según los días de atraso, sumado al valor a pagar.
            this.mora = new Mora(diasAtraso(), TASA_MORA_DIARIA);
            this.montoPagado = canon + mora.calcularValor(canon);
            this.estadoPago = PAGADO_CON_MORA;
        } else {
            this.mora = null;
            this.montoPagado = canon;
            this.estadoPago = PAGADO;
        }
    }

    /** RF-05: el periodo cubierto ("2026-11") debe ser el mes de la fecha límite de la cuota. */
    public boolean validarPeriodo() {
        try {
            return YearMonth.parse(periodoCubierto).equals(YearMonth.from(fechaLimite));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * RF-06: valor del interés de mora incluido en el monto pagado (0 si se pagó a tiempo o sigue pendiente).
     * Si el pago se cargó desde archivo, se recalcula con los datos guardados:
     * monto = canon * (1 + tasa * días)  →  interés = monto - monto / (1 + tasa * días).
     */
    public double calcularMora() {
        if (!verificarFechaLimite()) {
            return 0;
        }
        if (mora != null) {
            return mora.valorInteres;
        }
        double canon = montoPagado / (1 + TASA_MORA_DIARIA * diasAtraso());
        return montoPagado - canon;
    }

    /** RF-06: true si el pago se registró después de la fecha límite (es decir, con atraso). */
    public boolean verificarFechaLimite() {
        return fechaPago != null && fechaPago.isAfter(fechaLimite);
    }

    /** RF-06: días entre la fecha límite y la fecha de pago (0 si se pagó a tiempo o sigue pendiente). */
    public int diasAtraso() {
        return verificarFechaLimite() ? (int) ChronoUnit.DAYS.between(fechaLimite, fechaPago) : 0;
    }

    /** Un pago está pendiente mientras no tenga fecha de pago (no se ha registrado). */
    public boolean estaPendiente() {
        return fechaPago == null;
    }

    /** Un pago está en mora si sigue pendiente y su fecha límite ya pasó. */
    public boolean estaEnMora(LocalDate hoy) {
        return estaPendiente() && fechaLimite.isBefore(hoy);
    }

    /**
     * RF-16: un pago pendiente avisa si ya está en mora o si vence en los próximos 7 días.
     * Un pago ya registrado no genera aviso.
     */
    @Override
    public List<Notificacion> generarNotificaciones(LocalDate hoy) {
        List<Notificacion> avisos = new ArrayList<>();
        String idOrigen = "PAGO:" + numeroContrato + ":" + idPago;
        String descripcion = "Pago " + idPago + " del contrato " + numeroContrato + " (periodo " + periodoCubierto + ")";
        if (estaEnMora(hoy)) {
            avisos.add(new Notificacion(Notificacion.PAGO_EN_MORA,
                    descripcion + " en mora desde el " + fechaLimite + ".", fechaLimite, idOrigen));
        } else if (estaPendiente() && !fechaLimite.isAfter(hoy.plusDays(DIAS_AVISO_PREVIO))) {
            avisos.add(new Notificacion(Notificacion.PAGO_POR_VENCER,
                    descripcion + " vence el " + fechaLimite + ".", fechaLimite, idOrigen));
        }
        return avisos;
    }

    // Lo llama ContratoArriendo.agregarPago(): un pago pertenece a un solo contrato.
    void asignarContrato(int numero) {
        if (this.numeroContrato != 0 && this.numeroContrato != numero) {
            throw new IllegalStateException("El pago " + idPago + " ya pertenece al contrato " + this.numeroContrato + ".");
        }
        this.numeroContrato = numero;
    }

    // ===================== Persistencia (RNF-01) =====================

    @Override
    public void guardar() {
        if (numeroContrato == 0) {
            throw new IllegalStateException("El pago " + idPago + " no pertenece a ningún contrato: agréguelo con "
                    + "ContratoArriendo.agregarPago() antes de guardarlo.");
        }
        String[] campos = {
                clave(),                                           // clave del registro: numeroContrato:idPago
                fechaLimite.toString(),
                fechaPago == null ? "" : fechaPago.toString(),     // vacío mientras el pago esté pendiente
                periodoCubierto,
                String.valueOf(montoPagado),
                estadoPago
        };
        ArchivoPlano.guardarRegistro(ARCHIVO, campos);
    }

    @Override
    public void cargar() {
        String[] registro = ArchivoPlano.buscarRegistro(ARCHIVO, clave());
        if (registro == null) {
            throw new IllegalStateException("No hay ningún pago guardado con la clave " + clave() + ".");
        }
        try {
            this.fechaLimite = LocalDate.parse(registro[1]);
            this.fechaPago = registro[2].isEmpty() ? null : LocalDate.parse(registro[2]);
            this.periodoCubierto = registro[3];
            this.montoPagado = Double.parseDouble(registro[4]);
            this.estadoPago = registro[5];
        } catch (RuntimeException e) {
            throw new IllegalStateException("El registro guardado del pago " + clave() + " está dañado: "
                    + e.getMessage(), e);
        }
    }

    // Lo llama ContratoArriendo al terminar (RF-10): una cuota que ya no se debe no debe volver al recargar.
    void eliminarDelArchivo() {
        ArchivoPlano.eliminarRegistro(ARCHIVO, clave());
    }

    // Clave única: el id del pago solo es único dentro de su contrato.
    private String clave() {
        return numeroContrato + ":" + idPago;
    }

    public int getNumeroContrato() {
        return numeroContrato;
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

        // RF-06: interés = canon * tasa diaria * días de atraso.
        public double calcularValor(double canon) {
            valorInteres = canon * tasaMora * diasAtraso;
            return valorInteres;
        }
    }
}
