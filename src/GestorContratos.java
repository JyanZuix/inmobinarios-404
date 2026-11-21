import java.util.ArrayList;
<<<<<<< HEAD
import java.util.List;

public class GestorContratos {
    private List<Contrato> contratos = new ArrayList<>();
    private List<Notificacion> notificaciones = new ArrayList<>();

    public void agregarContrato(Contrato contrato) {
        // Pendiente: RF-03
    }

    public List<Contrato> filtrarContratosEnMora() {
        throw new UnsupportedOperationException("Pendiente: RF-09");
    }

    public void verificarPagosVencidos() {
        // Pendiente: RF-09
    }

    public List<Contrato> listarContratosPorVencer(int dias) {
        throw new UnsupportedOperationException("Pendiente: RF-15");
    }

    public void recolectarNotificaciones() {
        // Pendiente: RF-16, RF-17, RF-19
    }

    public List<Notificacion> listarNotificacionesPendientes() {
        throw new UnsupportedOperationException("Pendiente: RF-20");
    }

    public List<Contrato> getContratos() {
        return List.copyOf(contratos);
    }

    public List<Notificacion> getNotificaciones() {
        return List.copyOf(notificaciones);
=======
import java.util.Collections;
import java.util.List;

/**
 * Gestor de contratos: agrupa todos los contratos del agente (RF-03).
 */
public class GestorContratos {

    private final List<Contrato> contratos = new ArrayList<>();

    // AQUÍ VAN las notificaciones del gestor (RF-16, RF-17, RF-19, RF-20): notificaciones : List<Notificacion>

    /** Agrega un contrato ya creado (con crearContrato()) a la lista del gestor. */
    public void agregarContrato(Contrato contrato) {
        if (contrato == null) {
            throw new IllegalArgumentException("No se puede agregar un contrato nulo.");
        }
        if (contrato.getEstado() == null) {
            throw new IllegalStateException("El contrato aún no ha sido creado: llame a crearContrato() antes de agregarlo.");
        }
        for (Contrato existente : contratos) {
            if (existente.getNumeroContrato() == contrato.getNumeroContrato()) {
                throw new IllegalArgumentException("Ya hay un contrato con el número " + contrato.getNumeroContrato()
                        + " en el gestor.");
            }
        }
        contratos.add(contrato);
    }

    // AQUÍ VAN filtrarContratosEnMora() y verificarPagosVencidos() (RF-09),
    // listarContratosPorVencer(dias) (RF-15), recolectarNotificaciones() y listarNotificacionesPendientes() (RF-20).

    public List<Contrato> getContratos() {
        return Collections.unmodifiableList(contratos);
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }
}
