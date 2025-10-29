package com.taller.proyecto_bd.ui.transacciones;

import com.taller.proyecto_bd.dao.CategoriaDAO;
import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.dao.DetalleVentaDAO;
import com.taller.proyecto_bd.dao.ProductoDAO;
import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.dao.VentaDAO;
import com.taller.proyecto_bd.models.Categoria;
import com.taller.proyecto_bd.models.Cliente;
import com.taller.proyecto_bd.models.DetalleVenta;
import com.taller.proyecto_bd.models.Producto;
import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.models.Venta;
import com.taller.proyecto_bd.services.VentaService;
import com.taller.proyecto_bd.utils.Constantes;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controlador para gestionar la pantalla de transacciones (ventas).
 */
public class TransaccionesController {

    private final ProductoDAO productoDAO = ProductoDAO.getInstance();
    private final CategoriaDAO categoriaDAO = CategoriaDAO.getInstance();
    private final ClienteDAO clienteDAO = ClienteDAO.getInstance();
    private final UsuarioDAO usuarioDAO = UsuarioDAO.getInstance();
    private final VentaDAO ventaDAO = VentaDAO.getInstance();
    private final DetalleVentaDAO detalleVentaDAO = DetalleVentaDAO.getInstance();
    private final VentaService ventaService = new VentaService();

    private final ObservableList<DetalleVenta> detallesVenta = FXCollections.observableArrayList();
    private final ObservableList<Venta> ventas = FXCollections.observableArrayList();

    private final DecimalFormat formatoMoneda = new DecimalFormat("$#,##0.00");
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Formulario principal
    @FXML private ComboBox<Cliente> cbVentaCliente;
    @FXML private ComboBox<Usuario> cbVentaVendedor;
    @FXML private ComboBox<Producto> cbProductoVenta;
    @FXML private TextField txtCantidadProducto;
    @FXML private RadioButton rbContado;
    @FXML private RadioButton rbCredito;
    @FXML private ToggleGroup grupoTipoPago;
    @FXML private VBox boxCredito;
    @FXML private TextField txtCuotaInicial;
    @FXML private ComboBox<Integer> cbPlazoMeses;
    @FXML private TextField txtInteres;
    @FXML private Label lblVentaMensaje;

    // Tabla de detalles
    @FXML private TableView<DetalleVenta> tablaDetalles;
    @FXML private TableColumn<DetalleVenta, String> colDetalleProducto;
    @FXML private TableColumn<DetalleVenta, String> colDetalleCantidad;
    @FXML private TableColumn<DetalleVenta, String> colDetallePrecio;
    @FXML private TableColumn<DetalleVenta, String> colDetalleSubtotal;
    @FXML private TableColumn<DetalleVenta, String> colDetalleIVA;
    @FXML private TableColumn<DetalleVenta, String> colDetalleTotal;

    // Resumen totals
    @FXML private Label lblSubtotal;
    @FXML private Label lblIVA;
    @FXML private Label lblTotal;

    // Historial
    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, String> colVentaCodigo;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colVentaVendedor;
    @FXML private TableColumn<Venta, String> colVentaFecha;
    @FXML private TableColumn<Venta, String> colVentaTipo;
    @FXML private TableColumn<Venta, String> colVentaTotal;
    @FXML private TextArea txtFactura;
    @FXML private Label lblHistorialMensaje;

    @FXML
    private void initialize() {
        configurarCombos();
        configurarTablas();
        detallesVenta.addListener((ListChangeListener<DetalleVenta>) change -> actualizarResumen());
        cargarDatosIniciales();
    }

    private void configurarCombos() {
        cbVentaCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                if (cliente == null) {
                    return "";
                }
                return cliente.getNombreCompleto() + " (" + cliente.getCedula() + ")";
            }

            @Override
            public Cliente fromString(String string) {
                if (string == null) return null;
                return cbVentaCliente.getItems().stream()
                        .filter(c -> string.contains(c.getCedula()))
                        .findFirst().orElse(null);
            }
        });
        cbVentaCliente.setCellFactory(list -> new ClienteListCell());

        cbVentaVendedor.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) {
                    return "";
                }
                return usuario.getNombreCompleto() + " (" + usuario.getRol() + ")";
            }

            @Override
            public Usuario fromString(String string) {
                if (string == null) return null;
                return cbVentaVendedor.getItems().stream()
                        .filter(u -> string.contains(u.getUsername()))
                        .findFirst().orElse(null);
            }
        });
        cbVentaVendedor.setCellFactory(list -> new UsuarioListCell());

        cbProductoVenta.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto producto) {
                if (producto == null) {
                    return "";
                }
                return producto.getCodigo() + " - " + producto.getNombre() + " (Stock: " + producto.getStockActual() + ")";
            }

            @Override
            public Producto fromString(String string) {
                if (string == null) return null;
                return cbProductoVenta.getItems().stream()
                        .filter(p -> string.contains(p.getCodigo()))
                        .findFirst().orElse(null);
            }
        });
        cbProductoVenta.setCellFactory(list -> new ProductoListCell());

        cbPlazoMeses.setItems(FXCollections.observableArrayList(12, 18, 24));
        cbPlazoMeses.getSelectionModel().selectFirst();
        txtInteres.setText(String.format(Locale.US, "%.2f", Constantes.INTERES_DEFAULT * 100));
    }

    private void configurarTablas() {
        tablaDetalles.setItems(detallesVenta);
        colDetalleProducto.setCellValueFactory(data -> new SimpleStringProperty(obtenerNombreProducto(data.getValue())));
        colDetalleCantidad.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCantidad())));
        colDetallePrecio.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getPrecioUnitario())));
        colDetalleSubtotal.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getSubtotal())));
        colDetalleIVA.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getMontoIVA())));
        colDetalleTotal.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getTotal())));

        tablaVentas.setItems(ventas);
        colVentaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colVentaCliente.setCellValueFactory(data -> new SimpleStringProperty(obtenerNombreCliente(data.getValue().getIdCliente())));
        colVentaVendedor.setCellValueFactory(data -> new SimpleStringProperty(obtenerNombreUsuario(data.getValue().getIdUsuario())));
        colVentaFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaVenta() != null ? formatoFecha.format(data.getValue().getFechaVenta()) : ""));
        colVentaTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isEsCredito() ? "Crédito" : "Contado"));
        colVentaTotal.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getTotal())));

        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> mostrarFactura(newVal));
    }

    private void cargarDatosIniciales() {
        refrescarCombos();
        refrescarVentas();
        limpiarVenta();
        actualizarResumen();
    }

    private void refrescarCombos() {
        cbVentaCliente.setItems(FXCollections.observableArrayList(clienteDAO.obtenerActivos()));
        cbVentaVendedor.setItems(FXCollections.observableArrayList(usuarioDAO.obtenerActivos()));
        cbProductoVenta.setItems(FXCollections.observableArrayList(productoDAO.obtenerActivos()));
    }

    private void refrescarVentas() {
        ventas.setAll(ventaDAO.obtenerTodas());
        tablaVentas.refresh();
    }

    @FXML
    private void agregarProducto() {
        limpiarMensaje(lblVentaMensaje);
        Producto producto = cbProductoVenta.getValue();
        if (producto == null) {
            mostrarMensaje(lblVentaMensaje, "Seleccione un producto.", true);
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidadProducto.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarMensaje(lblVentaMensaje, "Cantidad inválida.", true);
            return;
        }
        if (cantidad <= 0) {
            mostrarMensaje(lblVentaMensaje, "La cantidad debe ser mayor a cero.", true);
            return;
        }
        if (cantidad > producto.getStockActual()) {
            mostrarMensaje(lblVentaMensaje, "Cantidad supera el stock disponible.", true);
            return;
        }

        DetalleVenta existente = detallesVenta.stream()
                .filter(d -> d.getIdProducto() == producto.getIdProducto())
                .findFirst()
                .orElse(null);

        if (existente != null) {
            int nuevaCantidad = existente.getCantidad() + cantidad;
            if (nuevaCantidad > producto.getStockActual()) {
                mostrarMensaje(lblVentaMensaje, "No hay stock suficiente para acumular la cantidad.", true);
                return;
            }
            existente.setCantidad(nuevaCantidad);
            existente.calcularTotales(Constantes.IVA_DEFAULT);
            tablaDetalles.refresh();
        } else {
            DetalleVenta detalle = new DetalleVenta(producto.getIdProducto(), cantidad, producto.getPrecioVenta(), Constantes.IVA_DEFAULT);
            detalle.setNombreProducto(producto.getNombre());
            Categoria categoria = categoriaDAO.obtenerPorId(producto.getIdCategoria());
            if (categoria != null) {
                detalle.setCategoriaProducto(categoria.getNombre());
            }
            detallesVenta.add(detalle);
        }

        txtCantidadProducto.clear();
        tablaDetalles.refresh();
        actualizarResumen();
    }

    @FXML
    private void quitarDetalle() {
        limpiarMensaje(lblVentaMensaje);
        DetalleVenta seleccionado = tablaDetalles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje(lblVentaMensaje, "Seleccione un detalle para quitar.", true);
            return;
        }
        detallesVenta.remove(seleccionado);
        tablaDetalles.refresh();
        actualizarResumen();
    }

    @FXML
    private void registrarVenta() {
        limpiarMensaje(lblVentaMensaje);
        Cliente cliente = cbVentaCliente.getValue();
        Usuario vendedor = cbVentaVendedor.getValue();

        if (cliente == null || vendedor == null) {
            mostrarMensaje(lblVentaMensaje, "Seleccione cliente y vendedor.", true);
            return;
        }
        if (detallesVenta.isEmpty()) {
            mostrarMensaje(lblVentaMensaje, "Agregue al menos un producto.", true);
            return;
        }

        boolean esCredito = rbCredito.isSelected();
        double cuotaInicial = 0;
        int plazoMeses = 0;
        double interes = Constantes.INTERES_DEFAULT;
        double totalVenta = calcularTotalVenta();

        if (esCredito) {
            try {
                cuotaInicial = Double.parseDouble(obtenerTextoNumerico(txtCuotaInicial));
                interes = Double.parseDouble(obtenerTextoNumerico(txtInteres)) / 100.0;
            } catch (NumberFormatException ex) {
                mostrarMensaje(lblVentaMensaje, "Valores inválidos para cuota inicial o interés.", true);
                return;
            }
            Integer plazo = cbPlazoMeses.getValue();
            if (plazo == null) {
                mostrarMensaje(lblVentaMensaje, "Seleccione plazo en meses.", true);
                return;
            }
            plazoMeses = plazo;

            double minimoCuota = totalVenta * 0.30;
            if (cuotaInicial < minimoCuota) {
                mostrarMensaje(lblVentaMensaje, String.format(Locale.US, "La cuota inicial mínima es %.2f", minimoCuota), true);
                return;
            }
            if (cuotaInicial > totalVenta) {
                mostrarMensaje(lblVentaMensaje, "La cuota inicial no puede superar el total.", true);
                return;
            }
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        for (DetalleVenta origen : detallesVenta) {
            DetalleVenta copia = new DetalleVenta(origen.getIdProducto(), origen.getCantidad(), origen.getPrecioUnitario(), Constantes.IVA_DEFAULT);
            copia.setNombreProducto(origen.getNombreProducto());
            copia.setCategoriaProducto(origen.getCategoriaProducto());
            detalles.add(copia);
        }

        boolean exito = ventaService.realizarVenta(cliente, vendedor, detalles, esCredito, cuotaInicial, plazoMeses, interes);

        if (exito) {
            mostrarMensaje(lblVentaMensaje, "Venta registrada correctamente.", false);
            refrescarVentas();
            refrescarCombos();
            limpiarVenta();
        } else {
            mostrarMensaje(lblVentaMensaje, "No se pudo registrar la venta. Revise los datos.", true);
        }
    }

    @FXML
    private void limpiarVenta() {
        detallesVenta.clear();
        tablaDetalles.refresh();
        cbVentaCliente.getSelectionModel().clearSelection();
        cbVentaVendedor.getSelectionModel().clearSelection();
        cbProductoVenta.getSelectionModel().clearSelection();
        txtCantidadProducto.clear();
        rbContado.setSelected(true);
        seleccionarContado();
        txtCuotaInicial.clear();
        cbPlazoMeses.getSelectionModel().selectFirst();
        txtInteres.setText(String.format(Locale.US, "%.2f", Constantes.INTERES_DEFAULT * 100));
        limpiarMensaje(lblVentaMensaje);
        actualizarResumen();
    }

    @FXML
    private void seleccionarContado() {
        boxCredito.setVisible(false);
        boxCredito.setManaged(false);
    }

    @FXML
    private void seleccionarCredito() {
        boxCredito.setVisible(true);
        boxCredito.setManaged(true);
        if (txtInteres.getText() == null || txtInteres.getText().isBlank()) {
            txtInteres.setText(String.format(Locale.US, "%.2f", Constantes.INTERES_DEFAULT * 100));
        }
    }

    @FXML
    private void generarFacturaSeleccionada() {
        Venta venta = tablaVentas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            mostrarMensaje(lblHistorialMensaje, "Seleccione una venta del historial.", true);
            return;
        }
        mostrarFactura(venta);
        mostrarMensaje(lblHistorialMensaje, "Factura generada.", false);
    }

    private void mostrarFactura(Venta venta) {
        if (venta == null) {
            txtFactura.clear();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("FACTURA DE VENTA\n");
        sb.append("==============================\n");
        sb.append("Código: ").append(venta.getCodigo()).append("\n");
        sb.append("Fecha: ").append(venta.getFechaVenta() != null ? formatoFecha.format(venta.getFechaVenta()) : "N/A").append("\n");

        Cliente cliente = clienteDAO.obtenerPorId(venta.getIdCliente());
        if (cliente != null) {
            sb.append("Cliente: ").append(cliente.getNombreCompleto()).append(" - ").append(cliente.getCedula()).append("\n");
        }
        Usuario vendedor = usuarioDAO.obtenerPorId(venta.getIdUsuario());
        if (vendedor != null) {
            sb.append("Vendedor: ").append(vendedor.getNombreCompleto()).append("\n");
        }
        sb.append("Tipo: ").append(venta.isEsCredito() ? "Crédito" : "Contado").append("\n\n");
        sb.append(String.format("%-25s %6s %10s %10s\n", "Producto", "Cant", "Precio", "Total"));
        sb.append("------------------------------------------------\n");

        List<DetalleVenta> detalles = detalleVentaDAO.obtenerPorVenta(venta.getIdVenta());
        if (detalles.isEmpty() && venta.getDetalles() != null) {
            detalles = venta.getDetalles();
        }
        double subtotal = 0;
        double iva = 0;
        for (DetalleVenta d : detalles) {
            String nombre = d.getNombreProducto();
            if (nombre == null || nombre.isBlank()) {
                Producto producto = productoDAO.obtenerPorId(d.getIdProducto());
                nombre = producto != null ? producto.getNombre() : "Producto " + d.getIdProducto();
            }
            subtotal += d.getSubtotal();
            iva += d.getMontoIVA();
            sb.append(String.format(Locale.US, "%-25s %6d %10s %10s\n",
                    nombre.length() > 25 ? nombre.substring(0, 25) : nombre,
                    d.getCantidad(),
                    formatoMoneda.format(d.getPrecioUnitario()),
                    formatoMoneda.format(d.getTotal())));
        }
        sb.append("------------------------------------------------\n");
        sb.append("Subtotal: ").append(formatoMoneda.format(subtotal)).append("\n");
        sb.append("IVA: ").append(formatoMoneda.format(iva)).append("\n");
        sb.append("TOTAL: ").append(formatoMoneda.format(venta.getTotal())).append("\n");

        if (venta.isEsCredito()) {
            sb.append("Cuota inicial: ").append(formatoMoneda.format(venta.getCuotaInicial())).append("\n");
            sb.append("Plazo: ").append(venta.getPlazoMeses()).append(" meses\n");
        }
        txtFactura.setText(sb.toString());
    }

    private double calcularTotalVenta() {
        return detallesVenta.stream().mapToDouble(DetalleVenta::getTotal).sum();
    }

    private void actualizarResumen() {
        double subtotal = detallesVenta.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        double iva = detallesVenta.stream().mapToDouble(DetalleVenta::getMontoIVA).sum();
        double total = detallesVenta.stream().mapToDouble(DetalleVenta::getTotal).sum();

        lblSubtotal.setText(formatoMoneda.format(subtotal));
        lblIVA.setText(formatoMoneda.format(iva));
        lblTotal.setText(formatoMoneda.format(total));
    }

    private String obtenerNombreProducto(DetalleVenta detalle) {
        if (detalle.getNombreProducto() != null && !detalle.getNombreProducto().isBlank()) {
            return detalle.getNombreProducto();
        }
        Producto producto = productoDAO.obtenerPorId(detalle.getIdProducto());
        return producto != null ? producto.getNombre() : "Producto";
    }

    private String obtenerNombreCliente(int idCliente) {
        Cliente cliente = clienteDAO.obtenerPorId(idCliente);
        return cliente != null ? cliente.getNombreCompleto() : "N/A";
    }

    private String obtenerNombreUsuario(int idUsuario) {
        Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
        return usuario != null ? usuario.getNombreCompleto() : "N/A";
    }

    private void mostrarMensaje(Label label, String texto, boolean esError) {
        if (label == null) {
            return;
        }
        label.setText(texto);
        label.setStyle(esError ? "-fx-text-fill: #f94144;" : "-fx-text-fill: #43aa8b;");
    }

    private void limpiarMensaje(Label label) {
        if (label != null) {
            label.setText("");
            label.setStyle("");
        }
    }

    private String obtenerTextoNumerico(TextField textField) {
        String texto = textField.getText();
        return (texto == null || texto.isBlank()) ? "0" : texto.trim();
    }

    private static class ClienteListCell extends javafx.scene.control.ListCell<Cliente> {
        @Override
        protected void updateItem(Cliente cliente, boolean empty) {
            super.updateItem(cliente, empty);
            if (empty || cliente == null) {
                setText(null);
            } else {
                setText(cliente.getNombreCompleto() + " (" + cliente.getCedula() + ")");
            }
        }
    }

    private static class UsuarioListCell extends javafx.scene.control.ListCell<Usuario> {
        @Override
        protected void updateItem(Usuario usuario, boolean empty) {
            super.updateItem(usuario, empty);
            if (empty || usuario == null) {
                setText(null);
            } else {
                setText(usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
            }
        }
    }

    private static class ProductoListCell extends javafx.scene.control.ListCell<Producto> {
        @Override
        protected void updateItem(Producto producto, boolean empty) {
            super.updateItem(producto, empty);
            if (empty || producto == null) {
                setText(null);
            } else {
                setText(producto.getCodigo() + " - " + producto.getNombre());
            }
        }
    }
}
