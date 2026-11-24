import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gestor del inventario de inmuebles del agente.
 * Es quien sabe qué propiedades están registradas, por eso valida aquí que no haya direcciones repetidas.
 */
public class GestorInmuebles extends Gestor {

    private final List<Propiedad> inmuebles = new ArrayList<>();

    /**
     * RF-01: registra una propiedad en el inventario.
     * No se permiten dos propiedades con la misma dirección (se ignoran mayúsculas y espacios sobrantes).
     * Tampoco dos propietarios distintos con el mismo documento: al guardar, uno borraría al otro del archivo.
     * Si el registro es exitoso, completa el enlace inverso: la propiedad queda en la lista de su propietario.
     */
    public void agregarPropiedad(Propiedad propiedad) {
        if (propiedad == null) {
            throw new IllegalArgumentException("No se puede registrar una propiedad nula.");
        }
        Propietario dueno = propiedad.getPropietario();
        for (Propiedad existente : inmuebles) {
            if (existente.getDireccion().equalsIgnoreCase(propiedad.getDireccion())) {
                throw new IllegalArgumentException("Ya existe una propiedad registrada con la dirección '"
                        + propiedad.getDireccion() + "'. No se permiten dos propiedades con la misma dirección.");
            }
            Propietario otro = existente.getPropietario();
            if (otro != dueno && otro.getDocumento().equals(dueno.getDocumento())) {
                throw new IllegalArgumentException("Ya hay un propietario registrado con el documento "
                        + dueno.getDocumento() + " (" + otro.getNombre() + "). Use ese mismo propietario para "
                        + "registrar la propiedad '" + propiedad.getDireccion() + "'.");
            }
        }
        if (!dueno.getPropiedades().contains(propiedad)) {
            dueno.agregarPropiedad(propiedad);
        }
        inmuebles.add(propiedad);
        Propiedad.contarRegistro(); // suma al contador estático de propiedades registradas
    }

    /**
     * RF-13: inmuebles disponibles en la modalidad pedida ("Arriendo" o "Venta", sin importar mayúsculas).
     * Código original del commit 9c2c055, que se había perdido en un merge.
     */
    public List<Propiedad> listarDisponibles(String modalidad) {
        if (modalidad == null || modalidad.isBlank()) {
            throw new IllegalArgumentException("La modalidad es obligatoria para consultar inmuebles disponibles.");
        }
        // Misma normalización que usa la propiedad al registrarse ("arriendo" → "Arriendo").
        String modalidadNormalizada = Propiedad.normalizarModo(modalidad);
        if (!Propiedad.MODO_ARRIENDO.equals(modalidadNormalizada) && !Propiedad.MODO_VENTA.equals(modalidadNormalizada)) {
            throw new IllegalArgumentException("Modalidad no válida: '" + modalidad
                    + "'. Debe ser '" + Propiedad.MODO_ARRIENDO + "' o '" + Propiedad.MODO_VENTA + "'.");
        }

        List<Propiedad> resultado = new ArrayList<>();
        for (Propiedad propiedad : inmuebles) {
            if (Propiedad.DISPONIBLE.equals(propiedad.getEstado()) && modalidadNormalizada.equals(propiedad.getModo())) {
                resultado.add(propiedad);
            }
        }
        return resultado;
    }

    /**
     * Quita una propiedad del inventario, por ejemplo si se registró por error. Solo se permite si nunca ha
     * tenido contratos ni interesados: si no, se perderían el historial y las relaciones guardadas en los
     * archivos. También la quita de la lista de su propietario, del contador y del archivo.
     */
    public void quitarPropiedad(String direccion) {
        Propiedad propiedad = buscarPropiedad(direccion);
        if (propiedad == null) {
            throw new IllegalArgumentException("No hay ninguna propiedad registrada con la dirección '" + direccion + "'.");
        }
        if (propiedad.getContratoActual() != null) {
            throw new IllegalStateException("No se puede quitar la propiedad '" + propiedad.getDireccion()
                    + "': tiene un contrato vigente (N° " + propiedad.getContratoActual().getNumeroContrato() + ").");
        }
        if (!propiedad.getHistorial().isEmpty()) {
            throw new IllegalStateException("No se puede quitar la propiedad '" + propiedad.getDireccion()
                    + "': tiene contratos en su historial y se perderían.");
        }
        if (!propiedad.getInteresados().isEmpty()) {
            throw new IllegalStateException("No se puede quitar la propiedad '" + propiedad.getDireccion()
                    + "': tiene " + propiedad.getInteresados().size() + " interesado(s) asociado(s).");
        }
        inmuebles.remove(propiedad);
        propiedad.getPropietario().quitarPropiedad(propiedad);
        Propiedad.descontarRegistro();
        propiedad.eliminarDelArchivo();
    }

    /**
     * Devuelve la propiedad registrada con esa dirección, o null si no existe. No importan las mayúsculas ni los
     * espacios sobrantes: la dirección buscada se normaliza igual que al registrarla.
     */
    public Propiedad buscarPropiedad(String direccion) {
        String buscada = Propiedad.normalizarDireccion(direccion);
        for (Propiedad propiedad : inmuebles) {
            if (propiedad.getDireccion().equalsIgnoreCase(buscada)) {
                return propiedad;
            }
        }
        return null;
    }

    public List<Propiedad> getInmuebles() {
        return Collections.unmodifiableList(inmuebles);
    }

    /**
     * RF-11: registra un interesado en uno o más inmuebles del inventario.
     * Si ya hay un interesado con el mismo contacto, no se crea otro: se le suman los inmuebles nuevos (por
     * ejemplo, si la semana siguiente se interesa en otro). Si el nombre no coincide con el registrado, se
     * rechaza, porque probablemente es un error de digitación. Devuelve el interesado (nuevo o existente).
     */
    public Interesado registrarInteresado(String nombre, String contacto, List<Propiedad> propiedades) {
        Interesado candidato = new Interesado(nombre, contacto, propiedades); // valida nombre, contacto e inmuebles
        for (Propiedad propiedad : candidato.getInmuebles()) {
            if (!inmuebles.contains(propiedad)) {
                throw new IllegalArgumentException("La propiedad '" + propiedad.getDireccion()
                        + "' no está registrada en el inventario.");
            }
        }
        Interesado existente = buscarInteresadoPorContacto(candidato.getContacto());
        if (existente == null) {
            enlazarInteresado(candidato);
            return candidato;
        }
        if (!existente.getNombre().equalsIgnoreCase(candidato.getNombre())) {
            throw new IllegalArgumentException("El contacto " + candidato.getContacto() + " ya pertenece al interesado "
                    + existente.getNombre() + "; no se puede registrar con el nombre " + candidato.getNombre() + ".");
        }
        List<Propiedad> nuevas = new ArrayList<>();
        for (Propiedad propiedad : candidato.getInmuebles()) {
            if (!existente.getInmuebles().contains(propiedad)) {
                nuevas.add(propiedad);
            }
        }
        if (nuevas.isEmpty()) {
            throw new IllegalArgumentException(existente.getNombre() + " ya está interesado en esos inmuebles.");
        }
        for (Propiedad propiedad : nuevas) {
            existente.agregarInmueble(propiedad);
            propiedad.agregarInteresado(existente);
        }
        return existente;
    }

    // Devuelve el interesado con ese contacto (escrito de cualquier forma, ver Utilidades.mismoContacto) o null.
    private Interesado buscarInteresadoPorContacto(String contacto) {
        for (Interesado interesado : getInteresados()) {
            if (Utilidades.mismoContacto(interesado.getContacto(), contacto)) {
                return interesado;
            }
        }
        return null;
    }

    private void enlazarInteresado(Interesado interesado) {
        for (Propiedad propiedad : interesado.getInmuebles()) {
            propiedad.agregarInteresado(interesado);
        }
    }

    // ===================== Lo que exige la clase padre Gestor =====================

    /** Las propiedades, sus propietarios y sus interesados. Cada persona se guarda una sola vez. */
    @Override
    protected List<Persistible> elementosPersistibles() {
        List<Persistible> elementos = new ArrayList<>(inmuebles);
        List<Propietario> propietarios = new ArrayList<>();
        for (Propiedad propiedad : inmuebles) {
            if (!propietarios.contains(propiedad.getPropietario())) {
                propietarios.add(propiedad.getPropietario());
            }
        }
        elementos.addAll(propietarios);
        elementos.addAll(getInteresados());
        return elementos;
    }

    /**
     * RNF-01: recrea propietarios y propiedades desde los archivos. Se llama sobre un gestor vacío, al arrancar.
     * Registro de propiedades.txt: dirección|área|valor|tipo|modo|estado|docPropietario|nContrato|datos propios...
     */
    @Override
    public void cargarTodo() {
        if (!inmuebles.isEmpty()) {
            throw new IllegalStateException("Solo se puede cargar el inventario en un gestor vacío.");
        }
        // 1. Propietarios, para poder enlazar cada propiedad con su dueño.
        Map<String, Propietario> propietarios = new HashMap<>();
        for (String[] r : ArchivoPlano.leerTodos("propietarios.txt")) {
            propietarios.put(r[0], new Propietario(r[0], r[1], r[2]));
        }
        // 2. Propiedades. Pasan por agregarPropiedad(), así se valida la dirección y se enlaza el dueño.
        for (String[] r : ArchivoPlano.leerTodos("propiedades.txt")) {
            try {
                Propiedad propiedad = crearPropiedad(r, propietarios.get(r[6]));
                agregarPropiedad(propiedad);
                propiedad.restaurarEstado(r[5]); // tal como quedó guardado (arrendada, en captación...)
            } catch (RuntimeException e) {
                throw new IllegalStateException("No se pudo cargar la propiedad '" + r[0] + "': " + e.getMessage(), e);
            }
        }
        // El contrato actual (r[7]) lo reconecta GestorContratos al cargar los contratos.
        for (String[] r : ArchivoPlano.leerTodos(Interesado.ARCHIVO)) {
            try {
                List<Propiedad> propiedades = new ArrayList<>();
                for (int i = 3; i < r.length; i++) {
                    Propiedad propiedad = buscarPropiedad(r[i]);
                    if (propiedad == null) {
                        throw new IllegalArgumentException("no existe la propiedad '" + r[i] + "'");
                    }
                    propiedades.add(propiedad);
                }
                Interesado interesado = new Interesado(UUID.fromString(r[0]), r[1], r[2], propiedades);
                interesado.cargarSeguimientosGuardados();
                enlazarInteresado(interesado);
            } catch (RuntimeException e) {
                throw new IllegalStateException("No se pudo cargar el interesado " + r[0] + ": " + e.getMessage(), e);
            }
        }
    }

    // Fábrica: el único lugar del sistema que pregunta el tipo de propiedad, porque al leer un texto hay que
    // decidir qué clase crear. Después de creada, el resto del sistema la usa de forma polimórfica.
    private static Propiedad crearPropiedad(String[] r, Propietario dueno) {
        String direccion = r[0];
        double area = Double.parseDouble(r[1]);
        double valorBase = Double.parseDouble(r[2]);
        String tipo = r[3];
        String modo = r[4];
        switch (tipo) {
            case "Apartamento":
                return new Apartamento(direccion, area, valorBase, modo, dueno, Integer.parseInt(r[8]));
            case "Local comercial":
                return new LocalComercial(direccion, area, valorBase, modo, dueno);
            default:
                throw new IllegalArgumentException("tipo de propiedad desconocido '" + tipo + "'");
        }
    }

    @Override
    public void recolectarNotificaciones(LocalDate hoy) {
        for (Interesado interesado : getInteresados()) {
            agregarNotificaciones(interesado.generarNotificaciones(hoy));
        }
    }

    /** Devuelve cada interesado una vez, incluso si está vinculado a varias propiedades. */
    public List<Interesado> getInteresados() {
        List<Interesado> interesados = new ArrayList<>();
        for (Propiedad propiedad : inmuebles) {
            for (Interesado interesado : propiedad.getInteresados()) {
                if (!interesados.contains(interesado)) {
                    interesados.add(interesado);
                }
            }
        }
        return interesados;
    }

    @Override
    protected boolean esMiNotificacion(Notificacion n) {
        return n.getIdOrigen().startsWith("INTERESADO:");
    }
}
