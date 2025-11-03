package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.models.Auditoria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controlador de la vista de Bitácora de Auditoría.
 * Permite visualizar y filtrar los registros de auditoría del sistema.
 *
 * @author Sistema
 * @version 1.0
 */
public class BitacoraViewController {

    // ==================== COMPONENTES FXML ====================

    @FXML private ComboBox<String> cmbFiltroAccion;
    @FXML private TextField txtFiltroUsuario;
    @FXML private ComboBox<String> cmbFiltroTiempo;

    @FXML private TableView<Auditoria> tablaAuditorias;
    @FXML private TableColumn<Auditoria, Integer> colID;
    @FXML private TableColumn<Auditoria, Date> colFecha;
    @FXML private TableColumn<Auditoria, String> colUsuario;
    @FXML private TableColumn<Auditoria, String> colAccion;
    @FXML private TableColumn<Auditoria, String> colTabla;
    @FXML private TableColumn<Auditoria, String> colDescripcion;
    @FXML private TableColumn<Auditoria, String> colIP;

    @FXML private Label lblTotalRegistros;
    @FXML private Label lblCriticas;

    // Panel de detalles
    @FXML private Label lblDetalleID;
    @FXML private Label lblDetalleFecha;
    @FXML private Label lblDetalleUsuario;
    @FXML private Label lblDetalleAccion;
    @FXML private Label lblDetalleTabla;
    @FXML private Label lblDetalleIP;
    @FXML private TextArea txtDetalleDescripcion;

    // ==================== DATOS ====================

    private final AuditoriaDAO auditoriaDAO = AuditoriaDAO.getInstance();
    private ObservableList<Auditoria> listaAuditorias = FXCollections.observableArrayList();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ==================== INICIALIZACIÓN ====================

    @FXML
    public void initialize() {
        configurarTabla();
        configurarFiltros();
        configurarEventos();
        cargarTodas();
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colID.setCellValueFactory(new PropertyValueFactory<>("idAuditoria"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaAccion"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        colTabla.setCellValueFactory(new PropertyValueFactory<>("tablaAfectada"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colIP.setCellValueFactory(new PropertyValueFactory<>("ip"));

        // Formatear la fecha en la tabla
        colFecha.setCellFactory(column -> new TableCell<Auditoria, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });

        // Colorear las filas según si son críticas
        tablaAuditorias.setRowFactory(tv -> new TableRow<Auditoria>() {
            @Override
            protected void updateItem(Auditoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.esCritica()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });

        tablaAuditorias.setItems(listaAuditorias);
    }

    /**
     * Configura los ComboBox de filtros
     */
    private void configurarFiltros() {
        // Filtro de acciones
        cmbFiltroAccion.setItems(FXCollections.observableArrayList(
                "Todas",
                "LOGIN",
                "LOGOUT",
                "LOGIN_FALLIDO",
                "CREAR",
                "ACTUALIZAR",
                "ELIMINAR",
                "ANULAR"
        ));
        cmbFiltroAccion.setValue("Todas");

        // Filtro de tiempo
        cmbFiltroTiempo.setItems(FXCollections.observableArrayList(
                "Todo el tiempo",
                "Últimas 24 horas",
                "Últimas 48 horas",
                "Última semana",
                "Último mes"
        ));
        cmbFiltroTiempo.setValue("Todo el tiempo");
    }

    /**
     * Configura los eventos de la tabla
     */
    private void configurarEventos() {
        // Evento de selección en la tabla
        tablaAuditorias.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> mostrarDetalles(newValue)
        );
    }

    // ==================== CARGA DE DATOS ====================

    /**
     * Carga todas las auditorías
     */
    @FXML
    private void cargarTodas() {
        List<Auditoria> auditorias = auditoriaDAO.obtenerTodas();
        listaAuditorias.clear();
        listaAuditorias.addAll(auditorias);
        actualizarEstadisticas();
    }

    /**
     * Aplica los filtros seleccionados
     */
    @FXML
    private void aplicarFiltros() {
        List<Auditoria> auditorias;

        // Filtrar por acción
        String accionSeleccionada = cmbFiltroAccion.getValue();
        if (accionSeleccionada != null && !accionSeleccionada.equals("Todas")) {
            auditorias = auditoriaDAO.obtenerPorAccion(accionSeleccionada);
        } else {
            auditorias = auditoriaDAO.obtenerTodas();
        }

        // Filtrar por usuario
        String usuarioTexto = txtFiltroUsuario.getText().trim();
        if (!usuarioTexto.isEmpty()) {
            try {
                int idUsuario = Integer.parseInt(usuarioTexto);
                auditorias = auditoriaDAO.obtenerPorUsuario(idUsuario);
            } catch (NumberFormatException e) {
                mostrarError("Error", "El ID de usuario debe ser un número.");
                return;
            }
        }

        // Filtrar por tiempo
        String tiempoSeleccionado = cmbFiltroTiempo.getValue();
        if (tiempoSeleccionado != null && !tiempoSeleccionado.equals("Todo el tiempo")) {
            int horas = switch (tiempoSeleccionado) {
                case "Últimas 24 horas" -> 24;
                case "Últimas 48 horas" -> 48;
                case "Última semana" -> 168;
                case "Último mes" -> 720;
                default -> 0;
            };
            if (horas > 0) {
                auditorias = auditoriaDAO.obtenerRecientes(horas);
            }
        }

        listaAuditorias.clear();
        listaAuditorias.addAll(auditorias);
        actualizarEstadisticas();
    }

    /**
     * Limpia todos los filtros
     */
    @FXML
    private void limpiarFiltros() {
        cmbFiltroAccion.setValue("Todas");
        txtFiltroUsuario.clear();
        cmbFiltroTiempo.setValue("Todo el tiempo");
        cargarTodas();
    }

    /**
     * Muestra solo las auditorías críticas
     */
    @FXML
    private void verCriticas() {
        List<Auditoria> criticas = auditoriaDAO.obtenerCriticas();
        listaAuditorias.clear();
        listaAuditorias.addAll(criticas);
        actualizarEstadisticas();
    }

    // ==================== DETALLES ====================

    /**
     * Muestra los detalles del registro seleccionado
     */
    private void mostrarDetalles(Auditoria auditoria) {
        if (auditoria == null) {
            limpiarDetalles();
            return;
        }

        lblDetalleID.setText(String.valueOf(auditoria.getIdAuditoria()));
        lblDetalleFecha.setText(dateFormat.format(auditoria.getFechaAccion()));
        lblDetalleUsuario.setText(auditoria.getNombreUsuario() + " (ID: " + auditoria.getIdUsuario() + ")");
        lblDetalleAccion.setText(auditoria.getAccion());
        lblDetalleTabla.setText(auditoria.getTablaAfectada() != null ? auditoria.getTablaAfectada() : "N/A");
        lblDetalleIP.setText(auditoria.getIp() != null ? auditoria.getIp() : "N/A");
        txtDetalleDescripcion.setText(auditoria.getDescripcion() != null ? auditoria.getDescripcion() : "Sin descripción");

        // Colorear la acción si es crítica
        if (auditoria.esCritica()) {
            lblDetalleAccion.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            lblDetalleAccion.setStyle("-fx-text-fill: black;");
        }
    }

    /**
     * Limpia el panel de detalles
     */
    private void limpiarDetalles() {
        lblDetalleID.setText("-");
        lblDetalleFecha.setText("-");
        lblDetalleUsuario.setText("-");
        lblDetalleAccion.setText("-");
        lblDetalleTabla.setText("-");
        lblDetalleIP.setText("-");
        txtDetalleDescripcion.clear();
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Actualiza las estadísticas mostradas
     */
    private void actualizarEstadisticas() {
        int total = listaAuditorias.size();
        long criticas = listaAuditorias.stream().filter(Auditoria::esCritica).count();

        lblTotalRegistros.setText("Total: " + total + " registros");
        lblCriticas.setText("Críticas: " + criticas);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Muestra un diálogo de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de información
     */
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}