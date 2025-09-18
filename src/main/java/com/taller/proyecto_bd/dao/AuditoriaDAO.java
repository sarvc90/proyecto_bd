package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Auditoria;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Auditoria.
 * Maneja operaciones CRUD sobre registros de auditoría (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class AuditoriaDAO {
    // ==================== ATRIBUTOS ====================
    private List<Auditoria> auditorias;

    // ==================== CONSTRUCTOR ====================

    public AuditoriaDAO() {
        this.auditorias = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    public boolean agregar(Auditoria auditoria) {
        if (auditoria != null) {
            return auditorias.add(auditoria);
        }
        return false;
    }

    public List<Auditoria> obtenerTodas() {
        return new ArrayList<>(auditorias);
    }

    public Auditoria obtenerPorId(int idAuditoria) {
        return auditorias.stream()
                .filter(a -> a.getIdAuditoria() == idAuditoria)
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Auditoria auditoria) {
        for (int i = 0; i < auditorias.size(); i++) {
            if (auditorias.get(i).getIdAuditoria() == auditoria.getIdAuditoria()) {
                auditorias.set(i, auditoria);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int idAuditoria) {
        return auditorias.removeIf(a -> a.getIdAuditoria() == idAuditoria);
    }

    // ==================== MÉTODOS EXTRA ====================

    public List<Auditoria> obtenerPorUsuario(int idUsuario) {
        List<Auditoria> resultado = new ArrayList<>();
        for (Auditoria a : auditorias) {
            if (a.getIdUsuario() == idUsuario) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public List<Auditoria> obtenerPorAccion(String accion) {
        List<Auditoria> resultado = new ArrayList<>();
        for (Auditoria a : auditorias) {
            if (a.getAccion() != null && a.getAccion().equalsIgnoreCase(accion)) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public List<Auditoria> obtenerPorTabla(String tabla) {
        List<Auditoria> resultado = new ArrayList<>();
        for (Auditoria a : auditorias) {
            if (a.getTablaAfectada() != null && a.getTablaAfectada().equalsIgnoreCase(tabla)) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public List<Auditoria> obtenerCriticas() {
        List<Auditoria> resultado = new ArrayList<>();
        for (Auditoria a : auditorias) {
            if (a.esCritica()) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    // ==================== DATOS DE PRUEBA ====================

    private void cargarDatosPrueba() {
        Auditoria a1 = new Auditoria(1, "LOGIN", "Usuario", "Ingreso al sistema", "192.168.0.1");
        a1.setIdAuditoria(1);
        a1.setNombreUsuario("Admin General");

        Auditoria a2 = new Auditoria(2, "CREAR_VENTA", "Venta", "Venta V001 creada", "192.168.0.2");
        a2.setIdAuditoria(2);
        a2.setNombreUsuario("Carlos Pérez");

        Auditoria a3 = new Auditoria(3, "ANULAR", "Credito", "Crédito 3 anulado por falta de pago", "192.168.0.3");
        a3.setIdAuditoria(3);
        a3.setNombreUsuario("Laura Gómez");

        Auditoria a4 = new Auditoria(2, "LOGIN_FALLIDO", "Usuario", "Intento de login con clave errada", "192.168.0.2");
        a4.setIdAuditoria(4);
        a4.setNombreUsuario("Carlos Pérez");

        auditorias.add(a1);
        auditorias.add(a2);
        auditorias.add(a3);
        auditorias.add(a4);
    }
}
