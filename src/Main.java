import java.time.LocalDate;

/**
 * Demostración de RF-01 (registrar propiedad), RF-02 (registrar arrendatario) y RF-03 (crear contrato).
 * Cada caso con error imprime el mensaje de la excepción, tal como lo vería quien opera el sistema.
 */
public class Main {

    public static void main(String[] args) {
        // El sistema crea los gestores con sus dependencias; Main solo los pide.
        SistemaInmobiliario sistema = new SistemaInmobiliario();
        GestorInmuebles gestorInmuebles = sistema.getGestorInmuebles();
        GestorClientes gestorClientes = sistema.getGestorClientes();
        GestorContratos gestorContratos = sistema.getGestorContratos();

        // ---------- RF-01: registrar propiedades ----------
        titulo("RF-01: registrar propiedades");
        Propietario dueno = new Propietario("100200300", "Carlos Rojas", "3001234567");
        Apartamento apto = new Apartamento("Calle 100 #15-20 Apto 301", 80, 2_500_000, "Arriendo", dueno, 3);
        LocalComercial local = new LocalComercial("Carrera 7 #45-10 Local 2", 50, 4_000_000, "Arriendo", dueno);
        Apartamento aptoVenta = new Apartamento("Calle 80 #30-12 Apto 502", 95, 350_000_000, "Venta", dueno, 3);
        Apartamento apto2 = new Apartamento("Calle 45 #10-05 Apto 101", 60, 1_900_000, "Arriendo", dueno, 2);

        intentar(() -> {
            // Al registrarlas, el gestor también las agrega a la lista de su propietario.
            gestorInmuebles.agregarPropiedad(apto);
            gestorInmuebles.agregarPropiedad(local);
            gestorInmuebles.agregarPropiedad(aptoVenta);
            gestorInmuebles.agregarPropiedad(apto2);
            System.out.println("OK: propiedades registradas = " + Propiedad.getTotalPropiedades());
            System.out.println("Depósito apto  = " + apto.calcularDepositoGarantia());
            System.out.println("Depósito local = " + local.calcularDepositoGarantia() + " (2 x valor + IVA)");
        });
        intentar(() -> gestorInmuebles.agregarPropiedad(
                new Apartamento("calle 100  #15-20 apto 301", 60, 1_800_000, "Arriendo", dueno, 2))); // dirección repetida
        intentar(() -> new Apartamento("Calle 1 #1-1", -5, 1_000_000, "Arriendo", dueno, 2));       // área inválida
        intentar(() -> new LocalComercial("Calle 2 #2-2", 30, 1_000_000, "Alquiler", dueno));       // modo inválido
        intentar(() -> new LocalComercial("Calle 3 #3-3", 30, 1_000_000, "Arriendo", null));        // sin propietario
        // Las propiedades rechazadas no quedan en la lista del propietario ni en el contador.
        System.out.println("Propiedades de " + dueno.getNombre() + " = " + dueno.getPropiedades().size()
                + "; total registradas = " + Propiedad.getTotalPropiedades());

        // ---------- RF-02: registrar arrendatarios ----------
        titulo("RF-02: registrar arrendatarios");
        Arrendatario ana = new Arrendatario("52111222", "Ana Gómez", "ana@correo.com");
        Arrendatario luis = new Arrendatario("79333444", "Luis Pérez", "3109876543");
        Comprador marta = new Comprador("41555666", "Marta Díaz", "marta@correo.com", true);
        intentar(() -> {
            gestorClientes.agregarCliente(ana);
            gestorClientes.agregarCliente(luis);
            gestorClientes.agregarCliente(marta);
            System.out.println("OK: " + ana.describir() + "; clientes registrados = "
                    + gestorClientes.getClientes().size());
        });
        intentar(() -> gestorClientes.agregarCliente(
                new Arrendatario(" 52111222 ", "Otra Persona", "x@y.com")));          // documento repetido
        intentar(() -> new Arrendatario("", "Sin documento", "x@y.com"));         // documento vacío

        // ---------- RF-03: crear contratos ----------
        titulo("RF-03: crear contratos");
        LocalDate hoy = LocalDate.of(2026, 11, 21);
        // El gestor es el único punto para crear contratos: verifica que la propiedad esté en el inventario,
        // lo formaliza y lo guarda. Los constructores piden Arrendatario (arriendo) o Comprador (venta), así
        // que un contrato de arriendo con un comprador ni siquiera compila.
        ContratoArriendo c1 = new ContratoArriendo(apto, ana, hoy, hoy.plusYears(1), 0.05);
        intentar(() -> {
            gestorContratos.agregarContrato(c1);
            System.out.println("OK: contrato " + c1.getNumeroContrato() + " creado; propiedad ahora '"
                    + apto.getEstado() + "'; monto final = " + c1.calcularMontoFinal());
        });
        // Propiedad ya arrendada (RF-04)
        intentar(() -> gestorContratos.agregarContrato(new ContratoArriendo(apto, luis, hoy, hoy.plusYears(1), 0.05)));
        // Propiedad que existe como objeto pero no está registrada en el inventario
        Apartamento noRegistrado = new Apartamento("Calle 9 #9-9", 40, 900_000, "Arriendo", dueno, 1);
        intentar(() -> gestorContratos.agregarContrato(
                new ContratoArriendo(noRegistrado, luis, hoy, hoy.plusYears(1), 0.05)));
        // Cliente que existe como objeto pero no está registrado en GestorClientes
        Arrendatario sinRegistrar = new Arrendatario("11111111", "Pedro Mora", "3150000000");
        intentar(() -> gestorContratos.agregarContrato(
                new ContratoArriendo(local, sinRegistrar, hoy, hoy.plusYears(1), 0.05)));
        // Liberar a mano una propiedad arrendada (saltarse RF-04) no está permitido
        intentar(() -> apto.cambiarEstado("disponible"));
        // Fechas incoherentes
        intentar(() -> new ContratoArriendo(local, luis, hoy, hoy.minusDays(3), 0.05));
        // Modo incorrecto
        intentar(() -> new ContratoArriendo(aptoVenta, luis, hoy, hoy.plusYears(1), 0.05));
        // Un cliente puede firmar varios contratos: Ana arrienda también apto2
        intentar(() -> {
            gestorContratos.agregarContrato(new ContratoArriendo(apto2, ana, hoy, hoy.plusYears(1), 0.05));
            System.out.println("OK: " + ana.getNombre() + " tiene " + ana.getContratosVigentes().size()
                    + " contratos vigentes.");
        });
        // Venta correcta
        ContratoVenta cv = new ContratoVenta(aptoVenta, marta, hoy, hoy.plusMonths(2), 70_000_000);
        intentar(() -> {
            gestorContratos.agregarContrato(cv);
            System.out.println("OK: contrato " + cv.getNumeroContrato() + " (venta); propiedad ahora '"
                    + aptoVenta.getEstado() + "'; saldo financiado = " + cv.getSaldoFinanciado());
        });
        // Un contrato que no se ha registrado en el gestor no se puede guardar
        ContratoArriendo sinCrear = new ContratoArriendo(local, luis, hoy, hoy.plusYears(1), 0.03);
        intentar(sinCrear::guardar);

        // ---------- RF-07: incremento anual del canon ----------
        titulo("RF-07: incremento anual del canon");
        ContratoArriendo c3 = new ContratoArriendo(local, luis, hoy, hoy.plusYears(3), 0.05);
        intentar(() -> {
            gestorContratos.agregarContrato(c3);
            System.out.println("Canon inicial = " + c3.getCanonActual());
            c3.aplicarIncrementoAnual(hoy.plusMonths(11));           // aún no se cumple el año
            System.out.println("A los 11 meses = " + c3.getCanonActual() + " (sin cambio)");
            c3.aplicarIncrementoAnual(hoy.plusYears(1).plusDays(1)); // primer aniversario
            System.out.println("Al año = " + c3.getCanonActual() + "; última actualización = "
                    + c3.getUltimaFechaActualizacion());
            c3.aplicarIncrementoAnual(hoy.plusYears(1).plusMonths(3)); // mismo año: no se vuelve a aplicar
            System.out.println("Al año y 3 meses = " + c3.getCanonActual() + " (sin cambio)");
            c3.aplicarIncrementoAnual(hoy.plusYears(2).plusMonths(1)); // segundo aniversario
            System.out.println("A los 2 años = " + c3.getCanonActual());
            c3.aplicarIncrementoAnual(hoy.plusYears(4));               // el aniversario 3 es el fin: no cuenta
            System.out.println("Al terminar = " + c3.getCanonActual() + " (sin cambio)");
        });
        intentar(() -> c3.aplicarIncrementoAnual(hoy.minusDays(1)));  // fecha anterior al inicio
        intentar(() -> sinCrear.aplicarIncrementoAnual(hoy.plusYears(1).plusDays(1))); // contrato sin crear

        // ---------- RF-08: historial de pagos ----------
        titulo("RF-08: historial de pagos");
        intentar(() -> {
            // Se agregan en desorden; listarPagos() los devuelve ordenados por fecha de pago.
            c3.agregarPago(new Pago(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 10), "2027-01",
                    4_000_000, "pagado"));                                              // pagado con atraso
            c3.agregarPago(new Pago(3, LocalDate.of(2027, 2, 5), null, "2027-02", 0, "pendiente"));
            c3.agregarPago(new Pago(1, LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 3), "2026-12",
                    4_000_000, "pagado"));
            for (Pago p : c3.listarPagos()) {
                System.out.println("Pago " + p.getIdPago() + " | periodo " + p.getPeriodoCubierto()
                        + " | límite " + p.getFechaLimite()
                        + " | pagado " + (p.getFechaPago() == null ? "-" : p.getFechaPago())
                        + " | " + p.getEstadoPago());
            }
        });
        intentar(() -> c3.agregarPago(new Pago(1, LocalDate.of(2027, 3, 5), null, "2027-03", 0, "pendiente"))); // id repetido
        intentar(() -> sinCrear.agregarPago(new Pago(1, LocalDate.of(2026, 12, 5), null, "2026-12", 0, "pendiente")));

        // ---------- RF-12: fechas de seguimiento ----------
        titulo("RF-12: fechas de seguimiento");
        Interesado sofia = new Interesado("Sofía Ruiz", "3125550000");
        intentar(() -> {
            // Se registran en desorden y con una fecha ya pasada, que no debe salir como pendiente.
            sofia.registrarSeguimiento(hoy.minusDays(5));
            sofia.registrarSeguimiento(hoy.plusDays(10));
            sofia.registrarSeguimiento(hoy);
            sofia.registrarSeguimiento(hoy.plusDays(2));
            System.out.println("Citas pendientes de " + sofia.getNombre() + ":");
            for (LocalDate cita : sofia.consultarSeguimientos(hoy)) {
                System.out.println("  - " + cita);
            }
            // El mismo comportamiento lo hereda cualquier Cliente, porque vive en Persona.
            luis.registrarSeguimiento(hoy.plusDays(7));
            System.out.println("Citas pendientes de " + luis.getNombre() + ": " + luis.consultarSeguimientos(hoy));
        });
        intentar(() -> sofia.registrarSeguimiento(hoy.plusDays(2)));  // fecha repetida
        intentar(() -> sofia.registrarSeguimiento(null));             // fecha nula

        // ---------- Persistencia (RNF-01) ----------
        titulo("Persistencia");
        intentar(() -> {
            sistema.guardarTodo(); // cada gestor guarda lo suyo (contratos de arriendo guardan también sus pagos)
            for (String archivo : new String[]{"propietarios.txt", "propiedades.txt", "clientes.txt",
                    "seguimientos.txt", "contratos.txt", "pagos.txt"}) {
                System.out.println("datos/" + archivo + ": " + contarLineas(archivo) + " registros");
            }
            // cargar() vuelve a leer un objeto que ya existe; recrear el sistema completo es el paso 5.
            apto.cargar();
            ana.cargar();
            c3.cargar();
            System.out.println("OK: guardado y vuelto a leer (propiedad, cliente y contrato).");
        });
        System.out.println("Contratos en el gestor: " + gestorContratos.getContratos().size());
    }

    private static long contarLineas(String archivo) {
        try {
            return java.nio.file.Files.readAllLines(java.nio.file.Paths.get("datos", archivo)).size();
        } catch (java.io.IOException e) {
            return 0; // el archivo no existe: no se guardó nada de ese tipo
        }
    }

    private static void titulo(String t) {
        System.out.println("\n=== " + t + " ===");
    }

    private static void intentar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            System.out.println("ERROR (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }
}
