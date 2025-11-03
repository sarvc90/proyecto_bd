package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.controllers.CalculadoraController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.text.DecimalFormat;

/**
 * Controlador de la vista de Calculadora.
 * Interactúa con CalculadoraController para realizar los cálculos.
 *
 * @author Sistema
 * @version 1.0
 */
public class CalculadoraViewController {

    // ==================== COMPONENTES FXML ====================

    // Tab 1: IVA
    @FXML private TextField txtSubtotalIVA;
    @FXML private TextField txtPorcentajeIVA;
    @FXML private TextField txtResultadoIVA;
    @FXML private TextField txtTotalConIVA;

    // Tab 2: Cuotas de Crédito
    @FXML private TextField txtPrecioArticulo;
    @FXML private ComboBox<String> cmbPlazoMeses;
    @FXML private TextField txtCuotaInicial;
    @FXML private TextField txtSaldoFinanciar;
    @FXML private TextField txtInteresCredito;
    @FXML private TextField txtTotalFinanciar;
    @FXML private TextField txtValorCuota;
    @FXML private TextField txtTotalPagar;

    // Tab 3: Margen de Ganancia
    @FXML private TextField txtCosto;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtMargen;
    @FXML private TextField txtGanancia;

    // Tab 4: Descuentos
    @FXML private TextField txtPrecioOriginal;
    @FXML private TextField txtPorcentajeDescuento;
    @FXML private TextField txtAhorro;
    @FXML private TextField txtPrecioFinal;

    // Tab 5: Utilidad
    @FXML private TextField txtCostoUtilidad;
    @FXML private TextField txtPrecioVentaUtilidad;
    @FXML private TextField txtPorcentajeIVAUtilidad;
    @FXML private TextField txtUtilidad;

    // ==================== INSTANCIA DEL CONTROLADOR DE NEGOCIO ====================
    private final CalculadoraController calculadora = new CalculadoraController();
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    // ==================== INICIALIZACIÓN ====================

    @FXML
    public void initialize() {
        // Configurar ComboBox de plazos de crédito
        if (cmbPlazoMeses != null) {
            cmbPlazoMeses.getItems().addAll("12", "18", "24");
            cmbPlazoMeses.setValue("12");
        }
    }

    // ==================== MÉTODOS DE CÁLCULO ====================

    /**
     * TAB 1: Calcular IVA
     */
    @FXML
    private void calcularIVA() {
        try {
            double subtotal = parseDouble(txtSubtotalIVA.getText());
            double porcentajeIVA = parseDouble(txtPorcentajeIVA.getText());

            double valorIVA = calculadora.calcularIVA(subtotal, porcentajeIVA);
            double totalConIVA = calculadora.calcularTotalConIVA(subtotal, porcentajeIVA);

            txtResultadoIVA.setText(df.format(valorIVA));
            txtTotalConIVA.setText(df.format(totalConIVA));

        } catch (NumberFormatException e) {
            mostrarError("Error", "Por favor ingrese valores numéricos válidos.");
        }
    }

    @FXML
    private void limpiarIVA() {
        txtSubtotalIVA.clear();
        txtPorcentajeIVA.clear();
        txtResultadoIVA.clear();
        txtTotalConIVA.clear();
    }

    /**
     * TAB 2: Calcular Cuotas de Crédito
     * Política: 30% cuota inicial, 70% + 5% interés financiado
     * Plazos: 12, 18 o 24 meses
     */
    @FXML
    private void calcularCuota() {
        try {
            // Validar precio del artículo
            double precioArticulo = parseDouble(txtPrecioArticulo.getText());
            if (precioArticulo <= 0) {
                mostrarError("Error", "Por favor ingrese un precio válido mayor a 0.");
                return;
            }

            // Validar plazo seleccionado
            String plazoStr = cmbPlazoMeses.getValue();
            if (plazoStr == null || plazoStr.isEmpty()) {
                mostrarError("Error", "Por favor seleccione un plazo (12, 18 o 24 meses).");
                return;
            }
            int plazoMeses = Integer.parseInt(plazoStr);

            // CALCULAR SEGÚN LA POLÍTICA DE CRÉDITO

            // 1. Cuota inicial del 30%
            double cuotaInicial = precioArticulo * 0.30;

            // 2. Saldo a financiar (70%)
            double saldoFinanciar = precioArticulo * 0.70;

            // 3. Interés del 5% sobre el saldo a financiar
            double interes = saldoFinanciar * 0.05;

            // 4. Total a financiar (saldo + interés)
            double totalFinanciar = saldoFinanciar + interes;

            // 5. Valor de la cuota mensual
            double valorCuota = totalFinanciar / plazoMeses;

            // 6. Total a pagar (cuota inicial + total financiado)
            double totalPagar = cuotaInicial + totalFinanciar;

            // Mostrar resultados
            txtCuotaInicial.setText("$ " + df.format(cuotaInicial));
            txtSaldoFinanciar.setText("$ " + df.format(saldoFinanciar));
            txtInteresCredito.setText("$ " + df.format(interes));
            txtTotalFinanciar.setText("$ " + df.format(totalFinanciar));
            txtValorCuota.setText("$ " + df.format(valorCuota));
            txtTotalPagar.setText("$ " + df.format(totalPagar));

        } catch (NumberFormatException e) {
            mostrarError("Error", "Por favor ingrese valores válidos.");
        }
    }

    @FXML
    private void limpiarCuota() {
        txtPrecioArticulo.clear();
        cmbPlazoMeses.setValue("12");
        txtCuotaInicial.clear();
        txtSaldoFinanciar.clear();
        txtInteresCredito.clear();
        txtTotalFinanciar.clear();
        txtValorCuota.clear();
        txtTotalPagar.clear();
    }

    /**
     * TAB 3: Calcular Margen de Ganancia
     */
    @FXML
    private void calcularMargen() {
        try {
            double costo = parseDouble(txtCosto.getText());
            double precioVenta = parseDouble(txtPrecioVenta.getText());

            double margen = calculadora.calcularMargenGanancia(costo, precioVenta);
            double ganancia = precioVenta - costo;

            txtMargen.setText(df.format(margen) + " %");
            txtGanancia.setText(df.format(ganancia));

        } catch (NumberFormatException e) {
            mostrarError("Error", "Por favor ingrese valores numéricos válidos.");
        }
    }

    @FXML
    private void limpiarMargen() {
        txtCosto.clear();
        txtPrecioVenta.clear();
        txtMargen.clear();
        txtGanancia.clear();
    }

    /**
     * TAB 4: Calcular Descuento
     */
    @FXML
    private void calcularDescuento() {
        try {
            double precioOriginal = parseDouble(txtPrecioOriginal.getText());
            double porcentajeDescuento = parseDouble(txtPorcentajeDescuento.getText());

            double precioFinal = calculadora.calcularDescuento(precioOriginal, porcentajeDescuento);
            double ahorro = precioOriginal - precioFinal;

            txtAhorro.setText(df.format(ahorro));
            txtPrecioFinal.setText(df.format(precioFinal));

        } catch (NumberFormatException e) {
            mostrarError("Error", "Por favor ingrese valores numéricos válidos.");
        }
    }

    @FXML
    private void limpiarDescuento() {
        txtPrecioOriginal.clear();
        txtPorcentajeDescuento.clear();
        txtAhorro.clear();
        txtPrecioFinal.clear();
    }

    /**
     * TAB 5: Calcular Utilidad
     */
    @FXML
    private void calcularUtilidad() {
        try {
            double costo = parseDouble(txtCostoUtilidad.getText());
            double precioVenta = parseDouble(txtPrecioVentaUtilidad.getText());
            double porcentajeIVA = parseDouble(txtPorcentajeIVAUtilidad.getText());

            double utilidad = calculadora.calcularUtilidad(costo, precioVenta, porcentajeIVA);

            txtUtilidad.setText(df.format(utilidad));

        } catch (NumberFormatException e) {
            mostrarError("Error", "Por favor ingrese valores numéricos válidos.");
        }
    }

    @FXML
    private void limpiarUtilidad() {
        txtCostoUtilidad.clear();
        txtPrecioVentaUtilidad.clear();
        txtPorcentajeIVAUtilidad.clear();
        txtUtilidad.clear();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Parsea un texto a double, manejando valores vacíos
     */
    private double parseDouble(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text.trim());
    }

    /**
     * Muestra un diálogo de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de información
     */
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}