import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RF-03: Crear un contrato asociando una propiedad disponible y un cliente.
 * Clase abstracta: ContratoArriendo y ContratoVenta comparten fechas y estado, pero calculan distinto.
 *
 * Crear un contrato tiene dos pasos: primero se construye el objeto (se validan sus datos) y luego
 * GestorContratos.agregarContrato() lo formaliza con crearContrato() (verifica la disponibilidad, le asigna
 * número y deja la propiedad ocupada) y lo guarda en su lista.
 */
public abstract class Contrato implements Persistible, Notificable {

    // ===================== Constantes (estados del contrato) =====================
    public static final String ACTIVO = "activo";
    public static final String FINALIZADO = "finalizado";

    private static final String ARCHIVO = "contratos.txt";

    // ===================== Atributo estático =====================
    // Lleva la cuenta para dar a cada contrato un número consecutivo único.
    private static int contadorContratos = 0;

    // ===================== Atributos (protected: los heredan las hijas) =====================
    protected int numeroContrato;   // 0 mientras el contrato no se haya creado con crearContrato()
    protected LocalDate fechaInicio;
    protected LocalDate fechaFin;
    protected String estado;        // null mientras no se haya creado; luego "activo" o "finalizado"
    protected Propiedad propiedad;
    protected Cliente cliente;

    // ===================== Constructores =====================
    protected Contrato(Propiedad propiedad, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin) {
        // RF-03: la propiedad y el cliente deben existir.
        if (propiedad == null) {
            throw new IllegalArgumentException("No se puede crear el contrato: la propiedad no existe.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("No se puede crear el contrato: el cliente no existe.");
        }
        validarFechas(fechaInicio, fechaFin);
        this.propiedad = propiedad;
        this.cliente = cliente;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // Reconstruye un contrato ya creado (al cargarlo desde el archivo): conserva su número y su estado.
    protected Contrato(int numeroContrato, Propiedad propiedad, Cliente cliente,
                       LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this(propiedad, cliente, fechaInicio, fechaFin);
        if (numeroContrato < 1) {
            throw new IllegalArgumentException("El número de contrato debe ser mayor a cero (se recibió: "
                    + numeroContrato + ").");
        }
        if (!ACTIVO.equals(estado) && !FINALIZADO.equals(estado)) {
            throw new IllegalArgumentException("Estado de contrato desconocido: '" + estado + "'.");
        }
        this.numeroContrato = numeroContrato;
        this.estado = estado;
        // Los contratos nuevos seguirán la numeración después del mayor número cargado.
        contadorContratos = Math.max(contadorContratos, numeroContrato);
    }

    // ===================== Métodos de negocio =====================

    /** La propiedad solo se puede contratar si su estado es "disponible". */
    public boolean verificarDisponibilidad() {
        return Propiedad.DISPONIBLE.equals(propiedad.getEstado());
    }

    /**
     * RF-03: formaliza el contrato. Devuelve true si se creó; si no se puede crear, LANZA una excepción
     * que explica el motivo (así quien opera el sistema sabe qué corregir).
     * Default (sin public): solo lo llama GestorContratos.agregarContrato(), que antes verifica que la
     * propiedad esté en el inventario y después guarda el contrato en su lista.
     */
    boolean crearContrato() {
        if (estado != null) {
            throw new IllegalStateException("El contrato " + numeroContrato + " ya fue creado.");
        }
        if (!verificarDisponibilidad()) {
            throw new IllegalStateException("No se puede crear el contrato: la propiedad '" + propiedad.getDireccion()
                    + "' no está disponible (estado actual: " + propiedad.getEstado() + ").");
        }

        this.numeroContrato = generarNumeroContrato();
        this.estado = ACTIVO;
        propiedad.asignarContrato(this, estadoPropiedadAlCrear()); // ocupa la propiedad: contratoActual + estado
        cliente.asignarContrato(this);                             // historial del cliente (puede tener varios)
        return true;
    }

    // RF-04 ya se bloquea con verificarDisponibilidad() dentro de crearContrato().

    public void terminarContrato() {
        // Pendiente: RF-10 (estado "finalizado" y liberar la propiedad y el cliente)
    }

    /** Polimorfismo: cada tipo de contrato calcula su monto final a su manera. */
    public abstract double calcularMontoFinal();

    /** Estado en que queda la propiedad cuando se crea este tipo de contrato (arrendada / finalizado). */
    protected abstract String estadoPropiedadAlCrear();

    // ===================== Método estático =====================
    /** Siguiente número consecutivo de contrato. */
    public static int generarNumeroContrato() {
        return ++contadorContratos;
    }

    // ===================== Notificable =====================
    @Override
    public List<Notificacion> generarNotificaciones() {
        // AQUÍ VA RF-17: aviso de contratos próximos a vencer.
        return new ArrayList<>();
    }

    // ===================== Persistencia (RNF-01) =====================

    @Override
    public void guardar() {
        if (estado == null) {
            throw new IllegalStateException("El contrato aún no ha sido creado: regístrelo con GestorContratos.agregarContrato() antes de guardarlo.");
        }
        String[] comunes = {
                String.valueOf(numeroContrato),     // clave del registro
                getClass().getSimpleName(),         // tipo (ContratoArriendo / ContratoVenta): para recrearlo al cargar
                fechaInicio.toString(),
                fechaFin.toString(),
                estado,
                propiedad.getDireccion(),           // solo se guarda la referencia
                cliente.getDocumento()              // solo se guarda la referencia
        };
        // ContratoArriendo sobrescribe guardar() para guardar también sus pagos.
        ArchivoPlano.guardarRegistro(ARCHIVO, ArchivoPlano.concatenar(comunes, datosPropios()));
    }

    @Override
    public void cargar() {
        if (estado == null) {
            throw new IllegalStateException("El contrato aún no ha sido creado, por eso no tiene un registro que cargar.");
        }
        String[] registro = ArchivoPlano.buscarRegistro(ARCHIVO, String.valueOf(numeroContrato));
        if (registro == null) {
            throw new IllegalStateException("No hay ningún contrato guardado con el número " + numeroContrato + ".");
        }
        try {
            // registro[1] es el tipo del contrato: solo se usa al recrear el sistema (GestorContratos.cargarTodo).
            LocalDate inicio = LocalDate.parse(registro[2]);
            LocalDate fin = LocalDate.parse(registro[3]);
            validarFechas(inicio, fin);
            String estadoGuardado = registro[4];
            if (!ACTIVO.equals(estadoGuardado) && !FINALIZADO.equals(estadoGuardado)) {
                throw new IllegalArgumentException("estado desconocido '" + estadoGuardado + "'");
            }
            this.fechaInicio = inicio;
            this.fechaFin = fin;
            this.estado = estadoGuardado;
            // registro[5] (dirección de la propiedad) y registro[6] (documento del cliente) son referencias:
            // las reconecta GestorContratos.cargarTodo() al recrear el sistema.
            aplicarDatosPropios(Arrays.copyOfRange(registro, 7, registro.length));
        } catch (RuntimeException e) {
            throw new IllegalStateException("El registro guardado del contrato " + numeroContrato
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
    public int getNumeroContrato() { return numeroContrato; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
    public Propiedad getPropiedad() { return propiedad; }
    public Cliente getCliente() { return cliente; }

    // ===================== Auxiliares =====================

    // Fechas coherentes (RNF-02): ambas existen y el contrato termina después de empezar.
    private static void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y de fin del contrato son obligatorias.");
        }
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha de fin (" + fin + ") debe ser posterior a la fecha de inicio ("
                    + inicio + ").");
        }
    }
}
