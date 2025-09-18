package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa una Cuota de un Crédito en el sistema de electrodomésticos
 * @author Sistema
 * @version 1.0
 */
public class Cuota {
    // ==================== ATRIBUTOS ====================
    private int idCuota;
    private int numeroCuota;       // Ej: 1, 2, 3...
    private int idCredito;         // Relación con Crédito
    private double valor;          // Valor de la cuota
    private Date fechaVencimiento; // Fecha límite de pago
    private Date fechaPago;        // Fecha en que se pagó (null si no se ha pagado)
    private boolean pagada;

    // ==================== CONSTRUCTORES ====================

    public Cuota() {
        this.pagada = false;
    }

    public Cuota(int numeroCuota, int idCredito, double valor, Date fechaVencimiento) {
        this();
        this.numeroCuota = numeroCuota;
        this.idCredito = idCredito;
        this.valor = valor;
        this.fechaVencimiento = fechaVencimiento;
    }

    public Cuota(int idCuota, int numeroCuota, int idCredito,
                 double valor, Date fechaVencimiento,
                 Date fechaPago, boolean pagada) {
        this.idCuota = idCuota;
        this.numeroCuota = numeroCuota;
        this.idCredito = idCredito;
        this.valor = valor;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaPago = fechaPago;
        this.pagada = pagada;
    }

    // ==================== GETTERS ====================

    public int getIdCuota() { return idCuota; }
    public int getNumeroCuota() { return numeroCuota; }
    public int getIdCredito() { return idCredito; }
    public double getValor() { return valor; }
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public Date getFechaPago() { return fechaPago; }
    public boolean isPagada() { return pagada; }

    // ==================== SETTERS ====================

    public void setIdCuota(int idCuota) { this.idCuota = idCuota; }
    public void setNumeroCuota(int numeroCuota) { this.numeroCuota = numeroCuota; }
    public void setIdCredito(int idCredito) { this.idCredito = idCredito; }
    public void setValor(double valor) { this.valor = valor; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }
    public void setPagada(boolean pagada) { this.pagada = pagada; }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Registra el pago de la cuota
     */
    public void pagarCuota(Date fechaPago) {
        this.fechaPago = fechaPago != null ? fechaPago : new Date();
        this.pagada = true;
    }

    /**
     * Verifica si la cuota está vencida
     */
    public boolean estaVencida() {
        if (pagada) return false;
        Date hoy = new Date();
        return fechaVencimiento != null && hoy.after(fechaVencimiento);
    }

    /**
     * Calcula los días de atraso si la cuota está vencida
     */
    public long diasAtraso() {
        if (!estaVencida()) return 0;
        long diff = new Date().getTime() - fechaVencimiento.getTime();
        return diff / (1000 * 60 * 60 * 24); // días
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Cuota{" +
                "Num=" + numeroCuota +
                ", Valor=$" + String.format("%.2f", valor) +
                ", Vencimiento=" + fechaVencimiento +
                ", Pagada=" + pagada +
                (pagada ? ", Pago=" + fechaPago : "") +
                (estaVencida() ? " [MOROSA: " + diasAtraso() + " días]" : "") +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Cuota cuota = (Cuota) obj;
        return idCredito == cuota.idCredito && numeroCuota == cuota.numeroCuota;
    }

    @Override
    public int hashCode() {
        int result = idCredito;
        result = 31 * result + numeroCuota;
        return result;
    }
}
