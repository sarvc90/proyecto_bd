package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Venta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Venta.
 * Maneja operaciones CRUD sobre ventas (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.2
 */
public class VentaDAO {
    // ==================== ATRIBUTOS ====================
    private List<Venta> ventas;

    // ==================== CONSTRUCTOR ====================

    public VentaDAO() {
        this.ventas = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    /**
     * Agregar una nueva venta
     */
    public boolean agregar(Venta venta) {
        if (venta != null && venta.validarVenta()) { // ✅ se cambió validarDatosObligatorios() por validarVenta()
            return ventas.add(venta);
        }
        return false;
    }

    /**
     * Obtener todas las ventas
     */
    public List<Venta> obtenerTodas() {
        return new ArrayList<>(ventas);
    }

    /**
     * Buscar venta por ID
     */
    public Venta obtenerPorId(int id) {
        return ventas.stream()
                .filter(v -> v.getIdVenta() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Buscar venta por código
     */
    public Venta obtenerPorCodigo(String codigo) {
        return ventas.stream()
                .filter(v -> v.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    /**
     * Actualizar una venta existente
     */
    public boolean actualizar(Venta venta) {
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getIdVenta() == venta.getIdVenta()) {
                ventas.set(i, venta);
                return true;
            }
        }
        return false;
    }

    /**
     * Eliminar venta por ID
     */
    public boolean eliminar(int id) {
        return ventas.removeIf(v -> v.getIdVenta() == id);
    }

    // ==================== MÉTODOS EXTRA ====================

    public List<Venta> obtenerPorCliente(int idCliente) {
        List<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.getIdCliente() == idCliente) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    public List<Venta> obtenerPorUsuario(int idUsuario) {
        List<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.getIdUsuario() == idUsuario) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    public List<Venta> obtenerPorCredito(boolean esCredito) {
        List<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.isEsCredito() == esCredito) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    public List<Venta> obtenerActivas() {
        List<Venta> activas = new ArrayList<>();
        for (Venta v : ventas) {
            if ("ACTIVA".equalsIgnoreCase(v.getEstado())) {
                activas.add(v);
            }
        }
        return activas;
    }

    // ==================== DATOS DE PRUEBA ====================

    private void cargarDatosPrueba() {
        ventas.add(new Venta(1, "V001", 1, 101,
                new Date(), false,
                1000, 120, 1120,
                0, 0, "ACTIVA"));

        ventas.add(new Venta(2, "V002", 2, 102,
                new Date(), true,
                800, 96, 896,
                200, 6, "ACTIVA"));

        ventas.add(new Venta(3, "V003", 1, 101,
                new Date(), true,
                500, 60, 560,
                100, 12, "ANULADA"));
    }
}
