package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.dao.VentaDAO;
import com.taller.proyecto_bd.models.Auditoria;
import com.taller.proyecto_bd.models.Cliente;
import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.models.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador para la gestión de ventas
 * Permite visualizar, filtrar y administrar todas las ventas del sistema
 */
public class GestionVentasController {

    // ==================== COMPONENTES FXML ====================

    // Filtros y búsqueda
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnActualizar;

    // Estadísticas
    @FXML private Label lblTotalVentas;
    @FXML private Label lblMontoTotal;
    @FXML private Label lblVentasCredito;
    @FXML private Label lblVentasContado;

    // Tabla de ventas
    @FXML private TableView<Venta> tblVentas;
    @FXML private TableColumn<Venta, Integer> colIdVenta;
    @FXML private TableColumn<Venta, String> colCodigo;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, String> colVendedor;
    @FXML private TableColumn<Venta, Date> colFecha;
    @FXML private TableColumn<Venta, String> colTipo;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, String> colEstado;

    // Botones de acción
    @FXML private Button btnVerDetalles;
    @FXML private Button btnAnularVenta;

    // ==================== ATRIBUTOS ====================

    private VentaDAO ventaDAO;
    private ClienteDAO clienteDAO;
    private UsuarioDAO usuarioDAO;
    private AuditoriaDAO auditoriaDAO;

    private ObservableList<Venta> listaVentas;
    private NumberFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;

    // ==================== INICIALIZACIÓN ====================

    @FXML
    public void initialize() {
        // Inicializar DAOs
        ventaDAO = VentaDAO.getInstance();
        clienteDAO = ClienteDAO.getInstance();
        usuarioDAO = UsuarioDAO.getInstance();
        auditoriaDAO = AuditoriaDAO.getInstance();

        // Inicializar formateadores
        formatoMoneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Configurar componentes
        configurarTabla();
        configurarFiltros();
        configurarEventos();

        // Cargar datos iniciales
        cargarVentas("TODAS", "TODAS");
    }

    /**
     * Configurar las columnas de la tabla
     */
    private void configurarTabla() {
        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        // Columna de cliente (necesita join)
        colCliente.setCellValueFactory(cellData -> {
            int idCliente = cellData.getValue().getIdCliente();
            Cliente cliente = clienteDAO.obtenerPorId(idCliente);
            String nombreCliente = cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(nombreCliente);
        });

        // Columna de vendedor (necesita join)
        colVendedor.setCellValueFactory(cellData -> {
            int idUsuario = cellData.getValue().getIdUsuario();
            Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
            String nombreVendedor = usuario != null ? usuario.getNombre() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(nombreVendedor);
        });

        // Columna de fecha formateada
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        colFecha.setCellFactory(column -> new TableCell<Venta, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatoFecha.format(item));
                }
            }
        });

        // Columna de tipo
        colTipo.setCellValueFactory(cellData -> {
            String tipo = cellData.getValue().isEsCredito() ? "CRÉDITO" : "CONTADO";
            return new javafx.beans.property.SimpleStringProperty(tipo);
        });

        // Columna de total formateado
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(column -> new TableCell<Venta, Double>() {
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

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Evento de selección
        tblVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean ventaSeleccionada = newSelection != null;
            btnVerDetalles.setDisable(!ventaSeleccionada);
            btnAnularVenta.setDisable(!ventaSeleccionada ||
                (newSelection != null && !"REGISTRADA".equals(newSelection.getEstado())));
        });
    }

    /**
     * Configurar los ComboBox de filtros
     */
    private void configurarFiltros() {
        // Filtro por tipo
        cmbFiltroTipo.setItems(FXCollections.observableArrayList(
            "TODAS", "CRÉDITO", "CONTADO"
        ));
        cmbFiltroTipo.setValue("TODAS");

        // Filtro por estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "TODAS", "REGISTRADA", "PAGADA", "ANULADA"
        ));
        cmbFiltroEstado.setValue("TODAS");

        // Listener para cambios en los filtros
        cmbFiltroTipo.setOnAction(e -> aplicarFiltros());
        cmbFiltroEstado.setOnAction(e -> aplicarFiltros());
    }

    /**
     * Configurar eventos de los botones
     */
    private void configurarEventos() {
        // Doble clic en la tabla para ver detalles
        tblVentas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblVentas.getSelectionModel().getSelectedItem() != null) {
                verDetalles();
            }
        });
    }

    // ==================== MÉTODOS DE CARGA DE DATOS ====================

    /**
     * Cargar ventas según filtros
     */
    private void cargarVentas(String filtroTipo, String filtroEstado) {
        List<Venta> ventas = null;

        // Aplicar filtro por tipo primero
        if ("CRÉDITO".equals(filtroTipo)) {
            ventas = ventaDAO.obtenerPorCredito(true);
        } else if ("CONTADO".equals(filtroTipo)) {
            ventas = ventaDAO.obtenerPorCredito(false);
        } else {
            ventas = ventaDAO.obtenerTodas();
        }

        // Aplicar filtro por estado si no es "TODAS"
        if (!"TODAS".equals(filtroEstado) && ventas != null) {
            String estadoBuscado = filtroEstado;
            ventas = ventas.stream()
                .filter(v -> estadoBuscado.equals(v.getEstado()))
                .collect(java.util.stream.Collectors.toList());
        }

        listaVentas = FXCollections.observableArrayList(ventas != null ? ventas : new java.util.ArrayList<>());
        tblVentas.setItems(listaVentas);

        actualizarEstadisticas();
    }

    /**
     * Aplicar filtros seleccionados
     */
    private void aplicarFiltros() {
        String filtroTipo = cmbFiltroTipo.getValue();
        String filtroEstado = cmbFiltroEstado.getValue();
        cargarVentas(filtroTipo, filtroEstado);
    }

    /**
     * Actualizar las estadísticas en el panel superior
     */
    private void actualizarEstadisticas() {
        if (listaVentas == null || listaVentas.isEmpty()) {
            lblTotalVentas.setText("0");
            lblMontoTotal.setText(formatoMoneda.format(0));
            lblVentasCredito.setText("0");
            lblVentasContado.setText("0");
            return;
        }

        int totalVentas = listaVentas.size();
        double montoTotal = listaVentas.stream()
            .mapToDouble(Venta::getTotal)
            .sum();

        long ventasCredito = listaVentas.stream()
            .filter(Venta::isEsCredito)
            .count();

        long ventasContado = totalVentas - ventasCredito;

        lblTotalVentas.setText(String.valueOf(totalVentas));
        lblMontoTotal.setText(formatoMoneda.format(montoTotal));
        lblVentasCredito.setText(String.valueOf(ventasCredito));
        lblVentasContado.setText(String.valueOf(ventasContado));
    }

    // ==================== ACCIONES ====================

    /**
     * Buscar venta por código o cliente
     */
    @FXML
    private void buscar() {
        String textoBusqueda = txtBuscar.getText().trim();

        if (textoBusqueda.isEmpty()) {
            aplicarFiltros();
            return;
        }

        // Buscar por código primero
        Venta ventaPorCodigo = ventaDAO.obtenerPorCodigo(textoBusqueda);
        if (ventaPorCodigo != null) {
            listaVentas = FXCollections.observableArrayList(ventaPorCodigo);
            tblVentas.setItems(listaVentas);
            actualizarEstadisticas();
            return;
        }

        // Si no se encuentra por código, buscar por nombre de cliente
        List<Cliente> clientes = clienteDAO.buscarPorNombre(textoBusqueda);
        if (!clientes.isEmpty()) {
            List<Venta> ventasEncontradas = new java.util.ArrayList<>();
            for (Cliente cliente : clientes) {
                ventasEncontradas.addAll(ventaDAO.obtenerPorCliente(cliente.getIdCliente()));
            }
            listaVentas = FXCollections.observableArrayList(ventasEncontradas);
            tblVentas.setItems(listaVentas);
            actualizarEstadisticas();
        } else {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Búsqueda",
                "No se encontraron ventas con el criterio: " + textoBusqueda);
        }
    }

    /**
     * Actualizar la lista de ventas
     */
    @FXML
    private void actualizar() {
        txtBuscar.clear();
        aplicarFiltros();
        mostrarInfo("Lista actualizada correctamente");
    }

    /**
     * Ver detalles de la venta seleccionada
     */
    @FXML
    private void verDetalles() {
        Venta ventaSeleccionada = tblVentas.getSelectionModel().getSelectedItem();
        if (ventaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                "Por favor, seleccione una venta para ver sus detalles.");
            return;
        }

        // Obtener información completa
        Cliente cliente = clienteDAO.obtenerPorId(ventaSeleccionada.getIdCliente());
        Usuario vendedor = usuarioDAO.obtenerPorId(ventaSeleccionada.getIdUsuario());

        StringBuilder detalles = new StringBuilder();
        detalles.append("INFORMACIÓN DE LA VENTA\n");
        detalles.append("═══════════════════════════════════\n\n");
        detalles.append(String.format("Código: %s\n", ventaSeleccionada.getCodigo()));
        detalles.append(String.format("Fecha: %s\n", formatoFecha.format(ventaSeleccionada.getFechaVenta())));
        detalles.append(String.format("Estado: %s\n\n", ventaSeleccionada.getEstado()));

        detalles.append("CLIENTE\n");
        detalles.append("───────────────────────────────────\n");
        if (cliente != null) {
            detalles.append(String.format("Nombre: %s %s\n", cliente.getNombre(), cliente.getApellido()));
            detalles.append(String.format("Cédula: %s\n\n", cliente.getCedula()));
        }

        detalles.append("VENDEDOR\n");
        detalles.append("───────────────────────────────────\n");
        if (vendedor != null) {
            detalles.append(String.format("Nombre: %s\n\n", vendedor.getNombre()));
        }

        detalles.append("MONTOS\n");
        detalles.append("───────────────────────────────────\n");
        detalles.append(String.format("Subtotal: %s\n", formatoMoneda.format(ventaSeleccionada.getSubtotal())));
        detalles.append(String.format("IVA: %s\n", formatoMoneda.format(ventaSeleccionada.getIvaTotal())));
        detalles.append(String.format("TOTAL: %s\n\n", formatoMoneda.format(ventaSeleccionada.getTotal())));

        detalles.append("TIPO DE PAGO\n");
        detalles.append("───────────────────────────────────\n");
        if (ventaSeleccionada.isEsCredito()) {
            detalles.append("Tipo: CRÉDITO\n");
            detalles.append(String.format("Cuota Inicial: %s\n", formatoMoneda.format(ventaSeleccionada.getCuotaInicial())));
            detalles.append(String.format("Plazo: %d meses\n", ventaSeleccionada.getPlazoMeses()));
            detalles.append(String.format("Saldo a Financiar: %s\n", formatoMoneda.format(ventaSeleccionada.getSaldoFinanciar())));
            detalles.append(String.format("Monto Financiado (+ 5%% interés): %s\n", formatoMoneda.format(ventaSeleccionada.getMontoFinanciado())));
            detalles.append(String.format("Cuota Mensual: %s\n", formatoMoneda.format(ventaSeleccionada.getValorCuotaMensual())));
        } else {
            detalles.append("Tipo: CONTADO\n");
            detalles.append("Pago completo al momento de la venta\n");
        }

        mostrarAlerta(Alert.AlertType.INFORMATION, "Detalles de Venta", detalles.toString());
    }

    /**
     * Anular la venta seleccionada
     */
    @FXML
    private void anularVenta() {
        Venta ventaSeleccionada = tblVentas.getSelectionModel().getSelectedItem();
        if (ventaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                "Por favor, seleccione una venta para anular.");
            return;
        }

        if (!"REGISTRADA".equals(ventaSeleccionada.getEstado())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acción no permitida",
                "Solo se pueden anular ventas en estado REGISTRADA.");
            return;
        }

        // Confirmar anulación
        Optional<ButtonType> resultado = mostrarConfirmacion(
            "Confirmar Anulación",
            String.format("¿Está seguro de anular la venta %s?\n\nEsta acción no se puede deshacer.",
                ventaSeleccionada.getCodigo())
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            ventaSeleccionada.setEstado("ANULADA");

            if (ventaDAO.actualizar(ventaSeleccionada)) {
                // Registrar auditoría
                registrarAuditoria("ANULAR", "Venta anulada: " + ventaSeleccionada.getCodigo());

                mostrarInfo("Venta anulada exitosamente");
                actualizar();
            } else {
                mostrarError("No se pudo anular la venta. Intente nuevamente.");
            }
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Registrar una acción en auditoría
     */
    private void registrarAuditoria(String accion, String detalles) {
        try {
            Auditoria auditoria = new Auditoria();
            auditoria.setIdUsuario(1); // TODO: Obtener usuario actual de sesión
            auditoria.setAccion(accion);
            auditoria.setTablaAfectada("Ventas");
            auditoria.setDescripcion(detalles);
            auditoriaDAO.agregar(auditoria);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    /**
     * Mostrar alerta genérica
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Mostrar mensaje de información
     */
    private void mostrarInfo(String mensaje) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Información", mensaje);
    }

    /**
     * Mostrar mensaje de error
     */
    private void mostrarError(String mensaje) {
        mostrarAlerta(Alert.AlertType.ERROR, "Error", mensaje);
    }

    /**
     * Mostrar diálogo de confirmación
     */
    private Optional<ButtonType> mostrarConfirmacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        return alerta.showAndWait();
    }
}