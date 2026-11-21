import java.util.ArrayList;
import java.util.List;

public abstract class Propiedad implements Persistible {
    private static int contadorPropiedades = 0;

    protected String direccion;
    protected double area;
    protected double valorBase;
    protected String tipo;
    protected String modo;
    protected String estado;
    protected Propietario propietario;
    protected Contrato contratoActual;
    protected List<Contrato> historial = new ArrayList<>();
    protected List<Interesado> interesados = new ArrayList<>();

    public Propiedad(String direccion, double area, double valorBase, String tipo, String modo,
                     Propietario propietario) {
        this.direccion = direccion;
        this.area = area;
        this.valorBase = valorBase;
        this.tipo = tipo;
        this.modo = modo;
        this.estado = "Disponible";
        this.propietario = propietario;
        contadorPropiedades++;
    }

    public boolean validarDatos() {
        throw new UnsupportedOperationException("Pendiente: RF-01");
    }

    public void cambiarEstado(String nuevoEstado) {
        // Pendiente: RF-10
    }

    public abstract double calcularDepositoGarantia();

    public static int getTotalPropiedades() {
        return contadorPropiedades;
    }

    public void agregarInteresado(Interesado interesado) {
        // Pendiente: RF-11
    }

    public void asignarContrato(Contrato contrato) {
        // Pendiente: RF-03
    }

    public void liberarContrato() {
        // Pendiente: RF-10
    }

    @Override
    public void guardar() {
        // Pendiente: RNF-01
    }

    @Override
    public void cargar() {
        // Pendiente: RNF-01
    }

    public String getDireccion() {
        return direccion;
    }

    public double getArea() {
        return area;
    }

    public double getValorBase() {
        return valorBase;
    }

    public String getTipo() {
        return tipo;
    }

    public String getModo() {
        return modo;
    }

    public String getEstado() {
        return estado;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public Contrato getContratoActual() {
        return contratoActual;
    }

    public List<Contrato> getHistorial() {
        return List.copyOf(historial);
    }

    public List<Interesado> getInteresados() {
        return List.copyOf(interesados);
    }
}
