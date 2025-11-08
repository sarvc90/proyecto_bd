package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.models.Cliente;
import com.taller.proyecto_bd.models.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

/**
 * Controlador para la gestión de clientes
 */
public class ClientesController {

    // ==================== CAMPOS DEL FORMULARIO ====================
    @FXML
    private TextField txtCedula;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellido;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtLimiteCredito;
    @FXML
    private TextField txtSaldoPendiente;
    @FXML
    private PasswordField txtPassword;  // Campo opcional para contraseña
    @FXML
    private Label lblCreditoDisponible;
    @FXML
    private CheckBox chkActivo;

    // ==================== BOTONES ====================
    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;

    // ==================== TABLA ====================
    @FXML
    private TextField txtBuscar;
    @FXML
    private TableView<Cliente> tablaClientes;
    @FXML
    private TableColumn<Cliente, String> colCedula;
    @FXML
    private TableColumn<Cliente, String> colNombreCompleto;
    @FXML
    private TableColumn<Cliente, String> colTelefono;
    @FXML
    private TableColumn<Cliente, String> colEmail;
    @FXML
    private TableColumn<Cliente, Double> colLimiteCredito;
    @FXML
    private TableColumn<Cliente, Double> colSaldoPendiente;
    @FXML
    private TableColumn<Cliente, Boolean> colActivo;

    // ==================== LABELS DE INFORMACIÓN ====================
    @FXML
    private Label lblMensaje;
    @FXML
    private Label lblTotalClientes;
    @FXML
    private Label lblClientesActivos;
    @FXML
    private Label lblConCredito;
    @FXML
    private Label lblTotalCartera;

    // ==================== ATRIBUTOS ====================
    private ClienteDAO clienteDAO;
    private ObservableList<Cliente> listaClientes;
    private Cliente clienteSeleccionado;
    private NumberFormat formatoMoneda;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        clienteDAO = ClienteDAO.getInstance();
        listaClientes = FXCollections.observableArrayList();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        configurarTabla();
        cargarClientes();
        configurarEventos();
        configurarPermisos();
        actualizarEstadisticas();
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colLimiteCredito.setCellValueFactory(new PropertyValueFactory<>("limiteCredito"));
        colSaldoPendiente.setCellValueFactory(new PropertyValueFactory<>("saldoPendiente"));

        // Columna de nombre completo
        colNombreCompleto.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombreCompleto()));

        // Columna de estado con formato
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setCellFactory(col -> new TableCell<Cliente, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Activo" : "Inactivo");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;"
                            : "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });

        // Formato de moneda para límite de crédito
        colLimiteCredito.setCellFactory(col -> new TableCell<Cliente, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatoMoneda.format(item));
                }
            }
        });

        // Formato de moneda para saldo pendiente
        colSaldoPendiente.setCellFactory(col -> new TableCell<Cliente, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatoMoneda.format(item));
                    // Resaltar saldos pendientes
                    if (item > 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        tablaClientes.setItems(listaClientes);
    }

    /**
     * Carga solo los clientes activos en la tabla
     */
    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.obtenerActivos();
        listaClientes.clear();
        listaClientes.addAll(clientes);
        actualizarEstadisticas();
    }

    /**
     * Configura los eventos de la interfaz
     */
    private void configurarEventos() {
        // Selección en la tabla
        tablaClientes.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        clienteSeleccionado = newSelection;
                        cargarDatosFormulario(newSelection);
                    }
                });

        // Validación de números en límite de crédito
        txtLimiteCredito.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtLimiteCredito.setText(old);
            } else {
                actualizarCreditoDisponible();
            }
        });

        // Validación de cédula (solo números)
        txtCedula.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCedula.setText(old);
            }
        });

        // Validación de teléfono (solo números)
        txtTelefono.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtTelefono.setText(old);
            }
        });
    }

    /**
     * Configura permisos según el rol del usuario
     */
    private void configurarPermisos() {
        boolean puedeEditar = SessionManager.puedeEditar();
        boolean puedeEliminar = SessionManager.puedeEliminar();

        btnNuevo.setDisable(!puedeEditar);
        btnGuardar.setDisable(!puedeEditar);
        btnEliminar.setDisable(!puedeEliminar);

        // Deshabilitar campos si no puede editar
        if (!puedeEditar) {
            deshabilitarCampos();
        }
    }

    /**
     * Carga los datos de un cliente en el formulario
     */
    private void cargarDatosFormulario(Cliente cliente) {
        txtCedula.setText(cliente.getCedula());
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtDireccion.setText(cliente.getDireccion());
        txtTelefono.setText(cliente.getTelefono());
        txtEmail.setText(cliente.getEmail());
        txtLimiteCredito.setText(String.valueOf(cliente.getLimiteCredito()));
        txtSaldoPendiente.setText(String.valueOf(cliente.getSaldoPendiente()));
        chkActivo.setSelected(cliente.isActivo());

        actualizarCreditoDisponible();
    }

    /**
     * Actualiza el label de crédito disponible
     */
    private void actualizarCreditoDisponible() {
        try {
            double limite = txtLimiteCredito.getText().isEmpty() ? 0 : Double.parseDouble(txtLimiteCredito.getText());
            double saldo = txtSaldoPendiente.getText().isEmpty() ? 0 : Double.parseDouble(txtSaldoPendiente.getText());
            double disponible = limite - saldo;
            disponible = Math.max(disponible, 0);

            lblCreditoDisponible.setText(formatoMoneda.format(disponible));
        } catch (NumberFormatException e) {
            lblCreditoDisponible.setText(formatoMoneda.format(0));
        }
    }

    /**
     * Limpia todos los campos del formulario
     */
    @FXML
    private void nuevo() {
        limpiarCampos();
        clienteSeleccionado = null;
        txtCedula.requestFocus();
        ocultarMensaje();
    }

    /**
     * Guarda un cliente (nuevo o actualización)
     */
    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        try {
            Cliente cliente;
            boolean esNuevo = (clienteSeleccionado == null);

            if (esNuevo) {
                cliente = new Cliente();
            } else {
                cliente = clienteSeleccionado;
            }

            // Asignar valores del formulario
            cliente.setCedula(txtCedula.getText().trim());
            cliente.setNombre(txtNombre.getText().trim());
            cliente.setApellido(txtApellido.getText().trim());
            cliente.setDireccion(txtDireccion.getText().trim());
            cliente.setTelefono(txtTelefono.getText().trim());
            cliente.setEmail(txtEmail.getText().trim());
            cliente.setLimiteCredito(Double.parseDouble(txtLimiteCredito.getText()));
            cliente.setActivo(chkActivo.isSelected());

            // Asignar contraseña si se proporcionó (opcional)
            if (txtPassword != null && !txtPassword.getText().trim().isEmpty()) {
                cliente.setPassword(txtPassword.getText()); // El setter encripta automáticamente
            }

            // El saldo pendiente solo se modifica en transacciones, no manualmente
            if (esNuevo) {
                cliente.setSaldoPendiente(0.0);
            }

            boolean exito;
            if (esNuevo) {
                exito = clienteDAO.agregar(cliente);
            } else {
                exito = clienteDAO.actualizar(cliente);
            }

            if (exito) {
                mostrarMensajeExito(esNuevo ? "Cliente guardado exitosamente" : "Cliente actualizado exitosamente");
                cargarClientes();
                limpiarCampos();
                clienteSeleccionado = null;
            } else {
                mostrarMensajeError("Error al guardar el cliente. Verifique que la cédula no esté duplicada.");
            }

        } catch (NumberFormatException e) {
            mostrarMensajeError("Error en los datos numéricos. Verifique los campos.");
        } catch (Exception e) {
            mostrarMensajeError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Elimina el cliente seleccionado (eliminación lógica - cambia estado a
     * inactivo)
     */
    @FXML
    private void eliminar() {
        if (clienteSeleccionado == null) {
            mostrarMensajeError("Debe seleccionar un cliente para eliminar");
            return;
        }

        // Validar que no tenga saldo pendiente
        if (clienteSeleccionado.getSaldoPendiente() > 0) {
            mostrarMensajeError("No se puede eliminar un cliente con saldo pendiente");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea inactivar este cliente?");
        confirmacion.setContentText(
                clienteSeleccionado.getNombreCompleto() + " - " + clienteSeleccionado.getCedula() + "\n\n" +
                        "El cliente será marcado como inactivo.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Cambiar estado a inactivo (eliminación lógica)
            clienteSeleccionado.setActivo(false);
            boolean exito = clienteDAO.actualizar(clienteSeleccionado);

            if (exito) {
                mostrarMensajeExito("Cliente inactivado exitosamente");
                cargarClientes();
                limpiarCampos();
                clienteSeleccionado = null;
            } else {
                mostrarMensajeError("Error al inactivar el cliente");
            }
        }
    }

    /**
     * Busca clientes según el criterio
     */
    @FXML
    private void buscar() {
        String criterio = txtBuscar.getText().trim();

        if (criterio.isEmpty()) {
            cargarClientes();
            return;
        }

        // Buscar por cédula primero
        Cliente porCedula = clienteDAO.obtenerPorCedula(criterio);
        if (porCedula != null) {
            listaClientes.clear();
            listaClientes.add(porCedula);
            actualizarEstadisticas();
            return;
        }

        // Si no encontró por cédula, buscar por nombre
        List<Cliente> resultados = clienteDAO.buscarPorNombre(criterio);
        listaClientes.clear();
        listaClientes.addAll(resultados);
        actualizarEstadisticas();
    }

    /**
     * Limpia la búsqueda y recarga todos los clientes
     */
    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cargarClientes();
    }

    /**
     * Valida que todos los campos obligatorios estén completos
     */
    private boolean validarCampos() {
        if (txtCedula.getText().trim().isEmpty()) {
            mostrarMensajeError("La cédula es obligatoria");
            txtCedula.requestFocus();
            return false;
        }

        if (txtNombre.getText().trim().isEmpty()) {
            mostrarMensajeError("El nombre es obligatorio");
            txtNombre.requestFocus();
            return false;
        }

        if (txtApellido.getText().trim().isEmpty()) {
            mostrarMensajeError("El apellido es obligatorio");
            txtApellido.requestFocus();
            return false;
        }

        if (txtTelefono.getText().trim().isEmpty()) {
            mostrarMensajeError("El teléfono es obligatorio");
            txtTelefono.requestFocus();
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

        // Validar límite de crédito
        try {
            double limite = Double.parseDouble(txtLimiteCredito.getText());
            if (limite < 0) {
                mostrarMensajeError("El límite de crédito no puede ser negativo");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El límite de crédito debe ser un valor numérico válido");
            return false;
        }

        return true;
    }

    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarCampos() {
        txtCedula.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtLimiteCredito.setText("0.00");
        txtSaldoPendiente.setText("0.00");
        if (txtPassword != null) {
            txtPassword.clear();  // Limpiar campo de contraseña
        }
        lblCreditoDisponible.setText(formatoMoneda.format(0));
        chkActivo.setSelected(true);
        tablaClientes.getSelectionModel().clearSelection();
    }

    /**
     * Deshabilita todos los campos del formulario
     */
    private void deshabilitarCampos() {
        txtCedula.setDisable(true);
        txtNombre.setDisable(true);
        txtApellido.setDisable(true);
        txtDireccion.setDisable(true);
        txtTelefono.setDisable(true);
        txtEmail.setDisable(true);
        txtLimiteCredito.setDisable(true);
        chkActivo.setDisable(true);
    }

    /**
     * Actualiza las estadísticas
     */
    private void actualizarEstadisticas() {
        lblTotalClientes.setText(String.valueOf(listaClientes.size()));

        int activos = (int) listaClientes.stream().filter(Cliente::isActivo).count();
        lblClientesActivos.setText(String.valueOf(activos));

        int conCredito = (int) listaClientes.stream()
                .filter(c -> c.getSaldoPendiente() > 0)
                .count();
        lblConCredito.setText(String.valueOf(conCredito));

        double totalCartera = listaClientes.stream()
                .mapToDouble(Cliente::getSaldoPendiente)
                .sum();
        lblTotalCartera.setText(formatoMoneda.format(totalCartera));
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
        try {
            SessionManager.cerrarSesion();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnNuevo.getScene().getWindow();
            stage.setTitle("Sistema de Electrodomésticos - Login");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarMensajeError("No se pudo mostrar la pantalla de login: " + e.getMessage());
        }
    }
}