package com.taller.proyecto_bd.utils;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Clase utilitaria para exportar reportes a PDF con formato profesional y gráficas
 */
public class PDFExporter {

    private static final DeviceRgb COLOR_PRIMARIO = new DeviceRgb(33, 150, 243); // Azul
    private static final DeviceRgb COLOR_SECUNDARIO = new DeviceRgb(76, 175, 80); // Verde
    private static final DeviceRgb COLOR_ENCABEZADO = new DeviceRgb(245, 245, 245); // Gris claro

    /**
     * Exporta una tabla de datos a PDF con formato profesional
     */
    public static void exportarAPDF(
            File archivo,
            String titulo,
            String resumen,
            ObservableList<TableColumn<Map<String, Object>, ?>> columnas,
            ObservableList<Map<String, Object>> datos,
            JFreeChart grafica) throws Exception {

        PdfWriter writer = new PdfWriter(archivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Configurar márgenes
        document.setMargins(40, 40, 40, 40);

        // ===== ENCABEZADO =====
        agregarEncabezado(document, titulo);

        // ===== FECHA Y HORA =====
        agregarFechaHora(document);

        // Línea separadora decorativa
        document.add(new Paragraph("\n"));
        Paragraph lineaSeparadora = new Paragraph("")
                .setBorderBottom(new SolidBorder(COLOR_PRIMARIO, 2))
                .setMarginBottom(10);
        document.add(lineaSeparadora);

        // ===== GRÁFICA (si existe) =====
        if (grafica != null) {
            agregarGrafica(document, grafica);
            document.add(new Paragraph("\n"));
        }

        // ===== TABLA DE DATOS =====
        if (!datos.isEmpty()) {
            agregarTablaDatos(document, columnas, datos);
            document.add(new Paragraph("\n"));
        }

        // ===== RESUMEN =====
        if (resumen != null && !resumen.trim().isEmpty()) {
            agregarResumen(document, resumen);
        }

        // ===== PIE DE PÁGINA =====
        agregarPieDePagina(document);

        document.close();
    }

    /**
     * Agrega el encabezado principal del documento
     */
    private static void agregarEncabezado(Document document, String titulo) {
        // Título principal
        Paragraph tituloPrincipal = new Paragraph("SISTEMA DE ELECTRODOMÉSTICOS")
                .setFontSize(20)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(tituloPrincipal);

        // Subtítulo
        Paragraph subtitulo = new Paragraph("Consultas y Reportes")
                .setFontSize(12)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(subtitulo);

        // Título del reporte
        Paragraph tituloReporte = new Paragraph(titulo)
                .setFontSize(16)
                .setBold()
                .setFontColor(COLOR_SECUNDARIO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);
        document.add(tituloReporte);
    }

    /**
     * Agrega la fecha y hora de generación
     */
    private static void agregarFechaHora(Document document) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Paragraph fechaHora = new Paragraph("Fecha de generación: " + ahora.format(formato))
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10);
        document.add(fechaHora);
    }

    /**
     * Agrega una gráfica al documento
     */
    private static void agregarGrafica(Document document, JFreeChart grafica) throws Exception {
        // Convertir JFreeChart a imagen
        BufferedImage bufferedImage = grafica.createBufferedImage(600, 400);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        // Agregar imagen al PDF
        com.itextpdf.layout.element.Image image = new com.itextpdf.layout.element.Image(
                ImageDataFactory.create(imageBytes));
        image.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        image.setWidth(UnitValue.createPercentValue(80));
        document.add(image);
    }

    /**
     * Agrega la tabla de datos al documento
     */
    private static void agregarTablaDatos(Document document,
                                          ObservableList<TableColumn<Map<String, Object>, ?>> columnas,
                                          ObservableList<Map<String, Object>> datos) {

        // Crear tabla con el número de columnas
        float[] anchoColumnas = new float[columnas.size()];
        for (int i = 0; i < columnas.size(); i++) {
            anchoColumnas[i] = 1;
        }

        Table table = new Table(UnitValue.createPercentArray(anchoColumnas));
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados de columna
        for (TableColumn<Map<String, Object>, ?> columna : columnas) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(columna.getText()).setBold())
                    .setBackgroundColor(COLOR_ENCABEZADO)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .setBorder(new SolidBorder(COLOR_PRIMARIO, 1));
            table.addHeaderCell(headerCell);
        }

        // Datos de la tabla
        boolean colorAlternado = false;
        for (Map<String, Object> fila : datos) {
            for (TableColumn<Map<String, Object>, ?> columna : columnas) {
                String valor = String.valueOf(fila.get(columna.getText()));

                Cell cell = new Cell()
                        .add(new Paragraph(valor).setFontSize(9))
                        .setPadding(6)
                        .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

                // Color alternado para filas
                if (colorAlternado) {
                    cell.setBackgroundColor(new DeviceRgb(250, 250, 250));
                }

                table.addCell(cell);
            }
            colorAlternado = !colorAlternado;
        }

        document.add(table);
    }

    /**
     * Agrega el resumen con cálculos
     */
    private static void agregarResumen(Document document, String resumen) {
        // Crear un cuadro con el resumen
        Div divResumen = new Div()
                .setBackgroundColor(new DeviceRgb(232, 245, 233))
                .setBorder(new SolidBorder(COLOR_SECUNDARIO, 2))
                .setPadding(15)
                .setMarginTop(10);

        Paragraph tituloResumen = new Paragraph("RESUMEN Y ANÁLISIS")
                .setBold()
                .setFontSize(12)
                .setFontColor(COLOR_SECUNDARIO)
                .setMarginBottom(5);
        divResumen.add(tituloResumen);

        Paragraph contenidoResumen = new Paragraph(resumen)
                .setFontSize(10)
                .setFixedLeading(14);
        divResumen.add(contenidoResumen);

        document.add(divResumen);
    }

    /**
     * Agrega el pie de página
     */
    private static void agregarPieDePagina(Document document) {
        document.add(new Paragraph("\n"));
        Paragraph lineaPie = new Paragraph("")
                .setBorderTop(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setMarginTop(10);
        document.add(lineaPie);

        Paragraph pieDePagina = new Paragraph(
                "Sistema de Gestión de Electrodomésticos | " +
                        "Generado automáticamente | " +
                        "© 2025")
                .setFontSize(8)
                .setItalic()
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);
        document.add(pieDePagina);
    }

    /**
     * Crea una gráfica de barras
     */
    public static JFreeChart crearGraficaBarras(String titulo, String ejeX, String ejeY,
                                                  Map<String, Number> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Number> entry : datos.entrySet()) {
            dataset.addValue(entry.getValue(), ejeY, entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                ejeX,
                ejeY,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        personalizarGrafica(chart);
        return chart;
    }

    /**
     * Crea una gráfica de pastel (pie)
     */
    public static JFreeChart crearGraficaPastel(String titulo, Map<String, Number> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Map.Entry<String, Number> entry : datos.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                titulo,
                dataset,
                true,
                true,
                false
        );

        personalizarGrafica(chart);
        return chart;
    }

    /**
     * Personaliza el estilo de la gráfica
     */
    private static void personalizarGrafica(JFreeChart chart) {
        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
    }
}