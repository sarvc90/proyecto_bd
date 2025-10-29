package com.taller.proyecto_bd.ui;


import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

/**
 * Controlador para Consultas y Reportes
 * 5 Consultas Multitabla + 5 Reportes con Cálculos
 */
public class ConsultasReportesController {

    @FXML private Label lblTituloResultado;
    @FXML private Button btnExportar;
    @FXML private TableView<Map<String, Object>> tablaResultados;
    @FXML private VBox panelResumen;
    @FXML private TextArea txtResumen;
    
    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private CategoriaDAO categoriaDAO;
    private VentaDAO ventaDAO;
    private DetalleVentaDAO detalleVentaDAO;
    private CreditoDAO creditoDAO;
    
    private NumberFormat formatoMoneda;
    
    @FXML
    public void initialize() {
        productoDAO = ProductoDAO.getInstance();
        clienteDAO = ClienteDAO.getInstance();
        categoriaDAO = CategoriaDAO.getInstance();
        ventaDAO = VentaDAO.getInstance();
        detalleVentaDAO = DetalleVentaDAO.getInstance();
        creditoDAO = CreditoDAO.getInstance();
        
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    }
    
    // ==================== CONSULTAS MULTITABLA ====================
    
    /**
     * CONSULTA 1: Productos por Categoría
     * Multitabla: Producto + Categoria
     */
    @FXML
    private void consultaProductosCategoria() {
        lblTituloResultado.setText("📋 Consulta: Productos por Categoría");
        limpiarTabla();
        
        List<Producto> productos = productoDAO.obtenerTodos();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        for (Producto p : productos) {
            Categoria cat = categoriaDAO.obtenerPorId(p.getIdCategoria());
            Map<String, Object> fila = new HashMap<>();
            fila.put("Código", p.getCodigo());
            fila.put("Producto", p.getNombre());
            fila.put("Marca", p.getMarca());
            fila.put("Categoría", cat != null ? cat.getNombre() : "Sin categoría");
            fila.put("Precio Venta", formatoMoneda.format(p.getPrecioVenta()));
            fila.put("Stock", String.valueOf(p.getStockActual()));
            fila.put("Estado", p.isActivo() ? "✓ Activo" : "✗ Inactivo");
            datos.add(fila);
        }
        
        mostrarResultados(datos);
        mostrarResumen("═══ RESUMEN ═══\n" +
                      "Total de productos: " + productos.size() + "\n" +
                      "Productos activos: " + productos.stream().filter(Producto::isActivo).count() + "\n" +
                      "Productos inactivos: " + productos.stream().filter(p -> !p.isActivo()).count());
        btnExportar.setDisable(false);
    }
    
    /**
     * CONSULTA 2: Clientes con Crédito Activo
     * Multitabla: Cliente (con cálculos de crédito)
     */
    @FXML
    private void consultaClientesCredito() {
        lblTituloResultado.setText("📋 Consulta: Clientes con Crédito Activo");
        limpiarTabla();
        
        List<Cliente> clientes = clienteDAO.obtenerTodos();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        for (Cliente c : clientes) {
            if (c.getSaldoPendiente() > 0) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("Cédula", c.getCedula());
                fila.put("Cliente", c.getNombreCompleto());
                fila.put("Teléfono", c.getTelefono());
                fila.put("Límite Crédito", formatoMoneda.format(c.getLimiteCredito()));
                fila.put("Saldo Pendiente", formatoMoneda.format(c.getSaldoPendiente()));
                fila.put("Crédito Disponible", formatoMoneda.format(c.getCreditoDisponible()));
                fila.put("% Usado", String.format("%.1f%%", 
                    (c.getSaldoPendiente() / c.getLimiteCredito() * 100)));
                datos.add(fila);
            }
        }
        
        mostrarResultados(datos);
        double totalCartera = clientes.stream().mapToDouble(Cliente::getSaldoPendiente).sum();
        mostrarResumen("═══ RESUMEN ═══\n" +
                      "Clientes con crédito activo: " + datos.size() + "\n" +
                      "Total cartera: " + formatoMoneda.format(totalCartera));
        btnExportar.setDisable(false);
    }
    
    /**
     * CONSULTA 3: Ventas con Detalle
     * Multitabla: Venta + Cliente + Usuario + DetalleVenta
     */
    @FXML
    private void consultaVentasDetalle() {
        lblTituloResultado.setText("📋 Consulta: Ventas con Detalle Completo");
        limpiarTabla();
        
        List<Venta> ventas = ventaDAO.obtenerTodas();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        for (Venta v : ventas) {
            Cliente cliente = clienteDAO.obtenerPorId(v.getIdCliente());
            List<DetalleVenta> detalles = detalleVentaDAO.obtenerPorVenta(v.getIdVenta());
            
            Map<String, Object> fila = new HashMap<>();
            fila.put("Código", v.getCodigo());
            fila.put("Cliente", cliente != null ? cliente.getNombreCompleto() : "N/A");
            fila.put("Fecha", v.getFechaVenta().toString());
            fila.put("Tipo", v.isEsCredito() ? "Crédito" : "Contado");
            fila.put("# Productos", String.valueOf(detalles.size()));
            fila.put("Subtotal", formatoMoneda.format(v.getSubtotal()));
            fila.put("IVA", formatoMoneda.format(v.getIvaTotal()));
            fila.put("Total", formatoMoneda.format(v.getTotal()));
            fila.put("Estado", v.getEstado());
            datos.add(fila);
        }
        
        mostrarResultados(datos);
        double totalVentas = ventas.stream().mapToDouble(Venta::getTotal).sum();
        long ventasCredito = ventas.stream().filter(Venta::isEsCredito).count();
        mostrarResumen("═══ RESUMEN ═══\n" +
                      "Total ventas: " + ventas.size() + "\n" +
                      "Ventas a crédito: " + ventasCredito + "\n" +
                      "Ventas de contado: " + (ventas.size() - ventasCredito) + "\n" +
                      "Total facturado: " + formatoMoneda.format(totalVentas));
        btnExportar.setDisable(false);
    }
    
    /**
     * CONSULTA 4: Productos con Bajo Stock
     * Multitabla: Producto + Categoria
     */
    @FXML
    private void consultaProductosBajoStock() {
        lblTituloResultado.setText("⚠️ Consulta: Productos con Bajo Stock (Alerta)");
        limpiarTabla();
        
        List<Producto> productos = productoDAO.obtenerStockBajo();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        for (Producto p : productos) {
            Categoria cat = categoriaDAO.obtenerPorId(p.getIdCategoria());
            Map<String, Object> fila = new HashMap<>();
            fila.put("Código", p.getCodigo());
            fila.put("Producto", p.getNombre());
            fila.put("Categoría", cat != null ? cat.getNombre() : "N/A");
            fila.put("Stock Actual", String.valueOf(p.getStockActual()));
            fila.put("Stock Mínimo", String.valueOf(p.getStockMinimo()));
            fila.put("Diferencia", String.valueOf(p.getStockMinimo() - p.getStockActual()));
            fila.put("Precio Compra", formatoMoneda.format(p.getPrecioCompra()));
            fila.put("Valor Reposición", formatoMoneda.format(
                (p.getStockMinimo() - p.getStockActual()) * p.getPrecioCompra()));
            datos.add(fila);
        }
        
        mostrarResultados(datos);
        double valorReposicion = productos.stream()
            .mapToDouble(p -> (p.getStockMinimo() - p.getStockActual()) * p.getPrecioCompra())
            .sum();
        mostrarResumen("⚠️ ALERTA DE INVENTARIO ⚠️\n" +
                      productos.size() + " productos con bajo stock\n\n" +
                      "Valor estimado de reposición:\n" +
                      formatoMoneda.format(valorReposicion));
        btnExportar.setDisable(false);
    }
    
    /**
     * CONSULTA 5: Créditos Activos
     * Multitabla: Credito + Cliente + Venta
     */
    @FXML
    private void consultaCreditosActivos() {
        lblTituloResultado.setText("📋 Consulta: Créditos Activos");
        limpiarTabla();
        
        List<Credito> creditos = creditoDAO.obtenerActivos();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        for (Credito c : creditos) {
            Cliente cliente = clienteDAO.obtenerPorId(c.getIdCliente());
            Venta venta = ventaDAO.obtenerPorId(c.getIdVenta());
            
            Map<String, Object> fila = new HashMap<>();
            fila.put("ID Crédito", String.valueOf(c.getIdCredito()));
            fila.put("Cliente", cliente != null ? cliente.getNombreCompleto() : "N/A");
            fila.put("Código Venta", venta != null ? venta.getCodigo() : "N/A");
            fila.put("Monto Total", formatoMoneda.format(c.getMontoTotal()));
            fila.put("Cuota Inicial", formatoMoneda.format(c.getCuotaInicial()));
            fila.put("Plazo", c.getPlazoMeses() + " meses");
            fila.put("Saldo Pendiente", formatoMoneda.format(c.getSaldoPendiente()));
            fila.put("% Pagado", String.format("%.1f%%", 
                ((c.getMontoTotal() - c.getSaldoPendiente()) / c.getMontoTotal() * 100)));
            datos.add(fila);
        }
        
        mostrarResultados(datos);
        double totalCreditos = creditos.stream().mapToDouble(Credito::getMontoTotal).sum();
        double totalPendiente = creditos.stream().mapToDouble(Credito::getSaldoPendiente).sum();
        mostrarResumen("═══ ANÁLISIS DE CRÉDITOS ═══\n" +
                      "Créditos activos: " + creditos.size() + "\n\n" +
                      "Monto total financiado:\n" + formatoMoneda.format(totalCreditos) + "\n\n" +
                      "Saldo pendiente total:\n" + formatoMoneda.format(totalPendiente) + "\n\n" +
                      "Recuperado:\n" + formatoMoneda.format(totalCreditos - totalPendiente));
        btnExportar.setDisable(false);
    }
    
    // ==================== REPORTES CON CÁLCULOS ====================
    
    /**
     * REPORTE 1: Resumen de Ventas
     * Cálculos: Total ventas, IVA, Subtotal, Promedios
     */
    @FXML
    private void reporteResumenVentas() {
        lblTituloResultado.setText("📄 Reporte: Resumen General de Ventas");
        limpiarTabla();
        
        List<Venta> ventas = ventaDAO.obtenerTodas();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        double totalSubtotal = 0, totalIVA = 0, totalVentas = 0;
        int cantidadVentas = 0, ventasCredito = 0, ventasContado = 0;
        
        for (Venta v : ventas) {
            totalSubtotal += v.getSubtotal();
            totalIVA += v.getIvaTotal();
            totalVentas += v.getTotal();
            cantidadVentas++;
            
            if (v.isEsCredito()) ventasCredito++;
            else ventasContado++;
            
            Map<String, Object> fila = new HashMap<>();
            fila.put("Código", v.getCodigo());
            fila.put("Fecha", v.getFechaVenta().toString());
            fila.put("Tipo", v.isEsCredito() ? "Crédito" : "Contado");
            fila.put("Subtotal", formatoMoneda.format(v.getSubtotal()));
            fila.put("IVA (19%)", formatoMoneda.format(v.getIvaTotal()));
            fila.put("Total", formatoMoneda.format(v.getTotal()));
            datos.add(fila);
        }
        
        // Agregar fila de totales
        Map<String, Object> totales = new HashMap<>();
        totales.put("Código", "═══ TOTAL ═══");
        totales.put("Fecha", "");
        totales.put("Tipo", cantidadVentas + " ventas");
        totales.put("Subtotal", formatoMoneda.format(totalSubtotal));
        totales.put("IVA (19%)", formatoMoneda.format(totalIVA));
        totales.put("Total", formatoMoneda.format(totalVentas));
        datos.add(totales);
        
        mostrarResultados(datos);
        mostrarResumen("═══ RESUMEN DE VENTAS ═══\n" +
                      "Total de ventas: " + cantidadVentas + "\n" +
                      "Ventas de contado: " + ventasContado + "\n" +
                      "Ventas a crédito: " + ventasCredito + "\n\n" +
                      "Subtotal: " + formatoMoneda.format(totalSubtotal) + "\n" +
                      "IVA (19%): " + formatoMoneda.format(totalIVA) + "\n" +
                      "TOTAL FACTURADO: " + formatoMoneda.format(totalVentas) + "\n\n" +
                      "Promedio por venta: " + formatoMoneda.format(cantidadVentas > 0 ? totalVentas/cantidadVentas : 0));
        btnExportar.setDisable(false);
    }
    
    /**
     * REPORTE 2: Inventario Valorizado
     * Cálculos: Valor inventario por categoría
     */
    @FXML
    private void reporteInventarioValorizado() {
        lblTituloResultado.setText("📄 Reporte: Inventario Valorizado por Categoría");
        limpiarTabla();
        
        List<Producto> productos = productoDAO.obtenerTodos();
        Map<String, Double> valorPorCategoria = new HashMap<>();
        Map<String, Integer> cantidadPorCategoria = new HashMap<>();
        
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        for (Producto p : productos) {
            Categoria cat = categoriaDAO.obtenerPorId(p.getIdCategoria());
            String nombreCat = cat != null ? cat.getNombre() : "Sin categoría";
            
            double valorProducto = p.getStockActual() * p.getPrecioCompra();
            valorPorCategoria.merge(nombreCat, valorProducto, Double::sum);
            cantidadPorCategoria.merge(nombreCat, p.getStockActual(), Integer::sum);
        }
        
        double totalValor = 0;
        int totalUnidades = 0;
        
        for (String categoria : valorPorCategoria.keySet()) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("Categoría", categoria);
            fila.put("Unidades", String.valueOf(cantidadPorCategoria.get(categoria)));
            fila.put("Valor Total", formatoMoneda.format(valorPorCategoria.get(categoria)));
            fila.put("% del Total", String.format("%.1f%%", 0.0)); // Se calculará después
            datos.add(fila);
            
            totalValor += valorPorCategoria.get(categoria);
            totalUnidades += cantidadPorCategoria.get(categoria);
        }
        
        // Calcular porcentajes
        for (Map<String, Object> fila : datos) {
            String categoria = (String) fila.get("Categoría");
            double valor = valorPorCategoria.get(categoria);
            fila.put("% del Total", String.format("%.1f%%", (valor / totalValor * 100)));
        }
        
        // Agregar totales
        Map<String, Object> totales = new HashMap<>();
        totales.put("Categoría", "═══ TOTAL INVENTARIO ═══");
        totales.put("Unidades", String.valueOf(totalUnidades));
        totales.put("Valor Total", formatoMoneda.format(totalValor));
        totales.put("% del Total", "100.0%");
        datos.add(totales);
        
        mostrarResultados(datos);
        mostrarResumen("═══ INVENTARIO VALORIZADO ═══\n" +
                      "Total de categorías: " + valorPorCategoria.size() + "\n" +
                      "Total unidades: " + totalUnidades + "\n\n" +
                      "VALOR TOTAL INVENTARIO:\n" + formatoMoneda.format(totalValor));
        btnExportar.setDisable(false);
    }
    
    /**
     * REPORTE 3: Estado de Cartera
     * Cálculos: Análisis de créditos y recuperación
     */
    @FXML
    private void reporteEstadoCartera() {
        lblTituloResultado.setText("📄 Reporte: Estado de Cartera de Clientes");
        limpiarTabla();
        
        List<Cliente> clientes = clienteDAO.obtenerTodos();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        double totalLimites = 0, totalSaldos = 0, totalDisponible = 0;
        int clientesConSaldo = 0;
        
        for (Cliente c : clientes) {
            if (c.getLimiteCredito() > 0) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("Cliente", c.getNombreCompleto());
                fila.put("Límite", formatoMoneda.format(c.getLimiteCredito()));
                fila.put("Saldo", formatoMoneda.format(c.getSaldoPendiente()));
                fila.put("Disponible", formatoMoneda.format(c.getCreditoDisponible()));
                fila.put("% Usado", String.format("%.1f%%", 
                    c.getLimiteCredito() > 0 ? (c.getSaldoPendiente() / c.getLimiteCredito() * 100) : 0));
                datos.add(fila);
                
                totalLimites += c.getLimiteCredito();
                totalSaldos += c.getSaldoPendiente();
                totalDisponible += c.getCreditoDisponible();
                if (c.getSaldoPendiente() > 0) clientesConSaldo++;
            }
        }
        
        // Totales
        Map<String, Object> totales = new HashMap<>();
        totales.put("Cliente", "═══ TOTAL ═══");
        totales.put("Límite", formatoMoneda.format(totalLimites));
        totales.put("Saldo", formatoMoneda.format(totalSaldos));
        totales.put("Disponible", formatoMoneda.format(totalDisponible));
        totales.put("% Usado", String.format("%.1f%%", 
            totalLimites > 0 ? (totalSaldos / totalLimites * 100) : 0));
        datos.add(totales);
        
        mostrarResultados(datos);
        mostrarResumen("═══ ESTADO DE CARTERA ═══\n" +
                      "Clientes con línea de crédito: " + datos.size() + "\n" +
                      "Clientes con saldo pendiente: " + clientesConSaldo + "\n\n" +
                      "Total límites otorgados:\n" + formatoMoneda.format(totalLimites) + "\n\n" +
                      "Total saldos pendientes:\n" + formatoMoneda.format(totalSaldos) + "\n\n" +
                      "Total crédito disponible:\n" + formatoMoneda.format(totalDisponible) + "\n\n" +
                      "% Cartera utilizada: " + String.format("%.1f%%", 
                        totalLimites > 0 ? (totalSaldos / totalLimites * 100) : 0));
        btnExportar.setDisable(false);
    }
    
    /**
     * REPORTE 4: Top Productos Vendidos
     * Cálculos: Ranking de productos más vendidos
     */
    @FXML
    private void reporteTopProductos() {
        lblTituloResultado.setText("📄 Reporte: Top 10 Productos Más Vendidos");
        limpiarTabla();
        
        List<DetalleVenta> detalles = detalleVentaDAO.obtenerTodos();
        Map<Integer, Integer> cantidadPorProducto = new HashMap<>();
        Map<Integer, Double> totalPorProducto = new HashMap<>();
        
        for (DetalleVenta d : detalles) {
            cantidadPorProducto.merge(d.getIdProducto(), d.getCantidad(), Integer::sum);
            totalPorProducto.merge(d.getIdProducto(), d.getTotal(), Double::sum);
        }
        
        // Ordenar por cantidad vendida
        List<Map.Entry<Integer, Integer>> ranking = new ArrayList<>(cantidadPorProducto.entrySet());
        ranking.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        int posicion = 1;
        double totalVendido = 0;
        int totalUnidades = 0;
        
        for (Map.Entry<Integer, Integer> entry : ranking) {
            if (posicion > 10) break; // Top 10
            
            Producto p = productoDAO.obtenerPorId(entry.getKey());
            if (p != null) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("Top", String.valueOf(posicion));
                fila.put("Producto", p.getNombre());
                fila.put("Marca", p.getMarca());
                fila.put("Unidades", String.valueOf(entry.getValue()));
                fila.put("Total Vendido", formatoMoneda.format(totalPorProducto.get(entry.getKey())));
                fila.put("Precio Promedio", formatoMoneda.format(
                    totalPorProducto.get(entry.getKey()) / entry.getValue()));
                datos.add(fila);
                
                totalVendido += totalPorProducto.get(entry.getKey());
                totalUnidades += entry.getValue();
            }
            posicion++;
        }
        
        mostrarResultados(datos);
        mostrarResumen("═══ TOP 10 PRODUCTOS ═══\n" +
                      "Total unidades vendidas (Top 10):\n" + totalUnidades + "\n\n" +
                      "Total facturado (Top 10):\n" + formatoMoneda.format(totalVendido) + "\n\n" +
                      "Ticket promedio:\n" + formatoMoneda.format(totalUnidades > 0 ? totalVendido/totalUnidades : 0));
        btnExportar.setDisable(false);
    }
    
    /**
     * REPORTE 5: Análisis de IVA
     * Cálculos: IVA recaudado por período
     */
    @FXML
    private void reporteAnalisisIVA() {
        lblTituloResultado.setText("📄 Reporte: Análisis de IVA Recaudado");
        limpiarTabla();
        
        List<Venta> ventas = ventaDAO.obtenerTodas();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        
        double totalIVA = 0, totalSubtotal = 0, totalVentas = 0;
        
        for (Venta v : ventas) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("Código Venta", v.getCodigo());
            fila.put("Fecha", v.getFechaVenta().toString());
            fila.put("Base Imponible", formatoMoneda.format(v.getSubtotal()));
            fila.put("IVA (19%)", formatoMoneda.format(v.getIvaTotal()));
            fila.put("Total", formatoMoneda.format(v.getTotal()));
            fila.put("% IVA Real", String.format("%.2f%%", 
                v.getSubtotal() > 0 ? (v.getIvaTotal() / v.getSubtotal() * 100) : 0));
            datos.add(fila);
            
            totalSubtotal += v.getSubtotal();
            totalIVA += v.getIvaTotal();
            totalVentas += v.getTotal();
        }
        
        // Totales
        Map<String, Object> totales = new HashMap<>();
        totales.put("Código Venta", "═══ TOTAL ═══");
        totales.put("Fecha", "");
        totales.put("Base Imponible", formatoMoneda.format(totalSubtotal));
        totales.put("IVA (19%)", formatoMoneda.format(totalIVA));
        totales.put("Total", formatoMoneda.format(totalVentas));
        totales.put("% IVA Real", String.format("%.2f%%", 
            totalSubtotal > 0 ? (totalIVA / totalSubtotal * 100) : 0));
        datos.add(totales);
        
        mostrarResultados(datos);
        mostrarResumen("═══ ANÁLISIS DE IVA ═══\n" +
                      "Total ventas: " + ventas.size() + "\n\n" +
                      "Base Imponible:\n" + formatoMoneda.format(totalSubtotal) + "\n\n" +
                      "IVA Recaudado (19%):\n" + formatoMoneda.format(totalIVA) + "\n\n" +
                      "Total Facturado:\n" + formatoMoneda.format(totalVentas) + "\n\n" +
                      "═══════════════════\n" +
                      "IVA a pagar a la DIAN:\n" + formatoMoneda.format(totalIVA));
        btnExportar.setDisable(false);
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Muestra los resultados en la tabla dinámica
     */
    private void mostrarResultados(ObservableList<Map<String, Object>> datos) {
        if (datos.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sin resultados");
            alert.setHeaderText(null);
            alert.setContentText("No hay datos para mostrar en esta consulta.");
            alert.showAndWait();
            return;
        }
        
        // Limpiar columnas anteriores
        tablaResultados.getColumns().clear();
        
        // Obtener las claves del primer mapa para crear columnas
        Map<String, Object> primera = datos.get(0);
        for (String clave : primera.keySet()) {
            TableColumn<Map<String, Object>, String> columna = new TableColumn<>(clave);
            columna.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(cellData.getValue().get(clave)))
            );
            columna.setPrefWidth(120);
            tablaResultados.getColumns().add(columna);
        }
        
        tablaResultados.setItems(datos);
    }
    
    /**
     * Muestra el resumen con cálculos
     */
    private void mostrarResumen(String resumen) {
        txtResumen.setText(resumen);
        panelResumen.setVisible(true);
    }
    
    /**
     * Limpia la tabla y oculta el resumen
     */
    private void limpiarTabla() {
        tablaResultados.getColumns().clear();
        tablaResultados.getItems().clear();
        panelResumen.setVisible(false);
        btnExportar.setDisable(true);
    }
    
    /**
     * Exporta los resultados a CSV
     */
    @FXML
    private void exportarResultados() {
        if (tablaResultados.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("No hay datos para exportar");
            alert.setContentText("Debe ejecutar una consulta o reporte primero.");
            alert.showAndWait();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte");
        fileChooser.setInitialFileName("reporte_" + System.currentTimeMillis() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivo CSV", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(tablaResultados.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Escribir encabezados
                ObservableList<TableColumn<Map<String, Object>, ?>> columnas = tablaResultados.getColumns();
                for (int i = 0; i < columnas.size(); i++) {
                    writer.append(columnas.get(i).getText());
                    if (i < columnas.size() - 1) writer.append(",");
                }
                writer.append("\n");
                
                // Escribir datos
                for (Map<String, Object> fila : tablaResultados.getItems()) {
                    for (int i = 0; i < columnas.size(); i++) {
                        String valor = String.valueOf(fila.get(columnas.get(i).getText()));
                        writer.append(valor);
                        if (i < columnas.size() - 1) writer.append(",");
                    }
                    writer.append("\n");
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setHeaderText("Exportación completada");
                alert.setContentText("El reporte se guardó exitosamente en:\n" + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error al exportar");
                alert.setContentText("No se pudo guardar el archivo:\n" + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    /**
     * Cierra la ventana
     */
    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) tablaResultados.getScene().getWindow();
        stage.close();
    }
}