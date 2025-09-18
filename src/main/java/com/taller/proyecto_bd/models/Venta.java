package com.taller.proyecto_bd.models;

import java.util.Date;
import java.util.List;

/**
 * Clase que representa la entidad Venta en el sistema de electrodomésticos
 * @author Sistema
 * @version 1.0
 */
public class Venta {
    // ==================== ATRIBUTOS ====================
    private int idVenta;
    private String codigo;               // Código único de la venta (ej: V-2025-001)
    private int idCliente;               // Relación con Cliente
    private int idUsuario;               // Vendedor que registró la venta
    private Date fechaVenta;
    private boolean esCredito;           // true = crédito, false = contado
    private double subtotal;
    private double ivaTotal;
    private double total;
    private double cuotaInicial;         // Solo aplica a crédito
    private int plazoMeses;              // Solo aplica a crédito
    private String estado;               // "REGISTRADA", "PAGADA", "ANULADA"

    // Atributos auxiliares
    private String nombreCliente;        // Para joins
    private String nombreVendedor;       // Para joins
    private List<DetalleVenta> detalles; // Lista de productos vendidos

    // ==================== CONSTRUCTORES ====================

    public Venta() {
        this.fechaVenta = new Date();
        this.estado = "REGISTRADA";
        this.esCredito = false;
        this.subtotal = 0.0;
        this.ivaTotal = 0.0;
        this.total = 0.0;
        this.cuotaInicial = 0.0;
        this.plazoMeses = 0;
    }

    public Venta(String codigo, int idCliente, int idUsuario, boolean esCredito) {
        this();
        this.codigo = codigo;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.esCredito = esCredito;
    }

    public Venta(int idVenta, String codigo, int idCliente, int idUsuario,
                 Date fechaVenta, boolean esCredito, double subtotal,
                 double ivaTotal, double total, double cuotaInicial,
                 int plazoMeses, String estado) {
        this.idVenta = idVenta;
        this.codigo = codigo;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.fechaVenta = fechaVenta;
        this.esCredito = esCredito;
        this.subtotal = subtotal;
        this.ivaTotal = ivaTotal;
        this.total = total;
        this.cuotaInicial = cuotaInicial;
        this.plazoMeses = plazoMeses;
        this.estado = estado;
    }

    // ==================== GETTERS ====================

    public int getIdVenta() { return idVenta; }
    public String getCodigo() { return codigo; }
    public int getIdCliente() { return idCliente; }
    public int getIdUsuario() { return idUsuario; }
    public Date getFechaVenta() { return fechaVenta; }
    public boolean isEsCredito() { return esCredito; }
    public double getSubtotal() { return subtotal; }
    public double getIvaTotal() { return ivaTotal; }
    public double getTotal() { return total; }
    public double getCuotaInicial() { return cuotaInicial; }
    public int getPlazoMeses() { return plazoMeses; }
    public String getEstado() { return estado; }
    public String getNombreCliente() { return nombreCliente; }
    public String getNombreVendedor() { return nombreVendedor; }
    public List<DetalleVenta> getDetalles() { return detalles; }

    // ==================== SETTERS ====================

    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setFechaVenta(Date fechaVenta) { this.fechaVenta = fechaVenta; }
    public void setEsCredito(boolean esCredito) { this.esCredito = esCredito; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public void setIvaTotal(double ivaTotal) { this.ivaTotal = ivaTotal; }
    public void setTotal(double total) { this.total = total; }
    public void setCuotaInicial(double cuotaInicial) { this.cuotaInicial = cuotaInicial; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public void setNombreVendedor(String nombreVendedor) { this.nombreVendedor = nombreVendedor; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Calcula los totales de la venta a partir de los detalles
     */
    public void calcularTotales() {
        if (detalles == null || detalles.isEmpty()) {
            subtotal = 0;
            ivaTotal = 0;
            total = 0;
            return;
        }

        subtotal = 0;
        ivaTotal = 0;
        for (DetalleVenta d : detalles) {
            subtotal += d.getSubtotal();
            ivaTotal += d.getMontoIVA();
        }
        total = subtotal + ivaTotal;
    }

    /**
     * Verifica si la venta es válida (tiene cliente y al menos un detalle)
     */
    public boolean validarVenta() {
        return idCliente > 0 && detalles != null && !detalles.isEmpty() && subtotal > 0;
    }

    /**
     * Obtiene una descripción corta de la venta
     */
    public String getResumen() {
        return "Venta " + codigo + " - Cliente: " + nombreCliente +
                " - Total: $" + String.format("%.2f", total) +
                (esCredito ? " [CRÉDITO]" : " [CONTADO]");
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Venta{" +
                "ID=" + idVenta +
                ", Código='" + codigo + '\'' +
                ", Cliente=" + nombreCliente +
                ", Vendedor=" + nombreVendedor +
                ", Fecha=" + fechaVenta +
                ", Total=$" + String.format("%.2f", total) +
                ", Tipo=" + (esCredito ? "Crédito" : "Contado") +
                ", Estado=" + estado +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Venta venta = (Venta) obj;
        return codigo != null && codigo.equals(venta.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }
}
