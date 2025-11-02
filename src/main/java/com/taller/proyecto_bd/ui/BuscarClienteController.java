package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.models.Cliente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controlador para la ventana de búsqueda rápida de clientes
 */
public class BuscarClienteController {

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colCedula;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private Button btnSeleccionar;
    @FXML private Button btnCancelar;

    private ClienteDAO clienteDAO;
    private ObservableList<Cliente> listaClientes;
    private Cliente clienteSeleccionado;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        clienteDAO = ClienteDAO.getInstance();

        configurarTabla();
        cargarClientes();
        configurarEventos();
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));

        colNombre.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNombre() + " " +
                                    cellData.getValue().getApellido()));

        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    /**
     * Configura los eventos
     */
    private void configurarEventos() {
        // Habilitar botón seleccionar solo cuando hay selección
        tblClientes.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                btnSeleccionar.setDisable(newValue == null);
            }
        );

        // Doble clic para seleccionar
        tblClientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblClientes.getSelectionModel().getSelectedItem() != null) {
                seleccionar();
            }
        });

        // Enter en el campo de búsqueda
        txtBuscar.setOnAction(event -> buscar());
    }

    /**
     * Carga todos los clientes activos
     */
    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.obtenerTodos().stream()
            .filter(Cliente::isActivo)
            .toList();

        listaClientes = FXCollections.observableArrayList(clientes);
        tblClientes.setItems(listaClientes);
    }

    /**
     * Busca clientes por texto
     */
    @FXML
    private void buscar() {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            cargarClientes();
            return;
        }

        List<Cliente> clientesFiltrados = clienteDAO.obtenerTodos().stream()
            .filter(Cliente::isActivo)
            .filter(c ->
                c.getCedula().toLowerCase().contains(textoBusqueda) ||
                c.getNombre().toLowerCase().contains(textoBusqueda) ||
                c.getApellido().toLowerCase().contains(textoBusqueda) ||
                (c.getTelefono() != null && c.getTelefono().toLowerCase().contains(textoBusqueda)) ||
                (c.getEmail() != null && c.getEmail().toLowerCase().contains(textoBusqueda))
            )
            .toList();

        listaClientes = FXCollections.observableArrayList(clientesFiltrados);
        tblClientes.setItems(listaClientes);

        if (clientesFiltrados.isEmpty()) {
            mostrarInformacion("Sin resultados", "No se encontraron clientes que coincidan con la búsqueda");
        }
    }

    /**
     * Selecciona el cliente y cierra la ventana
     */
    @FXML
    private void seleccionar() {
        clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            mostrarError("Debe seleccionar un cliente de la lista");
            return;
        }

        cerrarVentana();
    }

    /**
     * Cancela y cierra la ventana
     */
    @FXML
    private void cancelar() {
        clienteSeleccionado = null;
        cerrarVentana();
    }

    /**
     * Cierra la ventana
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Obtiene el cliente seleccionado
     */
    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    /**
     * Muestra un mensaje de información
     */
    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}