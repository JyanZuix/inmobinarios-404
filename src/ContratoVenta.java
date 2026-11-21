import java.time.LocalDate;

/**
 * Contrato de venta (RF-03): tiene valor de venta, cuota inicial y saldo financiado, sin pagos periódicos.
 * La fecha de fin es la fecha prevista de cierre de la venta.
 */
public class ContratoVenta extends Contrato {

    private double valorVenta;
    private double cuotaInicial;
    private double saldoFinanciado;

    public ContratoVenta(Propiedad propiedad, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin,
                         double cuotaInicial) {
        super(propiedad, cliente, fechaInicio, fechaFin);
        validarModo(propiedad);
        // El valor de venta es el valor base de la propiedad.
        double precio = propiedad.getValorBase();
        validarCuotaInicial(cuotaInicial, precio);
        this.valorVenta = precio;
        this.cuotaInicial = cuotaInicial;
        this.saldoFinanciado = precio - cuotaInicial;
    }

    // Reconstruye un contrato ya existente (al cargarlo desde el archivo)
    public ContratoVenta(int numeroContrato, Propiedad propiedad, Cliente cliente,
                         LocalDate fechaInicio, LocalDate fechaFin, String estado,
                         double valorVenta, double cuotaInicial, double saldoFinanciado) {
        super(numeroContrato, propiedad, cliente, fechaInicio, fechaFin, estado);
        validarModo(propiedad);
        validarCuotaInicial(cuotaInicial, valorVenta);
        if (saldoFinanciado < 0 || saldoFinanciado > valorVenta - cuotaInicial) {
            throw new IllegalArgumentException("El saldo financiado debe estar entre 0 y el valor de venta menos la "
                    + "cuota inicial; se recibió: " + saldoFinanciado + ".");
        }
        this.valorVenta = valorVenta;
        this.cuotaInicial = cuotaInicial;
        this.saldoFinanciado = saldoFinanciado;
    }

    public void registrarPagoUnico(double monto, LocalDate fecha) {
        // Pendiente
    }

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
    }

    // El valor de venta sale del valor base de la propiedad, así que debe estar en modo Venta.
    private static void validarModo(Propiedad propiedad) {
        if (!Propiedad.MODO_VENTA.equals(propiedad.getModo())) {
            throw new IllegalArgumentException("No se puede crear un contrato de venta: la propiedad '"
                    + propiedad.getDireccion() + "' está en modo " + propiedad.getModo() + ".");
        }
    }

    private static void validarCuotaInicial(double cuotaInicial, double precio) {
        if (!(cuotaInicial >= 0) || cuotaInicial > precio) {
            throw new IllegalArgumentException("La cuota inicial debe estar entre 0 y el valor de venta ("
                    + precio + "); se recibió: " + cuotaInicial + ".");
        }
    }
}
