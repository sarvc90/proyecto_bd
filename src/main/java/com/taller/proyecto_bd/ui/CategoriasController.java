package com.taller.proyecto_bd.ui;



import com.taller.proyecto_bd.dao.CategoriaDAO;
import com.taller.proyecto_bd.models.Categoria;
import com.taller.proyecto_bd.models.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de categorías
 */
public class CategoriasController {

    // ==================== CAMPOS DEL FORMULARIO ====================
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cmbNivel;
    @FXML private ComboBox<Categoria> cmbCategoriaPadre;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblCantidadProductos;
    
    // ==================== BOTONES ====================
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    
    // ==================== TABLA ====================
    @FXML private TextField txtBuscar;
    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, String> colCodigo;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;
    @FXML private TableColumn<Categoria, String> colNivel;
    @FXML private TableColumn<Categoria, Integer> colProductos;
    @FXML private TableColumn<Categoria, Boolean> colActivo;
    
    // ==================== LABELS DE INFORMACIÓN ====================
    @FXML private Label lblMensaje;
    @FXML private Label lblTotalCategorias;
    @FXML private Label lblCategoriasActivas;
    @FXML private Label lblPrincipales;
    @FXML private Label lblSubcategorias;
    
    // ==================== ATRIBUTOS ====================
    private CategoriaDAO categoriaDAO;
    private ObservableList<Categoria> listaCategorias;
    private Categoria categoriaSeleccionada;
    
    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        categoriaDAO = CategoriaDAO.getInstance();
        listaCategorias = FXCollections.observableArrayList();
        
        configurarTabla();
        cargarCategoriasPadre();
        cargarCategorias();
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
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colProductos.setCellValueFactory(new PropertyValueFactory<>("cantidadProductos"));
        
        // Columna de nivel con descripción
        colNivel.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescripcionNivel())
        );
        
        // Columna de estado con formato
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setCellFactory(col -> new TableCell<Categoria, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Activa" : "Inactiva");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" : 
                                   "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        tablaCategorias.setItems(listaCategorias);
    }
    
    /**
     * Carga las categorías padre en el ComboBox
     */
    private void cargarCategoriasPadre() {
        List<Categoria> principales = categoriaDAO.obtenerPrincipales();
        cmbCategoriaPadre.setItems(FXCollections.observableArrayList(principales));
        
        // Configurar cómo se muestra cada categoría
        cmbCategoriaPadre.setCellFactory(param -> new ListCell<Categoria>() {
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
        
        cmbCategoriaPadre.setButtonCell(new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Ninguna (Categoría principal)");
                } else {
                    setText(item.getNombreCompleto());
                }
            }
        });
    }
    
    /**
     * Carga todas las categorías en la tabla
     */
    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.obtenerTodas();
        listaCategorias.clear();
        listaCategorias.addAll(categorias);
        actualizarEstadisticas();
    }
    
    /**
     * Configura los eventos de la interfaz
     */
    private void configurarEventos() {
        // Selección en la tabla
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    categoriaSeleccionada = newSelection;
                    cargarDatosFormulario(newSelection);
                }
            }
        );
        
        // Actualizar categorías padre cuando cambia el nivel
        cmbNivel.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.startsWith("1")) {
                cmbCategoriaPadre.setValue(null);
                cmbCategoriaPadre.setDisable(true);
            } else {
                cmbCategoriaPadre.setDisable(false);
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
     * Carga los datos de una categoría en el formulario
     */
    private void cargarDatosFormulario(Categoria categoria) {
        txtCodigo.setText(categoria.getCodigo());
        txtNombre.setText(categoria.getNombre());
        txtDescripcion.setText(categoria.getDescripcion());
        chkActivo.setSelected(categoria.isActivo());
        lblCantidadProductos.setText(String.valueOf(categoria.getCantidadProductos()));
        
        // Seleccionar el nivel
        int nivel = categoria.getNivel();
        switch (nivel) {
            case 1:
                cmbNivel.setValue("1 - Categoría Principal");
                cmbCategoriaPadre.setValue(null);
                cmbCategoriaPadre.setDisable(true);
                break;
            case 2:
                cmbNivel.setValue("2 - Subcategoría Nivel 1");
                cmbCategoriaPadre.setDisable(false);
                break;
            case 3:
                cmbNivel.setValue("3 - Subcategoría Nivel 2");
                cmbCategoriaPadre.setDisable(false);
                break;
        }
        
        // Seleccionar la categoría padre si tiene
        if (categoria.getIdCategoriaPadre() != null) {
            Categoria padre = categoriaDAO.obtenerPorId(categoria.getIdCategoriaPadre());
            if (padre != null) {
                cmbCategoriaPadre.setValue(padre);
            }
        }
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    @FXML
    private void nuevo() {
        limpiarCampos();
        categoriaSeleccionada = null;
        txtCodigo.requestFocus();
        ocultarMensaje();
    }
    
    /**
     * Guarda una categoría (nueva o actualización)
     */
    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }
        
        try {
            Categoria categoria;
            boolean esNueva = (categoriaSeleccionada == null);
            
            if (esNueva) {
                categoria = new Categoria();
            } else {
                categoria = categoriaSeleccionada;
            }
            
            // Asignar valores del formulario
            categoria.setCodigo(txtCodigo.getText().trim().toUpperCase());
            categoria.setNombre(txtNombre.getText().trim());
            categoria.setDescripcion(txtDescripcion.getText().trim());
            categoria.setActivo(chkActivo.isSelected());
            
            // Obtener el nivel
            String nivelStr = cmbNivel.getValue();
            int nivel = Integer.parseInt(nivelStr.substring(0, 1));
            categoria.setNivel(nivel);
            
            // Asignar categoría padre si no es principal
            if (nivel > 1 && cmbCategoriaPadre.getValue() != null) {
                categoria.setIdCategoriaPadre(cmbCategoriaPadre.getValue().getIdCategoria());
            } else {
                categoria.setIdCategoriaPadre(null);
            }
            
            boolean exito;
            if (esNueva) {
                exito = categoriaDAO.agregar(categoria);
            } else {
                exito = categoriaDAO.actualizar(categoria);
            }
            
            if (exito) {
                mostrarMensajeExito(esNueva ? "Categoría guardada exitosamente" : 
                                             "Categoría actualizada exitosamente");
                cargarCategorias();
                cargarCategoriasPadre(); // Actualizar la lista de padres
                limpiarCampos();
                categoriaSeleccionada = null;
            } else {
                mostrarMensajeError("Error al guardar la categoría. Verifique que el código no esté duplicado.");
            }
            
        } catch (Exception e) {
            mostrarMensajeError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina la categoría seleccionada
     */
    @FXML
    private void eliminar() {
        if (categoriaSeleccionada == null) {
            mostrarMensajeError("Debe seleccionar una categoría para eliminar");
            return;
        }
        
        // Validar que no tenga productos
        if (!categoriaSeleccionada.puedeEliminarse()) {
            mostrarMensajeError("No se puede eliminar una categoría con productos asociados");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar esta categoría?");
        confirmacion.setContentText(categoriaSeleccionada.getNombreCompleto());
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = categoriaDAO.eliminar(categoriaSeleccionada.getIdCategoria());
            
            if (exito) {
                mostrarMensajeExito("Categoría eliminada exitosamente");
                cargarCategorias();
                cargarCategoriasPadre(); // Actualizar la lista de padres
                limpiarCampos();
                categoriaSeleccionada = null;
            } else {
                mostrarMensajeError("Error al eliminar la categoría");
            }
        }
    }
    
    /**
     * Busca categorías según el criterio
     */
    @FXML
    private void buscar() {
        String criterio = txtBuscar.getText().trim().toLowerCase();
        
        if (criterio.isEmpty()) {
            cargarCategorias();
            return;
        }
        
        List<Categoria> todas = categoriaDAO.obtenerTodas();
        List<Categoria> filtradas = todas.stream()
                .filter(c -> 
                    (c.getCodigo() != null && c.getCodigo().toLowerCase().contains(criterio)) ||
                    (c.getNombre() != null && c.getNombre().toLowerCase().contains(criterio))
                )
                .collect(java.util.stream.Collectors.toList());
        
        listaCategorias.clear();
        listaCategorias.addAll(filtradas);
        actualizarEstadisticas();
    }
    
    /**
     * Limpia la búsqueda y recarga todas las categorías
     */
    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cargarCategorias();
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
        
        if (cmbNivel.getValue() == null) {
            mostrarMensajeError("Debe seleccionar un nivel");
            cmbNivel.requestFocus();
            return false;
        }
        
        // Validar que las subcategorías tengan padre
        String nivelStr = cmbNivel.getValue();
        int nivel = Integer.parseInt(nivelStr.substring(0, 1));
        
        if (nivel > 1 && cmbCategoriaPadre.getValue() == null) {
            mostrarMensajeError("Las subcategorías deben tener una categoría padre");
            cmbCategoriaPadre.requestFocus();
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
        txtDescripcion.clear();
        cmbNivel.setValue(null);
        cmbCategoriaPadre.setValue(null);
        cmbCategoriaPadre.setDisable(false);
        chkActivo.setSelected(true);
        lblCantidadProductos.setText("0");
        tablaCategorias.getSelectionModel().clearSelection();
    }
    
    /**
     * Deshabilita todos los campos del formulario
     */
    private void deshabilitarCampos() {
        txtCodigo.setDisable(true);
        txtNombre.setDisable(true);
        txtDescripcion.setDisable(true);
        cmbNivel.setDisable(true);
        cmbCategoriaPadre.setDisable(true);
        chkActivo.setDisable(true);
    }
    
    /**
     * Actualiza las estadísticas
     */
    private void actualizarEstadisticas() {
        lblTotalCategorias.setText(String.valueOf(listaCategorias.size()));
        
        int activas = (int) listaCategorias.stream().filter(Categoria::isActivo).count();
        lblCategoriasActivas.setText(String.valueOf(activas));
        
        int principales = (int) listaCategorias.stream()
                .filter(Categoria::esCategoriaPrincipal)
                .count();
        lblPrincipales.setText(String.valueOf(principales));
        
        int subcategorias = listaCategorias.size() - principales;
        lblSubcategorias.setText(String.valueOf(subcategorias));
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
