package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa el Inventario de productos en el sistema de electrodomésticos.
 * Controla el stock disponible, entradas y salidas.
 *
 * @author Sistema
 * @version 1.0
 */
public class Inventario {
    // ==================== ATRIBUTOS ====================
    private int idInventario;
    private int idProducto;        // Relación con Producto
    private int cantidadActual;    // Cantidad disponible en stock
    private int stockMinimo;       // Cantidad mínima antes de alerta
    private int stockMaximo;       // Límite máximo recomendado
    private Date ultimaActualizacion;

    // Atributos auxiliares
    private String nombreProducto;  // Para joins
    private String categoria;       // Para joins

    // ==================== CONSTRUCTORES ====================

    public Inventario() {
        this.cantidadActual = 0;
        this.stockMinimo = 5;
        this.stockMaximo = 100;
        this.ultimaActualizacion = new Date();
    }

    public Inventario(int idProducto, int cantidadActual, int stockMinimo, int stockMaximo) {
        this();
        this.idProducto = idProducto;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
    }

    public Inventario(int idInventario, int idProducto, int cantidadActual,
                      int stockMinimo, int stockMaximo, Date ultimaActualizacion) {
        this.idInventario = idInventario;
        this.idProducto = idProducto;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // ==================== GETTERS ====================

    public int getIdInventario() { return idInventario; }
    public int getIdProducto() { return idProducto; }
    public int getCantidadActual() { return cantidadActual; }
    public int getStockMinimo() { return stockMinimo; }
    public int getStockMaximo() { return stockMaximo; }
    public Date getUltimaActualizacion() { return ultimaActualizacion; }
    public String getNombreProducto() { return nombreProducto; }
    public String getCategoria() { return categoria; }

    // ==================== SETTERS ====================

    public void setIdInventario(int idInventario) { this.idInventario = idInventario; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public void setCantidadActual(int cantidadActual) { this.cantidadActual = cantidadActual; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public void setStockMaximo(int stockMaximo) { this.stockMaximo = stockMaximo; }
    public void setUltimaActualizacion(Date ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Registra una entrada de productos al inventario
     */
    public void registrarEntrada(int cantidad) {
        if (cantidad > 0) {
            this.cantidadActual += cantidad;
            this.ultimaActualizacion = new Date();
        }
    }

    /**
     * Registra una salida de productos del inventario
     */
    public boolean registrarSalida(int cantidad) {
        if (cantidad > 0 && cantidad <= this.cantidadActual) {
            this.cantidadActual -= cantidad;
            this.ultimaActualizacion = new Date();
            return true;
        }
        return false;
    }

    /**
     * Verifica si el stock está por debajo del mínimo
     */
    public boolean necesitaReposicion() {
        return cantidadActual <= stockMinimo;
    }

    /**
     * Verifica si el stock está por encima del máximo recomendado
     */
    public boolean sobreStock() {
        return cantidadActual > stockMaximo;
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Inventario{" +
                "ID=" + idInventario +
                ", Producto=" + nombreProducto +
                ", Cantidad=" + cantidadActual +
                ", StockMin=" + stockMinimo +
                ", StockMax=" + stockMaximo +
                ", Última actualización=" + ultimaActualizacion +
                (necesitaReposicion() ? " [BAJO STOCK]" : "") +
                (sobreStock() ? " [SOBRE STOCK]" : "") +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Inventario that = (Inventario) obj;
        return idProducto == that.idProducto;
    }

    @Override
    public int hashCode() {
        return idProducto;
    }
}
