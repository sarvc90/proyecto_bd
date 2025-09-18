package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;
import com.taller.proyecto_bd.controllers.*;

import java.util.Date;
import java.util.List;

/**
 * Servicio para gestionar el proceso completo de una Venta.
 * Coordina DAOs, controladores y lógica de negocio asociada.
 *
 * @author Sistema
 * @version 1.0
 */
public class VentaService {
    // ==================== DEPENDENCIAS ====================
    private VentaDAO ventaDAO = new VentaDAO();
    private DetalleVentaDAO detalleVentaDAO = new DetalleVentaDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private InventarioDAO inventarioDAO = new InventarioDAO();
    private CreditoDAO creditoDAO = new CreditoDAO();
    private CuotaDAO cuotaDAO = new CuotaDAO();
    private AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    private CalculadoraController calculadora = new CalculadoraController();

    // ==================== PROCESOS PRINCIPALES ====================

    /**
     * Realiza una venta en el sistema (contado o crédito).
     */
    public boolean realizarVenta(Cliente cliente, Usuario vendedor,
                                 List<DetalleVenta> detalles, boolean esCredito,
                                 double cuotaInicial, int plazoMeses, double interes) {

        // 1. Validar stock
        for (DetalleVenta d : detalles) {
            Inventario inv = inventarioDAO.obtenerPorProducto(d.getIdProducto());
            if (inv == null || inv.getCantidadActual() < d.getCantidad()) {
                System.out.println("Stock insuficiente para producto ID: " + d.getIdProducto());
                return false;
            }
        }

        // 2. Calcular totales
        double subtotal = detalles.stream().mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad()).sum();
        double iva = calculadora.calcularIVA(subtotal, 0.19); // IVA fijo 19% (ejemplo)
        double total = calculadora.calcularTotalConIVA(subtotal, 0.19);

        // 3. Crear objeto Venta
        Venta venta = new Venta(0, "V-" + System.currentTimeMillis(), cliente.getIdCliente(),
                vendedor.getIdUsuario(), new Date(), esCredito, subtotal, iva, total,
                cuotaInicial, plazoMeses, "REGISTRADA");

        if (!ventaDAO.agregar(venta)) return false;

        // 4. Guardar detalles de venta y actualizar inventario
        for (DetalleVenta d : detalles) {
            d.setIdVenta(venta.getIdVenta());
            detalleVentaDAO.agregar(d);

            Inventario inv = inventarioDAO.obtenerPorProducto(d.getIdProducto());
            inv.registrarSalida(d.getCantidad());
            inventarioDAO.actualizar(inv);
        }

        // 5. Si es crédito -> generar crédito + cuotas
        if (esCredito) {
            Credito credito = new Credito(0, venta.getIdVenta(), total, cuotaInicial, plazoMeses, interes);
            credito.generarCuotas();

            creditoDAO.agregar(credito);
            for (Cuota c : credito.getCuotas()) {
                cuotaDAO.agregar(c);
            }
        }

        // 6. Registrar en auditoría
        auditoriaDAO.agregar(new Auditoria(vendedor.getIdUsuario(), "CREAR_VENTA",
                "Venta", "Venta registrada ID=" + venta.getIdVenta(), "127.0.0.1"));

        return true;
    }

    /**
     * Anular una venta (y su crédito si aplica).
     */
    public boolean anularVenta(int idVenta, Usuario usuario) {
        Venta venta = ventaDAO.obtenerPorId(idVenta);
        if (venta == null) return false;

        // Devolver inventario
        List<DetalleVenta> detalles = detalleVentaDAO.obtenerPorVenta(idVenta);
        for (DetalleVenta d : detalles) {
            Inventario inv = inventarioDAO.obtenerPorProducto(d.getIdProducto());
            if (inv != null) {
                inv.registrarEntrada(d.getCantidad());
                inventarioDAO.actualizar(inv);
            }
        }

        // Anular crédito asociado
        Credito credito = creditoDAO.obtenerPorVenta(idVenta);
        if (credito != null) {
            credito.setEstado("ANULADO");
            creditoDAO.actualizar(credito);
        }

        // Cambiar estado de venta
        venta.setEstado("ANULADA");
        ventaDAO.actualizar(venta);

        // Registrar auditoría
        auditoriaDAO.agregar(new Auditoria(usuario.getIdUsuario(), "ANULAR_VENTA",
                "Venta", "Venta anulada ID=" + idVenta, "127.0.0.1"));

        return true;
    }
}
