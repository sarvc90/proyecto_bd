package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.DetalleVenta;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad DetalleVenta.
 * Maneja operaciones CRUD sobre los detalles de una venta (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.1
 */
public class DetalleVentaDAO {
    // ==================== ATRIBUTOS ====================
    private List<DetalleVenta> detalles;

    // ==================== CONSTRUCTOR ====================

    public DetalleVentaDAO() {
        this.detalles = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    public boolean agregar(DetalleVenta detalle) {
        if (detalle != null && detalle.validarDetalle()) {
            return detalles.add(detalle);
        }
        return false;
    }

    public List<DetalleVenta> obtenerTodos() {
        return new ArrayList<>(detalles);
    }

    public DetalleVenta obtenerPorId(int idDetalle) {
        return detalles.stream()
                .filter(d -> d.getIdDetalle() == idDetalle)
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(DetalleVenta detalle) {
        for (int i = 0; i < detalles.size(); i++) {
            if (detalles.get(i).getIdDetalle() == detalle.getIdDetalle()) {
                detalles.set(i, detalle);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int idDetalle) {
        return detalles.removeIf(d -> d.getIdDetalle() == idDetalle);
    }

    // ==================== MÉTODOS EXTRA ====================

    public List<DetalleVenta> obtenerPorVenta(int idVenta) {
        List<DetalleVenta> resultado = new ArrayList<>();
        for (DetalleVenta d : detalles) {
            if (d.getIdVenta() == idVenta) {
                resultado.add(d);
            }
        }
        return resultado;
    }

    public double calcularSubtotalVenta(int idVenta) {
        double subtotal = 0;
        for (DetalleVenta d : detalles) {
            if (d.getIdVenta() == idVenta) {
                subtotal += d.getSubtotal();
            }
        }
        return subtotal;
    }

    // ==================== DATOS DE PRUEBA ====================

    private void cargarDatosPrueba() {
        // Usamos el constructor con (idProducto, cantidad, precioUnitario, iva)
        DetalleVenta d1 = new DetalleVenta(1, 2, 500, 0.12); // producto 1, 2 unidades, $500 c/u
        d1.setIdDetalle(1);
        d1.setIdVenta(1);
        d1.setNombreProducto("Refrigerador LG");

        DetalleVenta d2 = new DetalleVenta(2, 1, 300, 0.12); // producto 2, 1 unidad, $300 c/u
        d2.setIdDetalle(2);
        d2.setIdVenta(1);
        d2.setNombreProducto("Televisor Samsung");

        DetalleVenta d3 = new DetalleVenta(3, 1, 800, 0.12); // producto 3, 1 unidad, $800 c/u
        d3.setIdDetalle(3);
        d3.setIdVenta(2);
        d3.setNombreProducto("Lavadora Whirlpool");

        detalles.add(d1);
        detalles.add(d2);
        detalles.add(d3);
    }
}
