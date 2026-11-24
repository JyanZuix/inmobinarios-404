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
    private static final int DIAS_AVISO_VENCIMIENTO = 30; // RF-17: avisar los contratos que vencen en 30 días o menos

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
        if (Propiedad.ARRENDADA.equals(propiedad.getEstado())) {
            // RF-04: una propiedad arrendada no admite otro contrato hasta que se termine el actual (RF-10).
            throw new IllegalStateException("No se puede crear el contrato: la propiedad '" + propiedad.getDireccion()
                    + "' ya está arrendada (contrato " + propiedad.getContratoActual().getNumeroContrato()
                    + "). Termine ese contrato antes de crear uno nuevo.");
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

    /**
     * RNF-01: vuelve a enlazar un contrato cargado desde archivo con su propiedad y su cliente.
     * Es el equivalente de crearContrato() para un contrato que ya existía: no genera número ni valida
     * disponibilidad, solo restaura las relaciones. Default: solo lo llama GestorContratos.cargarTodo().
     */
    void reconectar() {
        propiedad.reconectarContrato(this, ACTIVO.equals(estado)); // contratoActual (si sigue activo) + historial
        cliente.asignarContrato(this);                             // historial del cliente
    }

    /** RF-10 con la fecha de hoy (atajo para el uso real del sistema). */
    public void terminarContrato() {
        terminarContrato(LocalDate.now());
    }

    /**
     * RF-10: termina el contrato en la fecha dada y conserva el contrato en ambos historiales.
     * Si termina antes de lo previsto, la fecha de fin pasa a ser la fecha real de terminación.
     * La propiedad queda en el estado que corresponde al tipo de contrato (estadoPropiedadAlTerminar()):
     * un arriendo la deja disponible; una venta cerrada la deja finalizada (ya no se puede volver a ofrecer).
     */
    public void terminarContrato(LocalDate fecha) {
        finalizar(fecha, estadoPropiedadAlTerminar());
    }

    // Lo común a terminar un contrato y a cancelar una venta (ContratoVenta.cancelarVenta).
    protected void finalizar(LocalDate fecha, String estadoPropiedad) {
        if (!ACTIVO.equals(estado)) {
            throw new IllegalStateException("Solo se puede terminar un contrato activo.");
        }
        if (fecha == null || !fecha.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de terminación (" + fecha + ") debe ser posterior al inicio "
                    + "del contrato (" + fechaInicio + ").");
        }
        alTerminar(fecha);
        if (fecha.isBefore(fechaFin)) {
            fechaFin = fecha; // terminó antes de lo previsto
        }
        propiedad.liberarContrato(this, estadoPropiedad);
        estado = FINALIZADO;
    }

    /** Lo que cada tipo de contrato hace al terminar (por defecto nada; ContratoArriendo quita cuotas futuras). */
    protected void alTerminar(LocalDate fecha) {
        // Sin acciones por defecto.
    }

    /** Estado en que queda la propiedad al terminar el contrato. Por defecto vuelve a estar disponible. */
    protected String estadoPropiedadAlTerminar() {
        return Propiedad.DISPONIBLE;
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

    /** RF-09 y RF-19: true si el contrato tiene algún pago en mora a la fecha dada. */
    public boolean tienePagosVencidos(LocalDate hoy) {
        return moraDesde(hoy) != null;
    }

    /**
     * RF-19: fecha límite del pago en mora más antiguo, o null si no hay mora. Por defecto null, porque un
     * contrato de venta no tiene cuotas periódicas (ContratoArriendo lo sobrescribe).
     */
    public LocalDate moraDesde(LocalDate hoy) {
        return null;
    }

    /**
     * RF-16: crea las cuotas pendientes que correspondan hasta la fecha dada. Por defecto no hace nada,
     * porque solo el contrato de arriendo tiene cuotas mensuales (ContratoArriendo lo sobrescribe).
     */
    public void generarCuotasHasta(LocalDate fecha) {
        // Un contrato de venta no tiene cuotas periódicas.
    }

    /**
     * True si el contrato sigue activo y su fecha de fin es dentro de los próximos "dias" días o ya pasó
     * (un contrato vencido que nadie terminó también requiere atención). Lo usan RF-17 y RF-19.
     */
    public boolean vencePronto(LocalDate hoy, int dias) {
        return ACTIVO.equals(estado) && !fechaFin.isAfter(hoy.plusDays(dias));
    }

    // ===================== Notificable =====================

    /**
     * RF-17: un contrato activo avisa si termina en los próximos 30 días, o si ya venció y sigue activo
     * (hay que terminarlo con RF-10 para liberar la propiedad). Los contratos finalizados no avisan.
     */
    @Override
    public List<Notificacion> generarNotificaciones(LocalDate hoy) {
        List<Notificacion> avisos = new ArrayList<>();
        if (vencePronto(hoy, DIAS_AVISO_VENCIMIENTO)) {
            String idOrigen = "CONTRATO:" + numeroContrato;
            String descripcion = "Contrato " + numeroContrato + " (" + propiedad.getDireccion() + ", "
                    + cliente.getNombre() + ")";
            if (fechaFin.isBefore(hoy)) {
                avisos.add(new Notificacion(Notificacion.CONTRATO_VENCIDO,
                        descripcion + " venció el " + fechaFin + " y sigue activo.", fechaFin, idOrigen));
            } else {
                avisos.add(new Notificacion(Notificacion.CONTRATO_POR_VENCER,
                        descripcion + " vence el " + fechaFin + ".", fechaFin, idOrigen));
            }
        }
        return avisos;
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

    /**
     * Ficha del contrato para mostrarla en pantalla. Las hijas le agregan sus montos propios.
     * Un contrato que todavía no se registró en el gestor no tiene número ni estado.
     */
    @Override
    public String toString() {
        String numero = estado == null ? "(sin registrar)" : "N° " + numeroContrato;
        return "Contrato " + numero + " | " + propiedad.getDireccion() + " | " + cliente.getNombre()
                + " | del " + fechaInicio + " al " + fechaFin + " | estado: " + (estado == null ? "-" : estado)
                + " | monto final: " + Utilidades.moneda(calcularMontoFinal());
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
