import java.time.LocalDate;

public class ContratoVenta extends Contrato {
    private double valorVenta;
    private double cuotaInicial;
    private double saldoFinanciado;

    public ContratoVenta(Propiedad propiedad, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin,
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
    }
}
