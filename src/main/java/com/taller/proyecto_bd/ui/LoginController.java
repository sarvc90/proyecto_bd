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
    private ComboBox<String> cmbTipoUsuario;

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
    private ClienteDAO clienteDAO;
    private AuditoriaDAO auditoriaDAO;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        usuarioDAO = UsuarioDAO.getInstance();
        clienteDAO = ClienteDAO.getInstance();
        auditoriaDAO = AuditoriaDAO.getInstance();

        // Configurar ComboBox de tipo de usuario
        cmbTipoUsuario.getItems().addAll(
            "Vendedor / Administrador / Gerente",
            "Cliente"
        );
        cmbTipoUsuario.setValue("Vendedor / Administrador / Gerente");

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
        String tipoUsuario = cmbTipoUsuario.getValue();

        // Validar campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor ingrese usuario y contraseña");
            return;
        }

        // Deshabilitar botón mientras se valida
        btnIngresar.setDisable(true);
        lblError.setVisible(false);

        try {
            if ("Cliente".equals(tipoUsuario)) {
                // Login como Cliente
                handleLoginCliente(username, password);
            } else {
                // Login como Usuario del Sistema (vendedor, admin, gerente)
                handleLoginUsuario(username, password);
            }

        } catch (Exception e) {
            mostrarError("Error de conexión: " + e.getMessage());
            btnIngresar.setDisable(false);
            e.printStackTrace();
        }
    }

    /**
     * Maneja el login de usuarios del sistema (vendedor, admin, gerente)
     */
    private void handleLoginUsuario(String username, String password) {
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
    }

    /**
     * Maneja el login de clientes
     */
    private void handleLoginCliente(String cedula, String password) {
        // Buscar cliente por cédula
        Cliente cliente = clienteDAO.obtenerPorCedula(cedula);

        if (cliente != null) {
            boolean autenticado = false;

            // Verificar si el cliente tiene contraseña configurada
            if (cliente.tienePassword()) {
                // Cliente con contraseña: validar password
                autenticado = cliente.validarPassword(password);
            } else {
                // Cliente SIN contraseña (creado por admin): usar teléfono como fallback
                autenticado = password.equals(cliente.getTelefono());
            }

            if (autenticado) {
                if (!cliente.isActivo()) {
                    mostrarError("Cliente inactivo. Contacte con la tienda.");
                    btnIngresar.setDisable(false);
                    return;
                }

                // Login exitoso - crear un usuario temporal para el cliente
                Usuario usuarioCliente = crearUsuarioTemporalCliente(cliente);

                // Guardar sesión
                SessionManager.setUsuarioActual(usuarioCliente);

                // Guardar cédula si se marcó recordar
                if (chkRecordar.isSelected()) {
                    guardarUsuario(cedula);
                } else {
                    limpiarUsuarioGuardado();
                }

                // Registrar auditoría
                registrarAuditoriaCliente(cliente, "LOGIN_CLIENTE", true);

                // Abrir ventana principal (con acceso limitado para clientes)
                abrirVentanaPrincipal(usuarioCliente);

            } else {
                // Credenciales incorrectas
                registrarAuditoriaClienteFallida(cedula);
                mostrarError("Cédula o contraseña incorrectas");
                btnIngresar.setDisable(false);
            }
        } else {
            // Cliente no encontrado
            registrarAuditoriaClienteFallida(cedula);
            mostrarError("Cédula o contraseña incorrectas");
            btnIngresar.setDisable(false);
        }
    }

    /**
     * Crea un objeto Usuario temporal para un cliente
     */
    private Usuario crearUsuarioTemporalCliente(Cliente cliente) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(-cliente.getIdCliente()); // ID negativo para identificar clientes
        usuario.setUsername(cliente.getCedula());
        usuario.setNombreCompleto(cliente.getNombre() + " " + cliente.getApellido());
        usuario.setRol("CLIENTE");
        usuario.setEmail(cliente.getEmail());
        usuario.setTelefono(cliente.getTelefono());
        usuario.setActivo(cliente.isActivo());
        return usuario;
    }

    /**
     * Registra auditoría para clientes
     * NOTA: No se guarda en BD debido a restricción FK, solo se registra en log
     */
    private void registrarAuditoriaCliente(Cliente cliente, String accion, boolean exitoso) {
        try {
            String ip = obtenerIP();
            String descripcion = exitoso ?
                    "Login exitoso del cliente: " + cliente.getNombre() + " " + cliente.getApellido() :
                    "Intento de login fallido del cliente";

            // Solo registrar en log del sistema, no en BD (problema con FK)
            System.out.println(accion + " - Cliente ID: " + cliente.getIdCliente() +
                             " - " + cliente.getNombre() + " " + cliente.getApellido() +
                             " - IP: " + ip);
        } catch (Exception e) {
            System.err.println("Error al registrar log de auditoría de cliente: " + e.getMessage());
        }
    }

    /**
     * Registra un intento de login fallido de cliente
     * NOTA: No se guarda en BD debido a restricción FK, solo se registra en log
     */
    private void registrarAuditoriaClienteFallida(String cedula) {
        try {
            String ip = obtenerIP();
            // Solo registrar en log del sistema, no en BD (problema con FK)
            System.out.println("LOGIN_CLIENTE_FALLIDO - Cédula: " + cedula + " - IP: " + ip);
        } catch (Exception e) {
            System.err.println("Error al registrar log de intento fallido: " + e.getMessage());
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
     * NOTA: No se guarda en BD debido a restricción FK, solo se registra en log
     */
    private void registrarAuditoriaFallida(String username) {
        try {
            String ip = obtenerIP();
            // Solo registrar en log del sistema, no en BD (problema con FK)
            System.out.println("LOGIN_FALLIDO - Usuario: " + username + " - IP: " + ip);
        } catch (Exception e) {
            System.err.println("Error al registrar log de intento fallido: " + e.getMessage());
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
        rolCombo.getItems().addAll("CLIENTE", "VENDEDOR", "ADMIN", "GERENTE");
        rolCombo.getSelectionModel().selectFirst(); // Ahora selecciona CLIENTE por defecto

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

            // Validaciones comunes
            boolean invalido = nombreField.getText().trim().isEmpty() ||
                    passwordField.getText().isEmpty() ||
                    confirmarField.getText().isEmpty() ||
                    !passwordField.getText().equals(confirmarField.getText());

            if (esCliente) {
                // Para CLIENTE: validar campos específicos de cliente
                boolean limiteValido = esNumeroValido(limiteCreditoField.getText().trim());
                invalido = invalido ||
                        cedulaField.getText().trim().isEmpty() ||
                        apellidoClienteField.getText().trim().isEmpty() ||
                        telefonoField.getText().trim().isEmpty() ||
                        !limiteValido;
            } else {
                // Para VENDEDOR/ADMIN/GERENTE: validar campo Usuario
                invalido = invalido || usernameField.getText().trim().isEmpty();
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
            // Solo validar username duplicado si NO es cliente
            if (!"CLIENTE".equals(rolCombo.getValue())) {
                String username = usernameField.getText().trim();
                if (usuarioDAO.obtenerPorUsername(username) != null) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Usuario duplicado",
                            "El nombre de usuario ya existe. Elige uno diferente.");
                    e.consume();
                }
            }
        });

        dialogo.setResultConverter(dialogButton -> {
            System.out.println("=== RESULT CONVERTER EJECUTADO ===");
            System.out.println("Dialog button: " + dialogButton);
            System.out.println("Registrar button type: " + registrarButtonType);

            if (dialogButton == registrarButtonType) {
                System.out.println(">>> Botón de registro clickeado!");
                String rol = rolCombo.getValue();
                System.out.println(">>> Rol seleccionado: " + rol);

                // Si es CLIENTE, NO crear Usuario (solo Cliente)
                if ("CLIENTE".equals(rol)) {
                    System.out.println(">>> Creando cliente...");
                    Cliente clienteNuevo = new Cliente();
                    clienteNuevo.setCedula(cedulaField.getText().trim());
                    clienteNuevo.setNombre(nombreField.getText().trim());
                    clienteNuevo.setApellido(apellidoClienteField.getText().trim());
                    clienteNuevo.setDireccion(direccionField.getText().trim());
                    clienteNuevo.setTelefono(telefonoField.getText().trim());
                    clienteNuevo.setEmail(emailField.getText().trim());

                    // Validar y asignar límite de crédito
                    double limiteCredito = parsearDouble(limiteCreditoField.getText().trim(), 0);
                    if (limiteCredito > 99999999.99) {
                        System.err.println("ADVERTENCIA: Límite de crédito demasiado grande: " + limiteCredito + ", usando 0");
                        limiteCredito = 0;
                    }
                    clienteNuevo.setLimiteCredito(limiteCredito);
                    clienteNuevo.setActivo(activoCheck.isSelected());

                    // Establecer contraseña (el setter la encripta automáticamente)
                    clienteNuevo.setPassword(passwordField.getText());

                    clienteSeleccionado[0] = clienteNuevo;
                    System.out.println(">>> Cliente asignado a clienteSeleccionado[0]");
                    return null; // No retornar Usuario para clientes
                } else {
                    // Para ADMIN, VENDEDOR, GERENTE: crear Usuario
                    Usuario nuevo = new Usuario();
                    nuevo.setNombreCompleto(nombreField.getText().trim());
                    nuevo.setUsername(usernameField.getText().trim());
                    nuevo.setPassword(passwordField.getText()); // El setPassword encripta automáticamente
                    nuevo.setRol(rol);
                    nuevo.setEmail(emailField.getText().trim());
                    nuevo.setTelefono(telefonoField.getText().trim());
                    nuevo.setActivo(activoCheck.isSelected());

                    clienteSeleccionado[0] = null;
                    return nuevo;
                }
            }
            // NO sobrescribir clienteSeleccionado si ya tiene un valor
            System.out.println(">>> Botón diferente clickeado, clienteSeleccionado[0] mantiene su valor");
            return null;
        });

        Optional<Usuario> resultado = dialogo.showAndWait();

        // Verificar si se creó un cliente (sin usuario)
        if (clienteSeleccionado[0] != null) {
            Cliente clienteNuevo = clienteSeleccionado[0];
            System.out.println("DEBUG: Registrando cliente: " + clienteNuevo.getCedula());
            System.out.println("  Nombre: " + clienteNuevo.getNombre());
            System.out.println("  Apellido: " + clienteNuevo.getApellido());
            System.out.println("  Telefono: " + clienteNuevo.getTelefono());
            System.out.println("  Email: " + clienteNuevo.getEmail());
            System.out.println("  LimiteCredito: " + clienteNuevo.getLimiteCredito());
            System.out.println("  Tiene password: " + clienteNuevo.tienePassword());
            System.out.println("  Validacion datos obligatorios: " + clienteNuevo.validarDatosObligatorios());

            ClienteDAO clienteDAOInstance = ClienteDAO.getInstance();
            boolean clienteRegistrado = clienteDAOInstance.agregar(clienteNuevo);

            if (clienteRegistrado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso",
                        "Cliente registrado correctamente. Ahora puedes iniciar sesión con tu cédula y contraseña.");
                txtUsuario.setText(clienteNuevo.getCedula());
                txtPassword.clear();
                txtPassword.requestFocus();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Registro fallido",
                        "No fue posible registrar el cliente. Verifica que la cédula no esté duplicada.");
            }
            return;
        }

        // Si no es cliente, entonces es un usuario del sistema (admin, vendedor, gerente)
        resultado.ifPresent(usuario -> {
            System.out.println("DEBUG: Intentando registrar usuario: " + usuario.getUsername() + " con rol: " + usuario.getRol());
            boolean agregado = usuarioDAO.agregar(usuario);
            System.out.println("DEBUG: Usuario agregado: " + agregado);

            if (agregado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso",
                        "Usuario registrado correctamente. Ahora puedes iniciar sesión.");
                txtUsuario.setText(usuario.getUsername());
                txtPassword.clear();
                txtPassword.requestFocus();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Registro fallido",
                        "No fue posible registrar el usuario. Verifica que el nombre de usuario no esté duplicado.");
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
