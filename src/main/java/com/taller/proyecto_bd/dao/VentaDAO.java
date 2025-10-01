package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Venta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Venta.
 * Maneja operaciones CRUD sobre ventas (en memoria por ahora).
 */
public class VentaDAO {
    private List<Venta> ventas;
    private static int idCounter = 10000;
    private static VentaDAO instance;

    private VentaDAO() {
        this.ventas = new ArrayList<>();
        cargarDatosPrueba();
    }

    public static VentaDAO getInstance() {
        if (instance == null) {
            instance = new VentaDAO();
        }
        return instance;
    }

    public boolean agregar(Venta venta) {
        if (venta != null) {
            if (venta.getIdVenta() == 0) {
                venta.setIdVenta(idCounter++);
            }
            return ventas.add(venta);
        }
        return false;
    }

    public List<Venta> obtenerTodas() {
        return new ArrayList<>(ventas);
    }

    public Venta obtenerPorId(int id) {
        return ventas.stream()
                .filter(v -> v.getIdVenta() == id)
                .findFirst()
                .orElse(null);
    }

    public Venta obtenerPorCodigo(String codigo) {
        if (codigo == null) return null;
        return ventas.stream()
                .filter(v -> v.getCodigo() != null && v.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Venta venta) {
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getIdVenta() == venta.getIdVenta()) {
                ventas.set(i, venta);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int id) {
        return ventas.removeIf(v -> v.getIdVenta() == id);
    }

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

    public List<Venta> obtenerPorEstado(String estado) {
        if (estado == null) return new ArrayList<>();
        List<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.getEstado() != null && estado.equalsIgnoreCase(v.getEstado())) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    public List<Venta> obtenerActivas() {
        return obtenerPorEstado("REGISTRADA");
    }

    private void cargarDatosPrueba() {
        ventas.add(new Venta(idCounter++, "V001", 1, 101,
                new Date(), false,
                1000, 120, 1120,
                0, 0, "REGISTRADA"));

        ventas.add(new Venta(idCounter++, "V002", 2, 102,
                new Date(), true,
                800, 96, 896,
                200, 6, "REGISTRADA"));

        ventas.add(new Venta(idCounter++, "V003", 1, 101,
                new Date(), true,
                500, 60, 560,
                100, 12, "ANULADA"));
    }
}
