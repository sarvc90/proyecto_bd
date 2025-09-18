package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.models.*;

/**
 * Servicio de notificaciones.
 * Centraliza el envío de alertas a clientes, usuarios y administradores.
 * (Simulado con mensajes en consola / logs por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class NotificacionService {

    // ==================== CLIENTES ====================

    /** Notificar al cliente sobre un crédito aprobado */
    public void notificarCreditoAprobado(Cliente cliente, Credito credito) {
        if (cliente == null || credito == null) return;
        System.out.println("📢 Notificación a cliente " + cliente.getNombre() +
                ": Su crédito #" + credito.getIdCredito() +
                " ha sido aprobado. Saldo: $" + credito.getSaldoPendiente());
    }

    /** Notificar al cliente sobre cuota vencida */
    public void notificarCuotaVencida(Cliente cliente, Cuota cuota) {
        if (cliente == null || cuota == null) return;
        System.out.println("⚠️ Aviso a " + cliente.getNombre() +
                ": Su cuota #" + cuota.getNumeroCuota() +
                " está vencida desde el " + cuota.getFechaVencimiento() +
                ". Días de atraso: " + cuota.diasAtraso());
    }

    // ==================== INVENTARIO ====================

    /** Notificar al administrador sobre stock bajo */
    public void notificarStockBajo(Inventario inventario) {
        if (inventario == null) return;
        System.out.println("⚠️ ALERTA: El producto " + inventario.getNombreProducto() +
                " tiene stock bajo (" + inventario.getCantidadActual() + " unidades).");
    }

    /** Notificar sobre sobrestock */
    public void notificarSobreStock(Inventario inventario) {
        if (inventario == null) return;
        System.out.println("⚠️ ALERTA: El producto " + inventario.getNombreProducto() +
                " tiene sobrestock (" + inventario.getCantidadActual() + " unidades).");
    }

    // ==================== USUARIO ====================

    /** Notificar intento de login fallido */
    public void notificarLoginFallido(String username, String ip) {
        System.out.println("🚨 Alerta de seguridad: Login fallido para usuario " + username +
                " desde IP: " + ip);
    }

    /** Notificar bloqueo de cuenta */
    public void notificarBloqueoUsuario(Usuario usuario) {
        if (usuario == null) return;
        System.out.println("🔒 Usuario " + usuario.getUsername() +
                " ha sido bloqueado por intentos fallidos.");
    }
}
