package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.dao.CreditoDAO;
import com.taller.proyecto_bd.dao.CuotaDAO;
import com.taller.proyecto_bd.dao.VentaDAO;
import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.models.*;
import com.taller.proyecto_bd.controllers.CreditoController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador para gestionar el pago de cuotas de créditos.
 * Permite buscar clientes con créditos activos y registrar pagos de cuotas.
 *
 * @author Sistema
 * @version 1.0
 */
public class PagarCuotasController {

    // ==================== CAMPOS DE BÚSQUEDA ====================
    @FXML private TextField txtBuscarCliente;
    @FXML private Label lblClienteNombre;
    @FXML private Label lblClienteCedula;
    @FXML private Label lblSaldoPendiente;
    @FXML private Label lblCuotasVencidas;

    // ==================== TABLA DE CUOTAS ====================
    @FXML private TableView<Cuota> tablaCuotas;
    @FXML private TableColumn<Cuota, Integer> colNumeroCuota;
    @FXML private TableColumn<Cuota, String> colVenta;
    @FXML private TableColumn<Cuota, Double> colValor;
    @FXML private TableColumn<Cuota, String> colFechaVencimiento;
    @FXML private TableColumn<Cuota, String> colEstado;
    @FXML private TableColumn<Cuota, String> colDiasAtraso;

    // ==================== CAMPOS DE PAGO ====================
    @FXML private Label lblCuotaSeleccionada;
    @FXML private Label lblValorCuota;
    @FXML private DatePicker dpFechaPago;
    @FXML private TextField txtMontoPago;
    @FXML private TextArea txtObservaciones;

    // ==================== BOTONES ====================
    @FXML private Button btnBuscar;
    @FXML private Button btnRegistrarPago;
    @FXML private Button btnLimpiar;

    // ==================== LABELS DE MENSAJE ====================
    @FXML private Label lblMensaje;

    // ==================== ATRIBUTOS ====================
    private ClienteDAO clienteDAO;
    private CreditoDAO creditoDAO;
    private CuotaDAO cuotaDAO;
    private VentaDAO ventaDAO;
    private AuditoriaDAO auditoriaDAO;
    private CreditoController creditoController;

    private Cliente clienteSeleccionado;
    private Cuota cuotaSeleccionada;
    private ObservableList<Cuota> listaCuotas;
    private NumberFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        clienteDAO = ClienteDAO.getInstance();
        creditoDAO = CreditoDAO.getInstance();
        cuotaDAO = CuotaDAO.getInstance();
        ventaDAO = VentaDAO.getInstance();
        auditoriaDAO = AuditoriaDAO.getInstance();
        creditoController = new CreditoController();

        listaCuotas = FXCollections.observableArrayList();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        configurarTabla();
        configurarEventos();
        dpFechaPago.setValue(LocalDate.now());
        limpiarFormulario();
    }

    /**
     * Configura las columnas de la tabla de cuotas
     */
    private void configurarTabla() {
        colNumeroCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));

        // Columna de venta (obtener código de venta desde Credito)
        colVenta.setCellValueFactory(cellData -> {
            Cuota cuota = cellData.getValue();
            Credito credito = creditoDAO.obtenerPorId(cuota.getIdCredito());
            if (credito != null) {
                Venta venta = ventaDAO.obtenerPorId(credito.getIdVenta());
                if (venta != null) {
                    return new SimpleStringProperty(venta.getCodigo());
                }
            }
            return new SimpleStringProperty("N/A");
        });

        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValor.setCellFactory(col -> new TableCell<Cuota, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(formatoMoneda.format(valor));
                }
            }
        });

        colFechaVencimiento.setCellValueFactory(cellData -> {
            Date fecha = cellData.getValue().getFechaVencimiento();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });

        colEstado.setCellValueFactory(cellData -> {
            Cuota cuota = cellData.getValue();
            if (cuota.isPagada()) {
                return new SimpleStringProperty("PAGADA");
            } else if (cuota.estaVencida()) {
                return new SimpleStringProperty("VENCIDA");
            } else {
                return new SimpleStringProperty("PENDIENTE");
            }
        });

        colEstado.setCellFactory(col -> new TableCell<Cuota, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    if (estado.equals("PAGADA")) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if (estado.equals("VENCIDA")) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    } else {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    }
                }
            }
        });

        colDiasAtraso.setCellValueFactory(cellData -> {
            Cuota cuota = cellData.getValue();
            if (cuota.isPagada()) {
                return new SimpleStringProperty("0");
            } else if (cuota.estaVencida()) {
                return new SimpleStringProperty(String.valueOf(cuota.diasAtraso()));
            } else {
                return new SimpleStringProperty("0");
            }
        });

        tablaCuotas.setItems(listaCuotas);
    }

    /**
     * Configura los eventos de los controles
     */
    private void configurarEventos() {
        // Selección de cuota en la tabla
        tablaCuotas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cuotaSeleccionada = newVal;
                mostrarDetallesCuota(newVal);
            }
        });

        // Búsqueda con Enter
        txtBuscarCliente.setOnAction(e -> buscarCliente());

        // Actualizar monto cuando cambia la cuota seleccionada
        txtMontoPago.textProperty().addListener((obs, oldVal, newVal) -> {
            validarMontoPago();
        });
    }

    /**
     * Busca un cliente por cédula o nombre
     */
    @FXML
    private void buscarCliente() {
        String criterio = txtBuscarCliente.getText().trim();
        if (criterio.isEmpty()) {
            mostrarError("Ingrese la cédula o nombre del cliente");
            return;
        }

        // Buscar por cédula primero
        Cliente cliente = clienteDAO.obtenerPorCedula(criterio);

        // Si no se encuentra, buscar por nombre
        if (cliente == null) {
            List<Cliente> clientes = clienteDAO.buscarPorNombre(criterio);
            if (clientes.isEmpty()) {
                mostrarError("No se encontró ningún cliente con ese criterio");
                limpiarFormulario();
                return;
            } else if (clientes.size() == 1) {
                cliente = clientes.get(0);
            } else {
                // Mostrar selector si hay múltiples clientes
                cliente = mostrarSelectorClientes(clientes);
                if (cliente == null) {
                    return;
                }
            }
        }

        // Verificar que el cliente tenga créditos activos
        if (!creditoController.clienteTieneCreditoActivo(cliente.getIdCliente())) {
            mostrarAdvertencia("El cliente no tiene créditos activos");
            limpiarFormulario();
            return;
        }

        clienteSeleccionado = cliente;
        mostrarDatosCliente();
        cargarCuotasPendientes();
    }

    /**
     * Muestra un selector cuando hay múltiples clientes
     */
    private Cliente mostrarSelectorClientes(List<Cliente> clientes) {
        ChoiceDialog<Cliente> dialog = new ChoiceDialog<>(clientes.get(0), clientes);
        dialog.setTitle("Seleccionar Cliente");
        dialog.setHeaderText("Se encontraron varios clientes");
        dialog.setContentText("Seleccione el cliente:");

        // Personalizar cómo se muestra cada cliente
        dialog.getDialogPane().lookupButton(ButtonType.OK);

        Optional<Cliente> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Muestra los datos del cliente seleccionado
     */
    private void mostrarDatosCliente() {
        if (clienteSeleccionado == null) return;

        lblClienteNombre.setText(clienteSeleccionado.getNombreCompleto());
        lblClienteCedula.setText(clienteSeleccionado.getCedula());

        double saldoPendiente = creditoController.obtenerSaldoPendienteCliente(clienteSeleccionado.getIdCliente());
        lblSaldoPendiente.setText(formatoMoneda.format(saldoPendiente));

        List<Cuota> cuotasVencidas = creditoController.obtenerCuotasVencidasCliente(clienteSeleccionado.getIdCliente());
        lblCuotasVencidas.setText(String.valueOf(cuotasVencidas.size()));

        if (cuotasVencidas.size() > 0) {
            lblCuotasVencidas.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            lblCuotasVencidas.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
    }

    /**
     * Carga las cuotas pendientes del cliente
     */
    private void cargarCuotasPendientes() {
        if (clienteSeleccionado == null) return;

        listaCuotas.clear();
        List<Cuota> cuotas = creditoController.obtenerCuotasPendientesCliente(clienteSeleccionado.getIdCliente());
        listaCuotas.addAll(cuotas);

        if (cuotas.isEmpty()) {
            mostrarInfo("El cliente no tiene cuotas pendientes");
        }
    }

    /**
     * Muestra los detalles de la cuota seleccionada
     */
    private void mostrarDetallesCuota(Cuota cuota) {
        if (cuota == null) return;

        lblCuotaSeleccionada.setText("Cuota #" + cuota.getNumeroCuota());
        lblValorCuota.setText(formatoMoneda.format(cuota.getValor()));
        txtMontoPago.setText(String.valueOf(cuota.getValor()));

        btnRegistrarPago.setDisable(false);
    }

    /**
     * Valida el monto del pago
     */
    private void validarMontoPago() {
        try {
            if (txtMontoPago.getText().isEmpty()) {
                return;
            }
            double monto = Double.parseDouble(txtMontoPago.getText());
            if (cuotaSeleccionada != null && monto < cuotaSeleccionada.getValor()) {
                txtMontoPago.setStyle("-fx-border-color: red;");
            } else {
                txtMontoPago.setStyle("");
            }
        } catch (NumberFormatException e) {
            txtMontoPago.setStyle("-fx-border-color: red;");
        }
    }

    /**
     * Registra el pago de la cuota seleccionada
     */
    @FXML
    private void registrarPago() {
        if (cuotaSeleccionada == null) {
            mostrarError("Seleccione una cuota para pagar");
            return;
        }

        // Validar monto
        double monto;
        try {
            monto = Double.parseDouble(txtMontoPago.getText());
            if (monto < cuotaSeleccionada.getValor()) {
                mostrarError("El monto debe ser mayor o igual al valor de la cuota");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un monto válido");
            return;
        }

        // Validar fecha de pago
        if (dpFechaPago.getValue() == null) {
            mostrarError("Seleccione la fecha de pago");
            return;
        }

        // Confirmar el pago
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Pago");
        alert.setHeaderText("¿Desea registrar el pago de la cuota?");
        alert.setContentText(String.format("Cuota #%d\nValor: %s\nMonto a pagar: %s",
                cuotaSeleccionada.getNumeroCuota(),
                formatoMoneda.format(cuotaSeleccionada.getValor()),
                formatoMoneda.format(monto)));

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            realizarPago(monto);
        }
    }

    /**
     * Realiza el pago de la cuota
     */
    private void realizarPago(double monto) {
        Date fechaPago = Date.from(dpFechaPago.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Registrar el pago en la base de datos
        boolean pagoExitoso = cuotaDAO.registrarPago(cuotaSeleccionada.getIdCuota(), fechaPago);

        if (pagoExitoso) {
            // Verificar si todas las cuotas del crédito están pagadas
            Credito credito = creditoDAO.obtenerPorId(cuotaSeleccionada.getIdCredito());
            if (credito != null) {
                List<Cuota> cuotasPendientes = cuotaDAO.obtenerPendientesPorCredito(credito.getIdCredito());

                if (cuotasPendientes.isEmpty()) {
                    // Actualizar estado de la venta a PAGADA
                    Venta venta = ventaDAO.obtenerPorId(credito.getIdVenta());
                    if (venta != null) {
                        venta.setEstado("PAGADA");
                        ventaDAO.actualizar(venta);

                        // Actualizar saldo del cliente
                        double montoFinanciado = venta.getMontoFinanciado();
                        double nuevoSaldo = clienteSeleccionado.getSaldoPendiente() - montoFinanciado;
                        clienteSeleccionado.setSaldoPendiente(Math.max(nuevoSaldo, 0));
                        clienteDAO.actualizar(clienteSeleccionado);

                        mostrarExito("¡Pago registrado! El crédito ha sido completamente pagado.");
                    }
                } else {
                    mostrarExito("Pago registrado exitosamente");
                }
            }

            // Registrar auditoría
            Usuario usuario = SessionManager.getInstance().getUsuarioActual();
            if (usuario != null) {
                auditoriaDAO.agregar(new Auditoria(
                    usuario.getIdUsuario(),
                    "PAGO_CUOTA",
                    "Cuota",
                    String.format("Pago de cuota #%d del cliente %s por %s",
                        cuotaSeleccionada.getNumeroCuota(),
                        clienteSeleccionado.getNombreCompleto(),
                        formatoMoneda.format(monto)),
                    "127.0.0.1"
                ));
            }

            // Actualizar la vista
            cargarCuotasPendientes();
            mostrarDatosCliente();
            limpiarFormularioPago();
        } else {
            mostrarError("Error al registrar el pago");
        }
    }

    /**
     * Limpia el formulario completo
     */
    @FXML
    private void limpiarFormulario() {
        txtBuscarCliente.clear();
        lblClienteNombre.setText("-");
        lblClienteCedula.setText("-");
        lblSaldoPendiente.setText("-");
        lblCuotasVencidas.setText("-");
        listaCuotas.clear();
        limpiarFormularioPago();
        clienteSeleccionado = null;
        cuotaSeleccionada = null;
    }

    /**
     * Limpia solo los campos de pago
     */
    private void limpiarFormularioPago() {
        lblCuotaSeleccionada.setText("-");
        lblValorCuota.setText("-");
        txtMontoPago.clear();
        txtObservaciones.clear();
        dpFechaPago.setValue(LocalDate.now());
        btnRegistrarPago.setDisable(true);
        cuotaSeleccionada = null;
    }

    // ==================== MÉTODOS DE MENSAJES ====================

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

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
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
