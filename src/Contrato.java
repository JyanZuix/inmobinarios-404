import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RF-03: Crear un contrato asociando una propiedad disponible y un cliente.
 * Clase abstracta: ContratoArriendo y ContratoVenta comparten fechas y estado, pero calculan distinto.
 *
 * Crear un contrato tiene dos pasos: primero se construye el objeto (se validan sus datos) y luego
 * crearContrato() lo formaliza (verifica la disponibilidad, le asigna número y deja la propiedad ocupada).
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

    // ===================== Constructor =====================
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

    // ===================== Métodos de negocio =====================

    /** La propiedad solo se puede contratar si su estado es "disponible". */
    public boolean verificarDisponibilidad() {
        return Propiedad.DISPONIBLE.equals(propiedad.getEstado());
    }

    /**
     * RF-03: formaliza el contrato. Devuelve true si se creó; si no se puede crear, LANZA una excepción
     * que explica el motivo (así quien opera el sistema sabe qué corregir).
     */
    public boolean crearContrato() {
        if (estado != null) {
            throw new IllegalStateException("El contrato " + numeroContrato + " ya fue creado.");
        }
        if (!verificarDisponibilidad()) {
            throw new IllegalStateException("No se puede crear el contrato: la propiedad '" + propiedad.getDireccion()
                    + "' no está disponible (estado actual: " + propiedad.getEstado() + ").");
        }
        if (cliente.getContratoActual() != null) {
            throw new IllegalStateException("No se puede crear el contrato: " + cliente.getNombre()
                    + " ya tiene un contrato vigente (N° " + cliente.getContratoActual().getNumeroContrato() + ").");
        }

        this.numeroContrato = generarNumeroContrato();
        this.estado = ACTIVO;
        propiedad.cambiarEstado(estadoPropiedadAlCrear()); // la propiedad deja de estar disponible
        propiedad.registrarContrato(this);                 // contratoActual + historial de la propiedad
        cliente.registrarContrato(this);                   // contratoActual + historial del cliente
        return true;
    }

    // AQUÍ VA RF-10: terminarContrato() (estado "finalizado" y liberar la propiedad).
    // AQUÍ VA RF-04 completo: ya se bloquea con verificarDisponibilidad() dentro de crearContrato().

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
            throw new IllegalStateException("El contrato aún no ha sido creado: llame a crearContrato() antes de guardarlo.");
        }
        String[] comunes = {
                String.valueOf(numeroContrato),     // clave del registro
                fechaInicio.toString(),
                fechaFin.toString(),
                estado,
                propiedad.getDireccion(),           // solo se guarda la referencia
                cliente.getDocumento()              // solo se guarda la referencia
        };
        // AQUÍ VA: cuando exista RF-05, ContratoArriendo.guardar() deberá delegar también en sus Pago.
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
            LocalDate inicio = LocalDate.parse(registro[1]);
            LocalDate fin = LocalDate.parse(registro[2]);
            validarFechas(inicio, fin);
            String estadoGuardado = registro[3];
            if (!ACTIVO.equals(estadoGuardado) && !FINALIZADO.equals(estadoGuardado)) {
                throw new IllegalArgumentException("estado desconocido '" + estadoGuardado + "'");
            }
            this.fechaInicio = inicio;
            this.fechaFin = fin;
            this.estado = estadoGuardado;
            // registro[4] (dirección de la propiedad) y registro[5] (documento del cliente) son referencias:
            // se reconectan cuando los gestores carguen todo el sistema (mejora futura).
            aplicarDatosPropios(Arrays.copyOfRange(registro, 6, registro.length));
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
