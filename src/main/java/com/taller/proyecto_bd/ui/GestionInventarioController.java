package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.InventarioDAO;
import com.taller.proyecto_bd.dao.ProductoDAO;
import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.models.Inventario;
import com.taller.proyecto_bd.models.Producto;
import com.taller.proyecto_bd.models.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de inventario
 */
public class GestionInventarioController {

    // ==================== FILTROS Y BÚSQUEDA ====================
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnActualizar;

    // ==================== ESTADÍSTICAS ====================
    @FXML private Label lblTotalProductos;
    @FXML private Label lblBajoStock;
    @FXML private Label lblSobreStock;
    @FXML private Label lblValorTotal;

    // ==================== TABLA ====================
    @FXML private TableView<Inventario> tblInventario;
    @FXML private TableColumn<Inventario, Integer> colIdInventario;
    @FXML private TableColumn<Inventario, String> colProducto;
    @FXML private TableColumn<Inventario, String> colCategoria;
    @FXML private TableColumn<Inventario, Integer> colCantidadActual;
    @FXML private TableColumn<Inventario, Integer> colStockMinimo;
    @FXML private TableColumn<Inventario, Integer> colStockMaximo;
    @FXML private TableColumn<Inventario, String> colEstado;
    @FXML private TableColumn<Inventario, String> colUltimaActualizacion;

    // ==================== BOTONES DE ACCIÓN ====================
    @FXML private Button btnEntrada;
    @FXML private Button btnSalida;
    @FXML private Button btnAjustar;
    @FXML private Button btnEditar;

    // ==================== DAOs ====================
    private InventarioDAO inventarioDAO;
    private ProductoDAO productoDAO;
    private AuditoriaDAO auditoriaDAO;

    // ==================== DATOS ====================
    private ObservableList<Inventario> listaInventario;
    private SimpleDateFormat formatoFecha;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        // Inicializar DAOs
        inventarioDAO = InventarioDAO.getInstance();
        productoDAO = ProductoDAO.getInstance();
        auditoriaDAO = AuditoriaDAO.getInstance();

        // Inicializar formateador
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Configurar componentes
        configurarTabla();
        configurarFiltros();
        configurarEventos();

        // Cargar datos iniciales
        cargarInventario("TODOS");
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colIdInventario.setCellValueFactory(new PropertyValueFactory<>("idInventario"));
        colProducto.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNombreProducto()));
        colCategoria.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategoria()));
        colCantidadActual.setCellValueFactory(new PropertyValueFactory<>("cantidadActual"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colStockMaximo.setCellValueFactory(new PropertyValueFactory<>("stockMaximo"));

        // Columna de estado con estilo
        colEstado.setCellValueFactory(cellData -> {
            Inventario inv = cellData.getValue();
            String estado;
            if (inv.necesitaReposicion()) {
                estado = "BAJO STOCK";
            } else if (inv.sobreStock()) {
                estado = "SOBRE STOCK";
            } else {
                estado = "NORMAL";
            }
            return new SimpleStringProperty(estado);
        });

        colEstado.setCellFactory(column -> new TableCell<Inventario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "BAJO STOCK":
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                            break;
                        case "SOBRE STOCK":
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Columna de fecha
        colUltimaActualizacion.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUltimaActualizacion() != null) {
                return new SimpleStringProperty(
                    formatoFecha.format(cellData.getValue().getUltimaActualizacion()));
            }
            return new SimpleStringProperty("");
        });
    }

    /**
     * Configura los filtros
     */
    private void configurarFiltros() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "TODOS", "NORMAL", "BAJO STOCK", "SOBRE STOCK"
        ));
        cmbFiltroEstado.setValue("TODOS");
    }

    /**
     * Configura los eventos
     */
    private void configurarEventos() {
        // Selección de tabla para habilitar botones
        tblInventario.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean haySeleccion = newValue != null;
                btnEntrada.setDisable(!haySeleccion);
                btnSalida.setDisable(!haySeleccion);
                btnAjustar.setDisable(!haySeleccion);
                btnEditar.setDisable(!haySeleccion);
            }
        );

        // Doble clic para ver detalles
        tblInventario.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblInventario.getSelectionModel().getSelectedItem() != null) {
                verDetalles();
            }
        });

        // Filtro de estado
        cmbFiltroEstado.setOnAction(event -> {
            String filtro = cmbFiltroEstado.getValue();
            cargarInventario(filtro);
        });
    }

    /**
     * Carga el inventario según el filtro seleccionado
     */
    private void cargarInventario(String filtro) {
        List<Inventario> inventarios;

        switch (filtro) {
            case "BAJO STOCK":
                inventarios = inventarioDAO.obtenerBajoStock();
                break;
            case "SOBRE STOCK":
                inventarios = inventarioDAO.obtenerSobreStock();
                break;
            case "NORMAL":
                inventarios = inventarioDAO.obtenerTodos().stream()
                    .filter(inv -> !inv.necesitaReposicion() && !inv.sobreStock())
                    .toList();
                break;
            default: // TODOS
                inventarios = inventarioDAO.obtenerTodos();
                break;
        }

        listaInventario = FXCollections.observableArrayList(inventarios);
        tblInventario.setItems(listaInventario);

        actualizarEstadisticas();
    }

    /**
     * Actualiza las estadísticas del panel superior
     */
    private void actualizarEstadisticas() {
        List<Inventario> todos = inventarioDAO.obtenerTodos();

        lblTotalProductos.setText(String.valueOf(todos.size()));

        long bajoStock = todos.stream().filter(Inventario::necesitaReposicion).count();
        lblBajoStock.setText(String.valueOf(bajoStock));

        long sobreStock = todos.stream().filter(Inventario::sobreStock).count();
        lblSobreStock.setText(String.valueOf(sobreStock));

        // Calcular valor total del inventario (opcional)
        double valorTotal = 0;
        for (Inventario inv : todos) {
            Producto p = productoDAO.obtenerPorId(inv.getIdProducto());
            if (p != null) {
                valorTotal += p.getPrecioVenta() * inv.getCantidadActual();
            }
        }
        lblValorTotal.setText(String.format("$%,.2f", valorTotal));
    }

    /**
     * Buscar inventario por nombre de producto
     */
    @FXML
    private void buscar() {
        String busqueda = txtBuscar.getText().trim().toLowerCase();

        if (busqueda.isEmpty()) {
            cargarInventario(cmbFiltroEstado.getValue());
            return;
        }

        List<Inventario> inventarios = inventarioDAO.obtenerTodos().stream()
            .filter(inv -> inv.getNombreProducto().toLowerCase().contains(busqueda) ||
                          (inv.getCategoria() != null && inv.getCategoria().toLowerCase().contains(busqueda)))
            .toList();

        listaInventario = FXCollections.observableArrayList(inventarios);
        tblInventario.setItems(listaInventario);
    }

    /**
     * Actualizar la tabla
     */
    @FXML
    private void actualizar() {
        cargarInventario(cmbFiltroEstado.getValue());
        mostrarInformacion("Actualizado", "Inventario actualizado correctamente");
    }

    /**
     * Registrar entrada de productos
     */
    @FXML
    private void registrarEntrada() {
        Inventario seleccionado = tblInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un producto del inventario");
            return;
        }

        // Crear diálogo para ingresar cantidad
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Registrar Entrada");
        dialog.setHeaderText("Entrada de: " + seleccionado.getNombreProducto());
        dialog.setContentText("Cantidad a ingresar:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(cantidadStr -> {
            try {
                int cantidad = Integer.parseInt(cantidadStr);
                if (cantidad <= 0) {
                    mostrarError("La cantidad debe ser mayor a cero");
                    return;
                }

                if (inventarioDAO.registrarEntrada(seleccionado.getIdProducto(), cantidad)) {
                    mostrarInformacion("Éxito", "Entrada registrada correctamente");
                    cargarInventario(cmbFiltroEstado.getValue());
                } else {
                    mostrarError("Error al registrar la entrada");
                }
            } catch (NumberFormatException e) {
                mostrarError("Debe ingresar un número válido");
            }
        });
    }

    /**
     * Registrar salida de productos
     */
    @FXML
    private void registrarSalida() {
        Inventario seleccionado = tblInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un producto del inventario");
            return;
        }

        // Crear diálogo para ingresar cantidad
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Registrar Salida");
        dialog.setHeaderText("Salida de: " + seleccionado.getNombreProducto());
        dialog.setContentText("Cantidad disponible: " + seleccionado.getCantidadActual() +
                             "\nCantidad a retirar:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(cantidadStr -> {
            try {
                int cantidad = Integer.parseInt(cantidadStr);
                if (cantidad <= 0) {
                    mostrarError("La cantidad debe ser mayor a cero");
                    return;
                }

                if (cantidad > seleccionado.getCantidadActual()) {
                    mostrarError("No hay suficiente stock disponible");
                    return;
                }

                if (inventarioDAO.registrarSalida(seleccionado.getIdProducto(), cantidad)) {
                    mostrarInformacion("Éxito", "Salida registrada correctamente");
                    cargarInventario(cmbFiltroEstado.getValue());
                } else {
                    mostrarError("Error al registrar la salida");
                }
            } catch (NumberFormatException e) {
                mostrarError("Debe ingresar un número válido");
            }
        });
    }

    /**
     * Ajustar stock manualmente
     */
    @FXML
    private void ajustarStock() {
        Inventario seleccionado = tblInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un producto del inventario");
            return;
        }

        // Crear diálogo personalizado
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Ajustar Stock");
        dialog.setHeaderText("Ajuste de: " + seleccionado.getNombreProducto() +
                           "\nCantidad actual: " + seleccionado.getCantidadActual());

        ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtNuevaCantidad = new TextField();
        txtNuevaCantidad.setPromptText("Nueva cantidad");

        grid.add(new Label("Nueva cantidad en stock:"), 0, 0);
        grid.add(txtNuevaCantidad, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAceptar) {
                try {
                    return Integer.parseInt(txtNuevaCantidad.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Integer> resultado = dialog.showAndWait();
        resultado.ifPresent(nuevaCantidad -> {
            if (nuevaCantidad == null || nuevaCantidad < 0) {
                mostrarError("Debe ingresar una cantidad válida (mayor o igual a 0)");
                return;
            }

            seleccionado.setCantidadActual(nuevaCantidad);
            if (inventarioDAO.actualizar(seleccionado)) {
                mostrarInformacion("Éxito", "Stock ajustado correctamente");
                cargarInventario(cmbFiltroEstado.getValue());
            } else {
                mostrarError("Error al ajustar el stock");
            }
        });
    }

    /**
     * Editar límites de stock (mínimo y máximo)
     */
    @FXML
    private void editarLimites() {
        Inventario seleccionado = tblInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un producto del inventario");
            return;
        }

        // Crear diálogo personalizado
        Dialog<Inventario> dialog = new Dialog<>();
        dialog.setTitle("Editar Límites de Stock");
        dialog.setHeaderText("Producto: " + seleccionado.getNombreProducto());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtStockMinimo = new TextField(String.valueOf(seleccionado.getStockMinimo()));
        TextField txtStockMaximo = new TextField(String.valueOf(seleccionado.getStockMaximo()));

        grid.add(new Label("Stock Mínimo:"), 0, 0);
        grid.add(txtStockMinimo, 1, 0);
        grid.add(new Label("Stock Máximo:"), 0, 1);
        grid.add(txtStockMaximo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    seleccionado.setStockMinimo(Integer.parseInt(txtStockMinimo.getText()));
                    seleccionado.setStockMaximo(Integer.parseInt(txtStockMaximo.getText()));
                    return seleccionado;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Inventario> resultado = dialog.showAndWait();
        resultado.ifPresent(inventario -> {
            if (inventario == null) {
                mostrarError("Debe ingresar valores numéricos válidos");
                return;
            }

            if (inventario.getStockMinimo() < 0 || inventario.getStockMaximo() < 0 ||
                inventario.getStockMaximo() < inventario.getStockMinimo()) {
                mostrarError("Los valores deben ser válidos (mínimo >= 0, máximo >= mínimo)");
                return;
            }

            if (inventarioDAO.actualizar(inventario)) {
                mostrarInformacion("Éxito", "Límites actualizados correctamente");
                cargarInventario(cmbFiltroEstado.getValue());
            } else {
                mostrarError("Error al actualizar los límites");
            }
        });
    }

    /**
     * Ver detalles del inventario seleccionado
     */
    private void verDetalles() {
        Inventario seleccionado = tblInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        Producto producto = productoDAO.obtenerPorId(seleccionado.getIdProducto());
        if (producto == null) {
            mostrarError("No se encontró información del producto");
            return;
        }

        StringBuilder detalles = new StringBuilder();
        detalles.append("INFORMACIÓN DEL INVENTARIO\n\n");
        detalles.append("Producto: ").append(seleccionado.getNombreProducto()).append("\n");
        detalles.append("Categoría: ").append(seleccionado.getCategoria()).append("\n");
        detalles.append("Código: ").append(producto.getCodigo()).append("\n\n");

        detalles.append("STOCK\n");
        detalles.append("Cantidad Actual: ").append(seleccionado.getCantidadActual()).append("\n");
        detalles.append("Stock Mínimo: ").append(seleccionado.getStockMinimo()).append("\n");
        detalles.append("Stock Máximo: ").append(seleccionado.getStockMaximo()).append("\n");

        String estado;
        if (seleccionado.necesitaReposicion()) {
            estado = "⚠️ BAJO STOCK - Requiere reposición";
        } else if (seleccionado.sobreStock()) {
            estado = "⚠️ SOBRE STOCK";
        } else {
            estado = "✓ NORMAL";
        }
        detalles.append("Estado: ").append(estado).append("\n\n");

        detalles.append("INFORMACIÓN ADICIONAL\n");
        detalles.append("Precio de Venta: $").append(String.format("%,.2f", producto.getPrecioVenta())).append("\n");
        double valorInventario = producto.getPrecioVenta() * seleccionado.getCantidadActual();
        detalles.append("Valor en Inventario: $").append(String.format("%,.2f", valorInventario)).append("\n");

        if (seleccionado.getUltimaActualizacion() != null) {
            detalles.append("Última Actualización: ").append(formatoFecha.format(seleccionado.getUltimaActualizacion()));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Inventario");
        alert.setHeaderText(null);
        alert.setContentText(detalles.toString());
        alert.showAndWait();
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