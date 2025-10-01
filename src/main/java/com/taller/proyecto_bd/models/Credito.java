package com.taller.proyecto_bd.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase que representa la entidad Crédito en el sistema de electrodomésticos
 * @author Sistema
 * @version 1.0
 */
public class Credito {
    // ==================== ATRIBUTOS ====================
    private int idCredito;
    private int idVenta;              // Relación con Venta
    private int idCliente;            // Cliente asociado
    private double montoTotal;        // Valor financiado (después de cuota inicial)
    private double interes;           // % interés aplicado (ej: 0.05 = 5%)
    private int plazoMeses;           // Número de cuotas (12, 18, 24)
    private double cuotaInicial;      // Pago inicial (ej: 30%)
    private double saldoPendiente;    // Monto restante por pagar
    private String estado;            // "ACTIVO", "CANCELADO", "MOROSO"
    private Date fechaRegistro;

    // Atributos auxiliares
    private List<Cuota> cuotas;       // Lista de cuotas generadas

    // ==================== CONSTRUCTORES ====================

    public Credito() {
        this.fechaRegistro = new Date();
        this.estado = "ACTIVO";
        this.interes = 0.05; // interés fijo por defecto
        this.cuotas = new ArrayList<>();
    }

    public Credito(int idVenta, int idCliente, double montoTotal, double cuotaInicial,
                   int plazoMeses, double interes) {
        this();
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.montoTotal = montoTotal;
        this.cuotaInicial = cuotaInicial;
        this.plazoMeses = plazoMeses;
        this.interes = interes;
        this.saldoPendiente = montoTotal;
    }

    public Credito(int idCredito, int idVenta, int idCliente,
                   double montoTotal, double interes, int plazoMeses,
                   double cuotaInicial, double saldoPendiente,
                   String estado, Date fechaRegistro) {
        this.idCredito = idCredito;
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.montoTotal = montoTotal;
        this.interes = interes;
        this.plazoMeses = plazoMeses;
        this.cuotaInicial = cuotaInicial;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        this.cuotas = new ArrayList<>();
    }

    // ==================== GETTERS ====================

    public int getIdCredito() { return idCredito; }
    public int getIdVenta() { return idVenta; }
    public int getIdCliente() { return idCliente; }
    public double getMontoTotal() { return montoTotal; }
    public double getInteres() { return interes; }
    public int getPlazoMeses() { return plazoMeses; }
    public double getCuotaInicial() { return cuotaInicial; }
    public double getSaldoPendiente() { return saldoPendiente; }
    public String getEstado() { return estado; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public List<Cuota> getCuotas() { return cuotas; }

    // ==================== SETTERS ====================

    public void setIdCredito(int idCredito) { this.idCredito = idCredito; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
    public void setInteres(double interes) { this.interes = interes; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }
    public void setCuotaInicial(double cuotaInicial) { this.cuotaInicial = cuotaInicial; }
    public void setSaldoPendiente(double saldoPendiente) { this.saldoPendiente = saldoPendiente; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setCuotas(List<Cuota> cuotas) { this.cuotas = cuotas; }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Genera las cuotas a partir del monto, plazo y tasa de interés
     */
    public void generarCuotas() {
        cuotas = new ArrayList<>();
        if (plazoMeses <= 0) return;

        // Usar fórmula de interés compuesto
        double tasaMensual = interes / 12.0;
        double factor = Math.pow(1 + tasaMensual, plazoMeses);
        double valorCuota = montoTotal * (tasaMensual * factor) / (factor - 1);

        // Si no hay interés, dividir equitativamente
        if (interes == 0) {
            valorCuota = montoTotal / plazoMeses;
        }

        for (int i = 1; i <= plazoMeses; i++) {
            Cuota c = new Cuota(i, idCredito, valorCuota, new Date());
            cuotas.add(c);
        }
    }

    /**
     * Registra un pago de cuota y actualiza el saldo
     */
    public boolean pagarCuota(double monto) {
        if (monto <= 0 || monto > saldoPendiente) return false;
        saldoPendiente -= monto;

        if (saldoPendiente == 0) {
            estado = "CANCELADO";
        }
        return true;
    }

    /**
     * Verifica si el crédito tiene saldo pendiente
     */
    public boolean tieneSaldoPendiente() {
        return saldoPendiente > 0;
    }

    /**
     * Verifica si está moroso (saldo > 0 y fecha de alguna cuota vencida)
     */
    public boolean esMoroso() {
        if (cuotas == null) return false;
        return cuotas.stream().anyMatch(Cuota::estaVencida) && saldoPendiente > 0;
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Credito{" +
                "ID=" + idCredito +
                ", VentaID=" + idVenta +
                ", ClienteID=" + idCliente +
                ", Monto=$" + String.format("%.2f", montoTotal) +
                ", Interés=" + (interes * 100) + "%" +
                ", Plazo=" + plazoMeses + " meses" +
                ", Saldo Pendiente=$" + String.format("%.2f", saldoPendiente) +
                ", Estado=" + estado +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Credito credito = (Credito) obj;
        return idVenta == credito.idVenta;
    }

    @Override
    public int hashCode() {
        return idVenta;
    }
}
