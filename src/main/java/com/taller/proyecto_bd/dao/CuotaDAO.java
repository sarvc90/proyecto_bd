package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Cuota;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CuotaDAO {
    private final List<Cuota> cuotas;
    private static int idCounter = 1;
    private static CuotaDAO instance;

    private CuotaDAO() {
        this.cuotas = new ArrayList<>();
        cargarDatosPrueba();
    }

    public static CuotaDAO getInstance() {
        if (instance == null) {
            instance = new CuotaDAO();
        }
        return instance;
    }

    public boolean agregar(Cuota cuota) {
        if (cuota != null && cuota.getValor() > 0) {
            if (cuota.getIdCuota() == 0) {
                cuota.setIdCuota(idCounter++);
            }
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

    public List<Cuota> obtenerPorCredito(int idCredito) {
        List<Cuota> resultado = new ArrayList<>();
        for (Cuota c : cuotas) {
            if (c.getIdCredito() == idCredito) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public boolean pagarCuota(int idCuota) {
        Cuota cuota = obtenerPorId(idCuota);
        if (cuota != null && !cuota.isPagada()) {
            cuota.pagarCuota(new Date());
            return true;
        }
        return false;
    }

    /**
     * Obtener todas las cuotas vencidas
     */
    public List<Cuota> obtenerVencidas() {
        List<Cuota> resultado = new ArrayList<>();
        Date hoy = new Date();
        for (Cuota c : cuotas) {
            if (!c.isPagada() && c.getFechaVencimiento().before(hoy)) {
                resultado.add(c);
            }
        }
        return resultado;
    }


    private void cargarDatosPrueba() {
        Cuota c1 = new Cuota(1, 10001, 200, new Date(System.currentTimeMillis() + 86400000L));
        agregar(c1);

        Cuota c2 = new Cuota(2, 10001, 200, new Date(System.currentTimeMillis() - 86400000L));
        agregar(c2);

        Cuota c3 = new Cuota(3, 10002, 150, new Date(System.currentTimeMillis() + 604800000L));
        agregar(c3);

        Cuota c4 = new Cuota(1, 10003, 500, new Date());
        agregar(c4);
        c4.pagarCuota(new Date());
    }
}
