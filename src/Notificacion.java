import java.time.LocalDate;

/**
 * Aviso para el agente sobre algo que requiere su atención (RF-16 a RF-21).
 * La crean los objetos Notificable; la guardan y listan los gestores.
 */
public class Notificacion implements Persistible {

    // ===================== Tipos de aviso =====================
    public static final String PAGO_POR_VENCER = "PAGO_POR_VENCER"; // RF-16
    public static final String PAGO_EN_MORA = "PAGO_EN_MORA";       // RF-16

    private String tipo;
    private String mensaje;
    private LocalDate fechaEvento; // fecha del hecho que se avisa (fecha límite, fin del contrato, cita...)
    private boolean atendida;
    private String idOrigen;       // quién lo generó, p. ej. "PAGO:4:2" (contrato 4, pago 2)

    public Notificacion(String tipo, String mensaje, LocalDate fechaEvento, String idOrigen) {
        if (tipo == null || tipo.isBlank() || mensaje == null || mensaje.isBlank()
                || idOrigen == null || idOrigen.isBlank()) {
            throw new IllegalArgumentException("La notificación necesita tipo, mensaje y origen.");
        }
        if (fechaEvento == null) {
            throw new IllegalArgumentException("La notificación necesita la fecha del evento.");
        }
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

    /**
     * Dos notificaciones son "la misma" si avisan del mismo hecho: mismo tipo, mismo origen y misma fecha del
     * evento. El mensaje no cuenta (es solo texto). Así, recolectar dos veces no duplica avisos (RF-21).
     */
    public boolean esMismaQue(Notificacion otra) {
        return otra != null
                && tipo.equals(otra.tipo)
                && idOrigen.equals(otra.idOrigen)
                && fechaEvento.equals(otra.fechaEvento);
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

    @Override
    public String toString() {
        return "[" + tipo + "] " + fechaEvento + " - " + mensaje;
    }
}
