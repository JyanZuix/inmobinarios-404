import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RF-02: Registrar un cliente (documento, nombre y contacto).
 * Clase abstracta: Arrendatario y Comprador comparten estos datos y se diferencian en describir().
 * Nombre, contacto y seguimientos vienen de Persona.
 */
public abstract class Cliente extends Persona {

    private static final String ARCHIVO = "clientes.txt";
    private static final int DIAS_AVISO_VENCIMIENTO = 30; // RF-19: mismo plazo que RF-17

    // ===================== Atributos (protected: los heredan las hijas) =====================
    protected String documento;

    // Historial de todos los contratos del cliente (0..*). Un cliente puede tener varios vigentes a la vez.
    private final List<Contrato> historial = new ArrayList<>();

    // ===================== Constructor =====================
    protected Cliente(String documento, String nombre, String contacto) {
        super(nombre, contacto); // valida nombre y contacto
        // "52.111.222" y "52111222" son el mismo documento: se guarda solo con números (RF-02).
        this.documento = Utilidades.normalizarDocumento(documento, "cliente");
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
    /**
     * RF-19: avisos del cliente: sus citas de seguimiento de hoy o vencidas (igual que un interesado, vía
     * Persona) y, por cada contrato vigente, si vence en 30 días o menos o si tiene pagos en mora.
     * El mensaje dice el motivo y el contrato. Usa los mismos criterios que RF-16 y RF-17, pero desde el cliente.
     */
    @Override
    public List<Notificacion> generarNotificaciones(LocalDate hoy) {
        String idOrigen = "CLIENTE:" + clave();
        List<Notificacion> avisos = notificacionesDeSeguimiento(hoy, idOrigen);
        for (Contrato contrato : getContratosVigentes()) {
            String delContrato = nombre + ": el contrato " + contrato.getNumeroContrato() + " ("
                    + contrato.getPropiedad().getDireccion() + ")";
            if (contrato.vencePronto(hoy, DIAS_AVISO_VENCIMIENTO)) {
                // Dos tipos distintos (como en RF-17): si el agente atendió el aviso "por vencer", igual debe
                // enterarse cuando el contrato ya venció y sigue activo.
                boolean vencido = contrato.getFechaFin().isBefore(hoy);
                String tipo = vencido ? Notificacion.CLIENTE_CONTRATO_VENCIDO : Notificacion.CLIENTE_CONTRATO_POR_VENCER;
                String cuando = vencido
                        ? " terminó el " + contrato.getFechaFin() + " y sigue activo."
                        : " termina el " + contrato.getFechaFin() + ".";
                avisos.add(new Notificacion(tipo, delContrato + cuando,
                        contrato.getFechaFin(), idOrigen + ":CONTRATO:" + contrato.getNumeroContrato()));
            }
            if (contrato.tienePagosVencidos(hoy)) {
                // Fecha del evento = el pago en mora más antiguo: es la misma mientras dure esa mora, así
                // esMismaQue() no crea un aviso nuevo cada día que se revise.
                LocalDate desde = contrato.moraDesde(hoy);
                avisos.add(new Notificacion(Notificacion.CLIENTE_PAGOS_EN_MORA,
                        delContrato + " tiene pagos en mora desde el " + desde + ".", desde,
                        idOrigen + ":CONTRATO:" + contrato.getNumeroContrato()));
            }
        }
        return avisos;
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

    // Cada hija agrega aquí sus campos propios al guardar (por defecto no hay ninguno).
    protected String[] datosPropios() {
        return new String[0];
    }

    /** Ficha del cliente para mostrarla en pantalla: lo que dice describir() más el contacto. */
    @Override
    public String toString() {
        return describir() + " | contacto: " + contacto + " | contratos vigentes: " + getContratosVigentes().size();
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
