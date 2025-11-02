package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;
import javafx.beans.property.SimpleStringProperty;
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
 * Controlador para la gestión de créditos.
 * Permite visualizar, filtrar y administrar créditos del sistema.
 */
public class GestionCreditosController {

    // ==================== COMPONENTES UI ====================
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField txtBuscarCliente;
    @FXML private Button btnBuscarCliente;
    @FXML private Button btnActualizar;
    @FXML private Button btnVerDetalles;
    @FXML private Button btnAnularCredito;

    // Tabla de créditos
    @FXML private TableView<Credito> tblCreditos;
    @FXML private TableColumn<Credito, Integer> colIdCredito;
    @FXML private TableColumn<Credito, String> colCodigoVenta;
    @FXML private TableColumn<Credito, String> colCliente;
    @FXML private TableColumn<Credito, Double> colMontoTotal;
    @FXML private TableColumn<Credito, Double> colCuotaInicial;
    @FXML private TableColumn<Credito, Double> colSaldoPendiente;
    @FXML private TableColumn<Credito, Integer> colPlazoMeses;
    @FXML private TableColumn<Credito, Double> colInteres;
    @FXML private TableColumn<Credito, String> colEstado;
    @FXML private TableColumn<Credito, Date> colFechaRegistro;

    // Labels de información
    @FXML private Label lblTotalCreditos;
    @FXML private Label lblMontoTotalFinanciado;
    @FXML private Label lblSaldoPendienteTotal;
    @FXML private Label lblCreditosMorosos;

    // ==================== DAOs ====================
    private CreditoDAO creditoDAO;
    private VentaDAO ventaDAO;
    private ClienteDAO clienteDAO;
    private CuotaDAO cuotaDAO;
    private AuditoriaDAO auditoriaDAO;

    // ==================== DATOS ====================
    private ObservableList<Credito> listaCreditos;
    private NumberFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        // Inicializar DAOs
        creditoDAO = CreditoDAO.getInstance();
        ventaDAO = VentaDAO.getInstance();
        clienteDAO = ClienteDAO.getInstance();
        cuotaDAO = CuotaDAO.getInstance();
        auditoriaDAO = AuditoriaDAO.getInstance();

        // Inicializar formatos
        formatoMoneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        // Configurar tabla
        configurarTabla();

        // Configurar filtros
        configurarFiltros();

        // Cargar datos iniciales
        cargarCreditos("TODOS");

        // Configurar eventos
        configurarEventos();
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colIdCredito.setCellValueFactory(new PropertyValueFactory<>("idCredito"));

        colCodigoVenta.setCellValueFactory(cellData -> {
            Venta venta = ventaDAO.obtenerPorId(cellData.getValue().getIdVenta());
            return new SimpleStringProperty(venta != null ? venta.getCodigo() : "N/A");
        });

        colCliente.setCellValueFactory(cellData -> {
            Cliente cliente = clienteDAO.obtenerPorId(cellData.getValue().getIdCliente());
            return new SimpleStringProperty(cliente != null ? cliente.getNombreCompleto() : "N/A");
        });

        colMontoTotal.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        colMontoTotal.setCellFactory(col -> new TableCell<Credito, Double>() {
            @Override
            protected void updateItem(Double monto, boolean empty) {
                super.updateItem(monto, empty);
                setText(empty || monto == null ? "" : formatoMoneda.format(monto));
            }
        });

        colCuotaInicial.setCellValueFactory(new PropertyValueFactory<>("cuotaInicial"));
        colCuotaInicial.setCellFactory(col -> new TableCell<Credito, Double>() {
            @Override
            protected void updateItem(Double monto, boolean empty) {
                super.updateItem(monto, empty);
                setText(empty || monto == null ? "" : formatoMoneda.format(monto));
            }
        });

        colSaldoPendiente.setCellValueFactory(new PropertyValueFactory<>("saldoPendiente"));
        colSaldoPendiente.setCellFactory(col -> new TableCell<Credito, Double>() {
            @Override
            protected void updateItem(Double monto, boolean empty) {
                super.updateItem(monto, empty);
                setText(empty || monto == null ? "" : formatoMoneda.format(monto));
                if (!empty && monto != null) {
                    if (monto == 0) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (monto > 0) {
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        colPlazoMeses.setCellValueFactory(new PropertyValueFactory<>("plazoMeses"));

        colInteres.setCellValueFactory(new PropertyValueFactory<>("interes"));
        colInteres.setCellFactory(col -> new TableCell<Credito, Double>() {
            @Override
            protected void updateItem(Double interes, boolean empty) {
                super.updateItem(interes, empty);
                setText(empty || interes == null ? "" : String.format("%.2f%%", interes * 100));
            }
        });

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(col -> new TableCell<Credito, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(estado);
                    switch (estado) {
                        case "ACTIVO":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;");
                            break;
                        case "CANCELADO":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                            break;
                        case "MOROSO":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        colFechaRegistro.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));
        colFechaRegistro.setCellFactory(col -> new TableCell<Credito, Date>() {
            @Override
            protected void updateItem(Date fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setText(empty || fecha == null ? "" : formatoFecha.format(fecha));
            }
        });
    }

    /**
     * Configura los filtros del sistema
     */
    private void configurarFiltros() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "TODOS", "ACTIVO", "CANCELADO", "MOROSOS"
        ));
        cmbFiltroEstado.setValue("TODOS");

        cmbFiltroEstado.setOnAction(e -> {
            String filtro = cmbFiltroEstado.getValue();
            cargarCreditos(filtro);
        });
    }

    /**
     * Configura los eventos de los botones
     */
    private void configurarEventos() {
        tblCreditos.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean haySeleccion = newVal != null;
            btnVerDetalles.setDisable(!haySeleccion);
            btnAnularCredito.setDisable(!haySeleccion || !"ACTIVO".equals(newVal.getEstado()));
        });

        // Doble clic para ver detalles
        tblCreditos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblCreditos.getSelectionModel().getSelectedItem() != null) {
                verDetalles();
            }
        });
    }

    /**
     * Carga los créditos según el filtro seleccionado
     */
    private void cargarCreditos(String filtro) {
        List<Credito> creditos;

        switch (filtro) {
            case "ACTIVO":
                creditos = creditoDAO.obtenerActivos();
                break;
            case "CANCELADO":
                creditos = creditoDAO.obtenerCancelados();
                break;
            case "MOROSOS":
                creditos = creditoDAO.obtenerMorosos();
                break;
            case "TODOS":
            default:
                creditos = creditoDAO.obtenerTodos();
                break;
        }

        listaCreditos = FXCollections.observableArrayList(creditos);
        tblCreditos.setItems(listaCreditos);

        actualizarEstadisticas();
    }

    /**
     * Actualiza las estadísticas mostradas
     */
    private void actualizarEstadisticas() {
        List<Credito> todosCreditos = creditoDAO.obtenerTodos();
        List<Credito> creditosActivos = creditoDAO.obtenerActivos();
        List<Credito> creditosMorosos = creditoDAO.obtenerMorosos();

        lblTotalCreditos.setText(String.valueOf(todosCreditos.size()));

        double montoTotal = creditosActivos.stream()
                .mapToDouble(Credito::getMontoTotal)
                .sum();
        lblMontoTotalFinanciado.setText(formatoMoneda.format(montoTotal));

        double saldoTotal = creditosActivos.stream()
                .mapToDouble(Credito::getSaldoPendiente)
                .sum();
        lblSaldoPendienteTotal.setText(formatoMoneda.format(saldoTotal));

        lblCreditosMorosos.setText(String.valueOf(creditosMorosos.size()));
    }

    /**
     * Busca créditos por cliente
     */
    @FXML
    private void buscarPorCliente() {
        String textoBusqueda = txtBuscarCliente.getText().trim();

        if (textoBusqueda.isEmpty()) {
            mostrarError("Ingrese un nombre o cédula de cliente para buscar");
            return;
        }

        // Buscar cliente por cédula o nombre
        List<Cliente> clientes = clienteDAO.obtenerTodos().stream()
                .filter(c -> c.getCedula().contains(textoBusqueda) ||
                           c.getNombreCompleto().toLowerCase().contains(textoBusqueda.toLowerCase()))
                .toList();

        if (clientes.isEmpty()) {
            mostrarError("No se encontraron clientes con ese criterio de búsqueda");
            return;
        }

        // Si hay múltiples clientes, mostrar diálogo de selección
        Cliente clienteSeleccionado;
        if (clientes.size() == 1) {
            clienteSeleccionado = clientes.get(0);
        } else {
            // Crear diálogo de selección
            ChoiceDialog<Cliente> dialog = new ChoiceDialog<>(clientes.get(0), clientes);
            dialog.setTitle("Seleccionar Cliente");
            dialog.setHeaderText("Se encontraron varios clientes");
            dialog.setContentText("Seleccione un cliente:");

            Optional<Cliente> resultado = dialog.showAndWait();
            if (resultado.isEmpty()) {
                return;
            }
            clienteSeleccionado = resultado.get();
        }

        // Cargar créditos del cliente
        List<Credito> creditosCliente = creditoDAO.obtenerPorCliente(clienteSeleccionado.getIdCliente());
        listaCreditos = FXCollections.observableArrayList(creditosCliente);
        tblCreditos.setItems(listaCreditos);

        if (creditosCliente.isEmpty()) {
            mostrarInfo("El cliente " + clienteSeleccionado.getNombreCompleto() + " no tiene créditos registrados");
        }
    }

    /**
     * Actualiza la lista de créditos
     */
    @FXML
    private void actualizar() {
        String filtro = cmbFiltroEstado.getValue();
        cargarCreditos(filtro);
        txtBuscarCliente.clear();
        mostrarExito("Lista actualizada correctamente");
    }

    /**
     * Muestra los detalles del crédito seleccionado
     */
    @FXML
    private void verDetalles() {
        Credito creditoSeleccionado = tblCreditos.getSelectionModel().getSelectedItem();

        if (creditoSeleccionado == null) {
            mostrarError("Seleccione un crédito para ver sus detalles");
            return;
        }

        // Obtener información relacionada
        Venta venta = ventaDAO.obtenerPorId(creditoSeleccionado.getIdVenta());
        Cliente cliente = clienteDAO.obtenerPorId(creditoSeleccionado.getIdCliente());
        List<Cuota> cuotas = cuotaDAO.obtenerPorCredito(creditoSeleccionado.getIdCredito());

        long cuotasPagadas = cuotas.stream().filter(Cuota::isPagada).count();
        long cuotasPendientes = cuotas.stream().filter(c -> !c.isPagada()).count();
        long cuotasVencidas = cuotas.stream().filter(c -> !c.isPagada() && c.estaVencida()).count();

        double progreso = cuotas.isEmpty() ? 0 : (cuotasPagadas * 100.0) / cuotas.size();

        // Construir mensaje de detalles
        StringBuilder detalles = new StringBuilder();
        detalles.append("═══════════════════════════════════════\n");
        detalles.append("           DETALLES DEL CRÉDITO\n");
        detalles.append("═══════════════════════════════════════\n\n");

        detalles.append("INFORMACIÓN GENERAL\n");
        detalles.append("───────────────────────────────────────\n");
        detalles.append(String.format("ID Crédito: %d\n", creditoSeleccionado.getIdCredito()));
        detalles.append(String.format("Código Venta: %s\n", venta != null ? venta.getCodigo() : "N/A"));
        detalles.append(String.format("Cliente: %s\n", cliente != null ? cliente.getNombreCompleto() : "N/A"));
        detalles.append(String.format("Cédula: %s\n", cliente != null ? cliente.getCedula() : "N/A"));
        detalles.append(String.format("Fecha Registro: %s\n\n", formatoFecha.format(creditoSeleccionado.getFechaRegistro())));

        detalles.append("INFORMACIÓN FINANCIERA\n");
        detalles.append("───────────────────────────────────────\n");
        detalles.append(String.format("Monto Total: %s\n", formatoMoneda.format(creditoSeleccionado.getMontoTotal())));
        detalles.append(String.format("Cuota Inicial (30%%): %s\n", formatoMoneda.format(creditoSeleccionado.getCuotaInicial())));
        detalles.append(String.format("Saldo Financiado (70%%): %s\n", formatoMoneda.format(creditoSeleccionado.getMontoTotal() - creditoSeleccionado.getCuotaInicial())));
        detalles.append(String.format("Interés: %.2f%%\n", creditoSeleccionado.getInteres() * 100));
        detalles.append(String.format("Saldo Pendiente: %s\n", formatoMoneda.format(creditoSeleccionado.getSaldoPendiente())));
        detalles.append(String.format("Plazo: %d meses\n\n", creditoSeleccionado.getPlazoMeses()));

        detalles.append("ESTADO DE CUOTAS\n");
        detalles.append("───────────────────────────────────────\n");
        detalles.append(String.format("Total Cuotas: %d\n", cuotas.size()));
        detalles.append(String.format("Cuotas Pagadas: %d\n", cuotasPagadas));
        detalles.append(String.format("Cuotas Pendientes: %d\n", cuotasPendientes));
        detalles.append(String.format("Cuotas Vencidas: %d\n", cuotasVencidas));
        detalles.append(String.format("Progreso: %.1f%%\n\n", progreso));

        detalles.append("ESTADO\n");
        detalles.append("───────────────────────────────────────\n");
        detalles.append(String.format("Estado: %s\n", creditoSeleccionado.getEstado()));

        // Mostrar diálogo con los detalles
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Crédito");
        alert.setHeaderText(null);
        alert.setContentText(detalles.toString());

        // Hacer el área de texto más grande
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    /**
     * Anula un crédito seleccionado
     */
    @FXML
    private void anularCredito() {
        Credito creditoSeleccionado = tblCreditos.getSelectionModel().getSelectedItem();

        if (creditoSeleccionado == null) {
            mostrarError("Seleccione un crédito para anular");
            return;
        }

        if (!"ACTIVO".equals(creditoSeleccionado.getEstado())) {
            mostrarError("Solo se pueden anular créditos en estado ACTIVO");
            return;
        }

        // Confirmar anulación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Anulación");
        confirmacion.setHeaderText("¿Está seguro que desea anular este crédito?");
        confirmacion.setContentText("Esta acción no se puede deshacer. El saldo del cliente será ajustado.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        // Anular el crédito
        creditoSeleccionado.setEstado("CANCELADO");
        creditoSeleccionado.setSaldoPendiente(0);

        if (creditoDAO.actualizar(creditoSeleccionado)) {
            // Actualizar la venta asociada
            Venta venta = ventaDAO.obtenerPorId(creditoSeleccionado.getIdVenta());
            if (venta != null) {
                venta.setEstado("ANULADA");
                ventaDAO.actualizar(venta);
            }

            // Actualizar saldo del cliente
            Cliente cliente = clienteDAO.obtenerPorId(creditoSeleccionado.getIdCliente());
            if (cliente != null) {
                double montoFinanciado = creditoSeleccionado.getMontoTotal() - creditoSeleccionado.getCuotaInicial();
                double montoConInteres = montoFinanciado * 1.05;
                double nuevoSaldo = cliente.getSaldoPendiente() - montoConInteres;
                cliente.setSaldoPendiente(Math.max(nuevoSaldo, 0));
                clienteDAO.actualizar(cliente);
            }

            // Registrar auditoría
            Usuario usuarioActual = SessionManager.getUsuarioActual();
            if (usuarioActual != null) {
                auditoriaDAO.agregar(new Auditoria(
                    usuarioActual.getIdUsuario(),
                    "ANULAR_CREDITO",
                    "Credito",
                    String.format("Crédito #%d anulado", creditoSeleccionado.getIdCredito()),
                    "127.0.0.1"
                ));
            }

            mostrarExito("Crédito anulado correctamente");
            actualizar();
        } else {
            mostrarError("Error al anular el crédito");
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
