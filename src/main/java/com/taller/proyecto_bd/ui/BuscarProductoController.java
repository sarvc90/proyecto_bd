package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.ProductoDAO;
import com.taller.proyecto_bd.dao.CategoriaDAO;
import com.taller.proyecto_bd.models.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Controlador para la ventana de búsqueda rápida de productos
 */
public class BuscarProductoController {

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private Button btnSeleccionar;
    @FXML private Button btnCancelar;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private ObservableList<Producto> listaProductos;
    private Producto productoSeleccionado;
    private NumberFormat formatoMoneda;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        productoDAO = ProductoDAO.getInstance();
        categoriaDAO = CategoriaDAO.getInstance();
        formatoMoneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

        configurarTabla();
        cargarProductos();
        configurarEventos();
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colCategoria.setCellValueFactory(cellData -> {
            int idCategoria = cellData.getValue().getIdCategoria();
            var categoria = categoriaDAO.obtenerPorId(idCategoria);
            return new SimpleStringProperty(categoria != null ? categoria.getNombre() : "Sin categoría");
        });

        colPrecio.setCellValueFactory(cellData ->
            new SimpleStringProperty(formatoMoneda.format(cellData.getValue().getPrecioVenta())));

        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));

        // Estilo para productos sin stock
        colStock.setCellFactory(column -> new TableCell<Producto, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item == 0) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                    } else if (item <= 5) {
                        setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    /**
     * Configura los eventos
     */
    private void configurarEventos() {
        // Habilitar botón seleccionar solo cuando hay selección
        tblProductos.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                btnSeleccionar.setDisable(newValue == null);
            }
        );

        // Doble clic para seleccionar
        tblProductos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblProductos.getSelectionModel().getSelectedItem() != null) {
                seleccionar();
            }
        });

        // Enter en el campo de búsqueda
        txtBuscar.setOnAction(event -> buscar());
    }

    /**
     * Carga todos los productos activos
     */
    private void cargarProductos() {
        List<Producto> productos = productoDAO.obtenerActivos();

        listaProductos = FXCollections.observableArrayList(productos);
        tblProductos.setItems(listaProductos);
    }

    /**
     * Busca productos por texto
     */
    @FXML
    private void buscar() {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            cargarProductos();
            return;
        }

        List<Producto> productosFiltrados = productoDAO.obtenerActivos().stream()
            .filter(p ->
                p.getCodigo().toLowerCase().contains(textoBusqueda) ||
                p.getNombre().toLowerCase().contains(textoBusqueda) ||
                (p.getMarca() != null && p.getMarca().toLowerCase().contains(textoBusqueda)) ||
                (p.getModelo() != null && p.getModelo().toLowerCase().contains(textoBusqueda))
            )
            .toList();

        listaProductos = FXCollections.observableArrayList(productosFiltrados);
        tblProductos.setItems(listaProductos);

        if (productosFiltrados.isEmpty()) {
            mostrarInformacion("Sin resultados", "No se encontraron productos que coincidan con la búsqueda");
        }
    }

    /**
     * Selecciona el producto y cierra la ventana
     */
    @FXML
    private void seleccionar() {
        productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            mostrarError("Debe seleccionar un producto de la lista");
            return;
        }

        if (productoSeleccionado.getStockActual() == 0) {
            Alert confirmacion = new Alert(Alert.AlertType.WARNING);
            confirmacion.setTitle("Producto sin stock");
            confirmacion.setHeaderText("El producto seleccionado no tiene stock disponible");
            confirmacion.setContentText("¿Desea seleccionarlo de todos modos?");
            confirmacion.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) {
                return;
            }
        }

        cerrarVentana();
    }

    /**
     * Cancela y cierra la ventana
     */
    @FXML
    private void cancelar() {
        productoSeleccionado = null;
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
     * Obtiene el producto seleccionado
     */
    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
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