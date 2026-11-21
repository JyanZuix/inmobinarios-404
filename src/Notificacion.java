/**
 * Notificación para el agente. Solo está lo mínimo para que compile (RF-16 a RF-21 vienen después).
 */
public class Notificacion implements Persistible {

    // AQUÍ VAN tipo, mensaje, fechaEvento, atendida e idOrigen.
    // AQUÍ VAN marcarAtendida(), estaAtendida() y esMismaQue(otra).

    @Override
    public void guardar() {
        // AQUÍ VA la persistencia de Notificacion (RF-21).
    }

    @Override
    public void cargar() {
        // AQUÍ VA la lectura de Notificacion desde archivo.
    }
}
