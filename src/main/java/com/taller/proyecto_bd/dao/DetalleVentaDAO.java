package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.DetalleVenta;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad DetalleVenta.
 */
public class DetalleVentaDAO {
    private final List<DetalleVenta> detalles;
    private static int idCounter = 1;
    private static DetalleVentaDAO instance;

    private DetalleVentaDAO() {
        this.detalles = new ArrayList<>();
        cargarDatosPrueba();
    }

    public static DetalleVentaDAO getInstance() {
        if (instance == null) {
            instance = new DetalleVentaDAO();
        }
        return instance;
    }

    public boolean agregar(DetalleVenta detalle) {
        if (detalle != null && detalle.validarDetalle()) {
            if (detalle.getIdDetalle() <= 0) {
                detalle.setIdDetalle(idCounter++);
            }
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
        if (detalle == null || detalle.getIdDetalle() <= 0) return false;

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
        return detalles.stream()
                .filter(d -> d.getIdVenta() == idVenta)
                .mapToDouble(DetalleVenta::getSubtotal)
                .sum();
    }

    private void cargarDatosPrueba() {
        DetalleVenta d1 = new DetalleVenta(1, 2, 500, 0.12);
        d1.setIdVenta(10001);
        d1.setNombreProducto("Refrigerador LG");
        agregar(d1);

        DetalleVenta d2 = new DetalleVenta(2, 1, 300, 0.12);
        d2.setIdVenta(10001);
        d2.setNombreProducto("Televisor Samsung");
        agregar(d2);

        DetalleVenta d3 = new DetalleVenta(3, 1, 800, 0.12);
        d3.setIdVenta(10002);
        d3.setNombreProducto("Lavadora Whirlpool");
        agregar(d3);
    }
}
