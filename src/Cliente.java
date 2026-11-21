import java.util.ArrayList;
<<<<<<< HEAD
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
=======
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RF-02: Registrar un cliente (documento, nombre y contacto).
 * Clase abstracta: Arrendatario y Comprador comparten estos datos y se diferencian en describir().
 */
public abstract class Cliente implements Persistible, Notificable {

    private static final String ARCHIVO = "clientes.txt";

    // Documentos ya registrados, por tipo de cliente. Como no hay un gestor de clientes en el UML,
    // este conjunto estático es el que evita dos clientes del mismo tipo con el mismo documento.
    private static final Set<String> registrados = new HashSet<>();

    // ===================== Atributos (protected: los heredan las hijas) =====================
    protected String documento;
    protected String nombre;
    protected String contacto;
    protected Contrato contratoActual; // contrato vigente (0..1)
    // AQUÍ VA fechasSeguimiento : List<Date> (RF-12 / RF-19)

    // Historial de todos los contratos del cliente (0..*)
    private final List<Contrato> historial = new ArrayList<>();

    // ===================== Constructor =====================
    protected Cliente(String documento, String nombre, String contacto) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (contacto == null || contacto.isBlank()) {
            throw new IllegalArgumentException("El contacto del cliente es obligatorio.");
        }
        this.documento = documento.trim();
        this.nombre = nombre.trim();
        this.contacto = contacto.trim();

        // RF-02: no se permiten dos clientes del mismo tipo con el mismo documento.
        // Se registra de último, para que un cliente inválido no deje su documento "ocupado".
        if (!registrados.add(clave())) {
            throw new IllegalArgumentException("Ya existe un " + getClass().getSimpleName()
                    + " registrado con el documento " + this.documento
                    + ". No se permiten dos con el mismo número de documento.");
        }
    }

    // ===================== Métodos de negocio =====================

    public void actualizarContacto(String nuevoContacto) {
        if (nuevoContacto == null || nuevoContacto.isBlank()) {
            throw new IllegalArgumentException("El contacto del cliente no puede quedar vacío.");
        }
        this.contacto = nuevoContacto.trim();
    }

    public String obtenerInformacion() {
        return "Documento: " + documento + " | Nombre: " + nombre + " | Contacto: " + contacto;
    }

    /** Polimorfismo: cada tipo de cliente se describe a su manera. */
    public abstract String describir();

    // AQUÍ VAN registrarSeguimiento(fecha) y consultarSeguimientos() (RF-12 / RF-19).

    // Lo llama Contrato.crearContrato(): deja el contrato como vigente y lo suma al historial.
    void registrarContrato(Contrato contrato) {
        this.contratoActual = contrato;
        this.historial.add(contrato);
    }

    // AQUÍ VA RF-10: dejar contratoActual vacío cuando el contrato termina.

    // ===================== Notificable =====================
    @Override
    public List<Notificacion> generarNotificaciones() {
        // AQUÍ VA RF-19: avisos de seguimiento y de situación contractual del cliente.
        return new ArrayList<>();
    }

    // ===================== Persistencia (RNF-01) =====================

    @Override
    public void guardar() {
        String[] comunes = {
                clave(),                                                       // clave del registro: Tipo:documento
                documento,
                nombre,
                contacto,
                contratoActual == null ? "" : String.valueOf(contratoActual.getNumeroContrato())
        };
        ArchivoPlano.guardarRegistro(ARCHIVO, ArchivoPlano.concatenar(comunes, datosPropios()));
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }

    @Override
    public void cargar() {
<<<<<<< HEAD
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
=======
        String[] registro = ArchivoPlano.buscarRegistro(ARCHIVO, clave());
        if (registro == null) {
            throw new IllegalStateException("No hay ningún " + getClass().getSimpleName()
                    + " guardado con el documento " + documento + ".");
        }
        try {
            String nombreGuardado = registro[2];
            String contactoGuardado = registro[3];
            if (nombreGuardado.isBlank() || contactoGuardado.isBlank()) {
                throw new IllegalArgumentException("faltan datos obligatorios");
            }
            this.nombre = nombreGuardado;
            this.contacto = contactoGuardado;
            // registro[4] (número del contrato actual) es una referencia: se reconecta cuando los gestores
            // carguen todo el sistema (mejora futura).
            aplicarDatosPropios(Arrays.copyOfRange(registro, 5, registro.length));
        } catch (RuntimeException e) {
            throw new IllegalStateException("El registro guardado del cliente " + documento
                    + " está dañado: " + e.getMessage(), e);
        }
    }

    // Cada hija agrega aquí sus campos propios al guardar y los lee al cargar (por defecto no hay ninguno).
    protected String[] datosPropios() {
        return new String[0];
    }

    protected void aplicarDatosPropios(String[] datos) {
        // Sin datos propios por defecto.
    }

    // ===================== Getters =====================
    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
    public Contrato getContratoActual() { return contratoActual; }
    public List<Contrato> getHistorial() { return Collections.unmodifiableList(historial); }

    // Clave única: tipo de cliente + documento (un Arrendatario y un Comprador pueden tener el mismo documento).
    private String clave() {
        return getClass().getSimpleName() + ":" + documento;
>>>>>>> 1fba9540e0187e820ffd08e111b14c4c3f190c8f
    }
}
