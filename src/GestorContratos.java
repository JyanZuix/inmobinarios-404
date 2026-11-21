import java.util.ArrayList;
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
    }
}
