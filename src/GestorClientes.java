import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor de clientes del agente (arrendatarios y compradores).
 * Es quien sabe qué clientes están registrados, por eso valida aquí que no haya documentos repetidos (RF-02).
 */
public class GestorClientes extends Gestor {

    private final List<Cliente> clientes = new ArrayList<>();

    /**
     * RF-02: registra un cliente.
     * No se permiten dos clientes del mismo tipo con el mismo documento (un Arrendatario y un Comprador sí
     * pueden compartirlo, porque son roles distintos de la misma persona).
     */
    public void agregarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("No se puede registrar un cliente nulo.");
        }
        for (Cliente existente : clientes) {
            if (existente.clave().equals(cliente.clave())) {
                throw new IllegalArgumentException("Ya existe un " + cliente.getClass().getSimpleName()
                        + " registrado con el documento " + cliente.getDocumento()
                        + ". No se permiten dos con el mismo número de documento.");
            }
        }
        clientes.add(cliente);
    }

    /** Devuelve el arrendatario con ese documento, o null si no está registrado. */
    public Arrendatario buscarArrendatario(String documento) {
        for (Cliente cliente : clientes) {
            if (cliente instanceof Arrendatario && cliente.getDocumento().equals(documento)) {
                return (Arrendatario) cliente;
            }
        }
        return null;
    }

    /** Devuelve el comprador con ese documento, o null si no está registrado. */
    public Comprador buscarComprador(String documento) {
        for (Cliente cliente : clientes) {
            if (cliente instanceof Comprador && cliente.getDocumento().equals(documento)) {
                return (Comprador) cliente;
            }
        }
        return null;
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    // ===================== Lo que exige la clase padre Gestor =====================

    @Override
    protected List<Persistible> elementosPersistibles() {
        return new ArrayList<>(clientes);
    }

    @Override
    public void cargarTodo() {
        // Pendiente: RNF-01
    }

    @Override
    public void recolectarNotificaciones(LocalDate hoy) {
        // Pendiente: RF-19 (cada cliente se visita una sola vez, aunque tenga varios contratos)
    }
}
