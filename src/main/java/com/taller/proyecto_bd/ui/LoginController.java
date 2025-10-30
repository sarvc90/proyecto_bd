package com.taller.proyecto_bd.ui;


import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.models.Auditoria;
import com.taller.proyecto_bd.models.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

/**
 * Controlador para la pantalla de inicio de sesión
 */
public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private CheckBox chkRecordar;

    @FXML
    private Button btnIngresar;

    @FXML
    private Label lblError;

    private UsuarioDAO usuarioDAO;
    private AuditoriaDAO auditoriaDAO;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        usuarioDAO = UsuarioDAO.getInstance(); // Usar getInstance() porque es Singleton
        auditoriaDAO = AuditoriaDAO.getInstance(); // Usar getInstance() porque es Singleton

        // Configurar el Enter para hacer login
        txtPassword.setOnAction(event -> handleLogin(event));

        // Ocultar mensaje de error al inicio
        lblError.setVisible(false);

        // Cargar usuario guardado si existe
        cargarUsuarioGuardado();
    }

    /**
     * Maneja el evento de login
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        // Validar campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor ingrese usuario y contraseña");
            return;
        }

        // Deshabilitar botón mientras se valida
        btnIngresar.setDisable(true);
        lblError.setVisible(false);

        try {
            // Intentar autenticar al usuario (usa el método login del DAO)
            Usuario usuario = usuarioDAO.login(username, password);

            if (usuario != null) {
                if (!usuario.isActivo()) {
                    mostrarError("Usuario inactivo. Contacte al administrador.");
                    btnIngresar.setDisable(false);
                    return;
                }

                // Login exitoso
                registrarAuditoria(usuario, "LOGIN", true);

                // Guardar sesión
                SessionManager.setUsuarioActual(usuario);

                // Guardar usuario si se marcó recordar
                if (chkRecordar.isSelected()) {
                    guardarUsuario(username);
                } else {
                    limpiarUsuarioGuardado();
                }

                // Abrir ventana principal
                abrirVentanaPrincipal(usuario);

            } else {
                // Login fallido
                registrarAuditoriaFallida(username);
                mostrarError("Usuario o contraseña incorrectos");
                btnIngresar.setDisable(false);
            }

        } catch (Exception e) {
            mostrarError("Error de conexión: " + e.getMessage());
            btnIngresar.setDisable(false);
            e.printStackTrace();
        }
    }

    /**
     * Registra el intento de login en la auditoría
     */
    private void registrarAuditoria(Usuario usuario, String accion, boolean exitoso) {
        try {
            String ip = obtenerIP();
            String descripcion = exitoso ?
                    "Login exitoso de " + usuario.getUsername() :
                    "Intento de login fallido";

            Auditoria auditoria = new Auditoria(
                    usuario.getIdUsuario(),
                    accion,
                    "Usuario",
                    descripcion,
                    ip
            );
            
            // Establecer el nombre del usuario para el JOIN
            auditoria.setNombreUsuario(usuario.getNombreCompleto());

            auditoriaDAO.agregar(auditoria); // Usar agregar() en lugar de insertar()
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    /**
     * Registra un intento de login fallido
     */
    private void registrarAuditoriaFallida(String username) {
        try {
            String ip = obtenerIP();
            Auditoria auditoria = new Auditoria(
                    0, // Sin ID de usuario válido
                    "LOGIN_FALLIDO",
                    "Usuario",
                    "Intento de login fallido para usuario: " + username,
                    ip
            );
            auditoria.setNombreUsuario(username);
            auditoriaDAO.agregar(auditoria); // Usar agregar() en lugar de insertar()
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría fallida: " + e.getMessage());
        }
    }

    /**
     * Obtiene la dirección IP del equipo
     */
    private String obtenerIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    /**
     * Abre la ventana principal según el rol del usuario
     */
    private void abrirVentanaPrincipal(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/MainWindow.fxml"));
            Parent root = loader.load();

            // Obtener el controlador de la ventana principal y pasarle el usuario
            MainWindowController controller = loader.getController();
            controller.inicializarConUsuario(usuario);

            Stage stage = new Stage();
            stage.setTitle("Sistema de Electrodomésticos - " + usuario.getNombreCompleto());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana de login
            Stage loginStage = (Stage) btnIngresar.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            mostrarError("Error al abrir la ventana principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Abre un cuadro de diálogo para registrar un nuevo usuario.
     */
    @FXML
    private void mostrarRegistro(ActionEvent event) {
        Dialog<Usuario> dialogo = new Dialog<>();
        dialogo.setTitle("Registrar usuario");
        dialogo.setHeaderText("Crea una cuenta para ingresar al sistema");
        dialogo.initOwner(btnIngresar.getScene().getWindow());

        ButtonType registrarButtonType = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, registrarButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre completo");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Usuario");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        PasswordField confirmarField = new PasswordField();
        confirmarField.setPromptText("Confirmar contraseña");

        ComboBox<String> rolCombo = new ComboBox<>();
        rolCombo.getItems().addAll("VENDEDOR", "ADMIN", "GERENTE");
        rolCombo.getSelectionModel().selectFirst();

        TextField emailField = new TextField();
        emailField.setPromptText("correo@ejemplo.com");

        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Teléfono");

        CheckBox activoCheck = new CheckBox("Usuario activo");
        activoCheck.setSelected(true);

        grid.add(new Label("Nombre completo:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Usuario:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Confirmar contraseña:"), 0, 3);
        grid.add(confirmarField, 1, 3);
        grid.add(new Label("Rol:"), 0, 4);
        grid.add(rolCombo, 1, 4);
        grid.add(new Label("Email:"), 0, 5);
        grid.add(emailField, 1, 5);
        grid.add(new Label("Teléfono:"), 0, 6);
        grid.add(telefonoField, 1, 6);
        grid.add(activoCheck, 1, 7);

        dialogo.getDialogPane().setContent(grid);

        Node registrarButton = dialogo.getDialogPane().lookupButton(registrarButtonType);
        registrarButton.setDisable(true);

        Runnable validarCampos = () -> {
            boolean invalido = nombreField.getText().trim().isEmpty() ||
                    usernameField.getText().trim().isEmpty() ||
                    passwordField.getText().isEmpty() ||
                    confirmarField.getText().isEmpty() ||
                    !passwordField.getText().equals(confirmarField.getText());
            registrarButton.setDisable(invalido);
        };

        nombreField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        confirmarField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());

        validarCampos.run();

        registrarButton.addEventFilter(ActionEvent.ACTION, e -> {
            String username = usernameField.getText().trim();
            if (usuarioDAO.obtenerPorUsername(username) != null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Usuario duplicado",
                        "El nombre de usuario ya existe. Elige uno diferente.");
                e.consume();
            }
        });

        dialogo.setResultConverter(dialogButton -> {
            if (dialogButton == registrarButtonType) {
                Usuario nuevo = new Usuario();
                nuevo.setNombreCompleto(nombreField.getText().trim());
                nuevo.setUsername(usernameField.getText().trim());
                nuevo.setPassword(passwordField.getText());
                nuevo.setRol(rolCombo.getValue());
                nuevo.setEmail(emailField.getText().trim());
                nuevo.setTelefono(telefonoField.getText().trim());
                nuevo.setActivo(activoCheck.isSelected());
                return nuevo;
            }
            return null;
        });

        Optional<Usuario> resultado = dialogo.showAndWait();
        resultado.ifPresent(usuario -> {
            boolean agregado = usuarioDAO.agregar(usuario);
            if (agregado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso",
                        "Usuario registrado correctamente. Ahora puedes iniciar sesión.");
                txtUsuario.setText(usuario.getUsername());
                txtPassword.clear();
                txtPassword.requestFocus();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Registro fallido",
                        "No fue posible registrar el usuario. Intenta nuevamente.");
            }
        });
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.initOwner(btnIngresar.getScene().getWindow());
        alerta.showAndWait();
    }

    /**
     * Guarda el usuario para recordarlo
     */
    private void guardarUsuario(String username) {
        // Implementar con Preferences o archivo
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node("login");
        prefs.put("lastUsername", username);
    }

    /**
     * Carga el usuario guardado si existe
     */
    private void cargarUsuarioGuardado() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node("login");
        String lastUsername = prefs.get("lastUsername", "");
        if (!lastUsername.isEmpty()) {
            txtUsuario.setText(lastUsername);
            chkRecordar.setSelected(true);
            txtPassword.requestFocus();
        }
    }

    /**
     * Limpia el usuario guardado
     */
    private void limpiarUsuarioGuardado() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node("login");
        prefs.remove("lastUsername");
    }
}
