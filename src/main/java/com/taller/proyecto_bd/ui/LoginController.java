package com.taller.proyecto_bd.ui;


import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.models.Auditoria;
import com.taller.proyecto_bd.models.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
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
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
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