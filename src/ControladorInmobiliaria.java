import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/** Cada botón llama a una operación del sistema y actualiza la información que se ve. */
public class ControladorInmobiliaria {

    private static final String TODOS = "Todo el inventario";
    private static final String ARRIENDOS_DISPONIBLES = "Disponibles para arriendo";
    private static final String VENTAS_DISPONIBLES = "Disponibles para venta";
    private static final String APARTAMENTO = "Apartamento";
    private static final String LOCAL = "Local comercial";
    private static final String ARRENDATARIO = "Arrendatario";
    private static final String COMPRADOR = "Comprador";

    @FXML private ComboBox<String> filtroInmuebles;
    @FXML private ComboBox<ContratoArriendo> contratoPagos;
    @FXML private ListView<Propiedad> listaInmuebles;
    @FXML private ListView<Cliente> listaClientes;
    @FXML private ListView<Contrato> listaContratos;
    @FXML private ListView<String> listaPagos;
    @FXML private ListView<String> listaMora;
    @FXML private ListView<Interesado> listaInteresados;
    @FXML private ListView<Notificacion> listaAvisos;
    @FXML private Label canon;
    @FXML private Label estado;
    @FXML private Button botonGuardar;

    private SistemaInmobiliario sistema;

    void conectar(SistemaInmobiliario sistema) {
        this.sistema = sistema;
        configurarLista(listaInteresados, this::describirInteresado);
        configurarLista(listaAvisos, Notificacion::toString);
        configurarLista(listaContratos, Contrato::toString);
        configurarLista(listaInmuebles, Propiedad::toString);
        filtroInmuebles.getItems().setAll(TODOS, ARRIENDOS_DISPONIBLES, VENTAS_DISPONIBLES);
        filtroInmuebles.setValue(TODOS);
        refrescarListas();
    }

    // Inmuebles

    @FXML
    private void registrarInmueble() {
        TextField direccion = new TextField();
        TextField area = new TextField();
        TextField valor = new TextField();
        TextField habitaciones = new TextField("1");
        TextField documento = new TextField();
        TextField nombre = new TextField();
        TextField contacto = new TextField();
        ComboBox<String> tipo = elegir(List.of(APARTAMENTO, LOCAL));
        ComboBox<String> modalidad = elegir(List.of(Propiedad.MODO_ARRIENDO, Propiedad.MODO_VENTA));
        habitaciones.disableProperty().bind(tipo.valueProperty().isNotEqualTo(APARTAMENTO));

        if (formulario("Registrar inmueble", campo("Dirección", direccion), campo("Tipo", tipo),
                campo("Modalidad", modalidad), campo("Área en m²", area), campo("Valor base en pesos", valor),
                campo("Habitaciones (apartamento)", habitaciones), campo("Documento del propietario", documento),
                campo("Nombre del propietario (si es nuevo)", nombre),
                campo("Contacto del propietario (si es nuevo)", contacto))) {
            aplicar("Inmueble registrado.", () -> {
                Propietario dueno = propietario(documento.getText(), nombre.getText(), contacto.getText());
                double areaM2 = numero(area);
                double valorBase = numero(valor);
                Propiedad inmueble;
                if (APARTAMENTO.equals(tipo.getValue())) {
                    inmueble = new Apartamento(direccion.getText(), areaM2, valorBase, modalidad.getValue(),
                            dueno, Integer.parseInt(habitaciones.getText().trim()));
                } else {
                    inmueble = new LocalComercial(direccion.getText(), areaM2, valorBase, modalidad.getValue(), dueno);
                }
                sistema.getGestorInmuebles().agregarPropiedad(inmueble);
                filtroInmuebles.setValue(TODOS);
            });
        }
    }

    private Propietario propietario(String documento, String nombre, String contacto) {
        String clave = Utilidades.normalizarDocumento(documento, "propietario");
        for (Propiedad inmueble : sistema.getGestorInmuebles().getInmuebles()) {
            if (inmueble.getPropietario().getDocumento().equals(clave)) {
                return inmueble.getPropietario();
            }
        }
        return new Propietario(clave, nombre, contacto);
    }

    @FXML
    private void consultarInmuebles() {
        GestorInmuebles gestor = sistema.getGestorInmuebles();
        String filtro = filtroInmuebles.getValue();
        if (filtro == null || TODOS.equals(filtro)) {
            listaInmuebles.getItems().setAll(gestor.getInmuebles());
        } else {
            String modalidad = ARRIENDOS_DISPONIBLES.equals(filtro) ? Propiedad.MODO_ARRIENDO : Propiedad.MODO_VENTA;
            listaInmuebles.getItems().setAll(gestor.listarDisponibles(modalidad));
        }
    }

    @FXML
    private void consultarDeposito() {
        Propiedad inmueble = seleccionado(listaInmuebles, "un inmueble");
        if (inmueble != null) {
            try {
                informar("Depósito de garantía", inmueble.getDireccion() + "\n"
                        + Utilidades.moneda(inmueble.calcularDepositoGarantia()));
            } catch (IllegalStateException error) {
                estado.setText(error.getMessage());
            }
        }
    }

    // Clientes e interesados

    @FXML
    private void registrarCliente() {
        TextField documento = new TextField();
        TextField nombre = new TextField();
        TextField contacto = new TextField();
        ComboBox<String> tipo = elegir(List.of(ARRENDATARIO, COMPRADOR));
        CheckBox financiamiento = new CheckBox("Requiere financiamiento");
        financiamiento.disableProperty().bind(tipo.valueProperty().isNotEqualTo(COMPRADOR));
        if (formulario("Registrar cliente", campo("Tipo de cliente", tipo), campo("Documento", documento),
                campo("Nombre", nombre), campo("Teléfono o correo", contacto), financiamiento)) {
            aplicar("Cliente registrado.", () -> {
                Cliente cliente;
                if (COMPRADOR.equals(tipo.getValue())) {
                    cliente = new Comprador(documento.getText(), nombre.getText(), contacto.getText(),
                            financiamiento.isSelected());
                } else {
                    cliente = new Arrendatario(documento.getText(), nombre.getText(), contacto.getText());
                }
                sistema.getGestorClientes().agregarCliente(cliente);
            });
        }
    }

    @FXML
    private void registrarInteresado() {
        if (sistema.getGestorInmuebles().getInmuebles().isEmpty()) {
            estado.setText("Registra un inmueble antes de registrar interesados.");
            return;
        }
        TextField nombre = new TextField();
        TextField contacto = new TextField();
        ListView<Propiedad> inmuebles = new ListView<>();
        inmuebles.setPrefHeight(155);
        inmuebles.getItems().setAll(sistema.getGestorInmuebles().getInmuebles());
        inmuebles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        configurarLista(inmuebles, Propiedad::getDireccion);

        if (formulario("Registrar interesado", campo("Nombre", nombre), campo("Teléfono o correo", contacto),
                campo("Inmuebles (Ctrl o ⌘ + clic para seleccionar varios)", inmuebles))) {
            List<Propiedad> seleccion = new ArrayList<>(inmuebles.getSelectionModel().getSelectedItems());
            aplicar("Interesado registrado.", () -> sistema.getGestorInmuebles()
                    .registrarInteresado(nombre.getText(), contacto.getText(), seleccion));
        }
    }

    @FXML private void agregarCitaCliente() { agregarCita(seleccionado(listaClientes, "un cliente")); }
    @FXML private void agregarCitaInteresado() { agregarCita(seleccionado(listaInteresados, "un interesado")); }
    @FXML private void verCitasCliente() { verCitas(seleccionado(listaClientes, "un cliente")); }
    @FXML private void verCitasInteresado() { verCitas(seleccionado(listaInteresados, "un interesado")); }

    private void agregarCita(Persona persona) {
        if (persona == null) {
            return;
        }
        DatePicker fecha = new DatePicker(LocalDate.now());
        if (formulario("Seguimiento de " + persona.getNombre(), campo("Fecha de la cita", fecha))) {
            aplicar("Cita registrada.", () -> persona.registrarSeguimiento(fecha.getValue()));
        }
    }

    private void verCitas(Persona persona) {
        if (persona == null) {
            return;
        }
        List<LocalDate> fechas = persona.consultarSeguimientos(LocalDate.now());
        StringBuilder mensaje = new StringBuilder();
        for (LocalDate fecha : fechas) {
            mensaje.append(fecha).append('\n');
        }
        informar("Citas de " + persona.getNombre(),
                fechas.isEmpty() ? "No hay citas pendientes." : mensaje.toString().trim());
    }

    private String describirInteresado(Interesado interesado) {
        List<String> direcciones = new ArrayList<>();
        for (Propiedad inmueble : interesado.getInmuebles()) {
            direcciones.add(inmueble.getDireccion());
        }
        return interesado.getNombre() + " | " + interesado.getContacto() + " | "
                + String.join(", ", direcciones);
    }

    // Contratos

    @FXML private void crearArriendo() { crearContrato(false); }
    @FXML private void crearVenta() { crearContrato(true); }

    private void crearContrato(boolean venta) {
        String modalidad = venta ? Propiedad.MODO_VENTA : Propiedad.MODO_ARRIENDO;
        List<Propiedad> disponibles = sistema.getGestorInmuebles().listarDisponibles(modalidad);
        List<Cliente> clientes = new ArrayList<>();
        for (Cliente cliente : sistema.getGestorClientes().getClientes()) {
            if ((venta && cliente instanceof Comprador) || (!venta && cliente instanceof Arrendatario)) {
                clientes.add(cliente);
            }
        }
        if (disponibles.isEmpty() || clientes.isEmpty()) {
            estado.setText("Para crear el contrato necesitas un inmueble disponible en " + modalidad
                    + " y un " + (venta ? "comprador" : "arrendatario") + " registrado.");
            return;
        }
        ComboBox<Propiedad> inmueble = elegir(disponibles);
        ComboBox<Cliente> cliente = elegir(clientes);
        DatePicker inicio = new DatePicker(LocalDate.now());
        DatePicker fin = new DatePicker(venta ? LocalDate.now().plusMonths(1) : LocalDate.now().plusYears(1));
        TextField valor = new TextField(venta ? "" : "5");
        String etiqueta = venta ? "Cuota inicial en pesos (pago de contado: valor total)" : "Incremento anual (%)";
        if (formulario("Crear contrato de " + modalidad.toLowerCase(),
                campo("Inmueble", inmueble), campo("Cliente", cliente), campo("Fecha de inicio", inicio),
                campo("Fecha de fin", fin), campo(etiqueta, valor))) {
            aplicar("Contrato creado.", () -> {
                Contrato contrato;
                if (venta) {
                    contrato = new ContratoVenta(inmueble.getValue(), (Comprador) cliente.getValue(),
                            inicio.getValue(), fin.getValue(), numero(valor));
                } else {
                    contrato = new ContratoArriendo(inmueble.getValue(), (Arrendatario) cliente.getValue(),
                            inicio.getValue(), fin.getValue(), numero(valor) / 100);
                }
                sistema.getGestorContratos().agregarContrato(contrato);
            });
        }
    }

    @FXML
    private void terminarContrato() {
        Contrato contrato = seleccionado(listaContratos, "un contrato");
        if (contrato != null && puedeCerrar(contrato)) {
            DatePicker fecha = new DatePicker(LocalDate.now());
            String efecto = contrato instanceof ContratoVenta
                    ? "El inmueble quedará vendido." : "El inmueble volverá a estar disponible.";
            if (formulario("Terminar contrato N.º " + contrato.getNumeroContrato(),
                    new Label(efecto), campo("Fecha de terminación", fecha))) {
                aplicar("Contrato finalizado.", () -> contrato.terminarContrato(fecha.getValue()));
            }
        }
    }

    @FXML
    private void cancelarVenta() {
        Contrato contrato = seleccionado(listaContratos, "un contrato de venta");
        if (contrato == null) {
            return;
        }
        if (!(contrato instanceof ContratoVenta venta)) {
            estado.setText("Selecciona un contrato de venta.");
            return;
        }
        DatePicker fecha = new DatePicker(LocalDate.now());
        if (puedeCerrar(venta) && formulario("Cancelar venta N.º " + venta.getNumeroContrato(),
                new Label("El inmueble volverá a estar disponible."), campo("Fecha de cancelación", fecha))) {
            aplicar("Venta cancelada.", () -> venta.cancelarVenta(fecha.getValue()));
        }
    }

    private boolean puedeCerrar(Contrato contrato) {
        if (!Contrato.ACTIVO.equals(contrato.getEstado())) {
            estado.setText("Selecciona un contrato activo.");
            return false;
        }
        return true;
    }

    @FXML
    private void consultarVencimientos() {
        List<Contrato> contratos = sistema.getGestorContratos().listarContratosPorVencer(30);
        ListView<Contrato> resultados = new ListView<>();
        configurarLista(resultados, Contrato::toString);
        resultados.setPrefHeight(250);
        resultados.getItems().setAll(contratos);
        resultados.setPlaceholder(new Label("No hay contratos por vencer ni vencidos."));
        Alert ventana = new Alert(Alert.AlertType.INFORMATION);
        ventana.initOwner(ventana());
        ventana.setTitle("Vencimientos");
        ventana.setHeaderText("Contratos vencidos o con vencimiento en los próximos 30 días");
        ventana.getDialogPane().setPrefWidth(680);
        ventana.getDialogPane().setContent(resultados);
        ventana.showAndWait();
    }

    // Pagos y mora

    @FXML
    private void consultarPagos() {
        ContratoArriendo contrato = contratoPagos.getValue();
        listaPagos.getItems().clear();
        if (contrato == null) {
            canon.setText("");
            return;
        }
        contrato.generarCuotasHasta(LocalDate.now());
        canon.setText("Canon actual: " + Utilidades.moneda(contrato.getCanonActual()));
        for (Pago pago : contrato.listarPagos()) {
            String fila = pago.getPeriodoCubierto() + " | Límite: " + pago.getFechaLimite()
                    + " | " + pago.getEstadoPago();
            if (!pago.estaPendiente()) {
                fila += " el " + pago.getFechaPago() + " | Total: " + Utilidades.moneda(pago.getMontoPagado())
                        + " | Interés: " + Utilidades.moneda(pago.calcularMora());
            }
            listaPagos.getItems().add(fila);
        }
    }

    @FXML
    private void registrarPago() {
        ContratoArriendo contrato = contratoPagos.getValue();
        if (contrato == null || !Contrato.ACTIVO.equals(contrato.getEstado())) {
            estado.setText("Selecciona un contrato de arriendo activo.");
            return;
        }
        TextField periodo = new TextField(YearMonth.now().toString());
        DatePicker fecha = new DatePicker(LocalDate.now());
        if (formulario("Registrar pago mensual", campo("Periodo (AAAA-MM)", periodo),
                campo("Fecha de pago", fecha))) {
            aplicar("Pago registrado.", () -> sistema.getGestorContratos().registrarPago(
                    contrato.getNumeroContrato(), periodo.getText().trim(), fecha.getValue()));
        }
    }

    @FXML
    private void consultarMora() {
        mostrarMora();
        int cantidad = listaMora.getItems().size();
        estado.setText(cantidad == 1 ? "1 contrato en mora." : cantidad + " contratos en mora.");
    }

    private void mostrarMora() {
        LocalDate hoy = LocalDate.now();
        List<Contrato> contratos = sistema.getGestorContratos().filtrarContratosEnMora(hoy);
        listaMora.getItems().clear();
        for (Contrato contrato : contratos) {
            listaMora.getItems().add("N.º " + contrato.getNumeroContrato() + " | "
                    + contrato.getPropiedad().getDireccion() + " | Cuota vencida el " + contrato.moraDesde(hoy));
        }
    }

    // Avisos y guardado

    @FXML
    private void consultarAvisos() {
        sistema.recolectarNotificaciones(LocalDate.now());
        mostrarAvisosPendientes();
        guardar("Avisos actualizados.");
    }

    @FXML
    private void atenderAviso() {
        Notificacion aviso = seleccionado(listaAvisos, "un aviso");
        if (aviso == null) {
            return;
        }
        try {
            sistema.marcarNotificacionAtendida(aviso);
            estado.setText("Aviso atendido.");
        } catch (UncheckedIOException error) {
            mostrarFalloGuardado(error);
        }
        mostrarAvisosPendientes();
    }

    private void mostrarAvisosPendientes() {
        listaAvisos.getItems().setAll(sistema.listarNotificacionesPendientes());
    }

    private void aplicar(String mensaje, Runnable operacion) {
        try {
            operacion.run();
            refrescarListas();
            guardar(mensaje);
        } catch (NumberFormatException error) {
            estado.setText("Revisa los campos numéricos. Escribe los valores sin separador de miles.");
        } catch (IllegalArgumentException | IllegalStateException error) {
            estado.setText(error.getMessage());
        } catch (UncheckedIOException error) {
            estado.setText("No se pudo completar la operación en los archivos de datos. " + error.getMessage());
        }
    }

    private void refrescarListas() {
        consultarInmuebles();
        listaClientes.getItems().setAll(sistema.getGestorClientes().getClientes());
        listaContratos.getItems().setAll(sistema.getGestorContratos().getContratos());
        listaInteresados.getItems().setAll(sistema.getGestorInmuebles().getInteresados());

        ContratoArriendo seleccionado = contratoPagos.getValue();
        List<ContratoArriendo> arriendos = new ArrayList<>();
        for (Contrato contrato : sistema.getGestorContratos().getContratos()) {
            if (contrato instanceof ContratoArriendo arriendo) {
                arriendos.add(arriendo);
            }
        }
        contratoPagos.getItems().setAll(arriendos);
        contratoPagos.setValue(arriendos.contains(seleccionado) ? seleccionado : null);
        consultarPagos();
        mostrarMora();
        mostrarAvisosPendientes();
    }

    @FXML private void reintentarGuardado() { guardar("Cambios guardados."); }

    @FXML
    private void recargarDatos() {
        SistemaInmobiliario recargado = new SistemaInmobiliario();
        try {
            recargado.cargarTodo();
        } catch (RuntimeException error) {
            estado.setText("No se pudieron recargar los datos. " + error.getMessage());
            return;
        }
        sistema = recargado;
        refrescarListas();
        estado.setText("Datos recargados.");
    }

    private void guardar(String mensaje) {
        try {
            sistema.guardarTodo();
            botonGuardar.setVisible(false);
            botonGuardar.setManaged(false);
            estado.setText(mensaje);
        } catch (RuntimeException error) {
            mostrarFalloGuardado(error);
        }
    }

    private void mostrarFalloGuardado(RuntimeException error) {
        botonGuardar.setVisible(true);
        botonGuardar.setManaged(true);
        estado.setText("Guardado incompleto. Los cambios siguen en la ventana. " + error.getMessage());
    }

    // Controles comunes a los formularios

    private Window ventana() {
        return estado.getScene().getWindow();
    }

    private boolean formulario(String titulo, Node... campos) {
        return FormularioInmobiliario.mostrar(ventana(), titulo, campos);
    }

    private Node campo(String texto, Control entrada) {
        return FormularioInmobiliario.campo(texto, entrada);
    }

    private void informar(String titulo, String mensaje) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        aviso.initOwner(ventana());
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        aviso.showAndWait();
    }

    private <T> T seleccionado(ListView<T> lista, String instruccion) {
        T elemento = lista.getSelectionModel().getSelectedItem();
        if (elemento == null) {
            estado.setText("Selecciona " + instruccion + " en la lista.");
        }
        return elemento;
    }

    private static <T> ComboBox<T> elegir(List<T> opciones) {
        ComboBox<T> campo = new ComboBox<>();
        campo.getItems().setAll(opciones);
        campo.getSelectionModel().selectFirst();
        return campo;
    }

    private static <T> void configurarLista(ListView<T> lista, Function<T, String> formato) {
        lista.setCellFactory(control -> new ListCell<>() {
            {
                setWrapText(true);
                prefWidthProperty().bind(control.widthProperty().subtract(26));
            }

            @Override
            protected void updateItem(T elemento, boolean vacia) {
                super.updateItem(elemento, vacia);
                setText(vacia || elemento == null ? null : formato.apply(elemento));
            }
        });
    }

    private static double numero(TextField campo) {
        return Double.parseDouble(campo.getText().trim().replace(',', '.'));
    }
}
