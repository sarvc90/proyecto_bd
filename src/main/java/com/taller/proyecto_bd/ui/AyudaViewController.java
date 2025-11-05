package com.taller.proyecto_bd.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import com.taller.proyecto_bd.utils.ConexionBD;

/**
 * Controlador para el módulo de Ayuda
 * Proporciona documentación, tutoriales, FAQ y soporte técnico
 */
public class AyudaViewController {

    @FXML private Accordion accordionFAQ;
    @FXML private Label lblDatabaseInfo;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        cargarInformacionBaseDatos();

        // Expandir la primera pregunta del FAQ por defecto
        if (accordionFAQ != null && !accordionFAQ.getPanes().isEmpty()) {
            accordionFAQ.setExpandedPane(accordionFAQ.getPanes().get(0));
        }
    }

    /**
     * Carga la información de la base de datos conectada
     */
    private void cargarInformacionBaseDatos() {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn != null) {
                DatabaseMetaData metaData = conn.getMetaData();
                String dbName = conn.getCatalog();
                String dbProduct = metaData.getDatabaseProductName();
                String dbVersion = metaData.getDatabaseProductVersion();

                String info = String.format("Conectado a: %s (%s %s)",
                    dbName != null ? dbName : "Base de datos",
                    dbProduct,
                    dbVersion);

                if (lblDatabaseInfo != null) {
                    lblDatabaseInfo.setText(info);
                }
            } else {
                if (lblDatabaseInfo != null) {
                    lblDatabaseInfo.setText("No conectado a base de datos");
                    lblDatabaseInfo.setStyle("-fx-text-fill: #e74c3c;");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener información de la base de datos: " + e.getMessage());
            if (lblDatabaseInfo != null) {
                lblDatabaseInfo.setText("Error al obtener información de BD");
                lblDatabaseInfo.setStyle("-fx-text-fill: #e67e22;");
            }
        }
    }

    /**
     * Cierra la ventana de ayuda
     */
    @FXML
    private void cerrarVentana() {
        try {
            Stage stage = (Stage) accordionFAQ.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Error al cerrar ventana de ayuda: " + e.getMessage());
        }
    }
}