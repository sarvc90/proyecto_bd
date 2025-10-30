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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador para el módulo de Nueva Compra (Para Clientes)
 */
public class NuevaCompraController {

    // ==================== SECCIÓN INFORMACIÓN DEL CLIENTE ====================
    @FXML private Label lblClienteNombreTop;
    @FXML private Label lblCreditoDisponibleTop;
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
    @FXML private Label lblSubtotal;
    @FXML private Label lblIVA;
    @FXML private Label lblTotal;
    @FXML private Button btnLimpiar;
    @FXML private Button btnRealizarCompra;
    @FXML private Label lblMensaje;

    // ==================== ATRIBUTOS ====================
    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;
    private DetalleVentaDAO detalleVentaDAO;
    private CreditoDAO creditoDAO;
    private ClienteDAO clienteDAO;

    private Cliente clienteActual;
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
        productoDAO = ProductoDAO.getInstance();
        ventaDAO = VentaDAO.getInstance();
        detalleVentaDAO = DetalleVentaDAO.getInstance();
        creditoDAO = CreditoDAO.getInstance();
        clienteDAO = ClienteDAO.getInstance();

        carrito = FXCollections.observableArrayList();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        configurarTabla();
        configurarProductos();
        configurarSpinner();
        configurarFormaPago();

        // Cargar cliente desde la sesión
        cargarClienteActual();
    }

    /**
     * Carga la información del cliente desde la sesión
     */
    private void cargarClienteActual() {
        Usuario usuarioActual = SessionManager.getUsuarioActual();
        if (usuarioActual != null && "CLIENTE".equals(usuarioActual.getRol())) {
            // Buscar el cliente por email o username
            // Asumimos que el username del usuario cliente es su cédula
            String cedula = usuarioActual.getUsername();
            clienteActual = clienteDAO.obtenerPorCedula(cedula);

            if (clienteActual != null) {
                actualizarInfoCliente();
            } else {
                mostrarError("No se pudo cargar la información del cliente");
            }
        }
    }

    /**
     * Actualiza la información del cliente en la interfaz
     */
    private void actualizarInfoCliente() {
        if (clienteActual != null) {
            lblClienteNombreTop.setText(clienteActual.getNombreCompleto());
            lblClienteNombre.setText(clienteActual.getNombreCompleto());
            lblClienteCedula.setText(clienteActual.getCedula());
            lblClienteLimite.setText(formatoMoneda.format(clienteActual.getLimiteCredito()));
            lblClienteDisponible.setText(formatoMoneda.format(clienteActual.getCreditoDisponible()));
            lblCreditoDisponibleTop.setText("Crédito disponible: " +
                formatoMoneda.format(clienteActual.getCreditoDisponible()));
        }
    }

    /**
     * Configura la tabla de productos en el carrito
     */
    private void configurarTabla() {
        colProductoCodigo.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue().getProducto();
            return new javafx.beans.property.SimpleStringProperty(
                producto != null ? producto.getCodigo() : ""
            );
        });

        colProductoNombre.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue().getProducto();
            return new javafx.beans.property.SimpleStringProperty(
                producto != null ? producto.getNombre() : ""
            );
        });

        colProductoCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colProductoPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colProductoSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colProductoIVA.setCellValueFactory(new PropertyValueFactory<>("montoIVA"));
        colProductoTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Formatear columnas de moneda
        colProductoPrecio.setCellFactory(col -> crearCeldaMoneda());
        colProductoSubtotal.setCellFactory(col -> crearCeldaMoneda());
        colProductoIVA.setCellFactory(col -> crearCeldaMoneda());
        colProductoTotal.setCellFactory(col -> crearCeldaMoneda());

        // Columna de acciones
        configurarColumnaAcciones();

        tablaCarrito.setItems(carrito);
    }

    /**
     * Crea una celda formateada para mostrar moneda
     */
    private TableCell<DetalleVenta, Double> crearCeldaMoneda() {
        return new TableCell<DetalleVenta, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(formatoMoneda.format(valor));
                }
            }
        };
    }

    /**
     * Configura la columna de acciones con botón eliminar
     */
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<DetalleVenta, Void>() {
            private final Button btnEliminar = new Button("🗑️");

            {
                btnEliminar.getStyleClass().add("btn-danger");
                btnEliminar.setOnAction(event -> {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    eliminarDelCarrito(detalle);
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
     * Configura el ComboBox de productos
     */
    private void configurarProductos() {
        List<Producto> productos = productoDAO.obtenerActivos();
        cmbProducto.setItems(FXCollections.observableArrayList(productos));

        // Configurar cómo se muestra el producto en el ComboBox
        cmbProducto.setConverter(new StringConverter<Producto>() {
            @Override
            public String toString(Producto producto) {
                if (producto == null) return "";
                return producto.getCodigo() + " - " + producto.getNombre() +
                       " (" + formatoMoneda.format(producto.getPrecioVenta()) + ")";
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
     * Configura los listeners de la forma de pago
     */
    private void configurarFormaPago() {
        // Llenar opciones de plazo
        cmbPlazo.getItems().addAll("12 meses", "18 meses", "24 meses");

        // Habilitar/deshabilitar panel de crédito según selección
        rbContado.selectedProperty().addListener((obs, oldVal, newVal) -> {
            panelCredito.setDisable(newVal);
            calcularTotales();
        });

        rbCredito.selectedProperty().addListener((obs, oldVal, newVal) -> {
            panelCredito.setDisable(!newVal);
            if (newVal) {
                validarCreditoDisponible();
            }
            calcularTotales();
        });

        // Seleccionar plazo por defecto
        if (cmbPlazo.getItems().size() > 0) {
            cmbPlazo.getSelectionModel().select(0);
        }

        // Recalcular cuando cambie el plazo
        cmbPlazo.valueProperty().addListener((obs, oldVal, newVal) -> {
            calcularTotales();
        });
    }

    /**
     * Valida que el cliente tenga crédito disponible
     */
    private void validarCreditoDisponible() {
        if (clienteActual == null || !clienteActual.tieneCreditoDisponible()) {
            mostrarError("No tiene crédito disponible para compras a crédito");
            rbContado.setSelected(true);
        }
    }

    /**
     * Agrega un producto al carrito
     */
    @FXML
    private void agregarAlCarrito() {
        Producto producto = cmbProducto.getValue();
        if (producto == null) {
            mostrarAdvertencia("Debe seleccionar un producto");
            return;
        }

        int cantidad = spnCantidad.getValue();
        if (cantidad <= 0) {
            mostrarAdvertencia("La cantidad debe ser mayor a cero");
            return;
        }

        // Verificar stock disponible
        if (cantidad > producto.getStockActual()) {
            mostrarAdvertencia("Stock insuficiente. Disponible: " + producto.getStockActual());
            return;
        }

        // Crear detalle de venta
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecioVenta());

        // Calcular totales del detalle
        double subtotal = cantidad * producto.getPrecioVenta();
        double iva = subtotal * IVA_PORCENTAJE;
        double total = subtotal + iva;

        detalle.setSubtotal(subtotal);
        detalle.setMontoIVA(iva);
        detalle.setTotal(total);

        // Agregar al carrito
        carrito.add(detalle);
        calcularTotales();

        // Limpiar selección
        cmbProducto.setValue(null);
        spnCantidad.getValueFactory().setValue(1);

        mostrarMensaje("Producto agregado al carrito", false);
    }

    /**
     * Elimina un producto del carrito
     */
    private void eliminarDelCarrito(DetalleVenta detalle) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Producto");
        confirmacion.setHeaderText("¿Eliminar producto del carrito?");
        confirmacion.setContentText(detalle.getProducto().getNombre());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            carrito.remove(detalle);
            calcularTotales();
            mostrarMensaje("Producto eliminado", false);
        }
    }

    /**
     * Calcula los totales de la compra
     */
    private void calcularTotales() {
        double subtotal = 0;
        double ivaTotal = 0;
        double total = 0;

        for (DetalleVenta detalle : carrito) {
            subtotal += detalle.getSubtotal();
            ivaTotal += detalle.getMontoIVA();
            total += detalle.getTotal();
        }

        lblSubtotal.setText(formatoMoneda.format(subtotal));
        lblIVA.setText(formatoMoneda.format(ivaTotal));
        lblTotal.setText(formatoMoneda.format(total));

        // Si es crédito, calcular cuotas
        if (rbCredito.isSelected()) {
            calcularCredito(total);
        } else {
            limpiarDatosCredito();
        }
    }

    /**
     * Calcula los datos del crédito
     */
    private void calcularCredito(double total) {
        if (total <= 0) {
            limpiarDatosCredito();
            return;
        }

        double cuotaInicial = total * CUOTA_INICIAL_PORCENTAJE;
        double saldoFinanciar = total - cuotaInicial;
        double interes = saldoFinanciar * INTERES_PORCENTAJE;

        // Obtener plazo seleccionado
        String plazoStr = cmbPlazo.getValue();
        int meses = plazoStr != null ? Integer.parseInt(plazoStr.split(" ")[0]) : 12;

        double valorCuota = (saldoFinanciar + interes) / meses;

        lblCuotaInicial.setText(formatoMoneda.format(cuotaInicial));
        lblInteres.setText(formatoMoneda.format(interes));
        lblValorCuota.setText(formatoMoneda.format(valorCuota));
    }

    /**
     * Limpia los datos de crédito
     */
    private void limpiarDatosCredito() {
        lblCuotaInicial.setText(formatoMoneda.format(0));
        lblInteres.setText(formatoMoneda.format(0));
        lblValorCuota.setText(formatoMoneda.format(0));
    }

    /**
     * Limpia el carrito de compras
     */
    @FXML
    private void limpiarCarrito() {
        if (carrito.isEmpty()) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Limpiar Carrito");
        confirmacion.setHeaderText("¿Vaciar todo el carrito?");
        confirmacion.setContentText("Se eliminarán todos los productos");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            carrito.clear();
            calcularTotales();
            mostrarMensaje("Carrito limpiado", false);
        }
    }

    /**
     * Realiza la compra
     */
    @FXML
    private void realizarCompra() {
        // Validaciones
        if (carrito.isEmpty()) {
            mostrarAdvertencia("El carrito está vacío");
            return;
        }

        if (clienteActual == null) {
            mostrarError("No se ha identificado el cliente");
            return;
        }

        // Validar si es crédito que tenga crédito disponible
        boolean esCredito = rbCredito.isSelected();
        double totalCompra = calcularTotalCompra();

        if (esCredito) {
            if (!clienteActual.tieneCreditoDisponible()) {
                mostrarError("No tiene crédito disponible");
                return;
            }

            if (clienteActual.getCreditoDisponible() < totalCompra) {
                mostrarError("El monto de la compra excede su crédito disponible.\n" +
                           "Crédito disponible: " + formatoMoneda.format(clienteActual.getCreditoDisponible()) + "\n" +
                           "Total de la compra: " + formatoMoneda.format(totalCompra));
                return;
            }
        }

        // Confirmar la compra
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Compra");
        confirmacion.setHeaderText("¿Desea confirmar esta compra?");
        confirmacion.setContentText(
            "Total: " + formatoMoneda.format(totalCompra) + "\n" +
            "Forma de pago: " + (esCredito ? "Crédito" : "Contado")
        );

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            procesarCompra(esCredito, totalCompra);
        }
    }

    /**
     * Procesa la compra guardándola en la base de datos
     */
    private void procesarCompra(boolean esCredito, double totalCompra) {
        try {
            // 1. Crear la venta
            Venta venta = new Venta();
            venta.setCodigo(generarCodigoVenta());
            venta.setIdCliente(clienteActual.getIdCliente());
            venta.setIdUsuario(SessionManager.getIdUsuarioActual());
            venta.setFechaVenta(new java.util.Date());
            venta.setEsCredito(esCredito);

            // Calcular totales
            double subtotal = 0;
            double ivaTotal = 0;
            for (DetalleVenta detalle : carrito) {
                subtotal += detalle.getSubtotal();
                ivaTotal += detalle.getMontoIVA();
            }

            venta.setSubtotal(subtotal);
            venta.setIvaTotal(ivaTotal);
            venta.setTotal(totalCompra);
            venta.setEstado("REGISTRADA");

            if (esCredito) {
                double cuotaInicial = totalCompra * CUOTA_INICIAL_PORCENTAJE;
                venta.setCuotaInicial(cuotaInicial);

                String plazoStr = cmbPlazo.getValue();
                int plazoMeses = plazoStr != null ? Integer.parseInt(plazoStr.split(" ")[0]) : 12;
                venta.setPlazoMeses(plazoMeses);
            } else {
                venta.setCuotaInicial(0);
                venta.setPlazoMeses(0);
            }

            // 2. Guardar la venta
            boolean ventaGuardada = ventaDAO.agregar(venta);

            if (!ventaGuardada) {
                mostrarError("No se pudo guardar la venta");
                return;
            }

            // 3. Guardar los detalles de la venta
            boolean detallesGuardados = true;
            for (DetalleVenta detalle : carrito) {
                detalle.setIdVenta(venta.getIdVenta());
                detalle.setIdProducto(detalle.getProducto().getIdProducto());

                boolean detalleGuardado = detalleVentaDAO.agregar(detalle);
                if (!detalleGuardado) {
                    detallesGuardados = false;
                    System.err.println("Error al guardar detalle del producto: " +
                                     detalle.getProducto().getNombre());
                }

                // 4. Actualizar inventario (reducir stock)
                Producto producto = detalle.getProducto();
                int nuevoStock = producto.getStockActual() - detalle.getCantidad();
                producto.setStockActual(nuevoStock);
                productoDAO.actualizar(producto);
            }

            // 5. Si es crédito, crear el registro de crédito
            if (esCredito) {
                crearCredito(venta, totalCompra);
            }

            // 6. Actualizar saldo del cliente si es crédito
            if (esCredito) {
                double nuevoSaldo = clienteActual.getSaldoPendiente() + totalCompra;
                clienteActual.setSaldoPendiente(nuevoSaldo);
                clienteDAO.actualizar(clienteActual);
            }

            // 7. Mostrar mensaje de éxito
            mostrarCompraExitosa(venta);

            // 8. Limpiar el carrito
            carrito.clear();
            calcularTotales();

            // Recargar información del cliente
            clienteActual = clienteDAO.obtenerPorCedula(clienteActual.getCedula());
            actualizarInfoCliente();

        } catch (Exception e) {
            mostrarError("Error al procesar la compra: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un código único para la venta
     */
    private String generarCodigoVenta() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
        String fecha = sdf.format(new java.util.Date());
        int random = (int) (Math.random() * 1000);
        return "VC-" + fecha + "-" + String.format("%03d", random);
    }

    /**
     * Crea el registro de crédito para la venta
     */
    private void crearCredito(Venta venta, double totalCompra) {
        try {
            Credito credito = new Credito();
            credito.setIdVenta(venta.getIdVenta());
            credito.setIdCliente(venta.getIdCliente());
            credito.setMontoTotal(totalCompra);
            credito.setInteres(INTERES_PORCENTAJE);
            credito.setPlazoMeses(venta.getPlazoMeses());
            credito.setCuotaInicial(venta.getCuotaInicial());

            double saldoFinanciar = totalCompra - venta.getCuotaInicial();
            credito.setSaldoPendiente(saldoFinanciar);
            credito.setEstado("ACTIVO");
            credito.setFechaRegistro(new java.util.Date());

            boolean creditoGuardado = creditoDAO.agregar(credito);

            if (creditoGuardado) {
                // Generar las cuotas
                generarCuotas(credito);
            }
        } catch (Exception e) {
            System.err.println("Error al crear crédito: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera las cuotas del crédito
     */
    private void generarCuotas(Credito credito) {
        try {
            double saldoFinanciar = credito.getSaldoPendiente();
            double interes = saldoFinanciar * credito.getInteres();
            double totalConInteres = saldoFinanciar + interes;
            double valorCuota = totalConInteres / credito.getPlazoMeses();

            // Obtener CuotaDAO si existe
            // Por ahora, solo calculamos - la implementación de cuotas puede variar
            System.out.println("Crédito generado:");
            System.out.println("- Plazo: " + credito.getPlazoMeses() + " meses");
            System.out.println("- Valor cuota: " + formatoMoneda.format(valorCuota));
            System.out.println("- Total a pagar: " + formatoMoneda.format(totalConInteres));

        } catch (Exception e) {
            System.err.println("Error al generar cuotas: " + e.getMessage());
        }
    }

    /**
     * Muestra el mensaje de compra exitosa
     */
    private void mostrarCompraExitosa(Venta venta) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Compra Exitosa");
        alert.setHeaderText("¡Su compra se ha procesado correctamente!");

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Código de venta: ").append(venta.getCodigo()).append("\n");
        mensaje.append("Total: ").append(formatoMoneda.format(venta.getTotal())).append("\n");
        mensaje.append("Forma de pago: ").append(venta.isEsCredito() ? "Crédito" : "Contado").append("\n");

        if (venta.isEsCredito()) {
            mensaje.append("\nCuota inicial: ").append(formatoMoneda.format(venta.getCuotaInicial())).append("\n");
            mensaje.append("Plazo: ").append(venta.getPlazoMeses()).append(" meses\n");

            double saldoFinanciar = venta.getTotal() - venta.getCuotaInicial();
            double interes = saldoFinanciar * INTERES_PORCENTAJE;
            double valorCuota = (saldoFinanciar + interes) / venta.getPlazoMeses();

            mensaje.append("Valor cuota mensual: ").append(formatoMoneda.format(valorCuota)).append("\n");
        }

        mensaje.append("\n¡Gracias por su compra!");

        alert.setContentText(mensaje.toString());
        alert.showAndWait();
    }

    /**
     * Calcula el total de la compra
     */
    private double calcularTotalCompra() {
        double total = 0;
        for (DetalleVenta detalle : carrito) {
            total += detalle.getTotal();
        }
        return total;
    }

    /**
     * Cierra la ventana
     */
    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnRealizarCompra.getScene().getWindow();
        stage.close();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        lblMensaje.setVisible(true);
        lblMensaje.getStyleClass().clear();
        lblMensaje.getStyleClass().add(esError ? "error-label" : "success-label");
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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
}
