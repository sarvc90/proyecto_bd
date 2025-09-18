package com.taller.proyecto_bd.utils;

import java.util.regex.Pattern;

/**
 * Clase de utilidades para validaciones comunes en el sistema.
 * Contiene métodos estáticos reutilizables.
 *
 * @author Sistema
 * @version 1.0
 */
public class Validadores {

    // ==================== VALIDACIONES ====================

    /**
     * Valida una cédula (ejemplo Colombia: 6 a 10 dígitos numéricos).
     */
    public static boolean validarCedula(String cedula) {
        return cedula != null && cedula.matches("\\d{6,10}");
    }

    /**
     * Valida un correo electrónico básico.
     */
    public static boolean validarEmail(String email) {
        if (email == null) return false;
        String regex = "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}$";
        return Pattern.matches(regex, email);
    }

    /**
     * Valida un número de teléfono (ejemplo Colombia: 7 a 10 dígitos).
     */
    public static boolean validarTelefono(String telefono) {
        return telefono != null && telefono.matches("\\d{7,10}");
    }

    /**
     * Valida que un texto no esté vacío o nulo.
     */
    public static boolean validarTexto(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Valida que un número sea positivo.
     */
    public static boolean validarNumeroPositivo(double valor) {
        return valor > 0;
    }

    /**
     * Valida que una contraseña tenga mínimo 6 caracteres,
     * incluyendo mayúsculas, minúsculas y un número.
     */
    public static boolean validarPassword(String password) {
        if (password == null) return false;
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$";
        return Pattern.matches(regex, password);
    }
}
