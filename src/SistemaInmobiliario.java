import java.time.LocalDate;
import java.util.List;

/**
 * Punto de entrada del sistema: TIENE los tres gestores (composición) y los coordina.
 * Crea cada gestor con sus dependencias, y guarda, carga y recolecta notificaciones de todos en el orden
 * correcto, para que el resto del programa no tenga que conocer ese orden.
 */
public class SistemaInmobiliario {

    private final GestorInmuebles gestorInmuebles;
    private final GestorClientes gestorClientes;
    private final GestorContratos gestorContratos;

    // Los mismos tres gestores vistos como Gestor, en el orden en que se deben cargar:
    // los contratos necesitan que ya existan las propiedades y los clientes.
    private final Gestor[] gestores;

    public SistemaInmobiliario() {
        this.gestorInmuebles = new GestorInmuebles();
        this.gestorClientes = new GestorClientes();
        this.gestorContratos = new GestorContratos(gestorInmuebles, gestorClientes);
        this.gestores = new Gestor[]{gestorInmuebles, gestorClientes, gestorContratos};
    }

    // ===================== Persistencia (RNF-01) =====================

    /** Guarda todo el sistema. Cada gestor sabe guardar lo suyo (polimorfismo). */
    public void guardarTodo() {
        for (Gestor gestor : gestores) {
            gestor.guardarTodo();
        }
    }

    /** Recrea todo el sistema desde los archivos, en orden: inmuebles → clientes → contratos. */
    public void cargarTodo() {
        for (Gestor gestor : gestores) {
            gestor.cargarTodo();
        }
    }

    // ===================== Notificaciones =====================

    /** Pide a cada gestor que recolecte los avisos de sus objetos a la fecha dada (RF-16 a RF-19). */
    public void recolectarNotificaciones(LocalDate hoy) {
        for (Gestor gestor : gestores) {
            gestor.recolectarNotificaciones(hoy);
        }
    }

    /** RF-20: notificaciones pendientes de todos los gestores, ordenadas por fecha del evento. */
    public List<Notificacion> listarNotificacionesPendientes() {
        throw new UnsupportedOperationException("Pendiente: RF-20");
    }

    // ===================== Getters =====================
    public GestorInmuebles getGestorInmuebles() { return gestorInmuebles; }
    public GestorClientes getGestorClientes() { return gestorClientes; }
    public GestorContratos getGestorContratos() { return gestorContratos; }
}
