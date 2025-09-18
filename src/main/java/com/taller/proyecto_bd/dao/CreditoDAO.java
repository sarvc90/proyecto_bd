package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Credito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Credito.
 * Maneja operaciones CRUD sobre créditos (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class CreditoDAO {
    // ==================== ATRIBUTOS ====================
    private List<Credito> creditos;

    // ==================== CONSTRUCTOR ====================

    public CreditoDAO() {
        this.creditos = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    public boolean agregar(Credito credito) {
        if (credito != null && credito.getMontoTotal() > 0) {
            return creditos.add(credito);
        }
        return false;
    }

    public List<Credito> obtenerTodos() {
        return new ArrayList<>(creditos);
    }

    public Credito obtenerPorId(int idCredito) {
        return creditos.stream()
                .filter(c -> c.getIdCredito() == idCredito)
                .findFirst()
                .orElse(null);
    }

    public Credito obtenerPorVenta(int idVenta) {
        return creditos.stream()
                .filter(c -> c.getIdVenta() == idVenta)
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Credito credito) {
        for (int i = 0; i < creditos.size(); i++) {
            if (creditos.get(i).getIdCredito() == credito.getIdCredito()) {
                creditos.set(i, credito);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int idCredito) {
        return creditos.removeIf(c -> c.getIdCredito() == idCredito);
    }

    // ==================== MÉTODOS EXTRA ====================

    public List<Credito> obtenerPorCliente(int idCliente) {
        List<Credito> resultado = new ArrayList<>();
        for (Credito c : creditos) {
            if (c.getIdCliente() == idCliente) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Credito> obtenerPorEstado(String estado) {
        List<Credito> resultado = new ArrayList<>();
        for (Credito c : creditos) {
            if (c.getEstado().equalsIgnoreCase(estado)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Credito> obtenerActivos() {
        return obtenerPorEstado("ACTIVO");
    }

    public List<Credito> obtenerCancelados() {
        return obtenerPorEstado("CANCELADO");
    }

    public List<Credito> obtenerMorosos() {
        List<Credito> morosos = new ArrayList<>();
        for (Credito c : creditos) {
            if (c.esMoroso()) {
                morosos.add(c);
            }
        }
        return morosos;
    }

    // ==================== DATOS DE PRUEBA ====================

    private void cargarDatosPrueba() {
        Credito c1 = new Credito(1, 1, 800, 200, 6, 0.05);
        c1.setIdCredito(1);
        c1.generarCuotas();

        Credito c2 = new Credito(2, 2, 1200, 300, 12, 0.08);
        c2.setIdCredito(2);
        c2.generarCuotas();

        Credito c3 = new Credito(3, 1, 500, 100, 10, 0.07);
        c3.setIdCredito(3);
        c3.setEstado("CANCELADO");
        c3.setSaldoPendiente(0);

        creditos.add(c1);
        creditos.add(c2);
        creditos.add(c3);
    }
}
