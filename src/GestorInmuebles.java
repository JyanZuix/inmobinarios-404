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
     * Si el registro es exitoso, completa el enlace inverso: la propiedad queda en la lista de su propietario.
     */
    public void agregarPropiedad(Propiedad propiedad) {
        if (propiedad == null) {
            throw new IllegalArgumentException("No se puede registrar una propiedad nula.");
        }
        for (Propiedad existente : inmuebles) {
            if (existente.getDireccion().equalsIgnoreCase(propiedad.getDireccion())) {
                throw new IllegalArgumentException("Ya existe una propiedad registrada con la dirección '"
                        + propiedad.getDireccion() + "'. No se permiten dos propiedades con la misma dirección.");
            }
        }
        Propietario dueno = propiedad.getPropietario();
        if (!dueno.getPropiedades().contains(propiedad)) {
            dueno.agregarPropiedad(propiedad);
        }
        inmuebles.add(propiedad);
        Propiedad.contarRegistro(); // suma al contador estático de propiedades registradas
    }

    public List<Propiedad> listarDisponibles(String modalidad) {
        throw new UnsupportedOperationException("Pendiente: RF-13");
    }

    /** Devuelve la propiedad registrada con esa dirección (sin importar mayúsculas), o null si no existe. */
    public Propiedad buscarPropiedad(String direccion) {
        for (Propiedad propiedad : inmuebles) {
            if (propiedad.getDireccion().equalsIgnoreCase(direccion)) {
                return propiedad;
            }
        }
        return null;
    }

    public List<Propiedad> getInmuebles() {
        return Collections.unmodifiableList(inmuebles);
    }

    /** RF-11: valida el inventario antes de enlazar al interesado a todas sus propiedades. */
    public Interesado registrarInteresado(String nombre, String contacto, List<Propiedad> propiedades) {
        Interesado interesado = new Interesado(nombre, contacto, propiedades);
        for (Propiedad propiedad : interesado.getInmuebles()) {
            if (!inmuebles.contains(propiedad)) {
                throw new IllegalArgumentException("La propiedad '" + propiedad.getDireccion()
                        + "' no está registrada en el inventario.");
            }
        }
        enlazarInteresado(interesado);
        return interesado;
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
