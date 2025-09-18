package com.taller.proyecto_bd.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilidades para manejo y formateo de números.
 * Incluye operaciones comunes como redondeo, porcentajes y formateo de dinero.
 *
 * @author Sistema
 * @version 1.0
 */
public class NumberUtil {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    /**
     * Redondea un número a dos decimales.
     */
    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /**
     * Formatea un número con dos decimales.
     */
    public static String formatearDecimal(double valor) {
        return decimalFormat.format(valor);
    }

    /**
     * Formatea un valor como moneda (ejemplo: $1.200,50).
     */
    public static String formatearMoneda(double valor) {
        return currencyFormat.format(valor);
    }

    /**
     * Calcula un porcentaje (ejemplo: 20% de 150 = 30).
     */
    public static double calcularPorcentaje(double base, double porcentaje) {
        return base * (porcentaje / 100.0);
    }

    /**
     * Convierte un número en porcentaje con formato (ejemplo: "25.5%").
     */
    public static String formatearPorcentaje(double valor) {
        return decimalFormat.format(valor) + "%";
    }

    /**
     * Verifica si un número es positivo.
     */
    public static boolean esPositivo(double valor) {
        return valor > 0;
    }

    /**
     * Verifica si un número es negativo.
     */
    public static boolean esNegativo(double valor) {
        return valor < 0;
    }

    /**
     * Verifica si un número está en un rango (inclusive).
     */
    public static boolean enRango(double valor, double min, double max) {
        return valor >= min && valor <= max;
    }
}
