import java.util.ArrayList;
import java.util.List;

public abstract class Cliente extends Persona {
    protected String documento;
    protected Contrato contratoActual;
    protected List<Contrato> historial = new ArrayList<>();

    public Cliente(String documento, String nombre, String contacto) {
        super(nombre, contacto);
        this.documento = documento;
    }

    public String obtenerInformacion() {
        throw new UnsupportedOperationException("Pendiente: RF-02");
    }

    public abstract String describir();

    public void asignarContrato(Contrato contrato) {
        // Pendiente: RF-03
    }

    public void liberarContrato() {
        // Pendiente: RF-10
    }

    @Override
    public List<Notificacion> generarNotificaciones() {
        throw new UnsupportedOperationException("Pendiente: RF-19");
    }

    @Override
    public void guardar() {
        // Pendiente: RNF-01
    }

    @Override
    public void cargar() {
        // Pendiente: RNF-01
    }

    public String getDocumento() {
        return documento;
    }

    public Contrato getContratoActual() {
        return contratoActual;
    }

    public List<Contrato> getHistorial() {
        return List.copyOf(historial);
    }
}
