package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controlador de la lógica de negocio para las ventas.
 * Orquesta la interacción entre Venta, Producto, Cliente e Inventario.
 *
 * @author Sistema
 * @version 1.0
 */
public class VentaController {
    private VentaDAO ventaDAO = new VentaDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private CreditoDAO creditoDAO = new CreditoDAO();
    private DetalleVentaDAO detalleDAO = new DetalleVentaDAO();

    /**
     * Realizar una venta
     * @param idCliente cliente que compra
     * @param idUsuario vendedor que registra
     * @param detalles lista de productos con cantidades
     * @param esCredito true si es a crédito, false si es contado
     * @param cuotaInicial monto de cuota inicial si es crédito
     * @param plazoMeses número de meses si es crédito
     */
    public boolean realizarVenta(int idCliente, int idUsuario,
                                 List<DetalleVenta> detalles,
                                 boolean esCredito, double cuotaInicial, int plazoMeses) {
        // Validar cliente
        if (clienteDAO.obtenerPorId(idCliente) == null) {
            System.out.println("⚠ Cliente no encontrado");
            return false;
        }

        // Validar productos y stock
        for (DetalleVenta d : detalles) {
            Producto p = productoDAO.obtenerPorId(d.getIdProducto());
            if (p == null) {
                System.out.println("⚠ Producto no encontrado: " + d.getIdProducto());
                return false;
            }
            if (p.getStockActual() < d.getCantidad()) {
                System.out.println("⚠ Stock insuficiente para: " + p.getNombre());
                return false;
            }
        }

        // Crear la venta
        Venta venta = new Venta();
        venta.setIdVenta(generarId());
        venta.setCodigo("V-" + venta.getIdVenta());
        venta.setIdCliente(idCliente);
        venta.setIdUsuario(idUsuario);
        venta.setFechaVenta(new Date());
        venta.setEsCredito(esCredito);
        venta.setDetalles(new ArrayList<>(detalles));

        // Calcular totales
        venta.calcularTotales();

        // Guardar en DAO
        if (!ventaDAO.agregar(venta)) {
            return false;
        }

        // Actualizar stock
        for (DetalleVenta d : detalles) {
            Producto p = productoDAO.obtenerPorId(d.getIdProducto());
            p.setStockActual(p.getStockActual() - d.getCantidad());
            productoDAO.actualizar(p);
            d.setIdVenta(venta.getIdVenta());
            detalleDAO.agregar(d);
        }

        // Si es a crédito → generar crédito
        if (esCredito) {
            Credito credito = new Credito(
                    venta.getIdVenta(),
                    idCliente,
                    venta.getTotal() - cuotaInicial,
                    cuotaInicial,
                    plazoMeses,
                    0.05
            );
            credito.setIdCredito(generarId());
            credito.generarCuotas();
            creditoDAO.agregar(credito);
        }

        System.out.println("✅ Venta registrada con éxito: " + venta.getResumen());
        return true;
    }

    /** Obtener todas las ventas */
    public List<Venta> obtenerVentas() {
        return ventaDAO.obtenerTodas();
    }

    /** Obtener ventas de un cliente */
    public List<Venta> obtenerVentasPorCliente(int idCliente) {
        return ventaDAO.obtenerPorCliente(idCliente);
    }

    /** Anular una venta */
    public boolean anularVenta(int idVenta) {
        Venta venta = ventaDAO.obtenerPorId(idVenta);
        if (venta == null) return false;

        venta.setEstado("ANULADA");
        ventaDAO.actualizar(venta);
        System.out.println("⚠ Venta " + venta.getCodigo() + " anulada");
        return true;
    }

    // ==================== UTILIDADES ====================

    private int generarId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }
}
