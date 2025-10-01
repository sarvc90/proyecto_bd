package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.utils.Constantes; // Importar Constantes
import java.util.ArrayList;
import java.util.List;

public class CalculadoraService {
    // Usar la constante de IVA definida globalmente
    private static final double IVA_PORCENTAJE = Constantes.IVA_DEFAULT;

    // ==================== CALCULOS DE VENTA ====================

    /** Calcular subtotal (sin IVA) */
    public double calcularSubtotal(List<Double> precios) {
        if (precios == null) return 0;
        return precios.stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Calcular IVA total */
    public double calcularIVA(double subtotal) {
        return redondear2Decimales(subtotal * IVA_PORCENTAJE);
    }

    /** Calcular total con IVA */
    public double calcularTotalConIVA(double subtotal) {
        return redondear2Decimales(subtotal + calcularIVA(subtotal));
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
     * Fórmula: C = P * (i * (1+i)^n) / ((1+i)^n - 1)
     * Donde:
     *  P = monto financiado
     *  i = tasa de interés mensual (tasa anual / 100 / 12)
     *  n = número de cuotas
     */
    public double calcularCuotaMensual(double montoFinanciado, double interesAnual, int plazoMeses) {
        if (plazoMeses <= 0 || montoFinanciado <= 0) return 0;

        // CORRECCIÓN: Primero convertir a decimal, luego dividir por 12
        double tasaMensual = (interesAnual / 100.0) / 12.0;

        // Si no hay interés, dividir el monto entre las cuotas
        if (tasaMensual == 0) {
            return redondear2Decimales(montoFinanciado / plazoMeses);
        }

        double factor = Math.pow(1 + tasaMensual, plazoMeses);
        double cuota = montoFinanciado * (tasaMensual * factor) / (factor - 1);

        return redondear2Decimales(cuota);
    }

    /**
     * Genera todas las cuotas para un crédito.
     */
    public List<Double> generarPlanPagos(double montoFinanciado, double interesAnual, int plazoMeses) {
        List<Double> cuotas = new ArrayList<>();
        double cuotaMensual = calcularCuotaMensual(montoFinanciado, interesAnual, plazoMeses);

        for (int i = 0; i < plazoMeses; i++) {
            cuotas.add(cuotaMensual);
        }

        return cuotas;
    }

    /**
     * Calcula el interés total pagado en un crédito.
     */
    public double calcularInteresTotal(double montoFinanciado, double interesAnual, int plazoMeses) {
        if (plazoMeses <= 0 || montoFinanciado <= 0) return 0;

        double cuotaMensual = calcularCuotaMensual(montoFinanciado, interesAnual, plazoMeses);
        double totalPagado = cuotaMensual * plazoMeses;
        double interesTotal = totalPagado - montoFinanciado;

        return redondear2Decimales(Math.max(interesTotal, 0));
    }

    // ==================== UTILES ====================

    public double redondear2Decimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}