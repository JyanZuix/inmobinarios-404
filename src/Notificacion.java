<<<<<<< HEAD
import java.time.LocalDate;

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
=======
/**
 * Notificación para el agente. Solo está lo mínimo para que compile (RF-16 a RF-21 vienen después).
 */
public class Notificacion implements Persistible {

    // AQUÍ VAN tipo, mensaje, fechaEvento, atendida e idOrigen.
    // AQUÍ VAN marcarAtendida(), estaAtendida() y esMismaQue(otra).

    @Override
    public void guardar() {
        // AQUÍ VA la persistencia de Notificacion (RF-21).
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }

    @Override
    public void cargar() {
<<<<<<< HEAD
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
=======
        // AQUÍ VA la lectura de Notificacion desde archivo.
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }
}
