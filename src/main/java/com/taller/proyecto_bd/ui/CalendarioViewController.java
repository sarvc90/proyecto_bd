package com.taller.proyecto_bd.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Controlador de la vista del Calendario.
 * Muestra un calendario mensual interactivo.
 *
 * @author Sistema
 * @version 1.0
 */
public class CalendarioViewController {

    // ==================== COMPONENTES FXML ====================

    @FXML private Label lblMesAnio;
    @FXML private Label lblFechaActual;
    @FXML private Label lblDiaAnio;
    @FXML private GridPane gridCalendario;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<Integer> cmbAnio;

    // ==================== DATOS ====================

    private Calendar calendarioActual;
    private Calendar hoy;
    private final SimpleDateFormat formatoMesAnio = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
    private final SimpleDateFormat formatoFechaCompleta = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    private final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    // ==================== INICIALIZACIÓN ====================

    @FXML
    public void initialize() {
        // Inicializar calendarios
        calendarioActual = Calendar.getInstance();
        hoy = Calendar.getInstance();

        // Configurar ComboBoxes
        configurarComboBoxes();

        // Mostrar el calendario actual
        actualizarCalendario();
        actualizarInformacion();
    }

    /**
     * Configura los ComboBox de mes y año
     */
    private void configurarComboBoxes() {
        // Llenar ComboBox de meses
        cmbMes.getItems().addAll(MESES);

        // Llenar ComboBox de años (desde 2020 hasta 2030)
        for (int anio = 2020; anio <= 2030; anio++) {
            cmbAnio.getItems().add(anio);
        }
    }

    // ==================== ACTUALIZACIÓN DE VISTA ====================

    /**
     * Actualiza el calendario completo
     */
    private void actualizarCalendario() {
        // Actualizar título
        String mesAnio = formatoMesAnio.format(calendarioActual.getTime());
        lblMesAnio.setText(capitalizar(mesAnio));

        // Limpiar el grid
        gridCalendario.getChildren().clear();
        gridCalendario.getRowConstraints().clear();

        // Obtener el primer día del mes
        Calendar primerDia = (Calendar) calendarioActual.clone();
        primerDia.set(Calendar.DAY_OF_MONTH, 1);

        // Día de la semana del primer día (0 = Domingo, 6 = Sábado)
        int diaSemanaInicio = primerDia.get(Calendar.DAY_OF_WEEK) - 1;

        // Días del mes actual
        int diasEnMes = calendarioActual.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Crear 6 filas (máximo necesario para cualquier mes)
        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(16.66);
            gridCalendario.getRowConstraints().add(row);
        }

        // Rellenar el calendario
        int dia = 1;
        for (int fila = 0; fila < 6; fila++) {
            for (int col = 0; col < 7; col++) {
                if ((fila == 0 && col < diaSemanaInicio) || dia > diasEnMes) {
                    // Celda vacía
                    StackPane celda = crearCeldaVacia();
                    gridCalendario.add(celda, col, fila);
                } else {
                    // Celda con día
                    StackPane celda = crearCeldaDia(dia);
                    gridCalendario.add(celda, col, fila);
                    dia++;
                }
            }
            // Si ya mostramos todos los días, salir del loop
            if (dia > diasEnMes) break;
        }
    }

    /**
     * Crea una celda de día del calendario
     */
    private StackPane crearCeldaDia(int dia) {
        StackPane celda = new StackPane();
        celda.setAlignment(Pos.TOP_CENTER);
        celda.setStyle("-fx-padding: 5;");

        Label lblDia = new Label(String.valueOf(dia));
        lblDia.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Verificar si es el día actual
        if (esDiaActual(dia)) {
            celda.setStyle("-fx-background-color: #3498db; -fx-padding: 5;");
            lblDia.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");
        }
        // Verificar si es fin de semana
        else if (esFinDeSemana(dia)) {
            celda.setStyle("-fx-background-color: #fff9e6; -fx-padding: 5;");
        }

        celda.getChildren().add(lblDia);
        return celda;
    }

    /**
     * Crea una celda vacía
     */
    private StackPane crearCeldaVacia() {
        StackPane celda = new StackPane();
        celda.setStyle("-fx-background-color: #f8f9fa;");
        return celda;
    }

    /**
     * Verifica si un día es el día actual
     */
    private boolean esDiaActual(int dia) {
        return calendarioActual.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) &&
               calendarioActual.get(Calendar.MONTH) == hoy.get(Calendar.MONTH) &&
               dia == hoy.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Verifica si un día cae en fin de semana
     */
    private boolean esFinDeSemana(int dia) {
        Calendar temp = (Calendar) calendarioActual.clone();
        temp.set(Calendar.DAY_OF_MONTH, dia);
        int diaSemana = temp.get(Calendar.DAY_OF_WEEK);
        return diaSemana == Calendar.SATURDAY || diaSemana == Calendar.SUNDAY;
    }

    /**
     * Actualiza la información adicional (fecha actual, día del año)
     */
    private void actualizarInformacion() {
        // Fecha actual completa
        String fechaCompleta = formatoFechaCompleta.format(hoy.getTime());
        lblFechaActual.setText(capitalizar(fechaCompleta));

        // Día del año
        int diaDelAnio = hoy.get(Calendar.DAY_OF_YEAR);
        int diasEnAnio = hoy.getActualMaximum(Calendar.DAY_OF_YEAR);
        lblDiaAnio.setText("Día " + diaDelAnio + " de " + diasEnAnio);
    }

    // ==================== NAVEGACIÓN ====================

    /**
     * Navega al mes anterior
     */
    @FXML
    private void mesAnterior() {
        calendarioActual.add(Calendar.MONTH, -1);
        actualizarCalendario();
    }

    /**
     * Navega al mes siguiente
     */
    @FXML
    private void mesSiguiente() {
        calendarioActual.add(Calendar.MONTH, 1);
        actualizarCalendario();
    }

    /**
     * Vuelve al día de hoy
     */
    @FXML
    private void irHoy() {
        calendarioActual = (Calendar) hoy.clone();
        actualizarCalendario();
    }

    /**
     * Navega a la fecha seleccionada en los ComboBox
     */
    @FXML
    private void irFecha() {
        String mesSeleccionado = cmbMes.getValue();
        Integer anioSeleccionado = cmbAnio.getValue();

        if (mesSeleccionado != null && anioSeleccionado != null) {
            int indiceMes = -1;
            for (int i = 0; i < MESES.length; i++) {
                if (MESES[i].equals(mesSeleccionado)) {
                    indiceMes = i;
                    break;
                }
            }

            if (indiceMes != -1) {
                calendarioActual.set(Calendar.MONTH, indiceMes);
                calendarioActual.set(Calendar.YEAR, anioSeleccionado);
                actualizarCalendario();
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Capitaliza la primera letra de un texto
     */
    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}