package com.taller.proyecto_bd.ui.utilidades;

import com.taller.proyecto_bd.controllers.CalculadoraController;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.Locale;

/**
 * Controlador para las utilidades (calculadoras y calendario).
 */
public class UtilidadesController {

    private final CalculadoraController calculadora = new CalculadoraController();

    @FXML private TextField txtSubtotalIva;
    @FXML private TextField txtPorcentajeIva;
    @FXML private Label lblResultadoIva;

    @FXML private TextField txtCostoMargen;
    @FXML private TextField txtPrecioMargen;
    @FXML private Label lblResultadoMargen;

    @FXML private TextField txtMontoCredito;
    @FXML private TextField txtPlazoCredito;
    @FXML private TextField txtInteresCredito;
    @FXML private Label lblResultadoCuota;

    @FXML private TextField txtPrecioDescuento;
    @FXML private TextField txtPorcentajeDescuento;
    @FXML private Label lblResultadoDescuento;

    @FXML private TextField txtCostoUtilidad;
    @FXML private TextField txtPrecioUtilidad;
    @FXML private TextField txtIvaUtilidad;
    @FXML private Label lblResultadoUtilidad;

    @FXML private DatePicker dpCalendario;
    @FXML private Label lblInfoCalendario;

    @FXML
    private void initialize() {
        if (txtPorcentajeIva != null) {
            txtPorcentajeIva.setText("19");
        }
        if (txtInteresCredito != null) {
            txtInteresCredito.setText("5");
        }
        if (txtIvaUtilidad != null) {
            txtIvaUtilidad.setText("19");
        }
        if (dpCalendario != null) {
            dpCalendario.setValue(LocalDate.now());
        }
    }

    @FXML
    private void calcularIVA() {
        try {
            double subtotal = Double.parseDouble(txtSubtotalIva.getText().trim());
            double porcentaje = Double.parseDouble(txtPorcentajeIva.getText().trim()) / 100.0;
            double iva = calculadora.calcularIVA(subtotal, porcentaje);
            double total = calculadora.calcularTotalConIVA(subtotal, porcentaje);
            mostrarResultado(lblResultadoIva,
                    String.format(Locale.US, "IVA: $%.2f  |  Total con IVA: $%.2f", iva, total),
                    false);
        } catch (NumberFormatException e) {
            mostrarResultado(lblResultadoIva, "Ingrese valores numéricos válidos.", true);
        }
    }

    @FXML
    private void calcularMargen() {
        try {
            double costo = Double.parseDouble(txtCostoMargen.getText().trim());
            double precio = Double.parseDouble(txtPrecioMargen.getText().trim());
            double margen = calculadora.calcularMargenGanancia(costo, precio);
            double utilidad = precio - costo;
            mostrarResultado(lblResultadoMargen,
                    String.format(Locale.US, "Utilidad: $%.2f  |  Margen: %.2f%%", utilidad, margen),
                    false);
        } catch (NumberFormatException e) {
            mostrarResultado(lblResultadoMargen, "Ingrese costo y precio válidos.", true);
        }
    }

    @FXML
    private void calcularCuota() {
        try {
            double monto = Double.parseDouble(txtMontoCredito.getText().trim());
            int plazo = Integer.parseInt(txtPlazoCredito.getText().trim());
            double interes = Double.parseDouble(txtInteresCredito.getText().trim()) / 100.0;
            double cuota = calculadora.calcularValorCuota(monto, plazo, interes);
            mostrarResultado(lblResultadoCuota,
                    String.format(Locale.US, "Cuota mensual: $%.2f", cuota),
                    false);
        } catch (NumberFormatException e) {
            mostrarResultado(lblResultadoCuota, "Ingrese monto, plazo e interés válidos.", true);
        }
    }

    @FXML
    private void calcularDescuento() {
        try {
            double precio = Double.parseDouble(txtPrecioDescuento.getText().trim());
            double porcentaje = Double.parseDouble(txtPorcentajeDescuento.getText().trim()) / 100.0;
            double precioConDescuento = calculadora.calcularDescuento(precio, porcentaje);
            double ahorro = precio - precioConDescuento;
            mostrarResultado(lblResultadoDescuento,
                    String.format(Locale.US, "Precio final: $%.2f  |  Ahorro: $%.2f", precioConDescuento, ahorro),
                    false);
        } catch (NumberFormatException e) {
            mostrarResultado(lblResultadoDescuento, "Ingrese precio y descuento válidos.", true);
        }
    }

    @FXML
    private void calcularUtilidad() {
        try {
            double costo = Double.parseDouble(txtCostoUtilidad.getText().trim());
            double precio = Double.parseDouble(txtPrecioUtilidad.getText().trim());
            double iva = Double.parseDouble(txtIvaUtilidad.getText().trim()) / 100.0;
            double utilidad = calculadora.calcularUtilidad(costo, precio, iva);
            mostrarResultado(lblResultadoUtilidad,
                    String.format(Locale.US, "Utilidad neta: $%.2f", utilidad),
                    false);
        } catch (NumberFormatException e) {
            mostrarResultado(lblResultadoUtilidad, "Ingrese datos válidos para costo, precio e IVA.", true);
        }
    }

    @FXML
    private void mostrarInfoCalendario() {
        LocalDate fecha = dpCalendario.getValue();
        if (fecha == null) {
            mostrarResultado(lblInfoCalendario, "Seleccione una fecha del calendario.", true);
            return;
        }
        String diaSemana = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        int semana = fecha.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        mostrarResultado(lblInfoCalendario,
                String.format("%s, semana %d del año", capitalizar(diaSemana), semana),
                false);
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }

    private void mostrarResultado(Label label, String mensaje, boolean error) {
        if (label == null) {
            return;
        }
        label.setText(mensaje);
        label.setStyle(error ? "-fx-text-fill: #f94144;" : "-fx-text-fill: #1b4332;");
    }
}
