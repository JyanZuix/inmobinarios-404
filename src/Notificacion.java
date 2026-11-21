import java.time.LocalDate;

/**
 * Notificación para el agente (RF-16 a RF-21). Solo está el esqueleto; la lógica viene después.
 */
public class Notificacion implements Persistible {
    private String tipo;
    private String mensaje;
    private LocalDate fechaEvento;
    private boolean atendida;
    private String idOrigen;

    public Notificacion(String tipo, String mensaje, LocalDate fechaEvento, String idOrigen) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fechaEvento = fechaEvento;
        this.idOrigen = idOrigen;
        this.atendida = false;
    }

    public void marcarAtendida() {
        // Pendiente: RF-21
    }

    public boolean estaAtendida() {
        return atendida;
    }

    public boolean esMismaQue(Notificacion otra) {
        throw new UnsupportedOperationException("Pendiente: RF-21");
    }

    @Override
    public void guardar() {
        // Pendiente: RNF-01
    }

    @Override
    public void cargar() {
        // Pendiente: RNF-01
    }

    public String getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public String getIdOrigen() {
        return idOrigen;
    }
}
