package com.taller.proyecto_bd.ui;


import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.models.Auditoria;
import com.taller.proyecto_bd.models.Cliente;
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
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
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
        dialogo.setResizable(true);

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
        passwordField.setPromptText("Contrasena");

        PasswordField confirmarField = new PasswordField();
        confirmarField.setPromptText("Confirmar contrasena");

        ComboBox<String> rolCombo = new ComboBox<>();
        rolCombo.getItems().addAll("VENDEDOR", "ADMIN", "GERENTE", "CLIENTE");
        rolCombo.getSelectionModel().selectFirst();

        TextField emailField = new TextField();
        emailField.setPromptText("correo@ejemplo.com");

        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Telefono");

        CheckBox activoCheck = new CheckBox("Usuario activo");
        activoCheck.setSelected(true);

        TextField cedulaField = new TextField();
        cedulaField.setPromptText("Documento de identidad");

        TextField apellidoClienteField = new TextField();
        apellidoClienteField.setPromptText("Apellidos del cliente");

        TextField direccionField = new TextField();
        direccionField.setPromptText("Direccion del cliente");

        TextField limiteCreditoField = new TextField();
        limiteCreditoField.setPromptText("0.00");
        limiteCreditoField.setText("0");

        Label nombreLabel = new Label("Nombre completo:");
        Label usuarioLabel = new Label("Usuario:");
        Label contrasenaLabel = new Label("Contrasena:");
        Label confirmarLabel = new Label("Confirmar contrasena:");
        Label rolLabel = new Label("Rol:");
        Label emailLabel = new Label("Email:");
        Label telefonoLabel = new Label("Telefono:");
        Label cedulaLabel = new Label("Cedula:");
        Label apellidoLabel = new Label("Apellido:");
        Label direccionLabel = new Label("Direccion:");
        Label limiteLabel = new Label("Limite credito:");

        grid.add(nombreLabel, 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(usuarioLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(contrasenaLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(confirmarLabel, 0, 3);
        grid.add(confirmarField, 1, 3);
        grid.add(rolLabel, 0, 4);
        grid.add(rolCombo, 1, 4);
        grid.add(emailLabel, 0, 5);
        grid.add(emailField, 1, 5);
        grid.add(telefonoLabel, 0, 6);
        grid.add(telefonoField, 1, 6);
        grid.add(activoCheck, 1, 7);
        grid.add(cedulaLabel, 0, 8);
        grid.add(cedulaField, 1, 8);
        grid.add(apellidoLabel, 0, 9);
        grid.add(apellidoClienteField, 1, 9);
        grid.add(direccionLabel, 0, 10);
        grid.add(direccionField, 1, 10);
        grid.add(limiteLabel, 0, 11);
        grid.add(limiteCreditoField, 1, 11);

        final Cliente[] clienteSeleccionado = new Cliente[1];

        List<Node> camposCliente = Arrays.asList(
                cedulaLabel, cedulaField,
                apellidoLabel, apellidoClienteField,
                direccionLabel, direccionField,
                limiteLabel, limiteCreditoField
        );
        camposCliente.forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setPrefSize(420, 520);
        dialogo.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        dialogo.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        Node registrarButton = dialogo.getDialogPane().lookupButton(registrarButtonType);
        registrarButton.setDisable(true);

        Runnable validarCampos = () -> {
            boolean esCliente = "CLIENTE".equals(rolCombo.getValue());
            boolean invalido = nombreField.getText().trim().isEmpty() ||
                    usernameField.getText().trim().isEmpty() ||
                    passwordField.getText().isEmpty() ||
                    confirmarField.getText().isEmpty() ||
                    !passwordField.getText().equals(confirmarField.getText());

            if (esCliente) {
                boolean limiteValido = esNumeroValido(limiteCreditoField.getText().trim());
                invalido = invalido ||
                        cedulaField.getText().trim().isEmpty() ||
                        apellidoClienteField.getText().trim().isEmpty() ||
                        telefonoField.getText().trim().isEmpty() ||
                        !limiteValido;
            }
            registrarButton.setDisable(invalido);
        };

        nombreField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        confirmarField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        telefonoField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        cedulaField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        apellidoClienteField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());
        limiteCreditoField.textProperty().addListener((obs, oldVal, newVal) -> validarCampos.run());

        rolCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esCliente = "CLIENTE".equals(newVal);
            camposCliente.forEach(node -> {
                node.setManaged(esCliente);
                node.setVisible(esCliente);
            });
            validarCampos.run();
        });

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
                // El setPassword encripta automáticamente, así que pasamos la contraseña plana
                nuevo.setPassword(passwordField.getText());
                nuevo.setRol(rolCombo.getValue());
                nuevo.setEmail(emailField.getText().trim());
                nuevo.setTelefono(telefonoField.getText().trim());
                nuevo.setActivo(activoCheck.isSelected());

                if ("CLIENTE".equals(rolCombo.getValue())) {
                    Cliente clienteNuevo = new Cliente();
                    clienteNuevo.setCedula(cedulaField.getText().trim());
                    clienteNuevo.setNombre(nombreField.getText().trim());
                    clienteNuevo.setApellido(apellidoClienteField.getText().trim());
                    clienteNuevo.setDireccion(direccionField.getText().trim());
                    clienteNuevo.setTelefono(telefonoField.getText().trim());
                    clienteNuevo.setEmail(emailField.getText().trim());
                    clienteNuevo.setLimiteCredito(parsearDouble(limiteCreditoField.getText().trim(), 0));
                    // El saldoPendiente ya se inicializa en 0 en el constructor de Cliente
                    clienteNuevo.setActivo(activoCheck.isSelected());
                    clienteSeleccionado[0] = clienteNuevo;
                } else {
                    clienteSeleccionado[0] = null;
                }
                return nuevo;
            }
            clienteSeleccionado[0] = null;
            return null;
        });

        Optional<Usuario> resultado = dialogo.showAndWait();
        resultado.ifPresent(usuario -> {
            System.out.println("DEBUG: Intentando registrar usuario: " + usuario.getUsername() + " con rol: " + usuario.getRol());
            boolean agregado = usuarioDAO.agregar(usuario);
            System.out.println("DEBUG: Usuario agregado: " + agregado);

            if (agregado) {
                boolean clienteRegistrado = true;
                if ("CLIENTE".equals(usuario.getRol())) {
                    Cliente clienteNuevo = clienteSeleccionado[0];
                    System.out.println("DEBUG: Cliente creado: " + (clienteNuevo != null));
                    if (clienteNuevo != null) {
                        System.out.println("DEBUG: Datos del cliente - Cedula: " + clienteNuevo.getCedula() +
                                         ", Nombre: " + clienteNuevo.getNombre() +
                                         ", Apellido: " + clienteNuevo.getApellido());
                        ClienteDAO clienteDAO = ClienteDAO.getInstance();
                        clienteRegistrado = clienteDAO.agregar(clienteNuevo);
                        System.out.println("DEBUG: Cliente registrado: " + clienteRegistrado);
                    } else {
                        System.out.println("DEBUG: Cliente es null, no se puede registrar");
                        clienteRegistrado = false;
                    }
                    if (!clienteRegistrado) {
                        System.out.println("DEBUG: Eliminando usuario debido a fallo en registro de cliente");
                        usuarioDAO.eliminar(usuario.getIdUsuario());
                    }
                }

                if (clienteRegistrado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso",
                            "Usuario registrado correctamente. Ahora puedes iniciar sesion.");
                    txtUsuario.setText(usuario.getUsername());
                    txtPassword.clear();
                    txtPassword.requestFocus();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Registro incompleto",
                            "No fue posible guardar los datos del cliente. Intenta nuevamente.");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Registro fallido",
                        "No fue posible registrar el usuario. Intenta nuevamente.");
            }
        });
    }

    private boolean esNumeroValido(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return true;
        }
        try {
            double valor = Double.parseDouble(texto.trim());
            return valor >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double parsearDouble(String texto, double valorPorDefecto) {
        if (texto == null || texto.trim().isEmpty()) {
            return valorPorDefecto;
        }
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
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
