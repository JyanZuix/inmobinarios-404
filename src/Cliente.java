import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * RF-02: Registrar un cliente (documento, nombre y contacto).
 * Clase abstracta: Arrendatario y Comprador comparten estos datos y se diferencian en describir().
 * Nombre, contacto y seguimientos vienen de Persona.
 */
public abstract class Cliente extends Persona {

    private static final String ARCHIVO = "clientes.txt";

    // ===================== Atributos (protected: los heredan las hijas) =====================
    protected String documento;

    // Historial de todos los contratos del cliente (0..*). Un cliente puede tener varios vigentes a la vez.
    private final List<Contrato> historial = new ArrayList<>();

    // ===================== Constructor =====================
    protected Cliente(String documento, String nombre, String contacto) {
        super(nombre, contacto); // valida nombre y contacto
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio.");
        }
        this.documento = documento.trim();
        // RF-02 (documento no repetido) lo verifica GestorClientes.agregarCliente(), que conoce a todos los clientes.
    }

    // ===================== Métodos de negocio =====================

    public String obtenerInformacion() {
        return "Documento: " + documento + " | Nombre: " + nombre + " | Contacto: " + contacto;
    }

    /** Polimorfismo: cada tipo de cliente se describe a su manera. */
    public abstract String describir();

    // Lo llama Contrato.crearContrato(): suma el contrato al historial del cliente.
    void asignarContrato(Contrato contrato) {
        this.historial.add(contrato);
    }

    /**
     * Contratos del cliente que siguen activos. Se calcula desde el historial (no se guarda aparte),
     * así que al terminar un contrato (RF-10) no hay nada que sincronizar en el cliente.
     */
    public List<Contrato> getContratosVigentes() {
        List<Contrato> vigentes = new ArrayList<>();
        for (Contrato contrato : historial) {
            if (Contrato.ACTIVO.equals(contrato.getEstado())) {
                vigentes.add(contrato);
            }
        }
        return vigentes;
    }

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
                contacto
                // El historial no se guarda aquí: se reconstruye desde los contratos, que guardan el documento.
        };
        ArchivoPlano.guardarRegistro(ARCHIVO, ArchivoPlano.concatenar(comunes, datosPropios()));
        guardarSeguimientos(clave()); // las fechas de seguimiento van en seguimientos.txt
    }

    @Override
    public void cargar() {
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
            aplicarDatosPropios(Arrays.copyOfRange(registro, 4, registro.length));
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
    public List<Contrato> getHistorial() { return Collections.unmodifiableList(historial); }

    // Clave única: tipo de cliente + documento (un Arrendatario y un Comprador pueden tener el mismo documento).
    // Default (sin public): la usan la persistencia y GestorClientes.
    String clave() {
        return getClass().getSimpleName() + ":" + documento;
    }
}
