package com.taller.proyecto_bd.controllers;

/**
 * Controlador de utilidades de cálculo.
 * Realiza operaciones como IVA, márgenes, cuotas de crédito, descuentos, etc.
 *
 * @author Sistema
 * @version 1.0
 */
public class CalculadoraController {

    /** Calcular IVA de un subtotal */
    public double calcularIVA(double subtotal, double porcentajeIVA) {
        if (subtotal < 0 || porcentajeIVA < 0) return 0;
        return subtotal * porcentajeIVA;
    }

    /** Calcular total de venta con IVA */
    public double calcularTotalConIVA(double subtotal, double porcentajeIVA) {
        return subtotal + calcularIVA(subtotal, porcentajeIVA);
    }

    /** Calcular el valor de una cuota de crédito simple */
    public double calcularValorCuota(double monto, int plazoMeses, double interes) {
        if (monto <= 0 || plazoMeses <= 0) return 0;
        double montoConInteres = monto + (monto * interes);
        return montoConInteres / plazoMeses;
    }

    /** Calcular porcentaje de ganancia */
    public double calcularMargenGanancia(double costo, double precioVenta) {
        if (costo <= 0) return 0;
        return ((precioVenta - costo) / costo) * 100;
    }

    /** Calcular descuento aplicado */
    public double calcularDescuento(double precio, double porcentajeDescuento) {
        if (precio <= 0 || porcentajeDescuento < 0) return 0;
        return precio - (precio * porcentajeDescuento);
    }

    /** Calcular utilidad (precio - costo - impuestos) */
    public double calcularUtilidad(double costo, double precioVenta, double porcentajeIVA) {
        double iva = calcularIVA(precioVenta, porcentajeIVA);
        return precioVenta - costo - iva;
    }
}
