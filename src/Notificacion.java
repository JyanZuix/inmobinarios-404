import java.time.LocalDate;

/**
 * Aviso para el agente sobre algo que requiere su atención (RF-16 a RF-21).
 * La crean los objetos Notificable; la guardan y listan los gestores.
 */
public class Notificacion implements Persistible {

    // ===================== Tipos de aviso =====================
    public static final String PAGO_POR_VENCER = "PAGO_POR_VENCER"; // RF-16
    public static final String PAGO_EN_MORA = "PAGO_EN_MORA";       // RF-16
    public static final String CONTRATO_POR_VENCER = "CONTRATO_POR_VENCER"; // RF-17
    public static final String CONTRATO_VENCIDO = "CONTRATO_VENCIDO";       // RF-17 (activo con fecha de fin pasada)
    public static final String SEGUIMIENTO = "SEGUIMIENTO";                 // RF-18 y RF-19 (cita de hoy o vencida)
    public static final String CLIENTE_CONTRATO_POR_VENCER = "CLIENTE_CONTRATO_POR_VENCER"; // RF-19
    public static final String CLIENTE_PAGOS_EN_MORA = "CLIENTE_PAGOS_EN_MORA";             // RF-19

    public static final String ARCHIVO = "notificaciones.txt";

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

    // RF-21 marca el aviso como atendido y asi ya no sale en las pendeintes
    public void marcarAtendida() {
        atendida = true;
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

    // clave del registro, tipo + origen + fecha porque eso es lo que la hace unica
    public String clave() {
        return tipo + ":" + idOrigen + ":" + fechaEvento;
    }

    // RF-21 se guarda en notificaciones.txt con el estado atendida, asi al reinicar no vuelve
    @Override
    public void guardar() {
        String[] campos = {clave(), tipo, mensaje, fechaEvento.toString(), idOrigen, String.valueOf(atendida)};
        ArchivoPlano.guardarRegistro(ARCHIVO, campos);
    }

    // lee otra vez el registro y recupera mensaje y si ya estaba atendida
    @Override
    public void cargar() {
        String[] r = ArchivoPlano.buscarRegistro(ARCHIVO, clave());
        if (r != null) {
            mensaje = r[2];
            atendida = Boolean.parseBoolean(r[5]);
        }
    }

    // crea la notificacion desde una linea del archvio
    public static Notificacion desdeRegistro(String[] r) {
        Notificacion n = new Notificacion(r[1], r[2], LocalDate.parse(r[3]), r[4]);
        n.atendida = Boolean.parseBoolean(r[5]);
        return n;
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
