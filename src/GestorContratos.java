import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de contratos: agrupa todos los contratos del agente (RF-03).
 */
public class GestorContratos extends Gestor {

    private final GestorInmuebles inmuebles; // inventario contra el que se verifica que la propiedad exista
    private final GestorClientes clientes;   // clientes contra los que se verifica que el cliente exista
    private final List<Contrato> contratos = new ArrayList<>();

    public GestorContratos(GestorInmuebles inmuebles, GestorClientes clientes) {
        if (inmuebles == null) {
            throw new IllegalArgumentException("El gestor de contratos necesita el inventario de inmuebles.");
        }
        if (clientes == null) {
            throw new IllegalArgumentException("El gestor de contratos necesita el gestor de clientes.");
        }
        this.inmuebles = inmuebles;
        this.clientes = clientes;
    }

    /**
     * RF-03: único punto para crear un contrato. Verifica que la propiedad y el cliente estén registrados,
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
        if (!clientes.getClientes().contains(contrato.getCliente())) {
            throw new IllegalArgumentException("No se puede crear el contrato: el cliente "
                    + contrato.getCliente().getNombre() + " no está registrado.");
        }
        contrato.crearContrato(); // lanza excepción si la propiedad no está disponible (RF-04)
        contratos.add(contrato);
    }

    public List<Contrato> filtrarContratosEnMora() {
        return filtrarContratosEnMora(LocalDate.now());
    }

    public void verificarPagosVencidos() {
        verificarPagosVencidos(LocalDate.now());
    }

    /** RF-09: incluye contratos activos con al menos una cuota vencida y sin pagar. */
    public List<Contrato> filtrarContratosEnMora(LocalDate hoy) {
        verificarPagosVencidos(hoy);
        List<Contrato> enMora = new ArrayList<>();
        for (Contrato contrato : contratos) {
            if (Contrato.ACTIVO.equals(contrato.getEstado()) && contrato.tienePagosVencidos(hoy)) {
                enMora.add(contrato);
            }
        }
        return enMora;
    }

    private void verificarPagosVencidos(LocalDate hoy) {
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha de consulta es obligatoria.");
        }
        generarCuotasHasta(hoy);
    }

        /**
     * RF-15: lista los contratos activos cuya fecha de fin vence dentro de los próximos 'dias'
     * (o ya vencieron y siguen activos).
     */
    public List<Contrato> listarContratosPorVencer(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("El número de días no puede ser negativo (se recibió: " + dias + ").");
        }
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);

        List<Contrato> porVencer = new ArrayList<>();
        for (Contrato contrato : contratos) {
            boolean estaActivo = !"finalizado".equalsIgnoreCase(contrato.getEstado());
            LocalDate fechaFin = contrato.getFechaFin();
            if (estaActivo && fechaFin != null && !fechaFin.isAfter(fechaLimite)) {
                porVencer.add(contrato);
            }
        }
        return porVencer;
    }


    public List<Contrato> getContratos() {
        return Collections.unmodifiableList(contratos);
    }

    // ===================== Lo que exige la clase padre Gestor =====================

    @Override
    protected List<Persistible> elementosPersistibles() {
        return new ArrayList<>(contratos);
    }

    /**
     * RNF-01: recrea los contratos y sus pagos. Necesita que inmuebles y clientes ya estén cargados
     * (SistemaInmobiliario.cargarTodo() respeta ese orden). Se llama sobre un gestor vacío, al arrancar.
     * Registro de contratos.txt: número|tipo|inicio|fin|estado|dirección|documento|datos propios...
     */
    @Override
    public void cargarTodo() {
        if (!contratos.isEmpty()) {
            throw new IllegalStateException("Solo se pueden cargar los contratos en un gestor vacío.");
        }
        // Los contratos de arriendo por número, para enlazarles sus pagos después.
        Map<Integer, ContratoArriendo> arriendos = new HashMap<>();

        for (String[] r : ArchivoPlano.leerTodos("contratos.txt")) {
            try {
                Contrato contrato;
                switch (r[1]) {
                    case "ContratoArriendo":
                        ContratoArriendo arriendo = crearArriendo(r);
                        arriendos.put(arriendo.getNumeroContrato(), arriendo);
                        contrato = arriendo;
                        break;
                    case "ContratoVenta":
                        contrato = crearVenta(r);
                        break;
                    default:
                        throw new IllegalArgumentException("tipo de contrato desconocido '" + r[1] + "'");
                }
                contrato.reconectar(); // contratoActual e historial de la propiedad, historial del cliente
                contratos.add(contrato);
            } catch (RuntimeException e) {
                throw new IllegalStateException("No se pudo cargar el contrato " + r[0] + ": " + e.getMessage(), e);
            }
        }

        // Pagos: clave numeroContrato:idPago|fechaLímite|fechaPago (vacía si pendiente)|periodo|monto|estado
        for (String[] r : ArchivoPlano.leerTodos("pagos.txt")) {
            String[] clave = r[0].split(":");
            ContratoArriendo arriendo = arriendos.get(Integer.parseInt(clave[0]));
            if (arriendo == null) {
                throw new IllegalStateException("El pago " + r[0] + " pertenece a un contrato de arriendo que no existe.");
            }
            LocalDate fechaPago = r[2].isEmpty() ? null : LocalDate.parse(r[2]);
            arriendo.cargarPago(new Pago(Integer.parseInt(clave[1]), LocalDate.parse(r[1]), fechaPago, r[3],
                    Double.parseDouble(r[4]), r[5]));
        }
    }

    // ---- Fábricas: crean cada tipo de contrato con su constructor de reconstrucción ----

    private ContratoArriendo crearArriendo(String[] r) {
        Arrendatario arrendatario = clientes.buscarArrendatario(r[6]);
        if (arrendatario == null) {
            throw new IllegalArgumentException("no existe el arrendatario con documento " + r[6]);
        }
        return new ContratoArriendo(Integer.parseInt(r[0]), propiedadDe(r), arrendatario,
                LocalDate.parse(r[2]), LocalDate.parse(r[3]), r[4],
                Double.parseDouble(r[7]), Double.parseDouble(r[8]), LocalDate.parse(r[9]));
    }

    private ContratoVenta crearVenta(String[] r) {
        Comprador comprador = clientes.buscarComprador(r[6]);
        if (comprador == null) {
            throw new IllegalArgumentException("no existe el comprador con documento " + r[6]);
        }
        return new ContratoVenta(Integer.parseInt(r[0]), propiedadDe(r), comprador,
                LocalDate.parse(r[2]), LocalDate.parse(r[3]), r[4],
                Double.parseDouble(r[7]), Double.parseDouble(r[8]), Double.parseDouble(r[9]));
    }

    private Propiedad propiedadDe(String[] r) {
        Propiedad propiedad = inmuebles.buscarPropiedad(r[5]);
        if (propiedad == null) {
            throw new IllegalArgumentException("no existe la propiedad '" + r[5] + "'");
        }
        return propiedad;
    }

    @Override
    public void recolectarNotificaciones(LocalDate hoy) {
        // Cada contrato avisa lo suyo; uno de arriendo incluye los avisos de sus pagos (RF-16).
        for (Contrato contrato : contratos) {
            agregarNotificaciones(contrato.generarNotificaciones(hoy));
        }
    }

    /** RF-16: cada contrato crea las cuotas pendientes que le falten hasta la fecha dada (polimorfismo). */
    public void generarCuotasHasta(LocalDate fecha) {
        for (Contrato contrato : contratos) {
            contrato.generarCuotasHasta(fecha);
        }
    }
}
