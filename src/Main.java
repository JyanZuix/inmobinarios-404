import java.time.LocalDate;
import java.util.List;

/**
 * Demostración de los requisitos implementados con datos de ejemplo.
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

        // ---------- RF-09: contratos con pagos en mora ----------
        titulo("RF-09: contratos con pagos en mora");
        LocalDate diaRevision = LocalDate.of(2027, 1, 30);
        intentar(() -> {
            for (Contrato contrato : gestorContratos.filtrarContratosEnMora(diaRevision)) {
                System.out.println("Contrato " + contrato.getNumeroContrato() + " | "
                        + contrato.getPropiedad().getDireccion() + " | pago vencido desde "
                        + contrato.moraDesde(diaRevision));
            }
        });

        // ---------- RF-11: registrar interesados ----------
        titulo("RF-11: registrar interesados");
        Interesado sofia = gestorInmuebles.registrarInteresado("Sofía Ruiz", "3125550000",
                List.of(aptoVenta, apto2));
        System.out.println(sofia.getNombre() + " está asociada a " + sofia.getInmuebles().size() + " inmuebles.");
        intentar(() -> gestorInmuebles.registrarInteresado("Sin inmueble", "3125550001", List.of()));
        intentar(() -> gestorInmuebles.registrarInteresado("Sin contacto", "", List.of(apto)));
        intentar(() -> gestorInmuebles.registrarInteresado("Fuera del inventario", "3125550002",
                List.of(apto, noRegistrado)));

        // ---------- RF-12: fechas de seguimiento ----------
        titulo("RF-12: fechas de seguimiento");
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

        // ---------- RF-16: avisos de pagos en mora o por vencer ----------
        titulo("RF-16: pagos en mora o por vencer");
        intentar(() -> {
            // El sistema crea las cuotas que falten hasta dentro de 7 días y recolecta los avisos.
            sistema.recolectarNotificaciones(diaRevision);
            System.out.println("Pagos del contrato " + c3.getNumeroContrato() + " al " + diaRevision + ":");
            for (Pago p : c3.listarPagos()) {
                System.out.println("  Pago " + p.getIdPago() + " | periodo " + p.getPeriodoCubierto()
                        + " | límite " + p.getFechaLimite() + " | " + p.getEstadoPago());
            }
            System.out.println("Avisos de pagos (los pagos ya registrados no avisan):");
            imprimirAvisos(gestorContratos, "PAGO:");
            int cantidad = gestorContratos.getNotificaciones().size();
            sistema.recolectarNotificaciones(diaRevision); // recolectar otra vez el mismo día
            System.out.println("Recolectar de nuevo no duplica: " + cantidad + " -> "
                    + gestorContratos.getNotificaciones().size() + " avisos.");
        });

        // ---------- RF-17: avisos de contratos próximos a vencer ----------
        titulo("RF-17: contratos próximos a vencer");
        LocalDate otraRevision = LocalDate.of(2027, 11, 1);
        intentar(() -> {
            // Al 2027-11-01: los contratos 1 y 2 terminan el 2027-11-21 (en 20 días); la venta terminaba el
            // 2027-01-21 y sigue activa (vencida); el contrato 4 termina en 2029 y no avisa.
            sistema.recolectarNotificaciones(otraRevision);
            System.out.println("Avisos de contratos al " + otraRevision + ":");
            imprimirAvisos(gestorContratos, "CONTRATO:");
            System.out.println("¿Contrato " + c3.getNumeroContrato() + " vence pronto? " + c3.vencePronto(otraRevision, 30));
        });

        // ---------- RF-18: avisos de seguimientos de interesados ----------
        titulo("RF-18: seguimientos de interesados");
        intentar(() -> {
            // Sofía (RF-12) se interesa en dos propiedades; sus citas: 2026-11-16 (vencida), hoy, +2 y +10 días.
            sistema.recolectarNotificaciones(hoy);
            System.out.println("Avisos de interesados al " + hoy + " (las citas futuras no avisan; cada cita una vez,"
                    + " aunque Sofía esté en dos propiedades):");
            imprimirAvisos(gestorInmuebles, "INTERESADO:");
        });

        // ---------- RF-19: avisos de los clientes ----------
        titulo("RF-19: seguimientos y situación contractual de los clientes");
        intentar(() -> {
            // Al 2027-11-01: Ana tiene 2 contratos que terminan en 20 días y con cuotas sin pagar; Luis tiene una
            // cita vencida (2026-11-28) y una cuota en mora; la venta de Marta venció y sigue activa.
            // Cada cliente se visita una vez aunque tenga varios contratos.
            sistema.recolectarNotificaciones(otraRevision);
            System.out.println("Avisos de clientes al " + otraRevision + ":");
            imprimirAvisos(gestorClientes, "CLIENTE:");
            int cantidad = gestorClientes.getNotificaciones().size();
            sistema.recolectarNotificaciones(otraRevision.plusDays(1)); // otro día: la misma mora no se repite
            System.out.println("Revisar al día siguiente no repite avisos: " + cantidad + " -> "
                    + gestorClientes.getNotificaciones().size() + ".");
        });

        // ---------- RF-20: ver las notificaciones pendientes ----------
        titulo("RF-20: notificaciones pendientes");
        intentar(() -> {
            // se juntan las de los 3 gestores, sin las atendidas y ordenadas por la fecha del evnto
            List<Notificacion> pendientes = sistema.listarNotificacionesPendientes();
            System.out.println("Pendientes: " + pendientes.size() + " (se muestran las primeras 5)");
            for (int i = 0; i < pendientes.size() && i < 5; i++) {
                System.out.println("  " + pendientes.get(i));
            }
        });

        // ---------- RF-21: marcar una notificacion como atendida ----------
        titulo("RF-21: marcar una notificación como atendida");
        String[] claveAtendida = new String[1]; // para revisarla despues de recargar
        intentar(() -> {
            // se marca la primera pendiente, la mas viejaa
            Notificacion primera = sistema.listarNotificacionesPendientes().get(0);
            int antes = sistema.listarNotificacionesPendientes().size();
            sistema.marcarNotificacionAtendida(primera);
            claveAtendida[0] = primera.clave();
            System.out.println("Atendida: " + primera);
            System.out.println("Pendientes: " + antes + " -> " + sistema.listarNotificacionesPendientes().size());
            // si se recolecta otra vez el mismo evento no vuelve a salir
            sistema.recolectarNotificaciones(hoy);
            sistema.recolectarNotificaciones(otraRevision);
            System.out.println("Recolectar de nuevo no la revive: ¿sigue pendiente? "
                    + sistema.listarNotificacionesPendientes().contains(primera));
        });

        // ---------- RF-10: terminar un contrato y liberar la propiedad ----------
        titulo("RF-10: terminar un contrato y liberar la propiedad");
        intentar(() -> {
            c1.terminarContrato();
            System.out.println("Contrato " + c1.getNumeroContrato() + ": " + c1.getEstado()
                    + " | inmueble: " + apto.getEstado() + " | contrato actual: " + apto.getContratoActual());
            System.out.println("El contrato se conserva en ambos historiales: "
                    + (apto.getHistorial().contains(c1) && ana.getHistorial().contains(c1)));
            System.out.println("El contrato aparece en mora después de cerrarlo: "
                    + gestorContratos.filtrarContratosEnMora(diaRevision).contains(c1));
        });
        intentar(c1::terminarContrato); // ya finalizado

        // ---------- Persistencia (RNF-01) ----------
        titulo("Persistencia");
        intentar(() -> {
            sistema.guardarTodo(); // cada gestor guarda lo suyo (contratos de arriendo guardan también sus pagos)
            for (String archivo : new String[]{"propietarios.txt", "propiedades.txt", "clientes.txt",
                    "interesados.txt", "seguimientos.txt", "contratos.txt", "pagos.txt", "notificaciones.txt"}) {
                System.out.println("datos/" + archivo + ": " + contarLineas(archivo) + " registros");
            }
        });

        // Simula un reinicio: un sistema NUEVO, vacío, que se recrea solo desde los archivos de datos/.
        titulo("Persistencia: recargar el sistema desde archivos");
        intentar(() -> {
            SistemaInmobiliario recargado = new SistemaInmobiliario();
            recargado.cargarTodo(); // inmuebles → clientes → contratos (y sus pagos)
            System.out.println("Recuperados: " + recargado.getGestorInmuebles().getInmuebles().size() + " propiedades, "
                    + recargado.getGestorClientes().getClientes().size() + " clientes, "
                    + recargado.getGestorContratos().getContratos().size() + " contratos.");

            Propiedad localCargado = recargado.getGestorInmuebles().buscarPropiedad(local.getDireccion());
            System.out.println(localCargado.getDireccion() + ": '" + localCargado.getEstado() + "', dueño "
                    + localCargado.getPropietario().getNombre() + ", contrato actual N° "
                    + localCargado.getContratoActual().getNumeroContrato());

            ContratoArriendo c3Cargado = (ContratoArriendo) localCargado.getContratoActual();
            System.out.println("Canon tras RF-07 = " + c3Cargado.getCanonActual() + "; pagos = "
                    + c3Cargado.listarPagos().size());

            Arrendatario anaCargada = recargado.getGestorClientes().buscarArrendatario(ana.getDocumento());
            Arrendatario luisCargado = recargado.getGestorClientes().buscarArrendatario(luis.getDocumento());
            System.out.println(anaCargada.getNombre() + " tiene " + anaCargada.getContratosVigentes().size()
                    + " contratos vigentes; citas de " + luisCargado.getNombre() + ": "
                    + luisCargado.consultarSeguimientos(hoy));

            Propiedad aptoCargado = recargado.getGestorInmuebles().buscarPropiedad(apto.getDireccion());
            System.out.println("Inmueble liberado tras recargar: " + aptoCargado.getEstado());
            for (Interesado interesado : recargado.getGestorInmuebles().getInteresados()) {
                System.out.println(interesado.getNombre() + ": " + interesado.getInmuebles().size()
                        + " inmuebles y citas " + interesado.consultarSeguimientos(hoy));
            }

            // RF-21 la notificacion atendida sigue atendida despues del reinico
            recargado.recolectarNotificaciones(hoy);
            boolean sigueAtendida = true;
            for (Notificacion n : recargado.listarNotificacionesPendientes()) {
                if (n.clave().equals(claveAtendida[0])) {
                    sigueAtendida = false;
                }
            }
            System.out.println("Notificaciones pendientes tras recargar: "
                    + recargado.listarNotificacionesPendientes().size()
                    + " | la atendida sigue atendida: " + sigueAtendida);
        });
    }

    // Imprime los avisos del gestor cuyo origen empieza como se indica ("PAGO:", "CONTRATO:"...).
    private static void imprimirAvisos(Gestor gestor, String origen) {
        for (Notificacion n : gestor.getNotificaciones()) {
            if (n.getIdOrigen().startsWith(origen)) {
                System.out.println("  " + n);
            }
        }
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
