package com.taller.proyecto_bd.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utilidades para manejo de fechas en el sistema.
 * Incluye formateo, cálculos de diferencias y sumas de días/meses.
 *
 * @author Sistema
 * @version 1.0
 */
public class DateUtils {

    private static final String DEFAULT_FORMAT = "dd/MM/yyyy";

    /**
     * Convierte un objeto Date a String con el formato por defecto (dd/MM/yyyy).
     */
    public static String formatearFecha(Date fecha) {
        if (fecha == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_FORMAT);
        return sdf.format(fecha);
    }

    /**
     * Convierte un objeto Date a String con un formato personalizado.
     */
    public static String formatearFecha(Date fecha, String formato) {
        if (fecha == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(formato);
        return sdf.format(fecha);
    }

    /**
     * Convierte un String en Date usando el formato por defecto.
     */
    public static Date parsearFecha(String fechaStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_FORMAT);
            return sdf.parse(fechaStr);
        } catch (ParseException e) {
            throw new RuntimeException("Formato de fecha inválido: " + fechaStr, e);
        }
    }

    /**
     * Suma días a una fecha.
     */
    public static Date sumarDias(Date fecha, int dias) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }

    /**
     * Suma meses a una fecha.
     */
    public static Date sumarMeses(Date fecha, int meses) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.add(Calendar.MONTH, meses);
        return cal.getTime();
    }

    /**
     * Calcula la diferencia en días entre dos fechas.
     */
    public static long diferenciaEnDias(Date fecha1, Date fecha2) {
        long diff = fecha2.getTime() - fecha1.getTime();
        return diff / (1000 * 60 * 60 * 24); // milisegundos a días
    }

    /**
     * Verifica si una fecha es anterior a hoy.
     */
    public static boolean esAnteriorAHoy(Date fecha) {
        Date hoy = new Date();
        return fecha.before(hoy);
    }

    /**
     * Verifica si una fecha es posterior a hoy.
     */
    public static boolean esPosteriorAHoy(Date fecha) {
        Date hoy = new Date();
        return fecha.after(hoy);
    }
}
