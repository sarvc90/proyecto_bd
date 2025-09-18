package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.models.Credito;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que centraliza cálculos financieros y de utilidad
 * para el sistema de electrodomésticos.
 *
 * @author Sistema
 * @version 1.0
 */
public class CalculadoraService {
    private static final double IVA_PORCENTAJE = 0.19; // 19% IVA

    // ==================== CALCULOS DE VENTA ====================

    /** Calcular subtotal (sin IVA) */
    public double calcularSubtotal(List<Double> precios) {
        if (precios == null) return 0;
        return precios.stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Calcular IVA total */
    public double calcularIVA(double subtotal) {
        return subtotal * IVA_PORCENTAJE;
    }

    /** Calcular total con IVA */
    public double calcularTotalConIVA(double subtotal) {
        return subtotal + calcularIVA(subtotal);
    }

    // ==================== CALCULOS DE CREDITO ====================

    /**
     * Calcula el monto financiado (total - cuota inicial).
     */
    public double calcularMontoFinanciado(double total, double cuotaInicial) {
        return Math.max(total - cuotaInicial, 0);
    }

    /**
     * Calcula el valor de la cuota mensual con interés compuesto.
     * Fórmula: C = P * (i / (1 - (1+i)^-n))
     * Donde:
     *  P = monto financiado
     *  i = tasa de interés mensual
     *  n = número de cuotas
     */
    public double calcularCuotaMensual(double montoFinanciado, double interesAnual, int plazoMeses) {
        if (plazoMeses <= 0) return 0;
        double i = interesAnual / 12; // interés mensual
        return montoFinanciado * (i / (1 - Math.pow(1 + i, -plazoMeses)));
    }

    /**
     * Genera todas las cuotas para un crédito.
     */
    public List<Double> generarPlanPagos(double montoFinanciado, double interesAnual, int plazoMeses) {
        List<Double> cuotas = new ArrayList<>();
        double cuota = calcularCuotaMensual(montoFinanciado, interesAnual, plazoMeses);
        for (int i = 0; i < plazoMeses; i++) {
            cuotas.add(cuota);
        }
        return cuotas;
    }

    /**
     * Calcula el interés total pagado en un crédito.
     */
    public double calcularInteresTotal(double montoFinanciado, double interesAnual, int plazoMeses) {
        double cuota = calcularCuotaMensual(montoFinanciado, interesAnual, plazoMeses);
        return (cuota * plazoMeses) - montoFinanciado;
    }

    // ==================== UTILES ====================

    public double redondear2Decimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
