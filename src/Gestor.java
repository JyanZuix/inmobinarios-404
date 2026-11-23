import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase padre de todos los gestores (GestorInmuebles, GestorClientes, GestorContratos).
 * Lo que todos hacen igual se escribe aquí una sola vez; lo que cambia queda abstracto y cada hijo lo implementa.
 */
public abstract class Gestor {

    // Notificaciones recolectadas por este gestor (RF-16 a RF-21).
    protected final List<Notificacion> notificaciones = new ArrayList<>();

    // ===================== Persistencia (RNF-01) =====================

    /** Guarda todos los objetos del gestor. Es igual para todos: recorre y llama guardar() de cada uno. */
    public void guardarTodo() {
        for (Persistible elemento : elementosPersistibles()) {
            elemento.guardar();
        }
        // RF-21 tambien se guardan las notificasiones con su estado
        for (Notificacion n : notificaciones) {
            n.guardar();
        }
    }

    // RF-21 al arrancar se traen las notificaciones guardadas, incluidas las atendidas para que no se dupliquen.
    // todas van en el mismo archivo, cada gestor solo toma las que son suyas segun el origen
    public void cargarNotificaciones() {
        List<Notificacion> leidas = new ArrayList<>();
        for (String[] r : ArchivoPlano.leerTodos(Notificacion.ARCHIVO)) {
            Notificacion n = Notificacion.desdeRegistro(r);
            if (esMiNotificacion(n)) {
                leidas.add(n);
            }
        }
        agregarNotificaciones(leidas);
    }

    // cada hijo dice que origenes le perteneceen (PAGO:, CONTRATO:, ...)
    protected abstract boolean esMiNotificacion(Notificacion n);

    /**
     * Cada hijo entrega los objetos que hay que guardar. Como una Propiedad (o un Cliente, o un Contrato)
     * ES un Persistible, basta con devolver new ArrayList<>(listaDelHijo), sin casts.
     */
    protected abstract List<Persistible> elementosPersistibles();

    /** Cada hijo sabe recrear sus objetos desde los archivos al arrancar el sistema. */
    public abstract void cargarTodo();

    // ===================== Notificaciones =====================

    /** Cada hijo recorre sus objetos Notificable y guarda los avisos nuevos (RF-16 a RF-19). */
    public abstract void recolectarNotificaciones(LocalDate hoy);

    /**
     * Agrega los avisos nuevos y descarta los que ya existen (mismo hecho, ver Notificacion.esMismaQue()).
     * Igual para todos los gestores: cada hijo lo usa dentro de su recolectarNotificaciones().
     */
    protected void agregarNotificaciones(List<Notificacion> nuevas) {
        for (Notificacion nueva : nuevas) {
            boolean yaExiste = false;
            for (Notificacion existente : notificaciones) {
                if (existente.esMismaQue(nueva)) {
                    yaExiste = true;
                    break;
                }
            }
            if (!yaExiste) {
                notificaciones.add(nueva);
            }
        }
    }

    // RF-20 solo las que no estan atendidas
    public List<Notificacion> listarNotificacionesPendientes() {
        List<Notificacion> pendientes = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            if (!n.estaAtendida()) {
                pendientes.add(n);
            }
        }
        return pendientes;
    }

    public List<Notificacion> getNotificaciones() {
        return Collections.unmodifiableList(notificaciones);
    }
}
