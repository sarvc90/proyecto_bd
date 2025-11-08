package com.taller.proyecto_bd.ui;



import com.taller.proyecto_bd.dao.ProductoDAO;
import com.taller.proyecto_bd.dao.CategoriaDAO;
import com.taller.proyecto_bd.dao.InventarioDAO;
import com.taller.proyecto_bd.models.Producto;
import com.taller.proyecto_bd.models.SessionManager;
import com.taller.proyecto_bd.models.Categoria;
import com.taller.proyecto_bd.models.Inventario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador para la gestión de productos
 */
public class ProductosController {

    // ==================== CAMPOS DEL FORMULARIO ====================
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModelo;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStockActual;
    @FXML private TextField txtStockMinimo;
    @FXML private TextField txtStockMaximo;
    @FXML private TextField txtGarantia;
    @FXML private TextField txtUbicacion;
    @FXML private CheckBox chkActivo;
    
    // ==================== BOTONES ====================
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    
    // ==================== TABLA ====================
    @FXML private TextField txtBuscar;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Double> colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Boolean> colActivo;
    
    // ==================== LABELS DE INFORMACIÓN ====================
    @FXML private Label lblMensaje;
    @FXML private Label lblTotalProductos;
    @FXML private Label lblProductosActivos;
    @FXML private Label lblValorInventario;
    
    // ==================== ATRIBUTOS ====================
    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private InventarioDAO inventarioDAO;
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
        inventarioDAO = InventarioDAO.getInstance();
        listaProductos = FXCollections.observableArrayList();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        configurarTabla();
        cargarCategorias();
        cargarProductos();
        configurarEventos();
        configurarPermisos();
        actualizarEstadisticas();
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        
        // Columna de estado con formato
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setCellFactory(col -> new TableCell<Producto, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Activo" : "Inactivo");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" : 
                                   "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        // Formato de precio
        colPrecioVenta.setCellFactory(col -> new TableCell<Producto, Double>() {
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
        
        tablaProductos.setItems(listaProductos);
    }
    
    /**
     * Carga las categorías en el ComboBox
     */
    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.obtenerTodas();
        cmbCategoria.setItems(FXCollections.observableArrayList(categorias));
        
        // Configurar cómo se muestra cada categoría
        cmbCategoria.setCellFactory(param -> new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreCompleto());
                }
            }
        });
        
        cmbCategoria.setButtonCell(new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreCompleto());
                }
            }
        });
    }
    
    /**
     * Carga todos los productos en la tabla
     */
    private void cargarProductos() {
        List<Producto> productos = productoDAO.obtenerTodos();
        
        // Agregar nombre de categoría a cada producto
        for (Producto p : productos) {
            Categoria cat = categoriaDAO.obtenerPorId(p.getIdCategoria());
            if (cat != null) {
                p.setNombreCategoria(cat.getNombre());
            }
        }
        
        listaProductos.clear();
        listaProductos.addAll(productos);
        actualizarEstadisticas();
    }
    
    /**
     * Configura los eventos de la interfaz
     */
    private void configurarEventos() {
        // Selección en la tabla
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    productoSeleccionado = newSelection;
                    cargarDatosFormulario(newSelection);
                }
            }
        );
        
        // Validación de números en campos numéricos
        txtPrecioCompra.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPrecioCompra.setText(old);
            }
        });
        
        txtPrecioVenta.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPrecioVenta.setText(old);
            }
        });
        
        txtStockActual.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStockActual.setText(old);
            }
        });
        
        txtStockMinimo.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStockMinimo.setText(old);
            }
        });
        
        txtStockMaximo.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStockMaximo.setText(old);
            }
        });
        
        txtGarantia.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtGarantia.setText(old);
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
     * Carga los datos de un producto en el formulario
     */
    private void cargarDatosFormulario(Producto producto) {
        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        txtMarca.setText(producto.getMarca());
        txtModelo.setText(producto.getModelo());
        txtDescripcion.setText(producto.getDescripcion());
        txtPrecioCompra.setText(String.valueOf(producto.getPrecioCompra()));
        txtPrecioVenta.setText(String.valueOf(producto.getPrecioVenta()));
        txtStockActual.setText(String.valueOf(producto.getStockActual()));
        txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
        txtStockMaximo.setText(String.valueOf(producto.getStockMaximo()));
        txtGarantia.setText(String.valueOf(producto.getGarantiaMeses()));
        txtUbicacion.setText(producto.getUbicacionAlmacen());
        chkActivo.setSelected(producto.isActivo());
        
        // Seleccionar la categoría
        Categoria cat = categoriaDAO.obtenerPorId(producto.getIdCategoria());
        if (cat != null) {
            cmbCategoria.setValue(cat);
        }
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    @FXML
    private void nuevo() {
        limpiarCampos();
        productoSeleccionado = null;
        txtCodigo.requestFocus();
        ocultarMensaje();
    }
    
    /**
     * Guarda un producto (nuevo o actualización)
     */
    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }
        
        try {
            Producto producto;
            boolean esNuevo = (productoSeleccionado == null);
            
            if (esNuevo) {
                producto = new Producto();
            } else {
                producto = productoSeleccionado;
            }
            
            // Asignar valores del formulario
            producto.setCodigo(txtCodigo.getText().trim().toUpperCase());
            producto.setNombre(txtNombre.getText().trim());
            producto.setMarca(txtMarca.getText().trim());
            producto.setModelo(txtModelo.getText().trim());
            producto.setDescripcion(txtDescripcion.getText().trim());
            producto.setIdCategoria(cmbCategoria.getValue().getIdCategoria());
            producto.setPrecioCompra(Double.parseDouble(txtPrecioCompra.getText()));
            producto.setPrecioVenta(Double.parseDouble(txtPrecioVenta.getText()));
            producto.setStockActual(Integer.parseInt(txtStockActual.getText()));
            producto.setStockMinimo(Integer.parseInt(txtStockMinimo.getText()));
            producto.setStockMaximo(Integer.parseInt(txtStockMaximo.getText()));
            producto.setGarantiaMeses(Integer.parseInt(txtGarantia.getText()));
            producto.setUbicacionAlmacen(txtUbicacion.getText().trim());
            producto.setActivo(chkActivo.isSelected());
            
            boolean exito;
            if (esNuevo) {
                exito = productoDAO.agregar(producto);

                // Si se guardó exitosamente, crear el inventario automáticamente
                if (exito && producto.getIdProducto() > 0) {
                    System.out.println("DEBUG: Creando inventario para producto ID: " + producto.getIdProducto());

                    Inventario inventario = new Inventario();
                    inventario.setIdProducto(producto.getIdProducto());
                    inventario.setCantidadActual(producto.getStockActual());
                    inventario.setStockMinimo(producto.getStockMinimo());
                    inventario.setStockMaximo(producto.getStockMaximo());

                    boolean inventarioCreado = inventarioDAO.agregar(inventario);
                    if (inventarioCreado) {
                        System.out.println("DEBUG: Inventario creado exitosamente para producto: " + producto.getNombre());
                    } else {
                        System.err.println("ADVERTENCIA: El producto se guardó pero no se pudo crear el inventario");
                    }
                }
            } else {
                exito = productoDAO.actualizar(producto);

                // Si se actualizó el producto, actualizar también el inventario
                if (exito) {
                    Inventario inventarioExistente = inventarioDAO.obtenerPorProducto(producto.getIdProducto());
                    if (inventarioExistente != null) {
                        inventarioExistente.setCantidadActual(producto.getStockActual());
                        inventarioExistente.setStockMinimo(producto.getStockMinimo());
                        inventarioExistente.setStockMaximo(producto.getStockMaximo());
                        inventarioDAO.actualizar(inventarioExistente);
                    } else {
                        // Si no existe inventario, crearlo
                        System.out.println("DEBUG: Producto sin inventario detectado. Creando inventario...");
                        Inventario nuevoInventario = new Inventario();
                        nuevoInventario.setIdProducto(producto.getIdProducto());
                        nuevoInventario.setCantidadActual(producto.getStockActual());
                        nuevoInventario.setStockMinimo(producto.getStockMinimo());
                        nuevoInventario.setStockMaximo(producto.getStockMaximo());
                        inventarioDAO.agregar(nuevoInventario);
                    }
                }
            }

            if (exito) {
                mostrarMensajeExito(esNuevo ? "Producto e inventario guardados exitosamente" :
                                             "Producto e inventario actualizados exitosamente");
                cargarProductos();
                limpiarCampos();
                productoSeleccionado = null;
            } else {
                mostrarMensajeError("Error al guardar el producto. Verifique que el código no esté duplicado.");
            }
            
        } catch (NumberFormatException e) {
            mostrarMensajeError("Error en los datos numéricos. Verifique los campos.");
        } catch (Exception e) {
            mostrarMensajeError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina el producto seleccionado
     * Si el producto tiene ventas asociadas, ofrece marcarlo como inactivo en su lugar
     */
    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mostrarMensajeError("Debe seleccionar un producto para eliminar");
            return;
        }

        // Verificar si tiene ventas asociadas
        boolean tieneVentas = productoDAO.tieneVentasAsociadas(productoSeleccionado.getIdProducto());

        if (tieneVentas) {
            // Si tiene ventas, ofrecer marcar como inactivo
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("No se puede eliminar");
            alerta.setHeaderText("El producto tiene ventas asociadas");
            alerta.setContentText("Este producto no puede ser eliminado porque ya tiene ventas registradas.\n\n" +
                    "¿Desea marcarlo como INACTIVO en su lugar?\n\n" +
                    "Producto: " + productoSeleccionado.getNombreCompleto());

            ButtonType btnInactivar = new ButtonType("Marcar como Inactivo");
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alerta.getButtonTypes().setAll(btnInactivar, btnCancelar);

            Optional<ButtonType> resultado = alerta.showAndWait();

            if (resultado.isPresent() && resultado.get() == btnInactivar) {
                // Marcar como inactivo
                productoSeleccionado.setActivo(false);
                boolean exito = productoDAO.actualizar(productoSeleccionado);

                if (exito) {
                    mostrarMensajeExito("Producto marcado como inactivo exitosamente");
                    cargarProductos();
                    limpiarCampos();
                    productoSeleccionado = null;
                } else {
                    mostrarMensajeError("Error al actualizar el producto");
                }
            }
        } else {
            // Si NO tiene ventas, se puede eliminar permanentemente
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("¿Está seguro que desea eliminar este producto?");
            confirmacion.setContentText("Esta acción NO se puede deshacer.\n\n" +
                    "Producto: " + productoSeleccionado.getNombreCompleto());

            Optional<ButtonType> resultado = confirmacion.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean exito = productoDAO.eliminar(productoSeleccionado.getIdProducto());

                if (exito) {
                    mostrarMensajeExito("Producto eliminado exitosamente");
                    cargarProductos();
                    limpiarCampos();
                    productoSeleccionado = null;
                } else {
                    mostrarMensajeError("Error al eliminar el producto. Verifique que no tenga dependencias.");
                }
            }
        }
    }
    
    /**
     * Busca productos según el criterio
     */
    @FXML
    private void buscar() {
        String criterio = txtBuscar.getText().trim();
        
        if (criterio.isEmpty()) {
            cargarProductos();
            return;
        }
        
        List<Producto> resultados = productoDAO.buscarPorNombreOMarca(criterio);
        
        // Agregar nombre de categoría
        for (Producto p : resultados) {
            Categoria cat = categoriaDAO.obtenerPorId(p.getIdCategoria());
            if (cat != null) {
                p.setNombreCategoria(cat.getNombre());
            }
        }
        
        listaProductos.clear();
        listaProductos.addAll(resultados);
        actualizarEstadisticas();
    }
    
    /**
     * Limpia la búsqueda y recarga todos los productos
     */
    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cargarProductos();
    }
    
    /**
     * Valida que todos los campos obligatorios estén completos
     */
    private boolean validarCampos() {
        if (txtCodigo.getText().trim().isEmpty()) {
            mostrarMensajeError("El código es obligatorio");
            txtCodigo.requestFocus();
            return false;
        }
        
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarMensajeError("El nombre es obligatorio");
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtMarca.getText().trim().isEmpty()) {
            mostrarMensajeError("La marca es obligatoria");
            txtMarca.requestFocus();
            return false;
        }
        
        if (cmbCategoria.getValue() == null) {
            mostrarMensajeError("Debe seleccionar una categoría");
            cmbCategoria.requestFocus();
            return false;
        }
        
        try {
            double precioCompra = Double.parseDouble(txtPrecioCompra.getText());
            double precioVenta = Double.parseDouble(txtPrecioVenta.getText());
            
            if (precioCompra < 0 || precioVenta < 0) {
                mostrarMensajeError("Los precios no pueden ser negativos");
                return false;
            }
            
            if (precioVenta < precioCompra) {
                mostrarMensajeError("El precio de venta no puede ser menor al precio de compra");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("Los precios deben ser valores numéricos válidos");
            return false;
        }
        
        try {
            int stockActual = Integer.parseInt(txtStockActual.getText());
            int stockMin = Integer.parseInt(txtStockMinimo.getText());
            int stockMax = Integer.parseInt(txtStockMaximo.getText());
            
            if (stockActual < 0 || stockMin < 0 || stockMax < 0) {
                mostrarMensajeError("Los valores de stock no pueden ser negativos");
                return false;
            }
            
            if (stockMax < stockMin) {
                mostrarMensajeError("El stock máximo no puede ser menor al stock mínimo");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("Los valores de stock deben ser números enteros válidos");
            return false;
        }
        
        return true;
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarCampos() {
        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtModelo.clear();
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        txtPrecioCompra.setText("0.00");
        txtPrecioVenta.setText("0.00");
        txtStockActual.setText("0");
        txtStockMinimo.setText("0");
        txtStockMaximo.setText("100");
        txtGarantia.setText("12");
        txtUbicacion.clear();
        chkActivo.setSelected(true);
        tablaProductos.getSelectionModel().clearSelection();
    }
    
    /**
     * Deshabilita todos los campos del formulario
     */
    private void deshabilitarCampos() {
        txtCodigo.setDisable(true);
        txtNombre.setDisable(true);
        txtMarca.setDisable(true);
        txtModelo.setDisable(true);
        txtDescripcion.setDisable(true);
        cmbCategoria.setDisable(true);
        txtPrecioCompra.setDisable(true);
        txtPrecioVenta.setDisable(true);
        txtStockActual.setDisable(true);
        txtStockMinimo.setDisable(true);
        txtStockMaximo.setDisable(true);
        txtGarantia.setDisable(true);
        txtUbicacion.setDisable(true);
        chkActivo.setDisable(true);
    }
    
    /**
     * Actualiza las estadísticas
     */
    private void actualizarEstadisticas() {
        lblTotalProductos.setText(String.valueOf(listaProductos.size()));
        
        int activos = (int) listaProductos.stream().filter(Producto::isActivo).count();
        lblProductosActivos.setText(String.valueOf(activos));
        
        double valorInventario = listaProductos.stream()
                .mapToDouble(Producto::getValorInventario)
                .sum();
        lblValorInventario.setText(formatoMoneda.format(valorInventario));
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
        Stage stage = (Stage) btnNuevo.getScene().getWindow();
        stage.close();
    }
}