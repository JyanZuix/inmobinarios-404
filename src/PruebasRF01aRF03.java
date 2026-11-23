import java.time.LocalDate;

/**
 * Pruebas de RF-01, RF-02 y RF-03: revisa cada criterio de aceptación del README y muestra PASÓ o FALLÓ.
 * No necesita librerías: se ejecuta como cualquier Main (clic derecho → Run 'PruebasRF01aRF03.main()').
 *
 * Usa un sistema nuevo y no guarda nada, así que no toca los archivos de la carpeta datos/.
 */
public class PruebasRF01aRF03 {

    private static int pasaron = 0;
    private static int fallaron = 0;

    public static void main(String[] args) {
        probarRF01();
        probarRF02();
        probarRF03();

        System.out.println("\n=== Resultado: " + pasaron + " pasaron, " + fallaron + " fallaron ===");
        if (fallaron > 0) {
            System.out.println("Revise las líneas marcadas con FALLÓ.");
        }
    }

    // ===================== RF-01: registrar una propiedad =====================
    private static void probarRF01() {
        titulo("RF-01: registrar una propiedad");
        GestorInmuebles gestor = new SistemaInmobiliario().getGestorInmuebles();
        Propietario dueno = new Propietario("1.020.304", "Dueño Prueba", "3001234567");
        int totalAntes = Propiedad.getTotalPropiedades();

        Apartamento apto = new Apartamento("Calle Prueba 1 #10-20", 70, 2_000_000, "arriendo", dueno, 2);
        gestor.agregarPropiedad(apto);
        verificar("Una propiedad válida queda en el inventario", gestor.buscarPropiedad("Calle Prueba 1 #10-20") == apto);
        verificar("La propiedad queda en la lista de su propietario", dueno.getPropiedades().contains(apto));
        verificar("El contador estático sube en 1", Propiedad.getTotalPropiedades() == totalAntes + 1);
        verificar("Una propiedad nueva nace disponible", Propiedad.DISPONIBLE.equals(apto.getEstado()));
        verificar("El modo se escribe con el formato oficial ('arriendo' → 'Arriendo')",
                Propiedad.MODO_ARRIENDO.equals(apto.getModo()));
        verificar("El documento del propietario se guarda sin puntos", "1020304".equals(dueno.getDocumento()));

        // No se permiten dos propiedades con la misma dirección
        debeFallar("Dirección repetida, en minúsculas",
                () -> gestor.agregarPropiedad(new Apartamento("calle prueba 1 #10-20", 50, 1_000_000, "Arriendo", dueno, 1)));
        debeFallar("Dirección repetida, con espacios de más alrededor de # y -",
                () -> gestor.agregarPropiedad(new Apartamento("Calle  Prueba 1 # 10 - 20", 50, 1_000_000, "Arriendo", dueno, 1)));
        verificar("Buscar con otro formato encuentra la misma propiedad", gestor.buscarPropiedad("CALLE PRUEBA 1 # 10-20") == apto);

        // El sistema exige área y valor base mayores a cero, un modo válido y un propietario
        debeFallar("Área en cero", () -> new Apartamento("Calle Prueba 2 #1-1", 0, 1_000_000, "Arriendo", dueno, 1));
        debeFallar("Área negativa", () -> new Apartamento("Calle Prueba 2 #1-1", -10, 1_000_000, "Arriendo", dueno, 1));
        debeFallar("Valor base en cero", () -> new LocalComercial("Calle Prueba 2 #1-1", 30, 0, "Arriendo", dueno));
        debeFallar("Dirección vacía", () -> new LocalComercial("   ", 30, 1_000_000, "Arriendo", dueno));
        debeFallar("Modo inválido ('Alquiler')", () -> new LocalComercial("Calle Prueba 2 #1-1", 30, 1_000_000, "Alquiler", dueno));
        debeFallar("Sin propietario", () -> new LocalComercial("Calle Prueba 2 #1-1", 30, 1_000_000, "Arriendo", null));
        debeFallar("Apartamento sin habitaciones", () -> new Apartamento("Calle Prueba 2 #1-1", 30, 1_000_000, "Arriendo", dueno, 0));
        verificar("Las propiedades rechazadas no cuentan", Propiedad.getTotalPropiedades() == totalAntes + 1);

        // Propietarios
        debeFallar("Otro propietario con el mismo documento",
                () -> gestor.agregarPropiedad(new LocalComercial("Calle Prueba 3 #1-1", 30, 1_000_000, "Arriendo",
                        new Propietario("1020304", "Otro Dueño", "3009876543"))));
        debeFallar("Documento del propietario con letras", () -> new Propietario("ABC123", "Dueño", "3001234567"));
        debeFallar("Contacto del propietario inválido", () -> new Propietario("1020304", "Dueño", "no tengo"));

        // Quitar una propiedad
        LocalComercial errada = new LocalComercial("Calle Prueba 4 #1-1", 30, 1_000_000, "Arriendo", dueno);
        gestor.agregarPropiedad(errada);
        gestor.quitarPropiedad("calle prueba 4 # 1-1");
        verificar("Quitar: la propiedad sale del inventario", gestor.buscarPropiedad("Calle Prueba 4 #1-1") == null);
        verificar("Quitar: la propiedad sale de la lista del propietario", !dueno.getPropiedades().contains(errada));
        verificar("Quitar: el contador vuelve a su valor", Propiedad.getTotalPropiedades() == totalAntes + 1);
        debeFallar("Quitar una dirección que no está registrada", () -> gestor.quitarPropiedad("Calle Prueba 99 #9-9"));

        // Depósito de garantía (cada tipo con su regla)
        LocalComercial local = new LocalComercial("Calle Prueba 5 #1-1", 30, 1_000_000, "Arriendo", dueno);
        verificar("Depósito del apartamento = 1 canon", apto.calcularDepositoGarantia() == 2_000_000);
        verificar("Depósito del local = 2 cánones + IVA", Math.abs(local.calcularDepositoGarantia() - 2_380_000) < 0.01);
    }

    // ===================== RF-02: registrar un arrendatario =====================
    private static void probarRF02() {
        titulo("RF-02: registrar un arrendatario");
        GestorClientes gestor = new SistemaInmobiliario().getGestorClientes();

        Arrendatario ana = new Arrendatario("52.111.222", "Ana Prueba", "ana@correo.com");
        gestor.agregarCliente(ana);
        verificar("Un arrendatario válido queda registrado", gestor.getClientes().contains(ana));
        verificar("El documento se guarda sin puntos", "52111222".equals(ana.getDocumento()));
        verificar("Se encuentra buscando con o sin puntos",
                gestor.buscarArrendatario("52111222") == ana && gestor.buscarArrendatario("52.111.222") == ana);

        // No se permiten dos arrendatarios con el mismo documento
        debeFallar("Documento repetido igual", () -> gestor.agregarCliente(new Arrendatario("52111222", "Otra", "3001112233")));
        debeFallar("Documento repetido con espacios", () -> gestor.agregarCliente(new Arrendatario(" 52111222 ", "Otra", "3001112233")));
        debeFallar("Documento repetido con puntos", () -> gestor.agregarCliente(new Arrendatario("52.111.222", "Otra", "3001112233")));
        Comprador mismaPersona = new Comprador("52111222", "Ana Prueba", "ana@correo.com", false);
        gestor.agregarCliente(mismaPersona);
        verificar("La misma persona sí puede ser también comprador", gestor.getClientes().contains(mismaPersona));

        // Datos obligatorios y con formato válido
        debeFallar("Documento vacío", () -> new Arrendatario("", "Sin documento", "3001112233"));
        debeFallar("Documento con letras", () -> new Arrendatario("52111ABC", "Letras", "3001112233"));
        debeFallar("Documento demasiado corto", () -> new Arrendatario("123", "Corto", "3001112233"));
        debeFallar("Nombre vacío", () -> new Arrendatario("80123456", " ", "3001112233"));
        debeFallar("Contacto vacío", () -> new Arrendatario("80123456", "Sin contacto", ""));
        debeFallar("Contacto que no es teléfono ni correo", () -> new Arrendatario("80123456", "Malo", "asdf"));
        debeFallar("Teléfono con muy pocos dígitos", () -> new Arrendatario("80123456", "Malo", "12345"));
        verificar("Acepta un celular", new Arrendatario("80123456", "Cel", "310 987 6543").getContacto() != null);
        verificar("Acepta un teléfono con +57", new Arrendatario("80123457", "Fijo", "+57 601 2345678").getContacto() != null);
        verificar("Acepta un correo", new Arrendatario("80123458", "Correo", "luis.perez@unisabana.edu.co").getContacto() != null);
    }

    // ===================== RF-03: crear un contrato =====================
    private static void probarRF03() {
        titulo("RF-03: crear un contrato");
        SistemaInmobiliario sistema = new SistemaInmobiliario();
        GestorInmuebles inmuebles = sistema.getGestorInmuebles();
        GestorClientes clientes = sistema.getGestorClientes();
        GestorContratos contratos = sistema.getGestorContratos();
        LocalDate hoy = LocalDate.of(2026, 11, 23);

        Propietario dueno = new Propietario("9988776", "Dueño Contratos", "3001234567");
        Apartamento apto = new Apartamento("Calle Contrato 1 #1-1", 70, 2_000_000, "Arriendo", dueno, 2);
        Apartamento otroApto = new Apartamento("Calle Contrato 2 #2-2", 70, 1_500_000, "Arriendo", dueno, 2);
        Apartamento enVenta = new Apartamento("Calle Contrato 3 #3-3", 90, 300_000_000, "Venta", dueno, 3);
        inmuebles.agregarPropiedad(apto);
        inmuebles.agregarPropiedad(otroApto);
        inmuebles.agregarPropiedad(enVenta);
        Arrendatario luis = new Arrendatario("79333444", "Luis Prueba", "3109876543");
        Arrendatario sofia = new Arrendatario("41222333", "Sofía Prueba", "3124445566");
        Comprador marta = new Comprador("41555666", "Marta Prueba", "marta@correo.com", true);
        clientes.agregarCliente(luis);
        clientes.agregarCliente(sofia);
        clientes.agregarCliente(marta);

        // El contrato se crea si la propiedad existe, está disponible y el arrendatario existe
        ContratoArriendo c1 = new ContratoArriendo(apto, luis, hoy, hoy.plusYears(1), 0.05);
        contratos.agregarContrato(c1);
        verificar("El contrato queda activo", Contrato.ACTIVO.equals(c1.getEstado()));
        verificar("El contrato recibe un número", c1.getNumeroContrato() > 0);
        verificar("La propiedad pasa a 'arrendada'", Propiedad.ARRENDADA.equals(apto.getEstado()));
        verificar("La propiedad conoce su contrato actual", apto.getContratoActual() == c1);
        verificar("El contrato queda en el historial del arrendatario", luis.getHistorial().contains(c1));
        verificar("El gestor guarda el contrato", contratos.getContratos().contains(c1));

        // ... y si no, se rechaza
        debeFallar("Propiedad ya arrendada (RF-04)",
                () -> contratos.agregarContrato(new ContratoArriendo(apto, sofia, hoy, hoy.plusYears(1), 0.05)));
        Apartamento fuera = new Apartamento("Calle Contrato 9 #9-9", 40, 900_000, "Arriendo", dueno, 1);
        debeFallar("Propiedad que no está en el inventario",
                () -> contratos.agregarContrato(new ContratoArriendo(fuera, sofia, hoy, hoy.plusYears(1), 0.05)));
        Arrendatario sinRegistrar = new Arrendatario("11111111", "Sin Registrar", "3150000000");
        debeFallar("Arrendatario que no está registrado",
                () -> contratos.agregarContrato(new ContratoArriendo(otroApto, sinRegistrar, hoy, hoy.plusYears(1), 0.05)));
        debeFallar("Propiedad nula", () -> new ContratoArriendo(null, sofia, hoy, hoy.plusYears(1), 0.05));
        debeFallar("Arrendatario nulo", () -> new ContratoArriendo(otroApto, null, hoy, hoy.plusYears(1), 0.05));
        debeFallar("Fecha de fin antes del inicio", () -> new ContratoArriendo(otroApto, sofia, hoy, hoy.minusDays(1), 0.05));
        debeFallar("Arriendo de menos de un mes", () -> new ContratoArriendo(otroApto, sofia, hoy, hoy.plusDays(29), 0.05));
        debeFallar("Porcentaje de incremento negativo", () -> new ContratoArriendo(otroApto, sofia, hoy, hoy.plusYears(1), -0.05));
        debeFallar("Arriendo sobre una propiedad en modo Venta",
                () -> new ContratoArriendo(enVenta, sofia, hoy, hoy.plusYears(1), 0.05));
        debeFallar("Quitar del inventario una propiedad con contrato", () -> inmuebles.quitarPropiedad(apto.getDireccion()));

        otroApto.cambiarEstado(Propiedad.EN_CAPTACION);
        debeFallar("Propiedad 'en captación'",
                () -> contratos.agregarContrato(new ContratoArriendo(otroApto, sofia, hoy, hoy.plusYears(1), 0.05)));
        otroApto.cambiarEstado(Propiedad.DISPONIBLE);
        ContratoArriendo justoUnMes = new ContratoArriendo(otroApto, sofia, hoy, hoy.plusMonths(1), 0.05);
        contratos.agregarContrato(justoUnMes);
        verificar("Un arriendo de exactamente un mes sí se acepta", Contrato.ACTIVO.equals(justoUnMes.getEstado()));
        verificar("Los números de contrato no se repiten", justoUnMes.getNumeroContrato() != c1.getNumeroContrato());

        // Contrato de venta
        debeFallar("Cuota inicial mayor que el precio",
                () -> new ContratoVenta(enVenta, marta, hoy, hoy.plusMonths(2), 400_000_000));
        ContratoVenta venta = new ContratoVenta(enVenta, marta, hoy, hoy.plusMonths(2), 60_000_000);
        contratos.agregarContrato(venta);
        verificar("Venta: la propiedad pasa a 'finalizado'", Propiedad.FINALIZADO.equals(enVenta.getEstado()));
        verificar("Venta: saldo financiado = precio - cuota inicial", venta.getSaldoFinanciado() == 240_000_000);
        debeFallar("Registrar dos veces el mismo contrato", () -> contratos.agregarContrato(c1));
    }

    // ===================== Auxiliares =====================

    private static void titulo(String texto) {
        System.out.println("\n=== " + texto + " ===");
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            pasaron++;
            System.out.println("PASÓ  " + descripcion);
        } else {
            fallaron++;
            System.out.println("FALLÓ " + descripcion);
        }
    }

    // La acción debe lanzar una excepción: si la lanza, el sistema rechazó bien el dato y la prueba pasa.
    private static void debeFallar(String descripcion, Runnable accion) {
        try {
            accion.run();
            fallaron++;
            System.out.println("FALLÓ " + descripcion + " (el sistema lo aceptó y debía rechazarlo)");
        } catch (RuntimeException e) {
            pasaron++;
            System.out.println("PASÓ  " + descripcion + " → " + e.getMessage());
        }
    }
}
