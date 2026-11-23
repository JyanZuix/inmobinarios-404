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

    /**
     * RNF-01: recrea los clientes y sus fechas de seguimiento. Se llama sobre un gestor vacío, al arrancar.
     * Registro de clientes.txt: Tipo:documento|documento|nombre|contacto|datos propios...
     */
    @Override
    public void cargarTodo() {
        if (!clientes.isEmpty()) {
            throw new IllegalStateException("Solo se pueden cargar los clientes en un gestor vacío.");
        }
        for (String[] r : ArchivoPlano.leerTodos("clientes.txt")) {
            try {
                agregarCliente(crearCliente(r)); // valida el documento repetido, igual que un registro normal
            } catch (RuntimeException e) {
                throw new IllegalStateException("No se pudo cargar el cliente '" + r[0] + "': " + e.getMessage(), e);
            }
        }
        // Seguimientos: clave|fecha1|fecha2... Las líneas de otras personas (p. ej. interesados) se ignoran aquí.
        for (String[] r : ArchivoPlano.leerTodos("seguimientos.txt")) {
            Cliente cliente = buscarPorClave(r[0]);
            if (cliente != null) {
                for (int i = 1; i < r.length; i++) {
                    cliente.registrarSeguimiento(LocalDate.parse(r[i]));
                }
            }
        }
    }

    // Fábrica: el tipo de cliente va al inicio de la clave ("Arrendatario:52111222").
    private static Cliente crearCliente(String[] r) {
        String tipo = r[0].substring(0, r[0].indexOf(':'));
        switch (tipo) {
            case "Arrendatario":
                return new Arrendatario(r[1], r[2], r[3]);
            case "Comprador":
                return new Comprador(r[1], r[2], r[3], Boolean.parseBoolean(r[4]));
            default:
                throw new IllegalArgumentException("tipo de cliente desconocido '" + tipo + "'");
        }
    }

    private Cliente buscarPorClave(String clave) {
        for (Cliente cliente : clientes) {
            if (cliente.clave().equals(clave)) {
                return cliente;
            }
        }
        return null;
    }

    @Override
    public void recolectarNotificaciones(LocalDate hoy) {
        // RF-19: cada cliente se visita una sola vez, aunque tenga varios contratos (por eso se recolecta aquí y
        // no recorriendo los contratos).
        for (Cliente cliente : clientes) {
            agregarNotificaciones(cliente.generarNotificaciones(hoy));
        }
    }

    @Override
    protected boolean esMiNotificacion(Notificacion n) {
        return n.getIdOrigen().startsWith("CLIENTE:");
    }
}
