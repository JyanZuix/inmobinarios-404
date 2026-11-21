import java.time.LocalDate;

<<<<<<< HEAD
public class ContratoVenta extends Contrato {
=======
/**
 * Contrato de venta (RF-03): tiene valor de venta, cuota inicial y saldo financiado, sin pagos periódicos.
 * La fecha de fin es la fecha prevista de cierre de la venta.
 */
public class ContratoVenta extends Contrato {

>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    private double valorVenta;
    private double cuotaInicial;
    private double saldoFinanciado;

    public ContratoVenta(Propiedad propiedad, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin,
<<<<<<< HEAD
                         double valorVenta, double cuotaInicial) {
        super(propiedad, cliente, fechaInicio, fechaFin);
        this.valorVenta = valorVenta;
        this.cuotaInicial = cuotaInicial;
        this.saldoFinanciado = valorVenta - cuotaInicial;
    }

    // Reconstruye un contrato ya existente (al cargarlo desde el archivo)
    public ContratoVenta(int numeroContrato, Propiedad propiedad, Cliente cliente,
                         LocalDate fechaInicio, LocalDate fechaFin, String estado,
                         double valorVenta, double cuotaInicial, double saldoFinanciado) {
        super(numeroContrato, propiedad, cliente, fechaInicio, fechaFin, estado);
        this.valorVenta = valorVenta;
        this.cuotaInicial = cuotaInicial;
        this.saldoFinanciado = saldoFinanciado;
    }

    @Override
    public double calcularMontoFinal() {
        throw new UnsupportedOperationException("Pendiente: RF-03");
    }

    public void registrarPagoUnico(double monto, LocalDate fecha) {
        // Pendiente
    }

    public double getValorVenta() {
        return valorVenta;
    }

    public double getCuotaInicial() {
        return cuotaInicial;
    }

    public double getSaldoFinanciado() {
        return saldoFinanciado;
=======
                         double cuotaInicial) {
        super(propiedad, cliente, fechaInicio, fechaFin);
        // El valor de venta es el valor base de la propiedad, así que la propiedad debe estar en modo Venta.
        if (!Propiedad.MODO_VENTA.equals(propiedad.getModo())) {
            throw new IllegalArgumentException("No se puede crear un contrato de venta: la propiedad '"
                    + propiedad.getDireccion() + "' está en modo " + propiedad.getModo() + ".");
        }
        double precio = propiedad.getValorBase();
        if (!(cuotaInicial >= 0) || cuotaInicial > precio) {
            throw new IllegalArgumentException("La cuota inicial debe estar entre 0 y el valor de venta ("
                    + precio + "); se recibió: " + cuotaInicial + ".");
        }
        this.valorVenta = precio;
        this.cuotaInicial = cuotaInicial;
        this.saldoFinanciado = precio - cuotaInicial;
    }

    // AQUÍ VA registrarPagoUnico(monto, fecha).

    // Cuota inicial + saldo financiado = valor de venta.
    @Override
    public double calcularMontoFinal() {
        return cuotaInicial + saldoFinanciado;
    }

    @Override
    protected String estadoPropiedadAlCrear() {
        return Propiedad.FINALIZADO;
    }

    public double getValorVenta() { return valorVenta; }
    public double getCuotaInicial() { return cuotaInicial; }
    public double getSaldoFinanciado() { return saldoFinanciado; }

    // ---- Persistencia: campos propios de esta hija ----
    @Override
    protected String[] datosPropios() {
        return new String[]{
                String.valueOf(valorVenta),
                String.valueOf(cuotaInicial),
                String.valueOf(saldoFinanciado)
        };
    }

    @Override
    protected void aplicarDatosPropios(String[] datos) {
        this.valorVenta = Double.parseDouble(datos[0]);
        this.cuotaInicial = Double.parseDouble(datos[1]);
        this.saldoFinanciado = Double.parseDouble(datos[2]);
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }
}
