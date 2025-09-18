package com.taller.proyecto_bd.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad para manejar la conexión a la base de datos.
 * Centraliza la configuración de acceso al motor.
 *
 * @author Sistema
 * @version 1.0
 */
public class ConexionBD {

    // ==================== CONFIGURACIÓN ====================
    private static final String URL = "jdbc:mysql://localhost:3306/nombreBD"; // <- Cambiar
    private static final String USER = "root"; // <- Cambiar
    private static final String PASSWORD = "";  // <- Cambiar

    // ==================== MÉTODOS ====================

    /**
     * Obtiene una conexión activa a la base de datos
     */
    public static Connection obtenerConexion() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("❌ Error de conexión a la BD: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cierra la conexión de forma segura
     */
    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
