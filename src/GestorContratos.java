import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor de contratos: agrupa todos los contratos del agente (RF-03).
 */
public class GestorContratos extends Gestor {

    private final GestorInmuebles inmuebles; // inventario contra el que se verifica que la propiedad exista
    private final List<Contrato> contratos = new ArrayList<>();

    public GestorContratos(GestorInmuebles inmuebles) {
        if (inmuebles == null) {
            throw new IllegalArgumentException("El gestor de contratos necesita el inventario de inmuebles.");
        }
        this.inmuebles = inmuebles;
    }

    /**
     * RF-03: único punto para crear un contrato. Verifica que la propiedad esté registrada en el inventario,
     * lo formaliza con crearContrato() y lo deja en la lista del gestor.
     */
    public void agregarContrato(Contrato contrato) {
        if (contrato == null) {
            throw new IllegalArgumentException("No se puede agregar un contrato nulo.");
        }
        if (contrato.getEstado() != null) {
            throw new IllegalStateException("El contrato " + contrato.getNumeroContrato() + " ya fue registrado.");
        }
        if (!inmuebles.getInmuebles().contains(contrato.getPropiedad())) {
            throw new IllegalArgumentException("No se puede crear el contrato: la propiedad '"
                    + contrato.getPropiedad().getDireccion() + "' no está registrada en el inventario.");
        }
        contrato.crearContrato(); // lanza excepción si la propiedad no está disponible (RF-04)
        contratos.add(contrato);
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

    public List<Contrato> getContratos() {
        return Collections.unmodifiableList(contratos);
    }

    // ===================== Lo que exige la clase padre Gestor =====================

    @Override
    protected List<Persistible> elementosPersistibles() {
        return new ArrayList<>(contratos);
    }

    @Override
    public void cargarTodo() {
        // Pendiente: RNF-01
    }

    @Override
    public void recolectarNotificaciones(LocalDate hoy) {
        // Pendiente: RF-16, RF-17
    }
}
