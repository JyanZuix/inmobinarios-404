import java.util.ArrayList;
import java.util.List;

/**
 * Pago de un contrato de arriendo. Solo está lo mínimo para que compile (RF-05, RF-06 vienen después).
 */
public class Pago implements Persistible, Notificable {

    // AQUÍ VAN idPago, fechaPago, periodoCubierto, montoPagado, estadoPago y mora (RF-05 / RF-06).
    // AQUÍ VAN registrarPago(), validarPeriodo(), calcularMora() y verificarFechaLimite().

    /** Clase anidada (RF-06): una mora no existe sin el pago atrasado que la origina. */
    public static class Mora {
        // AQUÍ VAN diasAtraso, tasaMora, valorInteres y calcularValor().
    }

    @Override
    public List<Notificacion> generarNotificaciones() {
        // AQUÍ VA RF-16: aviso de pagos en mora o por vencer.
        return new ArrayList<>();
    }

    @Override
    public void guardar() {
        // AQUÍ VA la persistencia de Pago.
    }

    @Override
    public void cargar() {
        // AQUÍ VA la lectura de Pago desde archivo.
    }
}
