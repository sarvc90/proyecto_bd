package com.taller.proyecto_bd.ui;



import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador para el módulo de Nueva Venta
 */
public class NuevaVentaController {

    // ==================== SECCIÓN CLIENTE ====================
    @FXML private TextField txtBuscarCliente;
    @FXML private Label lblClienteNombre;
    @FXML private Label lblClienteCedula;
    @FXML private Label lblClienteLimite;
    @FXML private Label lblClienteDisponible;
    
    // ==================== SECCIÓN PRODUCTOS ====================
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private Spinner<Integer> spnCantidad;
    @FXML private TableView<DetalleVenta> tablaCarrito;
    @FXML private TableColumn<DetalleVenta, String> colProductoCodigo;
    @FXML private TableColumn<DetalleVenta, String> colProductoNombre;
    @FXML private TableColumn<DetalleVenta, Integer> colProductoCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colProductoPrecio;
    @FXML private TableColumn<DetalleVenta, Double> colProductoSubtotal;
    @FXML private TableColumn<DetalleVenta, Double> colProductoIVA;
    @FXML private TableColumn<DetalleVenta, Double> colProductoTotal;
    @FXML private TableColumn<DetalleVenta, Void> colAcciones;
    
    // ==================== SECCIÓN FORMA DE PAGO ====================
    @FXML private RadioButton rbContado;
    @FXML private RadioButton rbCredito;
    @FXML private ToggleGroup grupoPago;
    @FXML private VBox panelCredito;
    @FXML private Label lblCuotaInicial;
    @FXML private ComboBox<String> cmbPlazo;
    @FXML private Label lblInteres;
    @FXML private Label lblValorCuota;
    
    // ==================== SECCIÓN TOTALES ====================
    @FXML private Label lblCodigoVenta;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIVA;
    @FXML private Label lblTotal;
    @FXML private Button btnNuevaVenta;
    @FXML private Button btnGuardarVenta;
    @FXML private Label lblMensaje;
    
    // ==================== ATRIBUTOS ====================
    private ClienteDAO clienteDAO;
    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;
    private DetalleVentaDAO detalleVentaDAO;
    private CuotaDAO cuotaDAO;

    private Cliente clienteSeleccionado;
    private ObservableList<DetalleVenta> carrito;
    private NumberFormat formatoMoneda;

    private static final double IVA_PORCENTAJE = 0.12; // 12%
    private static final double CUOTA_INICIAL_PORCENTAJE = 0.30; // 30%
    private static final double INTERES_PORCENTAJE = 0.05; // 5%
    
    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        // Inicializar DAOs
        clienteDAO = ClienteDAO.getInstance();
        productoDAO = ProductoDAO.getInstance();
        ventaDAO = VentaDAO.getInstance();
        detalleVentaDAO = DetalleVentaDAO.getInstance();
        cuotaDAO = CuotaDAO.getInstance();

        carrito = FXCollections.observableArrayList();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        configurarTabla();
        configurarProductos();
        configurarSpinner();
        configurarPlazos();
        configurarEventos();
        generarCodigoVenta();
    }
    
    /**
     * Configura la tabla del carrito
     */
    private void configurarTabla() {
        colProductoCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colProductoPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colProductoSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colProductoIVA.setCellValueFactory(new PropertyValueFactory<>("montoIVA"));
        colProductoTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Columna de código del producto
        colProductoCodigo.setCellValueFactory(cellData -> {
            int idProducto = cellData.getValue().getIdProducto();
            Producto p = productoDAO.obtenerPorId(idProducto);
            return new javafx.beans.property.SimpleStringProperty(p != null ? p.getCodigo() : "");
        });
        
        // Columna de nombre del producto
        colProductoNombre.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombreProducto())
        );
        
        // Formato de moneda
        configurarColumnasMoneda();
        
        // Columna de acciones (botón eliminar)
        configurarColumnaAcciones();
        
        tablaCarrito.setItems(carrito);
    }
    
    /**
     * Configura el formato de las columnas de moneda
     */
    private void configurarColumnasMoneda() {
        colProductoPrecio.setCellFactory(col -> new TableCell<DetalleVenta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatoMoneda.format(item));
            }
        });
        
        colProductoSubtotal.setCellFactory(col -> new TableCell<DetalleVenta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatoMoneda.format(item));
            }
        });
        
        colProductoIVA.setCellFactory(col -> new TableCell<DetalleVenta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatoMoneda.format(item));
            }
        });
        
        colProductoTotal.setCellFactory(col -> new TableCell<DetalleVenta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatoMoneda.format(item));
            }
        });
    }
    
    /**
     * Configura la columna de acciones con botón eliminar
     */
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<DetalleVenta, Void>() {
            private final Button btnEliminar = new Button("🗑");
            
            {
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnEliminar.setOnAction(event -> {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    carrito.remove(detalle);
                    calcularTotales();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });
    }
    
    /**
     * Carga los productos en el ComboBox
     */
    private void configurarProductos() {
        List<Producto> productos = productoDAO.obtenerActivos();
        cmbProducto.setItems(FXCollections.observableArrayList(productos));
        
        // Configurar cómo se muestra cada producto
        cmbProducto.setConverter(new StringConverter<Producto>() {
            @Override
            public String toString(Producto producto) {
                if (producto == null) return "";
                return producto.getCodigo() + " - " + producto.getNombre() + 
                       " - " + formatoMoneda.format(producto.getPrecioVenta()) +
                       " (Stock: " + producto.getStockActual() + ")";
            }
            
            @Override
            public Producto fromString(String string) {
                return null;
            }
        });
    }
    
    /**
     * Configura el Spinner de cantidad
     */
    private void configurarSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spnCantidad.setValueFactory(valueFactory);
    }

    /**
     * Configura el ComboBox de plazos de crédito
     */
    private void configurarPlazos() {
        cmbPlazo.getItems().addAll("12 meses", "18 meses", "24 meses");
    }

    /**
     * Configura los eventos de la interfaz
     */
    private void configurarEventos() {
        // Cambiar entre contado y crédito
        rbContado.selectedProperty().addListener((obs, old, newVal) -> {
            panelCredito.setDisable(newVal);
            if (newVal) {
                calcularTotales();
            }
        });
        
        rbCredito.selectedProperty().addListener((obs, old, newVal) -> {
            panelCredito.setDisable(!newVal);
            if (newVal) {
                calcularTotalesCredito();
            }
        });
        
        // Actualizar cálculos cuando cambia el plazo
        cmbPlazo.valueProperty().addListener((obs, old, newVal) -> {
            if (rbCredito.isSelected()) {
                calcularTotalesCredito();
            }
        });
    }
    
    /**
     * Genera un código único para la venta consultando la base de datos
     */
    private void generarCodigoVenta() {
        String codigo = ventaDAO.generarCodigoVenta();
        lblCodigoVenta.setText(codigo);
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
        
        // Si no encuentra por cédula, buscar por nombre
        if (cliente == null) {
            List<Cliente> clientes = clienteDAO.buscarPorNombre(criterio);
            if (clientes.isEmpty()) {
                mostrarError("No se encontró ningún cliente con ese criterio");
                return;
            }
            
            if (clientes.size() == 1) {
                cliente = clientes.get(0);
            } else {
                // Si hay varios, mostrar diálogo de selección
                cliente = mostrarDialogoSeleccionCliente(clientes);
            }
        }
        
        if (cliente != null) {
            seleccionarCliente(cliente);
        }
    }
    
    /**
     * Muestra un diálogo para seleccionar entre varios clientes
     */
    private Cliente mostrarDialogoSeleccionCliente(List<Cliente> clientes) {
        ChoiceDialog<Cliente> dialog = new ChoiceDialog<>(clientes.get(0), clientes);
        dialog.setTitle("Seleccionar Cliente");
        dialog.setHeaderText("Se encontraron varios clientes");
        dialog.setContentText("Seleccione un cliente:");
        
        // Configurar cómo se muestra cada cliente
        dialog.getItems().forEach(c -> c.toString());
        
        Optional<Cliente> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Selecciona un cliente y actualiza la información
     */
    private void seleccionarCliente(Cliente cliente) {
        if (!cliente.isActivo()) {
            mostrarError("El cliente está inactivo");
            return;
        }
        
        this.clienteSeleccionado = cliente;
        lblClienteNombre.setText(cliente.getNombreCompleto());
        lblClienteCedula.setText(cliente.getCedula());
        lblClienteLimite.setText(formatoMoneda.format(cliente.getLimiteCredito()));
        lblClienteDisponible.setText(formatoMoneda.format(cliente.getCreditoDisponible()));
        
        mostrarExito("Cliente seleccionado: " + cliente.getNombreCompleto());
    }
    
    /**
     * Abre el formulario para crear un nuevo cliente
     */
    @FXML
    private void nuevoCliente() {
        mostrarInfo("Funcionalidad de nuevo cliente en desarrollo.\nPor ahora use el módulo de Gestión de Clientes.");
    }
    
    /**
     * Agrega un producto al carrito
     */
    @FXML
    private void agregarProducto() {
        Producto producto = cmbProducto.getValue();
        
        if (producto == null) {
            mostrarError("Seleccione un producto");
            return;
        }
        
        int cantidad = spnCantidad.getValue();
        
        // Validar stock
        if (!producto.hayStockSuficiente(cantidad)) {
            mostrarError("Stock insuficiente. Disponible: " + producto.getStockActual());
            return;
        }
        
        // Verificar si el producto ya está en el carrito
        Optional<DetalleVenta> existente = carrito.stream()
                .filter(d -> d.getIdProducto() == producto.getIdProducto())
                .findFirst();
        
        if (existente.isPresent()) {
            // Si ya existe, aumentar la cantidad
            DetalleVenta detalle = existente.get();
            int nuevaCantidad = detalle.getCantidad() + cantidad;
            
            if (!producto.hayStockSuficiente(nuevaCantidad)) {
                mostrarError("Stock insuficiente para esa cantidad total");
                return;
            }
            
            detalle.setCantidad(nuevaCantidad);
            detalle.calcularTotales(IVA_PORCENTAJE);
        } else {
            // Si no existe, crear nuevo detalle
            DetalleVenta detalle = new DetalleVenta(
                producto.getIdProducto(),
                cantidad,
                producto.getPrecioVenta(),
                IVA_PORCENTAJE
            );
            detalle.setNombreProducto(producto.getNombre());
            carrito.add(detalle);
        }
        
        calcularTotales();
        tablaCarrito.refresh();
        
        // Limpiar selección
        cmbProducto.setValue(null);
        spnCantidad.getValueFactory().setValue(1);
    }
    
    /**
     * Calcula los totales de la venta
     */
    private void calcularTotales() {
        double subtotal = carrito.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        double iva = carrito.stream().mapToDouble(DetalleVenta::getMontoIVA).sum();
        double total = subtotal + iva;
        
        lblSubtotal.setText(formatoMoneda.format(subtotal));
        lblIVA.setText(formatoMoneda.format(iva));
        lblTotal.setText(formatoMoneda.format(total));
        
        if (rbCredito.isSelected()) {
            calcularTotalesCredito();
        }
    }
    
    /**
     * Calcula los totales para venta a crédito
     */
    private void calcularTotalesCredito() {
        double total = carrito.stream().mapToDouble(DetalleVenta::getTotal).sum();
        
        if (total == 0) {
            lblCuotaInicial.setText(formatoMoneda.format(0));
            lblInteres.setText(formatoMoneda.format(0));
            lblValorCuota.setText(formatoMoneda.format(0));
            return;
        }
        
        double cuotaInicial = total * CUOTA_INICIAL_PORCENTAJE;
        double saldoFinanciar = total - cuotaInicial;
        double interes = saldoFinanciar * INTERES_PORCENTAJE;
        double montoTotal = saldoFinanciar + interes;
        
        lblCuotaInicial.setText(formatoMoneda.format(cuotaInicial));
        lblInteres.setText(formatoMoneda.format(interes));
        
        // Calcular valor de cuota según el plazo
        String plazoStr = cmbPlazo.getValue();
        if (plazoStr != null) {
            int plazo = Integer.parseInt(plazoStr.split(" ")[0]);
            double valorCuota = montoTotal / plazo;
            lblValorCuota.setText(formatoMoneda.format(valorCuota));
        }
    }
    
    /**
     * Guarda la venta en la base de datos
     */
    @FXML
    private void guardarVenta() {
        if (!validarVenta()) {
            return;
        }
        
        try {
            // Crear la venta
            Venta venta = new Venta();
            venta.setCodigo(lblCodigoVenta.getText());
            venta.setIdCliente(clienteSeleccionado.getIdCliente());
            venta.setIdUsuario(SessionManager.getIdUsuarioActual());
            venta.setEsCredito(rbCredito.isSelected());
            
            // Calcular totales
            double subtotal = carrito.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
            double iva = carrito.stream().mapToDouble(DetalleVenta::getMontoIVA).sum();
            double total = subtotal + iva;
            
            venta.setSubtotal(subtotal);
            venta.setIvaTotal(iva);
            venta.setTotal(total);

            // Configurar crédito si aplica
            if (rbCredito.isSelected()) {
                String plazoStr = cmbPlazo.getValue();
                int plazo = Integer.parseInt(plazoStr.split(" ")[0]);

                // Usar el método helper de Venta para configurar crédito automáticamente
                venta.configurarComoCredito(plazo);
            }

            venta.setEstado("REGISTRADA");
            
            // Guardar venta
            boolean exitoVenta = ventaDAO.agregar(venta);
            
            if (!exitoVenta) {
                mostrarError("Error al guardar la venta");
                return;
            }
            
            // Guardar detalles
            for (DetalleVenta detalle : carrito) {
                detalle.setIdVenta(venta.getIdVenta());
                detalleVentaDAO.agregar(detalle);
                
                // Actualizar stock
                Producto producto = productoDAO.obtenerPorId(detalle.getIdProducto());
                if (producto != null) {
                    producto.vender(detalle.getCantidad());
                    productoDAO.actualizar(producto);
                }
            }
            
            // Si es crédito, crear crédito y generar las cuotas
            if (rbCredito.isSelected()) {
                try {
                    // Crear el registro de crédito
                    double montoFinanciado = venta.getSaldoFinanciar(); // 70% del total
                    Credito credito = new Credito(
                        venta.getIdVenta(),
                        clienteSeleccionado.getIdCliente(),
                        montoFinanciado,
                        venta.getCuotaInicial(),
                        venta.getPlazoMeses(),
                        INTERES_PORCENTAJE
                    );
                    credito.setSaldoPendiente(montoFinanciado * 1.05); // con interés

                    // Guardar el crédito en la base de datos
                    CreditoDAO creditoDAO = CreditoDAO.getInstance();
                    if (!creditoDAO.agregar(credito)) {
                        mostrarError("Error al crear el crédito");
                        return;
                    }

                    // Ahora generar las cuotas usando el idCredito
                    boolean cuotasGeneradas = cuotaDAO.generarCuotas(
                        credito.getIdCredito(),  // Usar idCredito en lugar de idVenta
                        venta.getTotal(),
                        venta.getPlazoMeses(),
                        venta.getFechaVenta()
                    );

                    if (!cuotasGeneradas) {
                        mostrarError("Advertencia: Error al generar las cuotas del crédito");
                    }

                    // Actualizar saldo del cliente
                    double saldoFinanciar = venta.getSaldoFinanciar();
                    double montoConInteres = saldoFinanciar * 1.05; // 5% de interés
                    clienteSeleccionado.setSaldoPendiente(
                        clienteSeleccionado.getSaldoPendiente() + montoConInteres
                    );
                    clienteDAO.actualizar(clienteSeleccionado);
                } catch (Exception e) {
                    mostrarError("Error al procesar el crédito: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
            
            mostrarExito("¡Venta guardada exitosamente! Código: " + venta.getCodigo());
            
            // Preguntar si desea imprimir factura
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Venta Exitosa");
            alert.setHeaderText("La venta se guardó correctamente");
            alert.setContentText("¿Desea imprimir la factura?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // TODO: Implementar impresión de factura
                mostrarInfo("Funcionalidad de impresión en desarrollo");
            }
            
            // Limpiar formulario
            nuevaVenta();
            
        } catch (Exception e) {
            mostrarError("Error al guardar la venta: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Valida que la venta esté completa
     */
    private boolean validarVenta() {
        if (clienteSeleccionado == null) {
            mostrarError("Debe seleccionar un cliente");
            return false;
        }
        
        if (carrito.isEmpty()) {
            mostrarError("Debe agregar al menos un producto");
            return false;
        }
        
        if (rbCredito.isSelected()) {
            if (cmbPlazo.getValue() == null) {
                mostrarError("Debe seleccionar el plazo de pago");
                return false;
            }
            
            // Validar que el cliente tenga crédito disponible
            double total = carrito.stream().mapToDouble(DetalleVenta::getTotal).sum();
            double cuotaInicial = total * CUOTA_INICIAL_PORCENTAJE;
            double saldoFinanciar = total - cuotaInicial;
            
            if (!clienteSeleccionado.puedeComprarACredito(saldoFinanciar)) {
                mostrarError("El cliente no tiene crédito disponible suficiente");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Limpia el formulario para una nueva venta
     */
    @FXML
    private void nuevaVenta() {
        carrito.clear();
        clienteSeleccionado = null;
        
        lblClienteNombre.setText("No seleccionado");
        lblClienteCedula.setText("-");
        lblClienteLimite.setText("$ 0.00");
        lblClienteDisponible.setText("$ 0.00");
        
        txtBuscarCliente.clear();
        cmbProducto.setValue(null);
        spnCantidad.getValueFactory().setValue(1);
        
        rbContado.setSelected(true);
        cmbPlazo.setValue(null);
        
        lblSubtotal.setText("$ 0.00");
        lblIVA.setText("$ 0.00");
        lblTotal.setText("$ 0.00");
        lblCuotaInicial.setText("$ 0.00");
        lblInteres.setText("$ 0.00");
        lblValorCuota.setText("$ 0.00");
        
        generarCodigoVenta();
        ocultarMensaje();
        
        configurarProductos(); // Recargar productos por si cambió el stock
    }
    
    /**
     * Cierra la ventana
     */
    @FXML
    private void cerrarVentana() {
        if (!carrito.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar");
            alert.setHeaderText("Hay productos en el carrito");
            alert.setContentText("¿Está seguro que desea salir sin guardar?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }
        
        Stage stage = (Stage) btnNuevaVenta.getScene().getWindow();
        stage.close();
    }
    
    // ==================== MÉTODOS DE MENSAJES ====================
    
    private void mostrarExito(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
    }
    
    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-background-color: #fadbd8; -fx-text-fill: #e74c3c;");
        lblMensaje.setVisible(true);
    }
    
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void ocultarMensaje() {
        lblMensaje.setVisible(false);
    }
}
