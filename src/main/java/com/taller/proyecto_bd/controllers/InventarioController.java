package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.ProductoDAO;
import com.taller.proyecto_bd.dao.InventarioDAO;
import com.taller.proyecto_bd.models.Inventario;
import com.taller.proyecto_bd.models.DetalleVenta;

import java.util.List;

/**
 * Controlador para la lógica de inventario.
 * Maneja la validación y actualización de stock en ventas y reposiciones.
 *
 * @author Sistema
 * @version 1.1
 */
public class InventarioController {
    private InventarioDAO inventarioDAO = InventarioDAO.getInstance();
    private ProductoDAO productoDAO = ProductoDAO.getInstance();

    /**
     * Verifica si hay suficiente stock para una lista de detalles de venta
     */
    public boolean verificarStock(List<DetalleVenta> detalles) {
        for (DetalleVenta d : detalles) {
            Inventario inv = inventarioDAO.obtenerPorProducto(d.getIdProducto());
            if (inv == null || inv.getCantidadActual() < d.getCantidad()) {
                return false; // No hay suficiente stock
            }
        }
        return true;
    }

    /**
     * Descuenta stock al registrar una venta
     */
    public boolean descontarStock(List<DetalleVenta> detalles) {
        if (!verificarStock(detalles)) {
            return false; // No se puede descontar si no hay suficiente stock
        }

        for (DetalleVenta d : detalles) {
            inventarioDAO.registrarSalida(d.getIdProducto(), d.getCantidad());
        }
        return true;
    }

    /**
     * Reponer stock de un producto
     */
    public boolean reponerStock(int idProducto, int cantidad) {
        return inventarioDAO.registrarEntrada(idProducto, cantidad);
    }

    /**
     * Obtener productos con stock bajo
     */
    public List<Inventario> obtenerProductosBajoStock(int limite) {
        return inventarioDAO.obtenerTodos()
                .stream()
                .filter(inv -> inv.getCantidadActual() <= limite)
                .toList();
    }

    /**
     * Consultar inventario completo (productos + stock)
     */
    public List<Inventario> obtenerInventarioCompleto() {
        return inventarioDAO.obtenerTodos();
    }
}
