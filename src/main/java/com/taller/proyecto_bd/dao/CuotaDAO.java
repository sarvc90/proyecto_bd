package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Cuota;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Cuota.
 * Maneja operaciones CRUD sobre cuotas (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class CuotaDAO {
    // ==================== ATRIBUTOS ====================
    private List<Cuota> cuotas;

    // ==================== CONSTRUCTOR ====================

    public CuotaDAO() {
        this.cuotas = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    public boolean agregar(Cuota cuota) {
        if (cuota != null && cuota.getValor() > 0) {
            return cuotas.add(cuota);
        }
        return false;
    }

    public List<Cuota> obtenerTodas() {
        return new ArrayList<>(cuotas);
    }

    public Cuota obtenerPorId(int idCuota) {
        return cuotas.stream()
                .filter(c -> c.getIdCuota() == idCuota)
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Cuota cuota) {
        for (int i = 0; i < cuotas.size(); i++) {
            if (cuotas.get(i).getIdCuota() == cuota.getIdCuota()) {
                cuotas.set(i, cuota);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int idCuota) {
        return cuotas.removeIf(c -> c.getIdCuota() == idCuota);
    }

    // ==================== MÉTODOS EXTRA ====================

    /** Cuotas de un crédito específico */
    public List<Cuota> obtenerPorCredito(int idCredito) {
        List<Cuota> resultado = new ArrayList<>();
        for (Cuota c : cuotas) {
            if (c.getIdCredito() == idCredito) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Cuotas pagadas */
    public List<Cuota> obtenerPagadas() {
        List<Cuota> resultado = new ArrayList<>();
        for (Cuota c : cuotas) {
            if (c.isPagada()) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Cuotas pendientes (no pagadas y no vencidas) */
    public List<Cuota> obtenerPendientes() {
        List<Cuota> resultado = new ArrayList<>();
        for (Cuota c : cuotas) {
            if (!c.isPagada() && !c.estaVencida()) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Cuotas vencidas */
    public List<Cuota> obtenerVencidas() {
        List<Cuota> resultado = new ArrayList<>();
        for (Cuota c : cuotas) {
            if (c.estaVencida()) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Registrar el pago de una cuota */
    public boolean pagarCuota(int idCuota) {
        Cuota cuota = obtenerPorId(idCuota);
        if (cuota != null && !cuota.isPagada()) {
            cuota.pagarCuota(new Date());
            return true;
        }
        return false;
    }

    // ==================== DATOS DE PRUEBA ====================

    private void cargarDatosPrueba() {
        Cuota c1 = new Cuota(1, 1, 200, new Date(System.currentTimeMillis() + 86400000L)); // cuota #1 de crédito 1
        c1.setIdCuota(1);

        Cuota c2 = new Cuota(2, 1, 200, new Date(System.currentTimeMillis() - 86400000L)); // cuota #2 de crédito 1, ya vencida
        c2.setIdCuota(2);

        Cuota c3 = new Cuota(3, 2, 150, new Date(System.currentTimeMillis() + 604800000L)); // cuota #3 de crédito 2
        c3.setIdCuota(3);

        Cuota c4 = new Cuota(1, 3, 500, new Date()); // cuota #1 de crédito 3
        c4.setIdCuota(4);
        c4.pagarCuota(new Date()); // marcada como pagada

        cuotas.add(c1);
        cuotas.add(c2);
        cuotas.add(c3);
        cuotas.add(c4);
    }

}
