import java.time.LocalDate;
import java.util.List;

public abstract class Contrato implements Persistible, Notificable {
    private static int contadorContratos = 0;

    protected int numeroContrato;
    protected LocalDate fechaInicio;
    protected LocalDate fechaFin;
    protected String estado;
    protected Propiedad propiedad;
    protected Cliente cliente;

    public Contrato(Propiedad propiedad, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin) {
        this(generarNumeroContrato(), propiedad, cliente, fechaInicio, fechaFin, "Activo");
    }

    // Reconstruye un contrato ya existente (al cargarlo desde el archivo)
    public Contrato(int numeroContrato, Propiedad propiedad, Cliente cliente,
                    LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.numeroContrato = numeroContrato;
        this.propiedad = propiedad;
        this.cliente = cliente;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        contadorContratos = Math.max(contadorContratos, numeroContrato);
    }

    public boolean crearContrato() {
        throw new UnsupportedOperationException("Pendiente: RF-03");
    }

    public boolean verificarDisponibilidad() {
        throw new UnsupportedOperationException("Pendiente: RF-04");
    }

    public void terminarContrato() {
        // Pendiente: RF-10
    }

    public abstract double calcularMontoFinal();

    public static int generarNumeroContrato() {
        return contadorContratos + 1;
    }

    @Override
    public List<Notificacion> generarNotificaciones() {
        throw new UnsupportedOperationException("Pendiente: RF-17");
    }

    @Override
    public void guardar() {
        // Pendiente: RNF-01
    }

    @Override
    public void cargar() {
        // Pendiente: RNF-01
    }

    public int getNumeroContrato() {
        return numeroContrato;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public Propiedad getPropiedad() {
        return propiedad;
    }

    public Cliente getCliente() {
        return cliente;
    }
}
