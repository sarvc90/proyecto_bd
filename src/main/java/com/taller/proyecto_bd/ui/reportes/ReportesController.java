package com.taller.proyecto_bd.ui.reportes;

import com.taller.proyecto_bd.dao.CategoriaDAO;
import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.dao.CreditoDAO;
import com.taller.proyecto_bd.dao.CuotaDAO;
import com.taller.proyecto_bd.dao.DetalleVentaDAO;
import com.taller.proyecto_bd.dao.InventarioDAO;
import com.taller.proyecto_bd.dao.ProductoDAO;
import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.dao.VentaDAO;
import com.taller.proyecto_bd.models.Categoria;
import com.taller.proyecto_bd.models.Cliente;
import com.taller.proyecto_bd.models.Credito;
import com.taller.proyecto_bd.models.Cuota;
import com.taller.proyecto_bd.models.DetalleVenta;
import com.taller.proyecto_bd.models.Inventario;
import com.taller.proyecto_bd.models.Producto;
import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.models.Venta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador responsable de los reportes y consultas avanzadas.
 */
public class ReportesController {

    private final VentaDAO ventaDAO = VentaDAO.getInstance();
    private final DetalleVentaDAO detalleVentaDAO = DetalleVentaDAO.getInstance();
    private final ProductoDAO productoDAO = ProductoDAO.getInstance();
    private final ClienteDAO clienteDAO = ClienteDAO.getInstance();
    private final UsuarioDAO usuarioDAO = UsuarioDAO.getInstance();
    private final CategoriaDAO categoriaDAO = CategoriaDAO.getInstance();
    private final InventarioDAO inventarioDAO = InventarioDAO.getInstance();
    private final CreditoDAO creditoDAO = CreditoDAO.getInstance();
    private final CuotaDAO cuotaDAO = CuotaDAO.getInstance();

    private final DecimalFormat formatoMoneda = new DecimalFormat("$#,##0.00");

    // Reporte: factura
    @FXML private ComboBox<Venta> cbReporteFacturaVenta;
    @FXML private TextArea txtReporteFactura;

    // Reporte: ventas mes
    @FXML private ComboBox<Integer> cbAnioVentasMes;
    @FXML private ComboBox<String> cbMesVentasMes;
    @FXML private Label lblVentasMesResultado;

    // Reporte: IVA trimestre
    @FXML private ComboBox<Integer> cbAnioIVA;
    @FXML private ComboBox<String> cbTrimestre;
    @FXML private Label lblIvaTrimestreResultado;

    // Reporte: ventas por tipo
    @FXML private DatePicker dpDesdeTipo;
    @FXML private DatePicker dpHastaTipo;
    @FXML private Label lblVentasContado;
    @FXML private Label lblVentasCredito;

    // Reporte: inventario por categoría
    @FXML private TableView<InventarioCategoriaRow> tablaInventarioCategoria;
    @FXML private TableColumn<InventarioCategoriaRow, String> colInvCategoria;
    @FXML private TableColumn<InventarioCategoriaRow, String> colInvProductos;
    @FXML private TableColumn<InventarioCategoriaRow, String> colInvStock;
    @FXML private TableColumn<InventarioCategoriaRow, String> colInvCostoCompra;
    @FXML private TableColumn<InventarioCategoriaRow, String> colInvValorVenta;

    // Reporte: clientes morosos
    @FXML private TableView<ClienteMorosoRow> tablaClientesMorosos;
    @FXML private TableColumn<ClienteMorosoRow, String> colMorosoCliente;
    @FXML private TableColumn<ClienteMorosoRow, String> colMorosoCedula;
    @FXML private TableColumn<ClienteMorosoRow, String> colMorosoSaldo;
    @FXML private TableColumn<ClienteMorosoRow, String> colMorosoCuotas;

    // Consultas
    @FXML private TableView<VentasClienteRow> tablaVentasCliente;
    @FXML private TableColumn<VentasClienteRow, String> colVCCliente;
    @FXML private TableColumn<VentasClienteRow, String> colVCVentas;
    @FXML private TableColumn<VentasClienteRow, String> colVCTotal;

    @FXML private TableView<ProductosVendidosRow> tablaProductosVendidos;
    @FXML private TableColumn<ProductosVendidosRow, String> colPVProducto;
    @FXML private TableColumn<ProductosVendidosRow, String> colPVCantidad;
    @FXML private TableColumn<ProductosVendidosRow, String> colPVTotal;

    @FXML private TableView<VentasVendedorRow> tablaVentasVendedor;
    @FXML private TableColumn<VentasVendedorRow, String> colVVendedor;
    @FXML private TableColumn<VentasVendedorRow, String> colVVentas;
    @FXML private TableColumn<VentasVendedorRow, String> colVVTotal;

    @FXML private TableView<CreditosClienteRow> tablaCreditosCliente;
    @FXML private TableColumn<CreditosClienteRow, String> colCCCliente;
    @FXML private TableColumn<CreditosClienteRow, String> colCCActivos;
    @FXML private TableColumn<CreditosClienteRow, String> colCCSaldo;

    @FXML private TableView<ValorInventarioRow> tablaValorInventario;
    @FXML private TableColumn<ValorInventarioRow, String> colVICategoria;
    @FXML private TableColumn<ValorInventarioRow, String> colVICompra;
    @FXML private TableColumn<ValorInventarioRow, String> colVIVenta;

    private final ObservableList<InventarioCategoriaRow> inventarioCategoriaData = FXCollections.observableArrayList();
    private final ObservableList<ClienteMorosoRow> clientesMorososData = FXCollections.observableArrayList();
    private final ObservableList<VentasClienteRow> ventasClienteData = FXCollections.observableArrayList();
    private final ObservableList<ProductosVendidosRow> productosVendidosData = FXCollections.observableArrayList();
    private final ObservableList<VentasVendedorRow> ventasVendedorData = FXCollections.observableArrayList();
    private final ObservableList<CreditosClienteRow> creditosClienteData = FXCollections.observableArrayList();
    private final ObservableList<ValorInventarioRow> valorInventarioData = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        configurarCombos();
        configurarTablas();
        cargarDatosIniciales();
    }

    private void configurarCombos() {
        cbReporteFacturaVenta.setConverter(new StringConverter<>() {
            @Override
            public String toString(Venta venta) {
                if (venta == null) {
                    return "";
                }
                return venta.getCodigo() + " - " + obtenerNombreCliente(venta.getIdCliente());
            }

            @Override
            public Venta fromString(String string) {
                if (string == null) return null;
                return cbReporteFacturaVenta.getItems().stream()
                        .filter(v -> string.startsWith(v.getCodigo()))
                        .findFirst()
                        .orElse(null);
            }
        });

        List<Integer> anios = obtenerAniosVentas();
        cbAnioVentasMes.setItems(FXCollections.observableArrayList(anios));
        cbAnioIVA.setItems(FXCollections.observableArrayList(anios));
        if (!anios.isEmpty()) {
            cbAnioVentasMes.getSelectionModel().selectFirst();
            cbAnioIVA.getSelectionModel().selectFirst();
        }

        List<String> meses = List.of("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");
        cbMesVentasMes.setItems(FXCollections.observableArrayList(meses));
        if (!meses.isEmpty()) {
            cbMesVentasMes.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
        }

        cbTrimestre.setItems(FXCollections.observableArrayList("1", "2", "3", "4"));
        cbTrimestre.getSelectionModel().select((LocalDate.now().getMonthValue() - 1) / 3);

        dpDesdeTipo.setValue(LocalDate.now().withDayOfMonth(1));
        dpHastaTipo.setValue(LocalDate.now());
    }

    private void configurarTablas() {
        tablaInventarioCategoria.setItems(inventarioCategoriaData);
        colInvCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colInvProductos.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getProductos())));
        colInvStock.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStockTotal())));
        colInvCostoCompra.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getValorCompra())));
        colInvValorVenta.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getValorVenta())));

        tablaClientesMorosos.setItems(clientesMorososData);
        colMorosoCliente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCliente()));
        colMorosoCedula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumento()));
        colMorosoSaldo.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getSaldoPendiente())));
        colMorosoCuotas.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCuotasVencidas())));

        tablaVentasCliente.setItems(ventasClienteData);
        colVCCliente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCliente()));
        colVCVentas.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCantidadVentas())));
        colVCTotal.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getTotalVendido())));

        tablaProductosVendidos.setItems(productosVendidosData);
        colPVProducto.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProducto()));
        colPVCantidad.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCantidad())));
        colPVTotal.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getTotalGenerado())));

        tablaVentasVendedor.setItems(ventasVendedorData);
        colVVendedor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVendedor()));
        colVVentas.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getVentas())));
        colVVTotal.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getTotal())));

        tablaCreditosCliente.setItems(creditosClienteData);
        colCCCliente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCliente()));
        colCCActivos.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCreditosActivos())));
        colCCSaldo.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getSaldoPendiente())));

        tablaValorInventario.setItems(valorInventarioData);
        colVICategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colVICompra.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getValorCosto())));
        colVIVenta.setCellValueFactory(data -> new SimpleStringProperty(formatoMoneda.format(data.getValue().getValorVenta())));
    }

    private void cargarDatosIniciales() {
        cbReporteFacturaVenta.setItems(FXCollections.observableArrayList(ventaDAO.obtenerTodas()));
        cargarInventarioPorCategoria();
        cargarClientesMorosos();
        cargarConsultaVentasPorCliente();
        cargarConsultaProductosMasVendidos();
        cargarConsultaVentasPorVendedor();
        cargarConsultaCreditosPorCliente();
        cargarConsultaValorInventario();
    }
    @FXML
    private void generarReporteFactura() {
        Venta venta = cbReporteFacturaVenta.getValue();
        if (venta == null) {
            txtReporteFactura.setText("Seleccione una venta para generar la factura.");
            return;
        }
        txtReporteFactura.setText(formatearFactura(venta));
    }

    @FXML
    private void calcularVentasMensuales() {
        Integer anio = cbAnioVentasMes.getValue();
        String mesNombre = cbMesVentasMes.getValue();
        if (anio == null || mesNombre == null) {
            mostrarMensaje(lblVentasMesResultado, "Seleccione año y mes.", true);
            return;
        }
        Month mes = mesDesdeNombre(mesNombre);
        double total = ventaDAO.obtenerTodas().stream()
                .filter(v -> v.getFechaVenta() != null)
                .filter(v -> {
                    LocalDate fecha = convertirFecha(v.getFechaVenta());
                    return fecha.getYear() == anio && fecha.getMonth() == mes;
                })
                .mapToDouble(Venta::getTotal)
                .sum();
        mostrarMensaje(lblVentasMesResultado,
                "Total de ventas " + mesNombre + " " + anio + ": " + formatoMoneda.format(total),
                false);
    }

    @FXML
    private void calcularIvaTrimestral() {
        Integer anio = cbAnioIVA.getValue();
        String trimestreTxt = cbTrimestre.getValue();
        if (anio == null || trimestreTxt == null) {
            mostrarMensaje(lblIvaTrimestreResultado, "Seleccione año y trimestre.", true);
            return;
        }
        int trimestre = Integer.parseInt(trimestreTxt);
        int mesInicio = (trimestre - 1) * 3 + 1;
        int mesFin = mesInicio + 2;

        double iva = ventaDAO.obtenerTodas().stream()
                .filter(v -> v.getFechaVenta() != null)
                .filter(v -> {
                    LocalDate fecha = convertirFecha(v.getFechaVenta());
                    int mes = fecha.getMonthValue();
                    return fecha.getYear() == anio && mes >= mesInicio && mes <= mesFin;
                })
                .mapToDouble(Venta::getIvaTotal)
                .sum();

        mostrarMensaje(lblIvaTrimestreResultado,
                "IVA total trimestre " + trimestre + " de " + anio + ": " + formatoMoneda.format(iva),
                false);
    }

    @FXML
    private void calcularVentasPorTipo() {
        LocalDate desde = dpDesdeTipo.getValue();
        LocalDate hasta = dpHastaTipo.getValue();
        if (desde == null || hasta == null) {
            lblVentasContado.setText("Ventas de contado: -");
            lblVentasCredito.setText("Ventas a crédito: -");
            return;
        }
        if (hasta.isBefore(desde)) {
            lblVentasContado.setText("Fechas inválidas");
            lblVentasCredito.setText("");
            return;
        }

        List<Venta> ventasPeriodo = ventaDAO.obtenerTodas().stream()
                .filter(v -> v.getFechaVenta() != null)
                .filter(v -> {
                    LocalDate fecha = convertirFecha(v.getFechaVenta());
                    return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                })
                .toList();

        long contado = ventasPeriodo.stream().filter(v -> !v.isEsCredito()).count();
        long credito = ventasPeriodo.stream().filter(Venta::isEsCredito).count();
        double total = ventasPeriodo.stream().mapToDouble(Venta::getTotal).sum();

        lblVentasContado.setText("Ventas de contado: " + contado);
        lblVentasCredito.setText("Ventas a crédito: " + credito + " | Total periodo: " + formatoMoneda.format(total));
    }
    private void cargarInventarioPorCategoria() {
        Map<String, InventarioCategoriaRow> mapa = new LinkedHashMap<>();
        for (Inventario inventario : inventarioDAO.obtenerTodos()) {
            Producto producto = productoDAO.obtenerPorId(inventario.getIdProducto());
            if (producto == null) {
                continue;
            }
            Categoria categoria = categoriaDAO.obtenerPorId(producto.getIdCategoria());
            String nombreCategoria = categoria != null ? categoria.getNombre() : "Sin categoría";

            InventarioCategoriaRow fila = mapa.computeIfAbsent(nombreCategoria,
                    k -> new InventarioCategoriaRow(nombreCategoria));
            fila.incrementarProducto();
            fila.incrementarStock(inventario.getCantidadActual());
            fila.incrementarValorCompra(inventario.getCantidadActual() * producto.getPrecioCompra());
            fila.incrementarValorVenta(inventario.getCantidadActual() * producto.getPrecioVenta());
        }
        inventarioCategoriaData.setAll(mapa.values());
    }

    private void cargarClientesMorosos() {
        List<ClienteMorosoRow> filas = new ArrayList<>();
        for (Credito credito : creditoDAO.obtenerMorosos()) {
            Cliente cliente = clienteDAO.obtenerPorId(credito.getIdCliente());
            if (cliente == null) {
                continue;
            }
            List<Cuota> cuotas = cuotaDAO.obtenerPorCredito(credito.getIdCredito());
            long vencidas = cuotas.stream().filter(Cuota::estaVencida).count();
            filas.add(new ClienteMorosoRow(cliente.getNombreCompleto(), cliente.getCedula(),
                    credito.getSaldoPendiente(), (int) vencidas));
        }
        clientesMorososData.setAll(filas);
    }

    private void cargarConsultaVentasPorCliente() {
        Map<Integer, VentasAcumuladas> mapa = new LinkedHashMap<>();
        for (Venta venta : ventaDAO.obtenerTodas()) {
            VentasAcumuladas acumulado = mapa.computeIfAbsent(venta.getIdCliente(),
                    k -> new VentasAcumuladas());
            acumulado.incrementarVenta(venta.getTotal());
        }

        List<VentasClienteRow> filas = mapa.entrySet().stream()
                .map(entry -> {
                    Cliente cliente = clienteDAO.obtenerPorId(entry.getKey());
                    String nombre = cliente != null ? cliente.getNombreCompleto() : "Cliente " + entry.getKey();
                    VentasAcumuladas datos = entry.getValue();
                    return new VentasClienteRow(nombre, datos.getCantidadVentas(), datos.getTotal());
                })
                .sorted(Comparator.comparingDouble(VentasClienteRow::getTotalVendido).reversed())
                .toList();
        ventasClienteData.setAll(filas);
    }

    private void cargarConsultaProductosMasVendidos() {
        Map<Integer, ProductoAcumulado> mapa = new LinkedHashMap<>();
        for (DetalleVenta detalle : detalleVentaDAO.obtenerTodos()) {
            ProductoAcumulado acumulado = mapa.computeIfAbsent(detalle.getIdProducto(),
                    k -> new ProductoAcumulado());
            acumulado.incrementar(detalle.getCantidad(), detalle.getTotal());
        }

        List<ProductosVendidosRow> filas = mapa.entrySet().stream()
                .map(entry -> {
                    Producto producto = productoDAO.obtenerPorId(entry.getKey());
                    String nombre = producto != null ? producto.getNombre() : "Producto " + entry.getKey();
                    ProductoAcumulado datos = entry.getValue();
                    return new ProductosVendidosRow(nombre, datos.getCantidad(), datos.getTotal());
                })
                .sorted(Comparator.comparingInt(ProductosVendidosRow::getCantidad).reversed())
                .toList();
        productosVendidosData.setAll(filas);
    }

    private void cargarConsultaVentasPorVendedor() {
        Map<Integer, VentasAcumuladas> mapa = new LinkedHashMap<>();
        for (Venta venta : ventaDAO.obtenerTodas()) {
            VentasAcumuladas acumulado = mapa.computeIfAbsent(venta.getIdUsuario(),
                    k -> new VentasAcumuladas());
            acumulado.incrementarVenta(venta.getTotal());
        }

        List<VentasVendedorRow> filas = mapa.entrySet().stream()
                .map(entry -> {
                    Usuario usuario = usuarioDAO.obtenerPorId(entry.getKey());
                    String nombre = usuario != null ? usuario.getNombreCompleto() : "Usuario " + entry.getKey();
                    VentasAcumuladas datos = entry.getValue();
                    return new VentasVendedorRow(nombre, datos.getCantidadVentas(), datos.getTotal());
                })
                .sorted(Comparator.comparingDouble(VentasVendedorRow::getTotal).reversed())
                .toList();
        ventasVendedorData.setAll(filas);
    }

    private void cargarConsultaCreditosPorCliente() {
        Map<Integer, VentasAcumuladas> mapa = new LinkedHashMap<>();
        for (Credito credito : creditoDAO.obtenerActivos()) {
            VentasAcumuladas acumulado = mapa.computeIfAbsent(credito.getIdCliente(),
                    k -> new VentasAcumuladas());
            acumulado.incrementarCredito(credito.getSaldoPendiente());
        }

        List<CreditosClienteRow> filas = mapa.entrySet().stream()
                .map(entry -> {
                    Cliente cliente = clienteDAO.obtenerPorId(entry.getKey());
                    String nombre = cliente != null ? cliente.getNombreCompleto() : "Cliente " + entry.getKey();
                    VentasAcumuladas datos = entry.getValue();
                    return new CreditosClienteRow(nombre, datos.getCreditosActivos(), datos.getTotal());
                })
                .sorted(Comparator.comparingDouble(CreditosClienteRow::getSaldoPendiente).reversed())
                .toList();
        creditosClienteData.setAll(filas);
    }

    private void cargarConsultaValorInventario() {
        Map<String, ValorInventarioRow> mapa = new LinkedHashMap<>();
        for (Inventario inventario : inventarioDAO.obtenerTodos()) {
            Producto producto = productoDAO.obtenerPorId(inventario.getIdProducto());
            if (producto == null) {
                continue;
            }
            Categoria categoria = categoriaDAO.obtenerPorId(producto.getIdCategoria());
            String nombre = categoria != null ? categoria.getNombre() : "Sin categoría";
            ValorInventarioRow fila = mapa.computeIfAbsent(nombre, ValorInventarioRow::new);
            fila.incrementarCosto(inventario.getCantidadActual() * producto.getPrecioCompra());
            fila.incrementarVenta(inventario.getCantidadActual() * producto.getPrecioVenta());
        }
        valorInventarioData.setAll(mapa.values());
    }
    private String formatearFactura(Venta venta) {
        StringBuilder sb = new StringBuilder();
        sb.append("REP-FACTURA\n");
        sb.append("==============================\n");
        sb.append("Venta: ").append(venta.getCodigo()).append("\n");
        sb.append("Fecha: ").append(venta.getFechaVenta() != null ? convertirFecha(venta.getFechaVenta()) : "N/A").append("\n");
        sb.append("Cliente: ").append(obtenerNombreCliente(venta.getIdCliente())).append("\n");
        sb.append("Vendedor: ").append(obtenerNombreUsuario(venta.getIdUsuario())).append("\n");
        sb.append("Tipo: ").append(venta.isEsCredito() ? "Crédito" : "Contado").append("\n\n");
        sb.append(String.format("%-24s %5s %10s %10s\n", "Producto", "Cant", "Precio", "Total"));
        sb.append("----------------------------------------------\n");

        List<DetalleVenta> detalles = detalleVentaDAO.obtenerPorVenta(venta.getIdVenta());
        if (detalles.isEmpty() && venta.getDetalles() != null) {
            detalles = venta.getDetalles();
        }
        double subtotal = 0;
        for (DetalleVenta detalle : detalles) {
            String nombre = detalle.getNombreProducto();
            if (nombre == null || nombre.isBlank()) {
                Producto producto = productoDAO.obtenerPorId(detalle.getIdProducto());
                nombre = producto != null ? producto.getNombre() : "Producto";
            }
            subtotal += detalle.getSubtotal();
            sb.append(String.format(Locale.US, "%-24s %5d %10s %10s\n",
                    nombre.length() > 24 ? nombre.substring(0, 24) : nombre,
                    detalle.getCantidad(),
                    formatoMoneda.format(detalle.getPrecioUnitario()),
                    formatoMoneda.format(detalle.getTotal())));
        }
        sb.append("----------------------------------------------\n");
        sb.append("Subtotal: ").append(formatoMoneda.format(subtotal)).append("\n");
        sb.append("IVA: ").append(formatoMoneda.format(venta.getIvaTotal())).append("\n");
        sb.append("TOTAL: ").append(formatoMoneda.format(venta.getTotal())).append("\n");
        if (venta.isEsCredito()) {
            sb.append("Cuota inicial: ").append(formatoMoneda.format(venta.getCuotaInicial())).append(" | ");
            sb.append("Plazo: ").append(venta.getPlazoMeses()).append(" meses\n");
        }
        return sb.toString();
    }

    private List<Integer> obtenerAniosVentas() {
        Set<Integer> anios = ventaDAO.obtenerTodas().stream()
                .filter(v -> v.getFechaVenta() != null)
                .map(v -> convertirFecha(v.getFechaVenta()).getYear())
                .collect(Collectors.toSet());
        if (anios.isEmpty()) {
            anios.add(LocalDate.now().getYear());
        }
        return anios.stream().sorted(Comparator.reverseOrder()).toList();
    }

    private LocalDate convertirFecha(java.util.Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Month mesDesdeNombre(String nombre) {
        return switch (nombre) {
            case "Enero" -> Month.JANUARY;
            case "Febrero" -> Month.FEBRUARY;
            case "Marzo" -> Month.MARCH;
            case "Abril" -> Month.APRIL;
            case "Mayo" -> Month.MAY;
            case "Junio" -> Month.JUNE;
            case "Julio" -> Month.JULY;
            case "Agosto" -> Month.AUGUST;
            case "Septiembre" -> Month.SEPTEMBER;
            case "Octubre" -> Month.OCTOBER;
            case "Noviembre" -> Month.NOVEMBER;
            default -> Month.DECEMBER;
        };
    }

    private String obtenerNombreCliente(int idCliente) {
        Cliente cliente = clienteDAO.obtenerPorId(idCliente);
        return cliente != null ? cliente.getNombreCompleto() : "Cliente " + idCliente;
    }

    private String obtenerNombreUsuario(int idUsuario) {
        Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
        return usuario != null ? usuario.getNombreCompleto() : "Usuario " + idUsuario;
    }

    private void mostrarMensaje(Label label, String texto, boolean error) {
        if (label == null) {
            return;
        }
        label.setText(texto);
        label.setStyle(error ? "-fx-text-fill: #f94144;" : "-fx-text-fill: #1b4332;");
    }
    // ----- Clases auxiliares -----

    private static class InventarioCategoriaRow {
        private final String categoria;
        private int productos;
        private int stockTotal;
        private double valorCompra;
        private double valorVenta;

        InventarioCategoriaRow(String categoria) {
            this.categoria = categoria;
        }

        void incrementarProducto() {
            productos++;
        }

        void incrementarStock(int cantidad) {
            stockTotal += cantidad;
        }

        void incrementarValorCompra(double valor) {
            valorCompra += valor;
        }

        void incrementarValorVenta(double valor) {
            valorVenta += valor;
        }

        String getCategoria() { return categoria; }
        int getProductos() { return productos; }
        int getStockTotal() { return stockTotal; }
        double getValorCompra() { return valorCompra; }
        double getValorVenta() { return valorVenta; }
    }

    private static class ClienteMorosoRow {
        private final String cliente;
        private final String documento;
        private final double saldoPendiente;
        private final int cuotasVencidas;

        ClienteMorosoRow(String cliente, String documento, double saldoPendiente, int cuotasVencidas) {
            this.cliente = cliente;
            this.documento = documento;
            this.saldoPendiente = saldoPendiente;
            this.cuotasVencidas = cuotasVencidas;
        }

        String getCliente() { return cliente; }
        String getDocumento() { return documento; }
        double getSaldoPendiente() { return saldoPendiente; }
        int getCuotasVencidas() { return cuotasVencidas; }
    }

    private static class VentasClienteRow {
        private final String cliente;
        private final int cantidadVentas;
        private final double totalVendido;

        VentasClienteRow(String cliente, int cantidadVentas, double totalVendido) {
            this.cliente = cliente;
            this.cantidadVentas = cantidadVentas;
            this.totalVendido = totalVendido;
        }

        String getCliente() { return cliente; }
        int getCantidadVentas() { return cantidadVentas; }
        double getTotalVendido() { return totalVendido; }
    }

    private static class ProductosVendidosRow {
        private final String producto;
        private final int cantidad;
        private final double totalGenerado;

        ProductosVendidosRow(String producto, int cantidad, double totalGenerado) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.totalGenerado = totalGenerado;
        }

        String getProducto() { return producto; }
        int getCantidad() { return cantidad; }
        double getTotalGenerado() { return totalGenerado; }
    }

    private static class VentasVendedorRow {
        private final String vendedor;
        private final int ventas;
        private final double total;

        VentasVendedorRow(String vendedor, int ventas, double total) {
            this.vendedor = vendedor;
            this.ventas = ventas;
            this.total = total;
        }

        String getVendedor() { return vendedor; }
        int getVentas() { return ventas; }
        double getTotal() { return total; }
    }

    private static class CreditosClienteRow {
        private final String cliente;
        private final int creditosActivos;
        private final double saldoPendiente;

        CreditosClienteRow(String cliente, int creditosActivos, double saldoPendiente) {
            this.cliente = cliente;
            this.creditosActivos = creditosActivos;
            this.saldoPendiente = saldoPendiente;
        }

        String getCliente() { return cliente; }
        int getCreditosActivos() { return creditosActivos; }
        double getSaldoPendiente() { return saldoPendiente; }
    }

    private static class ValorInventarioRow {
        private final String categoria;
        private double valorCosto;
        private double valorVenta;

        ValorInventarioRow(String categoria) {
            this.categoria = categoria;
        }

        void incrementarCosto(double valor) { valorCosto += valor; }
        void incrementarVenta(double valor) { valorVenta += valor; }

        String getCategoria() { return categoria; }
        double getValorCosto() { return valorCosto; }
        double getValorVenta() { return valorVenta; }
    }

    private static class VentasAcumuladas {
        private int cantidadVentas;
        private int creditosActivos;
        private double total;

        void incrementarVenta(double monto) {
            cantidadVentas++;
            total += monto;
        }

        void incrementarCredito(double saldo) {
            creditosActivos++;
            total += saldo;
        }

        int getCantidadVentas() { return cantidadVentas; }
        int getCreditosActivos() { return creditosActivos; }
        double getTotal() { return total; }
    }

    private static class ProductoAcumulado {
        private int cantidad;
        private double total;

        void incrementar(int cantidad, double total) {
            this.cantidad += cantidad;
            this.total += total;
        }

        int getCantidad() { return cantidad; }
        double getTotal() { return total; }
    }
}
