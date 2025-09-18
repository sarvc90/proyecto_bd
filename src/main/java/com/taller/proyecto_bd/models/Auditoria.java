package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa un registro de auditoría en el sistema de electrodomésticos
 * Permite rastrear las acciones realizadas por los usuarios.
 *
 * @author Sistema
 * @version 1.0
 */
public class Auditoria {
    // ==================== ATRIBUTOS ====================
    private int idAuditoria;
    private int idUsuario;        // Usuario que realizó la acción
    private String accion;        // "LOGIN", "CREAR_VENTA", "ANULAR_CREDITO", etc.
    private String tablaAfectada; // Ej: "Venta", "Producto"
    private String descripcion;   // Detalles de la acción
    private String ip;            // Dirección IP de origen (opcional)
    private Date fechaAccion;     // Cuándo se realizó

    // Atributos auxiliares
    private String nombreUsuario; // Para joins

    // ==================== CONSTRUCTORES ====================

    public Auditoria() {
        this.fechaAccion = new Date();
    }

    public Auditoria(int idUsuario, String accion, String tablaAfectada, String descripcion, String ip) {
        this();
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.descripcion = descripcion;
        this.ip = ip;
    }

    public Auditoria(int idAuditoria, int idUsuario, String accion,
                     String tablaAfectada, String descripcion, String ip, Date fechaAccion) {
        this.idAuditoria = idAuditoria;
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.descripcion = descripcion;
        this.ip = ip;
        this.fechaAccion = fechaAccion;
    }

    // ==================== GETTERS ====================

    public int getIdAuditoria() { return idAuditoria; }
    public int getIdUsuario() { return idUsuario; }
    public String getAccion() { return accion; }
    public String getTablaAfectada() { return tablaAfectada; }
    public String getDescripcion() { return descripcion; }
    public String getIp() { return ip; }
    public Date getFechaAccion() { return fechaAccion; }
    public String getNombreUsuario() { return nombreUsuario; }

    // ==================== SETTERS ====================

    public void setIdAuditoria(int idAuditoria) { this.idAuditoria = idAuditoria; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setAccion(String accion) { this.accion = accion; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setIp(String ip) { this.ip = ip; }
    public void setFechaAccion(Date fechaAccion) { this.fechaAccion = fechaAccion; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Devuelve un resumen simple del registro de auditoría
     */
    public String getResumen() {
        return "[" + fechaAccion + "] Usuario " + nombreUsuario +
                " (" + idUsuario + ") realizó: " + accion +
                " en " + tablaAfectada;
    }

    /**
     * Verifica si la auditoría corresponde a una acción crítica
     */
    public boolean esCritica() {
        return accion != null &&
                (accion.equalsIgnoreCase("ELIMINAR") ||
                        accion.equalsIgnoreCase("ANULAR") ||
                        accion.equalsIgnoreCase("LOGIN_FALLIDO"));
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Auditoria{" +
                "ID=" + idAuditoria +
                ", UsuarioID=" + idUsuario +
                ", Acción='" + accion + '\'' +
                ", Tabla='" + tablaAfectada + '\'' +
                ", Descripción='" + descripcion + '\'' +
                ", IP='" + ip + '\'' +
                ", Fecha=" + fechaAccion +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Auditoria that = (Auditoria) obj;
        return idAuditoria == that.idAuditoria;
    }

    @Override
    public int hashCode() {
        return idAuditoria;
    }
}
