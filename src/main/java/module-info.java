module com.taller.proyecto_bd {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.taller.proyecto_bd to javafx.fxml;
    exports com.taller.proyecto_bd;
}