module com.taller.proyecto_bd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // Exports para que otros módulos puedan usar tus clases
    exports com.taller.proyecto_bd.models;
    exports com.taller.proyecto_bd.dao;
    //exports com.taller.proyecto_bd.controllers;
    //exports com.taller.proyecto_bd.services;
    //exports com.taller.proyecto_bd.utils;
    exports com.taller.proyecto_bd.main;

    // Opens para JavaFX (reflexión)
    opens com.taller.proyecto_bd to javafx.fxml;
    opens com.taller.proyecto_bd.models to javafx.fxml;
    opens com.taller.proyecto_bd.controllers to javafx.fxml;
}