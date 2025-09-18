package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la generación de reportes del sistema.
 * No depende de un DAO específico, sino que consume datos de otros DAOs.
 *
 * @author Sistema
 * @version 1.0
 */
public class ReporteController {
    private VentaDAO ventaDAO = new VentaDAO();
    private CreditoDAO creditoDAO = new CreditoDAO();
    private CuotaDAO cuotaDAO = new CuotaDAO();
    private InventarioDAO inventarioDAO = new InventarioDAO();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Reporte de ventas totales (suma de todas las ventas)
     */
    public double generarReporteVentasTotales() {
        return ventaDAO.obtenerTodas()
                .stream()
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    /**
     * Reporte de ventas por cliente
     */
    public Map<Integer, Double> generarReporteVentasPorCliente() {
        Map<Integer, Double> reporte = new HashMap<>();
        for (Venta v : ventaDAO.obtenerTodas()) {
            reporte.put(v.getIdCliente(),
                    reporte.getOrDefault(v.getIdCliente(), 0.0) + v.getTotal());
        }
        return reporte;
    }

    /**
     * Reporte de créditos morosos
     */
    public List<Credito> generarReporteCreditosMorosos() {
        return creditoDAO.obtenerMorosos();
    }

    /**
     * Reporte de inventario bajo stock
     */
    public List<Inventario> generarReporteInventarioBajoStock() {
        return inventarioDAO.obtenerBajoStock();
    }

    /**
     * Reporte de usuarios activos
     */
    public List<Usuario> generarReporteUsuariosActivos() {
        return usuarioDAO.obtenerActivos();
    }

    /**
     * Reporte de cuotas vencidas
     */
    public List<Cuota> generarReporteCuotasVencidas() {
        return cuotaDAO.obtenerVencidas();
    }
}
