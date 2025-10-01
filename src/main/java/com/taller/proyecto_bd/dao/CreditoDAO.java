package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Credito;

import java.util.ArrayList;
import java.util.List;

public class CreditoDAO {
    private final List<Credito> creditos;
    private static int idCounter = 10000;
    private static CreditoDAO instance;

    private CreditoDAO() {
        this.creditos = new ArrayList<>();
        cargarDatosPrueba();
    }

    public static CreditoDAO getInstance() {
        if (instance == null) {
            instance = new CreditoDAO();
        }
        return instance;
    }

    public boolean agregar(Credito credito) {
        if (credito != null && credito.getMontoTotal() >= 0) {
            if (credito.getIdCredito() == 0) {
                credito.setIdCredito(idCounter++);
            }
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

    private void cargarDatosPrueba() {
        Credito c1 = new Credito(1001, 1001, 800, 200, 6, 5.0);
        c1.setIdCredito(idCounter++);
        c1.generarCuotas();

        Credito c2 = new Credito(1002, 1002, 1200, 300, 12, 8.0);
        c2.setIdCredito(idCounter++);
        c2.generarCuotas();

        Credito c3 = new Credito(1003, 1001, 500, 100, 10, 7.0);
        c3.setIdCredito(idCounter++);
        c3.setEstado("CANCELADO");
        c3.setSaldoPendiente(0);

        creditos.add(c1);
        creditos.add(c2);
        creditos.add(c3);
    }
}
