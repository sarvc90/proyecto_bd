package com.taller.proyecto_bd.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Clase de utilidades para encriptación de contraseñas y textos.
 * Usa algoritmos de hashing como SHA-256.
 *
 * @author Sistema
 * @version 1.0
 */
public class Encriptacion {

    /**
     * Genera un hash SHA-256 a partir de un texto plano.
     *
     * @param texto Texto a encriptar
     * @return Hash en formato hexadecimal
     */
    public static String encriptarSHA256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes());

            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar con SHA-256", e);
        }
    }

    /**
     * Codifica un texto en Base64 (no es seguro, pero útil en algunos casos).
     */
    public static String encriptarBase64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes());
    }

    /**
     * Decodifica un texto de Base64.
     */
    public static String desencriptarBase64(String textoEnBase64) {
        return new String(Base64.getDecoder().decode(textoEnBase64));
    }
}
