import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * RF-01: Registrar una propiedad (dirección, área, valor base, tipo y modo).
 * Clase abstracta: cada tipo de inmueble (Apartamento, LocalComercial...) agrega sus propias reglas.
 */
public abstract class Propiedad implements Persistible {

    // ===================== Constantes (estados y modos permitidos) =====================
    public static final String DISPONIBLE = "disponible";
    public static final String ARRENDADA = "arrendada";
    public static final String EN_CAPTACION = "en captación"; // el agente aún está captando/preparando el inmueble
    public static final String FINALIZADO = "finalizado";     // operación terminada (p. ej. inmueble vendido)

    public static final String MODO_ARRIENDO = "Arriendo";
    public static final String MODO_VENTA = "Venta";

    private static final String ARCHIVO = "propiedades.txt";

    // ===================== Atributo estático =====================
    // Cuenta cuántas propiedades se han registrado en el sistema (lo incrementa GestorInmuebles).
    private static int contadorPropiedades = 0;

    // ===================== Atributos (protected: los heredan las hijas) =====================
    protected String direccion;
    protected double area;
    protected double valorBase;   // canon mensual si el modo es Arriendo; precio de venta si el modo es Venta
    protected String tipo;
    protected String modo;
    protected String estado;
    protected Propietario propietario;      // dueño del inmueble (lo recibe el constructor)
    protected Contrato contratoActual;      // contrato vigente (0..1)
    protected List<Interesado> interesados = new ArrayList<>(); // AQUÍ VA RF-11: registrar interesados

    // Historial de todos los contratos que ha tenido la propiedad (0..*)
    private final List<Contrato> historial = new ArrayList<>();

    // ===================== Constructor =====================
    // Solo guarda el propietario: el enlace inverso (Propietario.agregarPropiedad) lo hace GestorInmuebles
    // cuando el registro es exitoso. Así no se pasa "this" desde el constructor y una propiedad rechazada
    // (datos inválidos o dirección repetida) no queda en la lista del dueño.
    public Propiedad(String direccion, double area, double valorBase, String tipo, String modo,
                     Propietario propietario) {
        // Se quitan espacios sobrantes (también los internos) para que "Calle 1  #2" y "Calle 1 #2" sean la misma.
        this.direccion = direccion == null ? null : direccion.trim().replaceAll("\\s+", " ");
        this.area = area;
        this.valorBase = valorBase;
        this.tipo = tipo == null ? null : tipo.trim();
        this.modo = normalizarModo(modo);
        this.propietario = propietario;
        this.estado = DISPONIBLE; // toda propiedad nueva nace disponible
        validarDatos();           // si algo está mal, lanza una excepción y la propiedad no se crea
    }

    // ===================== Métodos de negocio =====================

    /**
     * Valida los datos de la propiedad (RF-01 / RNF-02).
     * Devuelve true si todo es correcto; si algo está mal LANZA una excepción que explica el problema.
     */
    public boolean validarDatos() {
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("La dirección de la propiedad es obligatoria.");
        }
        if (!(area > 0) || Double.isInfinite(area)) {
            throw new IllegalArgumentException("El área debe ser mayor a cero (se recibió: " + area + ").");
        }
        if (!(valorBase > 0) || Double.isInfinite(valorBase)) {
            throw new IllegalArgumentException("El valor base debe ser mayor a cero (se recibió: " + valorBase + ").");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de propiedad es obligatorio (por ejemplo: Apartamento).");
        }
        if (!MODO_ARRIENDO.equals(modo) && !MODO_VENTA.equals(modo)) {
            throw new IllegalArgumentException("El modo debe ser '" + MODO_ARRIENDO + "' o '" + MODO_VENTA
                    + "' (se recibió: " + modo + ").");
        }
        if (propietario == null) {
            throw new IllegalArgumentException("La propiedad '" + direccion + "' debe tener un propietario.");
        }
        if (!esEstadoValido(estado)) {
            throw new IllegalArgumentException("Estado inválido: '" + estado + "'.");
        }
        return true;
    }

    /**
     * Cambio manual de estado que hace el agente: solo entre "disponible" y "en captación".
     * "Arrendada" y "finalizado" los pone un contrato al crearse, y una propiedad con contrato vigente
     * solo se libera al terminar ese contrato (RF-10). Así nadie puede saltarse RF-04 cambiando el estado.
     */
    public void cambiarEstado(String nuevoEstado) {
        if (contratoActual != null) {
            throw new IllegalStateException("La propiedad '" + direccion + "' tiene un contrato vigente (N° "
                    + contratoActual.getNumeroContrato() + "); se libera al terminar el contrato.");
        }
        String normalizado = nuevoEstado == null ? null : nuevoEstado.trim().toLowerCase();
        if (!DISPONIBLE.equals(normalizado) && !EN_CAPTACION.equals(normalizado)) {
            throw new IllegalArgumentException("Estado inválido: '" + nuevoEstado + "'. Manualmente solo se puede "
                    + "pasar a '" + DISPONIBLE + "' o '" + EN_CAPTACION + "'.");
        }
        this.estado = normalizado;
    }

    /**
     * Polimorfismo (RF-14): cada tipo de inmueble calcula su depósito de garantía a su manera.
     * Es abstracto aquí; lo implementan Apartamento y LocalComercial.
     */
    public abstract double calcularDepositoGarantia();

    public void agregarInteresado(Interesado interesado) {
        // Pendiente: RF-11
    }

    // AQUÍ VA RF-13 (listar disponibles por modalidad): vive en GestorInmuebles, no en la propiedad.

    // ===================== Método estático =====================
    public static int getTotalPropiedades() {
        return contadorPropiedades;
    }

    // Default (sin public): solo lo llama GestorInmuebles cuando una propiedad se registra con éxito.
    static void contarRegistro() {
        contadorPropiedades++;
    }

    // ===================== Relaciones con otras clases (acceso default) =====================

    // Lo llama Propietario.agregarPropiedad(): una propiedad tiene un solo dueño.
    void asignarPropietario(Propietario nuevoPropietario) {
        if (this.propietario != null && this.propietario != nuevoPropietario) {
            throw new IllegalStateException("La propiedad '" + direccion + "' ya pertenece a otro propietario ("
                    + propietario.getNombre() + ").");
        }
        this.propietario = nuevoPropietario;
    }

    // Lo llama Contrato.crearContrato(): deja el contrato como vigente, lo suma al historial y ocupa la
    // propiedad con el estado que corresponde al tipo de contrato (arrendada o finalizado), todo en un paso.
    void asignarContrato(Contrato contrato, String nuevoEstado) {
        this.contratoActual = contrato;
        this.historial.add(contrato);
        this.estado = nuevoEstado;
    }

    void liberarContrato() {
        // Pendiente: RF-10 (contratoActual = null y estado disponible al terminar un contrato)
    }

    // ---- Solo para recrear el sistema desde archivo (los llama la persistencia, nunca el agente) ----

    // Lo llama GestorInmuebles.cargarTodo(): deja el estado tal como quedó guardado (RNF-01), incluso
    // "arrendada" o "finalizado", que cambiarEstado() no permite poner a mano.
    void restaurarEstado(String estadoGuardado) {
        if (!esEstadoValido(estadoGuardado)) {
            throw new IllegalArgumentException("Estado guardado inválido: '" + estadoGuardado + "'.");
        }
        this.estado = estadoGuardado;
    }

    // Lo llama Contrato.reconectar(): suma el contrato al historial y, si sigue vigente, lo deja como actual.
    // No cambia el estado: ese ya se restauró desde el archivo de propiedades.
    void reconectarContrato(Contrato contrato, boolean vigente) {
        if (vigente) {
            if (contratoActual != null) {
                throw new IllegalStateException("Datos inconsistentes: la propiedad '" + direccion
                        + "' tiene dos contratos vigentes guardados.");
            }
            this.contratoActual = contrato;
        }
        this.historial.add(contrato);
    }

    // ===================== Persistencia (RNF-01) =====================

    @Override
    public void guardar() {
        validarDatos(); // nunca se guardan datos inválidos
        String[] comunes = {
                direccion,                                                     // clave del registro
                String.valueOf(area),
                String.valueOf(valorBase),
                tipo,
                modo,
                estado,
                propietario.getDocumento(),                                    // solo se guarda la referencia
                contratoActual == null ? "" : String.valueOf(contratoActual.getNumeroContrato())
        };
        ArchivoPlano.guardarRegistro(ARCHIVO, ArchivoPlano.concatenar(comunes, datosPropios()));
    }

    @Override
    public void cargar() {
        String[] registro = ArchivoPlano.buscarRegistro(ARCHIVO, direccion);
        if (registro == null) {
            throw new IllegalStateException("No hay ninguna propiedad guardada con la dirección '" + direccion + "'.");
        }
        try {
            this.area = Double.parseDouble(registro[1]);
            this.valorBase = Double.parseDouble(registro[2]);
            this.tipo = registro[3];
            this.modo = registro[4];
            this.estado = registro[5];
            // registro[6] (documento del propietario) y registro[7] (número del contrato actual) son referencias
            // a otros objetos: se reconectan cuando los gestores carguen todo el sistema (mejora futura).
            validarDatos();
            aplicarDatosPropios(Arrays.copyOfRange(registro, 8, registro.length));
        } catch (RuntimeException e) {
            throw new IllegalStateException("El registro guardado de la propiedad '" + direccion
                    + "' está dañado: " + e.getMessage(), e);
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
    public String getDireccion() { return direccion; }
    public double getArea() { return area; }
    public double getValorBase() { return valorBase; }
    public String getTipo() { return tipo; }
    public String getModo() { return modo; }
    public String getEstado() { return estado; }
    public Propietario getPropietario() { return propietario; }
    public Contrato getContratoActual() { return contratoActual; }
    public List<Contrato> getHistorial() { return Collections.unmodifiableList(historial); }
    public List<Interesado> getInteresados() { return Collections.unmodifiableList(interesados); }

    // ===================== Auxiliares privados =====================

    private static boolean esEstadoValido(String valor) {
        return DISPONIBLE.equals(valor) || ARRENDADA.equals(valor) || EN_CAPTACION.equals(valor)
                || FINALIZADO.equals(valor);
    }

    // Acepta "arriendo", "ARRIENDO", "venta"... y lo deja con el formato oficial.
    private static String normalizarModo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (MODO_ARRIENDO.equalsIgnoreCase(limpio)) {
            return MODO_ARRIENDO;
        }
        if (MODO_VENTA.equalsIgnoreCase(limpio)) {
            return MODO_VENTA;
        }
        return limpio;
    }
}
