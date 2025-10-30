package com.taller.proyecto_bd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX que inicializa el menú principal.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the login view using an absolute resource path rooted at the classpath
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("/vista/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Sistema de Gestión de Electrodomésticos");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
