import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Datos comunes de Cliente e Interesado: nombre, contacto y fechas de seguimiento (RF-12 / RF-19).
 */
public abstract class Persona implements Persistible, Notificable {
    private static final String ARCHIVO_SEGUIMIENTOS = "seguimientos.txt";

    protected String nombre;
    protected String contacto;
    // Privada: solo cambia con registrarSeguimiento(); las hijas la leen con consultarSeguimientos().
    private final List<LocalDate> fechasSeguimiento = new ArrayList<>();

    public Persona(String nombre, String contacto) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (contacto == null || contacto.isBlank()) {
            throw new IllegalArgumentException("El contacto es obligatorio.");
        }
        this.nombre = nombre.trim();
        this.contacto = contacto.trim();
    }

    public void actualizarContacto(String nuevoContacto) {
        if (nuevoContacto == null || nuevoContacto.isBlank()) {
            throw new IllegalArgumentException("El contacto no puede quedar vacío.");
        }
        this.contacto = nuevoContacto.trim();
    }

    /**
     * RF-12: registra una fecha de seguimiento (cita o contacto pendiente con esta persona).
     * No se rechazan fechas pasadas, porque al cargar desde archivo (RNF-01) se deben poder reconstruir.
     */
    public void registrarSeguimiento(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de seguimiento es obligatoria.");
        }
        if (fechasSeguimiento.contains(fecha)) {
            throw new IllegalArgumentException(nombre + " ya tiene un seguimiento registrado para el " + fecha + ".");
        }
        fechasSeguimiento.add(fecha);
    }

    /**
     * RF-12: devuelve las citas pendientes (en la fecha dada o después), de la más próxima a la más lejana.
     * Devuelve una lista nueva: modificarla no cambia los seguimientos de la persona.
     */
    public List<LocalDate> consultarSeguimientos(LocalDate desde) {
        if (desde == null) {
            throw new IllegalArgumentException("La fecha desde la que se consultan los seguimientos es obligatoria.");
        }
        List<LocalDate> pendientes = new ArrayList<>();
        for (LocalDate fecha : fechasSeguimiento) {
            if (!fecha.isBefore(desde)) {
                pendientes.add(fecha);
            }
        }
        Collections.sort(pendientes);
        return pendientes;
    }

    /**
     * RF-18 / RF-19: un aviso SEGUIMIENTO por cada cita que es hoy o ya pasó (la cita vencida sigue avisando
     * hasta que el agente marque el aviso como atendido; esMismaQue() evita repetirlo). Las citas futuras no
     * avisan. Cada hija lo llama desde generarNotificaciones() con su propio idOrigen.
     */
    protected List<Notificacion> notificacionesDeSeguimiento(LocalDate hoy, String idOrigen) {
        List<Notificacion> avisos = new ArrayList<>();
        for (LocalDate cita : fechasSeguimiento) {
            if (!cita.isAfter(hoy)) {
                String cuando = cita.isEqual(hoy) ? "es hoy (" + cita + ")" : "estaba para el " + cita + " (vencido)";
                avisos.add(new Notificacion(Notificacion.SEGUIMIENTO,
                        "Seguimiento con " + nombre + " (" + contacto + ") " + cuando + ".", cita, idOrigen));
            }
        }
        return avisos;
    }

    /**
     * RNF-01: guarda las fechas de seguimiento en una línea propia: clave|fecha1|fecha2|...
     * Cada hija la llama desde su guardar() con su propia clave (la Persona no sabe cómo se identifica cada hija).
     */
    protected void guardarSeguimientos(String clave) {
        String[] campos = new String[fechasSeguimiento.size() + 1];
        campos[0] = clave;
        for (int i = 0; i < fechasSeguimiento.size(); i++) {
            campos[i + 1] = fechasSeguimiento.get(i).toString();
        }
        ArchivoPlano.guardarRegistro(ARCHIVO_SEGUIMIENTOS, campos);
    }

    /** Restaura las fechas de una persona, incluso al volver a cargar la misma instancia. */
    protected void cargarSeguimientos(String clave) {
        String[] campos = ArchivoPlano.buscarRegistro(ARCHIVO_SEGUIMIENTOS, clave);
        List<LocalDate> guardadas = new ArrayList<>();
        if (campos != null) {
            for (int i = 1; i < campos.length; i++) {
                guardadas.add(LocalDate.parse(campos[i]));
            }
        }
        fechasSeguimiento.clear();
        fechasSeguimiento.addAll(guardadas);
    }

    public String getNombre() {
        return nombre;
    }

    public String getContacto() {
        return contacto;
    }
}
