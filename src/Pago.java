import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pago implements Persistible, Notificable {
    private static final String ARCHIVO = "pagos.txt";
    private static final int DIAS_AVISO_PREVIO = 7; // RF-16: avisar los pagos que vencen en 7 días o menos

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

        public double calcularValor() {
            throw new UnsupportedOperationException("Pendiente: RF-06");
        }
    }
}
