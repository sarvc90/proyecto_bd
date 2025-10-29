package com.taller.proyecto_bd.ui;



import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.models.SessionManager;
import com.taller.proyecto_bd.models.Usuario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de usuarios (Solo Administradores)
 */
public class UsuariosController {

    // ==================== CAMPOS DEL FORMULARIO ====================
    @FXML private TextField txtNombreCompleto;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRol;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblUltimoAcceso;
    @FXML private Label lblUsuarioActual;
    
    // ==================== BOTONES ====================
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnCambiarPassword;
    @FXML private Button btnBloquear;
    @FXML private Button btnTodos;
    @FXML private Button btnFiltroAdmin;
    @FXML private Button btnFiltroVendedor;
    @FXML private Button btnFiltroGerente;
    
    // ==================== TABLA ====================
    @FXML private TextField txtBuscar;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colTelefono;
    @FXML private TableColumn<Usuario, Date> colUltimoAcceso;
    @FXML private TableColumn<Usuario, Boolean> colActivo;
    
    // ==================== LABELS DE INFORMACIÓN ====================
    @FXML private Label lblMensaje;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblUsuariosActivos;
    @FXML private Label lblAdmins;
    @FXML private Label lblVendedores;
    
    // ==================== ATRIBUTOS ====================
    private UsuarioDAO usuarioDAO;
    private ObservableList<Usuario> listaUsuarios;
    private Usuario usuarioSeleccionado;
    private SimpleDateFormat formatoFecha;
    
    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        // Verificar que solo el admin puede acceder
        if (!SessionManager.esAdministrador()) {
            mostrarError("Solo los administradores pueden gestionar usuarios");
            cerrarVentana();
            return;
        }
        
        usuarioDAO = UsuarioDAO.getInstance();
        listaUsuarios = FXCollections.observableArrayList();
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        lblUsuarioActual.setText("Admin: " + SessionManager.getNombreUsuarioActual());
        
        configurarTabla();
        cargarUsuarios();
        configurarEventos();
        actualizarEstadisticas();
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        
        // Columna de último acceso con formato
        colUltimoAcceso.setCellValueFactory(new PropertyValueFactory<>("ultimoAcceso"));
        colUltimoAcceso.setCellFactory(col -> new TableCell<Usuario, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Nunca");
                } else {
                    setText(formatoFecha.format(item));
                }
            }
        });
        
        // Columna de estado con formato
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setCellFactory(col -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Activo" : "Bloqueado");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" : 
                                   "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        // Resaltar administradores en la tabla
        colRol.setCellFactory(col -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ADMIN".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if ("VENDEDOR".equals(item)) {
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        tablaUsuarios.setItems(listaUsuarios);
    }
    
    /**
     * Carga todos los usuarios en la tabla
     */
    private void cargarUsuarios() {
        List<Usuario> usuarios = usuarioDAO.obtenerTodos();
        listaUsuarios.clear();
        listaUsuarios.addAll(usuarios);
        actualizarEstadisticas();
    }
    
    /**
     * Configura los eventos de la interfaz
     */
    private void configurarEventos() {
        // Selección en la tabla
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    usuarioSeleccionado = newSelection;
                    cargarDatosFormulario(newSelection);
                }
            }
        );
        
        // Validación de username (sin espacios)
        txtUsername.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.contains(" ")) {
                txtUsername.setText(old);
            }
        });
    }
    
    /**
     * Carga los datos de un usuario en el formulario
     */
    private void cargarDatosFormulario(Usuario usuario) {
        txtNombreCompleto.setText(usuario.getNombreCompleto());
        txtUsername.setText(usuario.getUsername());
        txtPassword.clear(); // No mostrar la contraseña
        cmbRol.setValue(usuario.getRol());
        txtEmail.setText(usuario.getEmail());
        txtTelefono.setText(usuario.getTelefono());
        chkActivo.setSelected(usuario.isActivo());
        
        if (usuario.getUltimoAcceso() != null) {
            lblUltimoAcceso.setText(formatoFecha.format(usuario.getUltimoAcceso()));
        } else {
            lblUltimoAcceso.setText("Nunca");
        }
        
        // Actualizar texto del botón bloquear/desbloquear
        btnBloquear.setText(usuario.isActivo() ? "Bloquear" : "Desbloquear");
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    @FXML
    private void nuevo() {
        limpiarCampos();
        usuarioSeleccionado = null;
        txtNombreCompleto.requestFocus();
        ocultarMensaje();
    }
    
    /**
     * Guarda un usuario (nuevo o actualización)
     */
    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }
        
        try {
            Usuario usuario;
            boolean esNuevo = (usuarioSeleccionado == null);
            
            if (esNuevo) {
                usuario = new Usuario();
            } else {
                usuario = usuarioSeleccionado;
            }
            
            // Asignar valores del formulario
            usuario.setNombreCompleto(txtNombreCompleto.getText().trim());
            usuario.setUsername(txtUsername.getText().trim().toLowerCase());
            usuario.setRol(cmbRol.getValue());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setTelefono(txtTelefono.getText().trim());
            usuario.setActivo(chkActivo.isSelected());
            
            // Solo actualizar contraseña si se ingresó una nueva
            if (!txtPassword.getText().isEmpty()) {
                usuario.setPassword(txtPassword.getText()); // El setter ya hashea
            }
            
            boolean exito;
            if (esNuevo) {
                exito = usuarioDAO.agregar(usuario);
            } else {
                exito = usuarioDAO.actualizar(usuario);
            }
            
            if (exito) {
                mostrarMensajeExito(esNuevo ? "Usuario creado exitosamente" : 
                                             "Usuario actualizado exitosamente");
                cargarUsuarios();
                limpiarCampos();
                usuarioSeleccionado = null;
            } else {
                mostrarMensajeError("Error al guardar el usuario. Verifique que el username no esté duplicado.");
            }
            
        } catch (Exception e) {
            mostrarMensajeError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina el usuario seleccionado
     */
    @FXML
    private void eliminar() {
        if (usuarioSeleccionado == null) {
            mostrarMensajeError("Debe seleccionar un usuario para eliminar");
            return;
        }
        
        // No permitir eliminar al único administrador
        if ("ADMIN".equals(usuarioSeleccionado.getRol())) {
            long adminCount = listaUsuarios.stream()
                    .filter(u -> "ADMIN".equals(u.getRol()))
                    .count();
            if (adminCount <= 1) {
                mostrarMensajeError("No se puede eliminar al único administrador del sistema");
                return;
            }
        }
        
        // No permitir eliminar al usuario actual
        if (usuarioSeleccionado.getIdUsuario() == SessionManager.getIdUsuarioActual()) {
            mostrarMensajeError("No puede eliminar su propio usuario");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar este usuario?");
        confirmacion.setContentText(usuarioSeleccionado.getNombreCompleto() + " (" + usuarioSeleccionado.getUsername() + ")");
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = usuarioDAO.eliminar(usuarioSeleccionado.getIdUsuario());
            
            if (exito) {
                mostrarMensajeExito("Usuario eliminado exitosamente");
                cargarUsuarios();
                limpiarCampos();
                usuarioSeleccionado = null;
            } else {
                mostrarMensajeError("Error al eliminar el usuario");
            }
        }
    }
    
    /**
     * Cambia la contraseña del usuario seleccionado
     */
    @FXML
    private void cambiarPassword() {
        if (usuarioSeleccionado == null) {
            mostrarMensajeError("Debe seleccionar un usuario");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cambiar Contraseña");
        dialog.setHeaderText("Usuario: " + usuarioSeleccionado.getUsername());
        dialog.setContentText("Nueva contraseña:");
        
        Optional<String> resultado = dialog.showAndWait();
        
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            String nuevaPassword = resultado.get().trim();
            
            if (nuevaPassword.length() < 4) {
                mostrarMensajeError("La contraseña debe tener al menos 4 caracteres");
                return;
            }
            
            usuarioSeleccionado.setPassword(nuevaPassword); // El setter hashea automáticamente
            boolean exito = usuarioDAO.actualizar(usuarioSeleccionado);
            
            if (exito) {
                mostrarMensajeExito("Contraseña cambiada exitosamente");
            } else {
                mostrarMensajeError("Error al cambiar la contraseña");
            }
        }
    }
    
    /**
     * Bloquea o desbloquea un usuario
     */
    @FXML
    private void bloquearDesbloquear() {
        if (usuarioSeleccionado == null) {
            mostrarMensajeError("Debe seleccionar un usuario");
            return;
        }
        
        // No permitir bloquearse a sí mismo
        if (usuarioSeleccionado.getIdUsuario() == SessionManager.getIdUsuarioActual()) {
            mostrarMensajeError("No puede bloquear su propio usuario");
            return;
        }
        
        boolean estaActivo = usuarioSeleccionado.isActivo();
        String accion = estaActivo ? "bloquear" : "desbloquear";
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Está seguro que desea " + accion + " este usuario?");
        confirmacion.setContentText(usuarioSeleccionado.getNombreCompleto());
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            usuarioSeleccionado.setActivo(!estaActivo);
            boolean exito = usuarioDAO.actualizar(usuarioSeleccionado);
            
            if (exito) {
                mostrarMensajeExito("Usuario " + (estaActivo ? "bloqueado" : "desbloqueado") + " exitosamente");
                cargarUsuarios();
                btnBloquear.setText(!estaActivo ? "Bloquear" : "Desbloquear");
                chkActivo.setSelected(!estaActivo);
            } else {
                mostrarMensajeError("Error al actualizar el usuario");
            }
        }
    }
    
    /**
     * Busca usuarios según el criterio
     */
    @FXML
    private void buscar() {
        String criterio = txtBuscar.getText().trim().toLowerCase();
        
        if (criterio.isEmpty()) {
            cargarUsuarios();
            return;
        }
        
        List<Usuario> filtrados = listaUsuarios.stream()
                .filter(u -> 
                    (u.getNombreCompleto() != null && u.getNombreCompleto().toLowerCase().contains(criterio)) ||
                    (u.getUsername() != null && u.getUsername().toLowerCase().contains(criterio))
                )
                .collect(Collectors.toList());
        
        listaUsuarios.clear();
        listaUsuarios.addAll(filtrados);
        actualizarEstadisticas();
    }
    
    /**
     * Limpia la búsqueda y recarga todos los usuarios
     */
    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cargarUsuarios();
    }
    
    // ==================== FILTROS ====================
    
    @FXML
    private void filtrarTodos() {
        cargarUsuarios();
    }
    
    @FXML
    private void filtrarAdmin() {
        List<Usuario> admins = usuarioDAO.obtenerPorRol("ADMIN");
        listaUsuarios.clear();
        listaUsuarios.addAll(admins);
        actualizarEstadisticas();
    }
    
    @FXML
    private void filtrarVendedor() {
        List<Usuario> vendedores = usuarioDAO.obtenerPorRol("VENDEDOR");
        listaUsuarios.clear();
        listaUsuarios.addAll(vendedores);
        actualizarEstadisticas();
    }
    
    @FXML
    private void filtrarGerente() {
        List<Usuario> gerentes = usuarioDAO.obtenerPorRol("GERENTE");
        listaUsuarios.clear();
        listaUsuarios.addAll(gerentes);
        actualizarEstadisticas();
    }
    
    /**
     * Valida que todos los campos obligatorios estén completos
     */
    private boolean validarCampos() {
        if (txtNombreCompleto.getText().trim().isEmpty()) {
            mostrarMensajeError("El nombre completo es obligatorio");
            txtNombreCompleto.requestFocus();
            return false;
        }
        
        if (txtUsername.getText().trim().isEmpty()) {
            mostrarMensajeError("El username es obligatorio");
            txtUsername.requestFocus();
            return false;
        }
        
        if (usuarioSeleccionado == null && txtPassword.getText().isEmpty()) {
            mostrarMensajeError("La contraseña es obligatoria para usuarios nuevos");
            txtPassword.requestFocus();
            return false;
        }
        
        if (!txtPassword.getText().isEmpty() && txtPassword.getText().length() < 4) {
            mostrarMensajeError("La contraseña debe tener al menos 4 caracteres");
            txtPassword.requestFocus();
            return false;
        }
        
        if (cmbRol.getValue() == null) {
            mostrarMensajeError("Debe seleccionar un rol");
            cmbRol.requestFocus();
            return false;
        }
        
        // Validar email si no está vacío
        if (!txtEmail.getText().trim().isEmpty()) {
            String email = txtEmail.getText().trim();
            if (!email.contains("@") || !email.contains(".")) {
                mostrarMensajeError("El formato del email no es válido");
                txtEmail.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarCampos() {
        txtNombreCompleto.clear();
        txtUsername.clear();
        txtPassword.clear();
        cmbRol.setValue(null);
        txtEmail.clear();
        txtTelefono.clear();
        chkActivo.setSelected(true);
        lblUltimoAcceso.setText("Nunca");
        btnBloquear.setText("Bloquear");
        tablaUsuarios.getSelectionModel().clearSelection();
    }
    
    /**
     * Actualiza las estadísticas
     */
    private void actualizarEstadisticas() {
        lblTotalUsuarios.setText(String.valueOf(listaUsuarios.size()));
        
        int activos = (int) listaUsuarios.stream().filter(Usuario::isActivo).count();
        lblUsuariosActivos.setText(String.valueOf(activos));
        
        int admins = (int) listaUsuarios.stream()
                .filter(u -> "ADMIN".equals(u.getRol()))
                .count();
        lblAdmins.setText(String.valueOf(admins));
        
        int vendedores = (int) listaUsuarios.stream()
                .filter(u -> "VENDEDOR".equals(u.getRol()))
                .count();
        lblVendedores.setText(String.valueOf(vendedores));
    }
    
    /**
     * Muestra un mensaje de éxito
     */
    private void mostrarMensajeExito(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarMensajeError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-background-color: #fadbd8; -fx-text-fill: #e74c3c;");
        lblMensaje.setVisible(true);
    }
    
    /**
     * Muestra un mensaje de error con alerta
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Acceso Denegado");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Oculta el mensaje
     */
    private void ocultarMensaje() {
        lblMensaje.setVisible(false);
    }
    
    /**
     * Cierra la ventana
     */
    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnNuevo.getScene().getWindow();
        stage.close();
    }
}