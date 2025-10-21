package com.taller.proyecto_bd;

import com.taller.proyecto_bd.controllers.*;
import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;
import com.taller.proyecto_bd.services.*;
import com.taller.proyecto_bd.utils.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase Main para probar todas las funcionalidades del Backend
 * Sistema de Gestión de Electrodomésticos
 *
 * @author Sistema
 * @version 1.10 - 44 pruebas completas; IDs sincronizados para tests que crean datos
 */
public class Main {

    // Colores para consola
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private static int testsPasados = 0;
    private static int testsFallidos = 0;

    public static void main(String[] args) {
        System.out.println(CYAN + "╔════════════════════════════════════════════════════════════╗");
        System.out.println("║    SISTEMA DE GESTIÓN DE ELECTRODOMÉSTICOS - BACKEND      ║");
        System.out.println("║                   PRUEBAS COMPLETAS                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();

        // Ejecutar todas las pruebas
        //probarModelos();
       // probarDAOs();
        //probarControladores();
        //probarServicios();
        //probarUtilidades();
        //probarFlujoCompleto();

        ConexionBD.obtenerConexion();

        // Resumen final
        //mostrarResumen();
    }

    // ==================== PRUEBAS DE MODELOS ====================

    private static void probarModelos() {
        imprimirSeccion("PRUEBAS DE MODELOS");

        // 1. Producto
        probarTest("Crear Producto", () -> {
            Producto p = new Producto("P001", "Refrigerador", "LG", "X200",
                    1, 1200, 1500, 10, 2);
            return p.getNombre().equals("Refrigerador") && p.hayStock();
        });

        probarTest("Validaciones de Producto", () -> {
            Producto p = new Producto("P002", "TV", "Samsung", "Q60",
                    1, 800, 1200, 5, 1);
            return p.validarDatosObligatorios() && p.validarStock();
        });

        probarTest("Cálculo de utilidad de Producto", () -> {
            Producto p = new Producto("P003", "Lavadora", "Whirlpool", "W500",
                    1, 600, 900, 8, 2);
            return p.getUtilidad() == 300 && p.getMargenUtilidad() == 50.0;
        });

        // 2. Cliente
        probarTest("Crear Cliente", () -> {
            Cliente c = new Cliente("1234567890", "Juan", "Pérez",
                    "Calle 123", "3001234567", "juan@mail.com");
            return c.validarDatosObligatorios() && c.getNombreCompleto().equals("Juan Pérez");
        });

        probarTest("Validar crédito disponible", () -> {
            Cliente c = new Cliente("1098765432", "María", "López",
                    "Carrera 45", "3109876543", "maria@mail.com");
            c.setLimiteCredito(5000);
            c.setSaldoPendiente(2000);
            return c.tieneCreditoDisponible() && c.getCreditoDisponible() == 3000;
        });

        // 3. Usuario
        probarTest("Crear Usuario con encriptación", () -> {
            Usuario u = new Usuario("Admin Test", "admin_test", "password123", "ADMIN");
            return u.getUsername().equals("admin_test") && !u.getPassword().equals("password123");
        });

        probarTest("Validar rol de Usuario", () -> {
            Usuario u = new Usuario("Vendedor Test", "vendedor_test", "pass456", "VENDEDOR");
            return u.tieneRol("VENDEDOR") && !u.tieneRol("ADMIN");
        });

        // 4. Venta
        probarTest("Crear Venta", () -> {
            Venta v = new Venta("V-2025-001", 1, 1, false);
            v.setTotal(1500);
            return v.getCodigo().equals("V-2025-001") && !v.isEsCredito();
        });

        // 5. Crédito
        probarTest("Generar Crédito con cuotas", () -> {
            Credito c = new Credito(1, 1, 1000, 200, 6, 0.05);
            c.generarCuotas();
            return c.getCuotas().size() == 6 && c.getSaldoPendiente() == 1000;
        });

        // 6. Inventario
        probarTest("Control de stock en Inventario", () -> {
            Inventario inv = new Inventario(1, 15, 5, 50);
            inv.registrarSalida(10);
            return inv.getCantidadActual() == 5 && inv.necesitaReposicion();
        });

        System.out.println();
    }

    // ==================== PRUEBAS DE DAOs ====================

    private static void probarDAOs() {
        imprimirSeccion("PRUEBAS DE DAOs (ACCESO A DATOS)");

        // 1. ProductoDAO
        probarTest("ProductoDAO - Agregar y consultar", () -> {
            ProductoDAO dao = ProductoDAO.getInstance();
            int cantidadInicial = dao.obtenerTodos().size();

            Producto p = new Producto("TEST001", "Producto Test", "Marca", "Modelo",
                    1, 100, 150, 5, 2);
            dao.agregar(p);

            return dao.obtenerTodos().size() == cantidadInicial + 1;
        });

        // 2. ClienteDAO
        probarTest("ClienteDAO - Buscar por cédula", () -> {
            ClienteDAO dao = ClienteDAO.getInstance();
            Cliente c = dao.obtenerPorCedula("1234567890");
            return c != null && c.getNombre().equals("Carlos");
        });

        // 3. UsuarioDAO
        probarTest("UsuarioDAO - Login exitoso", () -> {
            UsuarioDAO dao = UsuarioDAO.getInstance();
            Usuario u = dao.login("admin", "1234");
            return u != null && u.getUsername().equals("admin");
        });

        probarTest("UsuarioDAO - Login fallido", () -> {
            UsuarioDAO dao = UsuarioDAO.getInstance();
            Usuario u = dao.login("admin", "wrongpassword");
            return u == null;
        });

        // 4. VentaDAO
        probarTest("VentaDAO - Obtener por cliente", () -> {
            VentaDAO dao = VentaDAO.getInstance();
            List<Venta> ventas = dao.obtenerPorCliente(1);
            return ventas != null;
        });

        // 5. CreditoDAO
        probarTest("CreditoDAO - Obtener morosos", () -> {
            CreditoDAO dao = CreditoDAO.getInstance();
            List<Credito> morosos = dao.obtenerMorosos();
            return morosos != null;
        });

        // 6. InventarioDAO
        probarTest("InventarioDAO - Productos bajo stock", () -> {
            InventarioDAO dao = InventarioDAO.getInstance();
            List<Inventario> bajoStock = dao.obtenerBajoStock();
            return bajoStock != null;
        });

        // 7. AuditoriaDAO
        probarTest("AuditoriaDAO - Registrar acción", () -> {
            AuditoriaDAO dao = AuditoriaDAO.getInstance();
            Auditoria a = new Auditoria(1, "TEST", "Test", "Prueba de auditoría", "127.0.0.1");
            return dao.agregar(a);
        });

        System.out.println();
    }

    // ==================== PRUEBAS DE CONTROLADORES ====================

    private static void probarControladores() {
        imprimirSeccion("PRUEBAS DE CONTROLADORES");

        // 1. CalculadoraController
        probarTest("CalculadoraController - Calcular IVA", () -> {
            CalculadoraController calc = new CalculadoraController();
            double iva = calc.calcularIVA(1000, 0.19);
            return iva == 190.0;
        });

        probarTest("CalculadoraController - Calcular total con IVA", () -> {
            CalculadoraController calc = new CalculadoraController();
            double total = calc.calcularTotalConIVA(1000, 0.19);
            return total == 1190.0;
        });

        probarTest("CalculadoraController - Calcular cuota", () -> {
            CalculadoraController calc = new CalculadoraController();
            double cuota = calc.calcularValorCuota(1200, 12, 0.05);
            return cuota > 0;
        });

        // 2. UsuarioController
        probarTest("UsuarioController - Login correcto", () -> {
            UsuarioController ctrl = new UsuarioController();
            Usuario u = ctrl.login("admin", "1234");
            return u != null;
        });

        probarTest("UsuarioController - Bloquear usuario", () -> {
            UsuarioController ctrl = new UsuarioController();
            UsuarioDAO dao = UsuarioDAO.getInstance();

            // Primero buscar un usuario existente
            Usuario usuarioExistente = dao.obtenerPorUsername("admin");
            if (usuarioExistente == null) {
                System.out.println("No se encontró usuario 'admin' para bloquear");
                return false;
            }

            // Intentar bloquear el usuario existente
            boolean resultado = ctrl.bloquearUsuario(usuarioExistente.getIdUsuario());

            // Reactivar el usuario para no afectar otras pruebas
            if (resultado) {
                ctrl.activarUsuario(usuarioExistente.getIdUsuario());
            }

            return resultado;
        });

        // 3. InventarioController - ahora creando producto/inventario temporales sincronizados
        probarTest("InventarioController - Verificar stock", () -> {
            ProductoDAO productoDAO = ProductoDAO.getInstance();
            InventarioDAO inventarioDAO = InventarioDAO.getInstance();

            // Crear producto temporal y obtener su ID real
            Producto productoTemp = new Producto("TEST-INVC", "Prod Ctrl", "MarcaX", "ModX",
                    1, 100, 120, 10, 2);
            productoDAO.agregar(productoTemp);
            Producto pReal = productoDAO.obtenerPorCodigo("TEST-INVC");
            if (pReal == null) return false;

            // Crear el inventario con el ID real
            Inventario invTemp = new Inventario(pReal.getIdProducto(), 10, 2, 50);
            inventarioDAO.agregar(invTemp);

            List<DetalleVenta> detalles = new ArrayList<>();
            detalles.add(new DetalleVenta(pReal.getIdProducto(), 1, pReal.getPrecioVenta(), 0.19));

            boolean ok = new InventarioController().verificarStock(detalles);

            // Limpiar
            inventarioDAO.eliminarPorProducto(pReal.getIdProducto());
            productoDAO.eliminar(pReal.getIdProducto());

            return ok;
        });

        // 4. ReporteController
        probarTest("ReporteController - Reporte de ventas totales", () -> {
            ReporteController ctrl = new ReporteController();
            double total = ctrl.generarReporteVentasTotales();
            return total >= 0; // Puede ser 0 si no hay ventas
        });

        probarTest("ReporteController - Reporte de créditos morosos", () -> {
            ReporteController ctrl = new ReporteController();
            List<Credito> morosos = ctrl.generarReporteCreditosMorosos();
            return morosos != null;
        });

        System.out.println();
    }

    // ==================== PRUEBAS DE SERVICIOS ====================

    private static void probarServicios() {
        imprimirSeccion("PRUEBAS DE SERVICIOS");

        // 1. CalculadoraService
        probarTest("CalculadoraService - Calcular subtotal", () -> {
            CalculadoraService service = new CalculadoraService();
            List<Double> precios = List.of(100.0, 200.0, 300.0);
            double subtotal = service.calcularSubtotal(precios);
            return subtotal == 600.0;
        });

        probarTest("CalculadoraService - Calcular cuota mensual", () -> {
            CalculadoraService service = new CalculadoraService();
            double cuota = service.calcularCuotaMensual(1200, 5.0, 12);
            return cuota > 0 && cuota > 100; // Cuota debe ser mayor al monto/plazo
        });

        probarTest("CalculadoraService - Calcular interés total", () -> {
            CalculadoraService service = new CalculadoraService();
            double interes = service.calcularInteresTotal(1000, 5.0, 12);
            return interes > 0;
        });

        // 2. ValidacionService
        probarTest("ValidacionService - Validar usuario activo", () -> {
            ValidacionService service = new ValidacionService();
            Usuario u = new Usuario("Test", "test", "pass", "ADMIN");
            return service.usuarioActivo(u);
        });

        probarTest("ValidacionService - Validar credenciales", () -> {
            ValidacionService service = new ValidacionService();
            return service.credencialesValidas("admin", "1234");
        });

        probarTest("ValidacionService - Validar stock suficiente", () -> {
            ValidacionService service = new ValidacionService();
            Inventario inv = new Inventario(1, 10, 2, 50);
            return service.stockSuficiente(inv, 5);
        });

        // 3. VentaService - SOLUCIÓN DEFINITIVA CON IDs CORRECTOS
        probarTest("VentaService - Crear venta de contado", () -> {
            VentaService service = new VentaService();
            ClienteDAO clienteDAO = ClienteDAO.getInstance();
            UsuarioDAO usuarioDAO = UsuarioDAO.getInstance();
            ProductoDAO productoDAO = ProductoDAO.getInstance();
            InventarioDAO inventarioDAO = InventarioDAO.getInstance();

            // Obtener datos reales existentes
            Cliente cliente = clienteDAO.obtenerPorCedula("1234567890");
            Usuario vendedor = usuarioDAO.obtenerPorUsername("admin");

            if (cliente == null || vendedor == null) {
                System.out.println("Cliente o vendedor no encontrado");
                return false;
            }

            // Crear producto temporal sin forzar ID
            System.out.println(YELLOW + "   Configurando producto para venta..." + RESET);
            Producto productoTemp = new Producto("VENTA-FINAL", "Producto Venta Final", "TestMarca", "TestModel",
                    1, 200, 300, 10, 3);
            productoDAO.agregar(productoTemp);

            // Recuperar producto con ID asignado por DAO
            Producto productoParaVenta = productoDAO.obtenerPorCodigo("VENTA-FINAL");
            if (productoParaVenta == null) {
                System.out.println("No se pudo crear producto para venta");
                return false;
            }

            // Crear inventario con el mismo idProducto
            Inventario inventarioTemp = new Inventario(productoParaVenta.getIdProducto(), 20, 5, 50);
            inventarioDAO.agregar(inventarioTemp);

            System.out.println(YELLOW + "   Producto ID: " + productoParaVenta.getIdProducto() + RESET);
            System.out.println(YELLOW + "   Inventario ID: " + productoParaVenta.getIdProducto() + RESET);

            List<DetalleVenta> detalles = new ArrayList<>();
            DetalleVenta d = new DetalleVenta(productoParaVenta.getIdProducto(), 1,
                    productoParaVenta.getPrecioVenta(), 0.19);
            detalles.add(d);

            boolean resultado = service.realizarVenta(cliente, vendedor, detalles, false, 0, 0, 0);

            // Limpiar
            inventarioDAO.eliminarPorProducto(productoParaVenta.getIdProducto());
            productoDAO.eliminar(productoParaVenta.getIdProducto());

            return resultado;
        });

        System.out.println();
    }

    // ==================== PRUEBAS DE UTILIDADES ====================

    private static void probarUtilidades() {
        imprimirSeccion("PRUEBAS DE UTILIDADES");

        // 1. Encriptacion
        probarTest("Encriptación SHA-256", () -> {
            String texto = "password123";
            String hash = Encriptacion.encriptarSHA256(texto);
            return hash != null && !hash.equals(texto) && hash.length() == 64;
        });

        // 2. Validadores
        probarTest("Validadores - Email válido", () -> {
            return Validadores.validarEmail("usuario@ejemplo.com");
        });

        probarTest("Validadores - Email inválido", () -> {
            return !Validadores.validarEmail("emailinvalido");
        });

        probarTest("Validadores - Teléfono válido", () -> {
            return Validadores.validarTelefono("3001234567");
        });

        probarTest("Validadores - Cédula válida", () -> {
            return Validadores.validarCedula("1234567890");
        });

        // 3. DateUtils
        probarTest("DateUtils - Sumar días", () -> {
            Date hoy = new Date();
            Date futuro = DateUtils.sumarDias(hoy, 30);
            return futuro.after(hoy);
        });

        probarTest("DateUtils - Formatear fecha", () -> {
            Date fecha = new Date();
            String formateada = DateUtils.formatearFecha(fecha);
            return formateada != null && formateada.contains("/");
        });

        // 4. NumberUtils
        probarTest("NumberUtils - Redondear", () -> {
            double redondeado = NumberUtils.redondear(123.456);
            return redondeado == 123.46;
        });

        probarTest("NumberUtils - Calcular porcentaje", () -> {
            double resultado = NumberUtils.calcularPorcentaje(1000, 19);
            return resultado == 190.0;
        });

        // 5. Constantes
        probarTest("Constantes - IVA definido", () -> {
            return Constantes.IVA_DEFAULT > 0;
        });

        System.out.println();
    }

    // ==================== FLUJO COMPLETO SOLUCIÓN DEFINITIVA ====================

    private static void probarFlujoCompleto() {
        imprimirSeccion("PRUEBA DE FLUJO COMPLETO DEL SISTEMA");

        System.out.println(YELLOW + "Simulando proceso completo de venta a crédito..." + RESET);

        // Variables para limpiar recursos
        Producto productoTemporal = null;
        ProductoDAO productoDAO = ProductoDAO.getInstance();
        InventarioDAO inventarioDAO = InventarioDAO.getInstance();

        try {
            // 1. Login
            System.out.println("1. Login de usuario...");
            UsuarioController usuarioCtrl = new UsuarioController();
            Usuario vendedor = usuarioCtrl.login("admin", "1234");
            if (vendedor == null) throw new Exception("Login fallido");
            System.out.println(GREEN + "   ✓ Usuario logueado: " + vendedor.getNombreCompleto() + RESET);

            // 2. Obtener cliente
            System.out.println("2. Buscando cliente...");
            ClienteDAO clienteDAO = ClienteDAO.getInstance();
            Cliente cliente = clienteDAO.obtenerPorCedula("1234567890");
            if (cliente == null) throw new Exception("Cliente no encontrado");
            System.out.println(GREEN + "   ✓ Cliente encontrado: " + cliente.getNombreCompleto() + RESET);

            // 3. CREAR PRODUCTO QUE EXISTA EN AMBOS DAOs
            System.out.println("3. Configurando producto para venta...");
            productoTemporal = new Producto("FLUJO-FINAL", "Producto Flujo Final", "MarcaTest", "ModeloTest",
                    1, 800, 1000, 20, 5);
            productoDAO.agregar(productoTemporal);

            // Obtener el producto recién creado con su ID real
            Producto productoParaVenta = productoDAO.obtenerPorCodigo("FLUJO-FINAL");
            if (productoParaVenta == null) throw new Exception("No se pudo crear producto temporal");

            // CREAR INVENTARIO con el MISMO ID
            Inventario inventarioTemp = new Inventario(productoParaVenta.getIdProducto(), 25, 5, 50);
            inventarioDAO.agregar(inventarioTemp);

            System.out.println(GREEN + "   ✓ Producto creado ID: " + productoParaVenta.getIdProducto() + RESET);
            System.out.println(GREEN + "   ✓ Inventario creado ID: " + productoParaVenta.getIdProducto() + RESET);
            System.out.println(GREEN + "   ✓ Stock configurado: " + inventarioTemp.getCantidadActual() + " unidades" + RESET);

            // 4. Preparar detalles de venta usando el ID REAL
            List<DetalleVenta> detalles = new ArrayList<>();
            DetalleVenta detalle1 = new DetalleVenta(productoParaVenta.getIdProducto(), 1,
                    productoParaVenta.getPrecioVenta(), 0.19);
            detalles.add(detalle1);

            // 5. Verificar inventario
            System.out.println("4. Verificando inventario...");
            InventarioController invCtrl = new InventarioController();
            if (!invCtrl.verificarStock(detalles)) {
                throw new Exception("Stock insuficiente para el producto ID: " + productoParaVenta.getIdProducto());
            }
            System.out.println(GREEN + "   ✓ Stock verificado correctamente" + RESET);

            // 6. Calcular totales
            System.out.println("5. Calculando totales...");
            CalculadoraService calcService = new CalculadoraService();
            List<Double> precios = List.of(productoParaVenta.getPrecioVenta());
            double subtotal = calcService.calcularSubtotal(precios);
            double iva = calcService.calcularIVA(subtotal);
            double total = calcService.calcularTotalConIVA(subtotal);
            System.out.println(GREEN + "   ✓ Subtotal: $" + subtotal + RESET);
            System.out.println(GREEN + "   ✓ IVA: $" + iva + RESET);
            System.out.println(GREEN + "   ✓ Total: $" + total + RESET);

            // 7. Realizar venta a crédito
            System.out.println("6. Procesando venta a crédito...");
            VentaService ventaService = new VentaService();
            boolean ventaExitosa = ventaService.realizarVenta(
                    cliente, vendedor, detalles, true, 300, 12, 0.05
            );

            if (!ventaExitosa) throw new Exception("Error al procesar venta");
            System.out.println(GREEN + "   ✓ Venta procesada exitosamente" + RESET);

            // 8. Generar reporte
            System.out.println("7. Generando reportes...");
            ReporteController reporteCtrl = new ReporteController();
            double ventasTotales = reporteCtrl.generarReporteVentasTotales();
            System.out.println(GREEN + "   ✓ Total de ventas en el sistema: $" + ventasTotales + RESET);

            // 9. Auditoría
            System.out.println("8. Verificando auditoría...");
            AuditoriaDAO auditoriaDAO = AuditoriaDAO.getInstance();
            List<Auditoria> auditorias = auditoriaDAO.obtenerPorUsuario(vendedor.getIdUsuario());
            System.out.println(GREEN + "   ✓ Registros de auditoría: " + auditorias.size() + RESET);

            System.out.println();
            System.out.println(GREEN + "════════════════════════════════════════════════════════" + RESET);
            System.out.println(GREEN + "  FLUJO COMPLETO EJECUTADO EXITOSAMENTE" + RESET);
            System.out.println(GREEN + "════════════════════════════════════════════════════════" + RESET);
            testsPasados++;

        } catch (Exception e) {
            System.out.println(RED + "✗ Error en flujo completo: " + e.getMessage() + RESET);
            testsFallidos++;
        } finally {
            // LIMPIAR recursos
            if (productoTemporal != null) {
                try {
                    Producto productoAEliminar = productoDAO.obtenerPorCodigo("FLUJO-FINAL");
                    if (productoAEliminar != null) {
                        inventarioDAO.eliminarPorProducto(productoAEliminar.getIdProducto());
                        productoDAO.eliminar(productoAEliminar.getIdProducto());
                        System.out.println(YELLOW + "   🧹 Producto e inventario temporal eliminados" + RESET);
                    }
                } catch (Exception e) {
                    System.out.println(YELLOW + "   ⚠ No se pudo limpiar recursos temporales: " + e.getMessage() + RESET);
                }
            }
        }

        System.out.println();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private static void imprimirSeccion(String titulo) {
        System.out.println(BLUE + "┌────────────────────────────────────────────────────────┐");
        System.out.println("│  " + titulo);
        System.out.println("└────────────────────────────────────────────────────────┘" + RESET);
    }

    private static void probarTest(String nombre, TestFunction test) {
        try {
            boolean resultado = test.ejecutar();
            if (resultado) {
                System.out.println(GREEN + "✓ " + RESET + nombre);
                testsPasados++;
            } else {
                System.out.println(RED + "✗ " + RESET + nombre + " - Retornó false");
                testsFallidos++;
            }
        } catch (Exception e) {
            System.out.println(RED + "✗ " + RESET + nombre + " - Excepción: " + e.getMessage());
            testsFallidos++;
        }
    }

    private static void mostrarResumen() {
        int total = testsPasados + testsFallidos;
        double porcentaje = (total > 0) ? (testsPasados * 100.0 / total) : 0;

        System.out.println();
        System.out.println(CYAN + "╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RESUMEN DE PRUEBAS                      ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Tests ejecutados:  %-35d║%n", total);
        System.out.println("║                                                            ║");
        System.out.printf("║  %s✓ Tests pasados:    %-30d%s║%n", GREEN, testsPasados, CYAN);
        System.out.printf("║  %s✗ Tests fallidos:   %-30d%s║%n", RED, testsFallidos, CYAN);
        System.out.println("║                                                            ║");
        System.out.printf("║  Porcentaje de éxito: %.1f%%                              ║%n", porcentaje);
        System.out.println("╠════════════════════════════════════════════════════════════╣");

        if (testsFallidos == 0) {
            System.out.println("║  " + GREEN + "RESULTADO: BACKEND APROBADO - TODO FUNCIONA BIEN" + CYAN + "       ║");
        } else {
            System.out.println("║  " + YELLOW + "RESULTADO: REVISAR TESTS FALLIDOS" + CYAN + "                     ║");
        }

        System.out.println("╚════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();

        if (testsFallidos == 0) {
            System.out.println(GREEN + "✓✓✓ FUNCIONA ✓✓✓");
        } else {
            System.out.println(YELLOW + "⚠ " + testsFallidos + " tests necesitan atención. Revisa los logs arriba." + RESET);
        }
    }

    // Interfaz funcional para tests
    @FunctionalInterface
    interface TestFunction {
        boolean ejecutar() throws Exception;
    }
}
