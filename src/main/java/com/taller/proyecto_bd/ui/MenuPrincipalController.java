package com.taller.proyecto_bd.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador principal encargado de manejar la navegación del menú.
 */
public class MenuPrincipalController {

    @FXML
    private StackPane panelCentral;
    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblMensajeBienvenida;
    @FXML
    private Label lblTitulo;

    @FXML
    private void initialize() {
        lblUsuario.setText("Usuario: admin");
        lblFecha.setText("Fecha: " + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @FXML
    private void abrirEntidades() {
        cargarVista("/vista/entidades.fxml", "Gestión de Entidades y Catálogos");
    }

    @FXML
    private void abrirTransacciones() {
        cargarVista("/vista/transacciones.fxml", "Registro de Ventas y Movimientos");
    }

    @FXML
    private void abrirReportes() {
        cargarVista("/vista/reportes.fxml", "Reportes y Consultas");
    }

    @FXML
    private void abrirUtilidades() {
        cargarVista("/vista/utilidades.fxml", "Utilidades del Sistema");
    }

    @FXML
    private void abrirAyuda() {
        cargarVista("/vista/ayuda.fxml", "Ayuda y Documentación");
    }

    private void cargarVista(String recurso, String mensaje) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(recurso));
            Node contenido = loader.load();
            panelCentral.getChildren().setAll(contenido);
            lblTitulo.setText(mensaje);
        } catch (IOException e) {
            lblMensajeBienvenida.setText("No se pudo cargar la vista solicitada.");
            panelCentral.getChildren().setAll(lblMensajeBienvenida);
            e.printStackTrace();
        }
    }
}
