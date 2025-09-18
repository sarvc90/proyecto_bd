package com.taller.proyecto_bd.utils;

/**
 * Clase de constantes globales del sistema.
 * Se usa para evitar valores mágicos en el código.
 *
 * @author Sistema
 * @version 1.0
 */
public class Constantes {

    // ==================== ROLES ====================
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_VENDEDOR = "VENDEDOR";
    public static final String ROL_GERENTE = "GERENTE";

    // ==================== ESTADOS DE VENTA ====================
    public static final String VENTA_REGISTRADA = "REGISTRADA";
    public static final String VENTA_PAGADA = "PAGADA";
    public static final String VENTA_ANULADA = "ANULADA";
    public static final String VENTA_ACTIVA = "ACTIVA";

    // ==================== ESTADOS DE CRÉDITO ====================
    public static final String CREDITO_ACTIVO = "ACTIVO";
    public static final String CREDITO_CANCELADO = "CANCELADO";
    public static final String CREDITO_MOROSO = "MOROSO";

    // ==================== CONFIGURACIONES DE NEGOCIO ====================
    public static final double IVA_DEFAULT = 0.19; // 19% por defecto
    public static final double INTERES_DEFAULT = 0.05; // 5% interés crédito

    // ==================== MENSAJES COMUNES ====================
    public static final String MSG_ERROR_GENERAL = "Ocurrió un error inesperado.";
    public static final String MSG_VENTA_NO_ENCONTRADA = "Venta no encontrada.";
    public static final String MSG_CREDITO_NO_ENCONTRADO = "Crédito no encontrado.";
    public static final String MSG_USUARIO_NO_AUTORIZADO = "Usuario no autorizado.";

    // ==================== FORMATOS ====================
    public static final String FORMATO_FECHA = "dd/MM/yyyy";
    public static final String FORMATO_HORA = "HH:mm:ss";
    public static final String FORMATO_MONEDA = "$###,###.##";

    // ==================== RUTAS DE ARCHIVOS (ejemplo) ====================
    public static final String RUTA_REPORTES = "data/reportes/";
    public static final String RUTA_BACKUPS = "data/backups/";
}
