package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa la entidad Detalle de Venta en el sistema de electrodomésticos
 * @author Sistema
 * @version 1.1 - Corregidos métodos getDescripcion y toString
 */
public class DetalleVenta {
    // ==================== ATRIBUTOS ====================
    private int idDetalle;
    private int idVenta;            // Relación con Venta
    private int idProducto;         // Relación con Producto
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private double montoIVA;
    private double total;
    private Date fechaRegistro;

    // Atributos auxiliares
    private String nombreProducto;  // Para joins
    private String categoriaProducto; // Para joins

    // ==================== CONSTRUCTORES ====================

    public DetalleVenta() {
        this.fechaRegistro = new Date();
        this.cantidad = 1;
        this.precioUnitario = 0.0;
        this.subtotal = 0.0;
        this.montoIVA = 0.0;
        this.total = 0.0;
    }

    public DetalleVenta(int idProducto, int cantidad, double precioUnitario, double iva) {
        this();
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        calcularTotales(iva);
    }

    public DetalleVenta(int idDetalle, int idVenta, int idProducto,
                        int cantidad, double precioUnitario,
                        double subtotal, double montoIVA, double total,
                        Date fechaRegistro) {
        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.montoIVA = montoIVA;
        this.total = total;
        this.fechaRegistro = fechaRegistro;
    }

    // ==================== GETTERS ====================

    public int getIdDetalle() { return idDetalle; }
    public int getIdVenta() { return idVenta; }
    public int getIdProducto() { return idProducto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getSubtotal() { return subtotal; }
    public double getMontoIVA() { return montoIVA; }
    public double getTotal() { return total; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public String getNombreProducto() { return nombreProducto; }
    public String getCategoriaProducto() { return categoriaProducto; }

    // ==================== SETTERS ====================

    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public void setMontoIVA(double montoIVA) { this.montoIVA = montoIVA; }
    public void setTotal(double total) { this.total = total; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setCategoriaProducto(String categoriaProducto) { this.categoriaProducto = categoriaProducto; }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Calcula los totales del detalle según cantidad, precio y %IVA
     */
    public void calcularTotales(double porcentajeIVA) {
        subtotal = cantidad * precioUnitario;
        montoIVA = subtotal * porcentajeIVA;
        total = subtotal + montoIVA;
    }

    /**
     * Verifica si el detalle tiene datos válidos
     */
    public boolean validarDetalle() {
        return idProducto > 0 && cantidad > 0 && precioUnitario >= 0;
    }

    /**
     * Obtiene una descripción corta del detalle
     */
    public String getDescripcion() {
        String nombre = (nombreProducto != null && !nombreProducto.trim().isEmpty()) ?
                nombreProducto : "Producto #" + idProducto;
        return String.format("%s x%d - Precio unitario: $%.2f - Total: $%.2f",
                nombre, cantidad, precioUnitario, total);
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        String nombre = (nombreProducto != null && !nombreProducto.trim().isEmpty()) ?
                nombreProducto : "Producto #" + idProducto;
        return String.format("DetalleVenta{Producto='%s', Cantidad=%d, PrecioUnitario=$%.2f, Subtotal=$%.2f, IVA=$%.2f, Total=$%.2f}",
                nombre, cantidad, precioUnitario, subtotal, montoIVA, total);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DetalleVenta that = (DetalleVenta) obj;
        return idProducto == that.idProducto && idVenta == that.idVenta;
    }

    @Override
    public int hashCode() {
        int result = idProducto;
        result = 31 * result + idVenta;
        return result;
    }
}