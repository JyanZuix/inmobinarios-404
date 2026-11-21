import java.time.LocalDate;

/**
 * Demostración de RF-01 (registrar propiedad), RF-02 (registrar arrendatario) y RF-03 (crear contrato).
 * Cada caso con error imprime el mensaje de la excepción, tal como lo vería quien opera el sistema.
 */
public class Main {

    public static void main(String[] args) {
        GestorInmuebles gestorInmuebles = new GestorInmuebles();
        GestorContratos gestorContratos = new GestorContratos();

        // ---------- RF-01: registrar propiedades ----------
        titulo("RF-01: registrar propiedades");
        Propietario dueno = new Propietario("100200300", "Carlos Rojas", "3001234567");
        Apartamento apto = new Apartamento("Calle 100 #15-20 Apto 301", 80, 2_500_000, "Arriendo", dueno, 3);
        LocalComercial local = new LocalComercial("Carrera 7 #45-10 Local 2", 50, 4_000_000, "Arriendo", dueno);
        Apartamento aptoVenta = new Apartamento("Calle 80 #30-12 Apto 502", 95, 350_000_000, "Venta", dueno, 3);

        intentar(() -> {
            // Al registrarlas, el gestor también las agrega a la lista de su propietario.
            gestorInmuebles.agregarPropiedad(apto);
            gestorInmuebles.agregarPropiedad(local);
            gestorInmuebles.agregarPropiedad(aptoVenta);
            System.out.println("OK: propiedades registradas = " + Propiedad.getTotalPropiedades());
            System.out.println("Depósito apto  = " + apto.calcularDepositoGarantia());
            System.out.println("Depósito local = " + local.calcularDepositoGarantia() + " (2 x valor + IVA)");
        });
        intentar(() -> gestorInmuebles.agregarPropiedad(
                new Apartamento("calle 100 #15-20 apto 301", 60, 1_800_000, "Arriendo", dueno, 2))); // dirección repetida
        intentar(() -> new Apartamento("Calle 1 #1-1", -5, 1_000_000, "Arriendo", dueno, 2));       // área inválida
        intentar(() -> new LocalComercial("Calle 2 #2-2", 30, 1_000_000, "Alquiler", dueno));       // modo inválido
        intentar(() -> new LocalComercial("Calle 3 #3-3", 30, 1_000_000, "Arriendo", null));        // sin propietario
        // Las propiedades rechazadas no quedan en la lista del propietario ni en el contador.
        System.out.println("Propiedades de " + dueno.getNombre() + " = " + dueno.getPropiedades().size()
                + "; total registradas = " + Propiedad.getTotalPropiedades());

        // ---------- RF-02: registrar arrendatarios ----------
        titulo("RF-02: registrar arrendatarios");
        Arrendatario ana = new Arrendatario("52111222", "Ana Gómez", "ana@correo.com");
        System.out.println("OK: " + ana.describir());
        intentar(() -> new Arrendatario("52111222", "Otra Persona", "x@y.com"));  // documento repetido
        intentar(() -> new Arrendatario("", "Sin documento", "x@y.com"));         // documento vacío
        Arrendatario luis = new Arrendatario("79333444", "Luis Pérez", "3109876543");
        Comprador marta = new Comprador("41555666", "Marta Díaz", "marta@correo.com", true);

        // ---------- RF-03: crear contratos ----------
        titulo("RF-03: crear contratos");
        LocalDate hoy = LocalDate.of(2026, 11, 21);
        ContratoArriendo c1 = new ContratoArriendo(apto, ana, hoy, hoy.plusYears(1), 0.05);
        intentar(() -> {
            c1.crearContrato();
            gestorContratos.agregarContrato(c1);
            System.out.println("OK: contrato " + c1.getNumeroContrato() + " creado; propiedad ahora '"
                    + apto.getEstado() + "'; monto final = " + c1.calcularMontoFinal());
        });
        // Propiedad ya arrendada
        intentar(() -> new ContratoArriendo(apto, luis, hoy, hoy.plusYears(1), 0.05).crearContrato());
        // Cliente con contrato vigente
        intentar(() -> new ContratoArriendo(local, ana, hoy, hoy.plusYears(1), 0.05).crearContrato());
        // Fechas incoherentes
        intentar(() -> new ContratoArriendo(local, luis, hoy, hoy.minusDays(3), 0.05));
        // Modo incorrecto
        intentar(() -> new ContratoArriendo(aptoVenta, luis, hoy, hoy.plusYears(1), 0.05));
        // Venta correcta
        ContratoVenta cv = new ContratoVenta(aptoVenta, marta, hoy, hoy.plusMonths(2), 70_000_000);
        intentar(() -> {
            cv.crearContrato();
            gestorContratos.agregarContrato(cv);
            System.out.println("OK: contrato " + cv.getNumeroContrato() + " (venta); propiedad ahora '"
                    + aptoVenta.getEstado() + "'; saldo financiado = " + cv.getSaldoFinanciado());
        });
        // Contrato sin crear no se puede guardar ni agregar
        ContratoArriendo sinCrear = new ContratoArriendo(local, luis, hoy, hoy.plusYears(1), 0.03);
        intentar(sinCrear::guardar);
        intentar(() -> gestorContratos.agregarContrato(sinCrear));

        // ---------- Persistencia (RNF-01) ----------
        titulo("Persistencia");
        intentar(() -> {
            dueno.guardar(); // stub: el propietario aún no persiste (AQUÍ VA)
            apto.guardar();
            local.guardar();
            aptoVenta.guardar();
            ana.guardar();
            luis.guardar();
            marta.guardar();
            c1.guardar();
            cv.guardar();
            apto.cargar();
            ana.cargar();
            c1.cargar();
            cv.cargar();
            System.out.println("OK: guardado y cargado en la carpeta datos/ (propiedades, clientes, contratos).");
        });
        System.out.println("Contratos en el gestor: " + gestorContratos.getContratos().size());
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
