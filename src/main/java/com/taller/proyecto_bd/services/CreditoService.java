package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;

import java.util.Date;
import java.util.List;

/**
 * Servicio para la gestión integral de Créditos.
 * Maneja registro, pagos, cancelaciones y estados.
 *
 * @author Sistema
 * @version 1.0
 */
public class CreditoService {
    // ==================== DEPENDENCIAS ====================
    private CreditoDAO creditoDAO = new CreditoDAO();
    private CuotaDAO cuotaDAO = new CuotaDAO();
    private AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    // ==================== PROCESOS PRINCIPALES ====================

    /**
     * Registrar un nuevo crédito y generar sus cuotas.
     */
    public boolean registrarCredito(Credito credito, Usuario usuario) {
        if (credito == null || credito.getMontoTotal() <= 0) return false;

        credito.generarCuotas();
        if (!creditoDAO.agregar(credito)) return false;

        for (Cuota c : credito.getCuotas()) {
            cuotaDAO.agregar(c);
        }

        auditoriaDAO.agregar(new Auditoria(usuario.getIdUsuario(), "CREAR_CREDITO",
                "Credito", "Crédito creado para venta ID=" + credito.getIdVenta(), "127.0.0.1"));

        return true;
    }

    /**
     * Registrar el pago de una cuota específica.
     */
    public boolean pagarCuota(int idCuota, Usuario usuario) {
        Cuota cuota = cuotaDAO.obtenerPorId(idCuota);
        if (cuota == null || cuota.isPagada()) return false;

        cuota.pagarCuota(new Date());
        cuotaDAO.actualizar(cuota);

        // Verificar si el crédito ya quedó cancelado
        Credito credito = creditoDAO.obtenerPorId(cuota.getIdCredito());
        if (credito != null) {
            boolean todasPagadas = cuotaDAO.obtenerPorCredito(credito.getIdCredito())
                    .stream().allMatch(Cuota::isPagada);

            if (todasPagadas) {
                credito.setEstado("CANCELADO");
                credito.setSaldoPendiente(0);
                creditoDAO.actualizar(credito);
            }
        }

        auditoriaDAO.agregar(new Auditoria(usuario.getIdUsuario(), "PAGO_CUOTA",
                "Cuota", "Pago de cuota ID=" + idCuota, "127.0.0.1"));

        return true;
    }

    /**
     * Anular un crédito (ej: si se anuló la venta).
     */
    public boolean anularCredito(int idCredito, Usuario usuario) {
        Credito credito = creditoDAO.obtenerPorId(idCredito);
        if (credito == null) return false;

        credito.setEstado("ANULADO");
        creditoDAO.actualizar(credito);

        auditoriaDAO.agregar(new Auditoria(usuario.getIdUsuario(), "ANULAR_CREDITO",
                "Credito", "Crédito anulado ID=" + idCredito, "127.0.0.1"));

        return true;
    }

    /**
     * Marcar crédito como moroso si tiene cuotas vencidas.
     */
    public boolean verificarMorosidad(int idCredito) {
        Credito credito = creditoDAO.obtenerPorId(idCredito);
        if (credito == null) return false;

        boolean tieneVencidas = cuotaDAO.obtenerPorCredito(idCredito)
                .stream().anyMatch(Cuota::estaVencida);

        if (tieneVencidas) {
            credito.setEstado("MOROSO");
            creditoDAO.actualizar(credito);
            return true;
        }

        return false;
    }

    // ==================== REPORTES ====================

    public List<Credito> listarActivos() {
        return creditoDAO.obtenerActivos();
    }

    public List<Credito> listarMorosos() {
        return creditoDAO.obtenerMorosos();
    }

    public List<Credito> listarCancelados() {
        return creditoDAO.obtenerCancelados();
    }
}
