package com.taller.proyecto_bd.ui;

import com.taller.proyecto_bd.models.SessionManager;


import com.taller.proyecto_bd.models.Usuario;

import com.taller.proyecto_bd.dao.AuditoriaDAO;
import com.taller.proyecto_bd.models.Auditoria;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Controlador para la ventana principal del sistema
 */
public class MainWindowController {

    // ==================== COMPONENTES DEL MENÚ ====================
    @FXML private Menu menuEntidades;
    @FXML private Menu menuTransacciones;
    @FXML private Menu menuConsultasReportes;
    @FXML private Menu menuUtilidades;
    
    @FXML private MenuItem menuItemCategorias;
    @FXML private MenuItem menuItemProductos;
    @FXML private MenuItem menuItemClientes;
    @FXML private MenuItem menuItemInventario;
    @FXML private MenuItem menuItemUsuarios;
    @FXML private MenuItem menuItemNuevaVenta;
    @FXML private MenuItem menuItemVentas;
    @FXML private MenuItem menuItemCreditos;
    @FXML private MenuItem menuItemCuotas;
    @FXML private MenuItem menuItemCalculadora;
    @FXML private MenuItem menuItemCalendario;
    @FXML private MenuItem menuItemBitacora;
    
    // ==================== BOTONES ACCESO RÁPIDO ====================
    @FXML private Button btnNuevaVenta;
    @FXML private Button btnBuscarCliente;
    @FXML private Button btnBuscarProducto;
    
    // ==================== LABELS DE INFORMACIÓN ====================
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblRolActual;
    @FXML private Label lblFechaHora;
    @FXML private Label lblEstado;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblProductosStock;
    @FXML private Label lblCreditosActivos;
    
    // ==================== CONTENEDOR PRINCIPAL ====================
    @FXML private StackPane contenedorPrincipal;
    
    private Usuario usuarioActual;
    private Timeline relojTimeline;
    private AuditoriaDAO auditoriaDAO;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        auditoriaDAO = AuditoriaDAO.getInstance();
        iniciarReloj();
        cargarEstadisticas();
    }

    /**
     * Inicializa la ventana con el usuario que ha iniciado sesión
     */
    public void inicializarConUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        
        // Actualizar información del usuario en la interfaz
        lblUsuarioActual.setText(usuario.getNombreCompleto());
        lblRolActual.setText(usuario.getRol());
        
        // Configurar permisos según el rol
        configurarPermisos();
        
        // Mensaje de bienvenida
        actualizarEstado("Bienvenido, " + usuario.getNombreCompleto());
    }

    /**
     * Configura los permisos de acceso según el rol del usuario
     */
    private void configurarPermisos() {
        // ADMIN: Acceso total a todo
        if (SessionManager.esAdministrador()) {
            // No hay restricciones
            return;
        }

        // VENDEDOR: No puede acceder a Usuarios ni Bitácora
        if (SessionManager.esVendedor()) {
            menuItemUsuarios.setDisable(true);
            menuItemBitacora.setDisable(true);
            return;
        }

        // GERENTE: Solo puede acceder a Consultas y Reportes
        if (SessionManager.esGerente()) {
            // Deshabilitar todo excepto consultas y reportes
            menuEntidades.setDisable(true);
            menuTransacciones.setDisable(true);
            menuUtilidades.setDisable(true);

            // Deshabilitar botones de acceso rápido
            btnNuevaVenta.setDisable(true);
            btnBuscarCliente.setDisable(true);
            btnBuscarProducto.setDisable(true);

            actualizarEstado("Usuario de solo consulta");
            return;
        }

        // CLIENTE: Solo puede acceder a Nueva Compra
        if (SessionManager.esCliente()) {
            // Deshabilitar todos los menús
            menuEntidades.setDisable(true);
            menuTransacciones.setDisable(true);
            menuConsultasReportes.setDisable(true);
            menuUtilidades.setDisable(true);

            // Deshabilitar botones de acceso rápido
            btnNuevaVenta.setDisable(true);
            btnBuscarCliente.setDisable(true);
            btnBuscarProducto.setDisable(true);

            actualizarEstado("Modo Cliente - Solo compras");

            // Abrir automáticamente la interfaz de Nueva Compra
            nuevaCompra();
        }
    }

    /**
     * Inicia el reloj en tiempo real
     */
    private void iniciarReloj() {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        
        relojTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            lblFechaHora.setText(formatoFecha.format(new Date()));
        }));
        
        relojTimeline.setCycleCount(Timeline.INDEFINITE);
        relojTimeline.play();
    }

    /**
     * Carga las estadísticas iniciales
     */
    private void cargarEstadisticas() {
        // TODO: Implementar con DAOs reales
        lblVentasHoy.setText("$ 0.00");
        lblProductosStock.setText("0");
        lblCreditosActivos.setText("0");
    }

    /**
     * Actualiza el mensaje de estado en la barra inferior
     */
    private void actualizarEstado(String mensaje) {
        lblEstado.setText(mensaje);
    }

    // ==================== MENÚ ENTIDADES ====================

    @FXML
    private void abrirCategorias() {
        actualizarEstado("Abriendo gestión de categorías...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Cateogrias.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestión de Categorías");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir gestión de categorías: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirProductos() {
        actualizarEstado("Abriendo gestión de productos...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Productos.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestión de Productos");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir gestión de productos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirClientes() {
        actualizarEstado("Abriendo gestión de clientes...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Clientes.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestión de Clientes");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir gestión de clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirInventario() {
        actualizarEstado("Abriendo gestión de inventario...");
        // TODO: Implementar
        mostrarMensaje("En desarrollo", "Módulo de Inventario en construcción");
    }

    @FXML
    private void abrirUsuarios() {
        if (!SessionManager.tienePermiso("USUARIOS")) {
            mostrarError("No tiene permisos para acceder a la gestión de usuarios");
            return;
        }
        actualizarEstado("Abriendo gestión de usuarios...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Usuarios.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestión de Usuarios (Solo Administrador)");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir gestión de usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== MENÚ TRANSACCIONES ====================

    @FXML
    private void nuevaVenta() {
        actualizarEstado("Iniciando nueva venta...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/NuevaVenta.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nueva Venta");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir nueva venta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Abre la interfaz de nueva compra para clientes
     */
    private void nuevaCompra() {
        actualizarEstado("Iniciando nueva compra...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/NuevaCompra.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nueva Compra - " + SessionManager.getUsuarioActual().getNombreCompleto());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

            // Si es cliente, configurar el comportamiento especial
            if (SessionManager.esCliente()) {
                // Usar Platform.runLater para asegurar que la ventana principal esté completamente cargada
                javafx.application.Platform.runLater(() -> {
                    try {
                        Stage mainStage = (Stage) lblUsuarioActual.getScene().getWindow();

                        // Al cerrar la ventana de compra, también cerrar la principal
                        stage.setOnCloseRequest(event -> {
                            mainStage.close();
                        });

                        // Ocultar la ventana principal mientras el cliente compra
                        mainStage.hide();
                    } catch (Exception e) {
                        System.err.println("Error al ocultar ventana principal: " + e.getMessage());
                    }
                });
            }

            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir nueva compra: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirVentas() {
        actualizarEstado("Abriendo gestión de ventas...");
        // TODO: Implementar
        mostrarMensaje("En desarrollo", "Módulo de Ventas en construcción");
    }

    @FXML
    private void abrirCreditos() {
        actualizarEstado("Abriendo gestión de créditos...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionCreditos.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Créditos");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir gestión de créditos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirCuotas() {
        actualizarEstado("Abriendo gestión de cuotas...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/PagarCuotas.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Cuotas - Pagos");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir gestión de cuotas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== CONSULTAS ====================

    @FXML
    private void consultaProductosCategoria() {
        actualizarEstado("Ejecutando consulta: Productos por Categoría");
        mostrarMensaje("Consulta", "Productos por Categoría - En desarrollo");
    }

    @FXML
    private void consultaClientesCredito() {
        actualizarEstado("Ejecutando consulta: Clientes con Crédito Activo");
        mostrarMensaje("Consulta", "Clientes con Crédito Activo - En desarrollo");
    }

    @FXML
    private void consultaVentasMes() {
        actualizarEstado("Ejecutando consulta: Ventas por Mes");
        mostrarMensaje("Consulta", "Ventas por Mes - En desarrollo");
    }

    @FXML
    private void consultaProductosBajoStock() {
        actualizarEstado("Ejecutando consulta: Productos con Bajo Stock");
        mostrarMensaje("Consulta", "Productos con Bajo Stock - En desarrollo");
    }

    @FXML
    private void consultaClientesMorosos() {
        actualizarEstado("Ejecutando consulta: Clientes Morosos");
        mostrarMensaje("Consulta", "Clientes Morosos - En desarrollo");
    }

    // ==================== REPORTES ====================

    @FXML
    private void reporteFacturaVenta() {
        actualizarEstado("Generando reporte: Factura de Venta");
        mostrarMensaje("Reporte", "Factura de Venta - En desarrollo");
    }

    @FXML
    private void reporteVentasMes() {
        actualizarEstado("Generando reporte: Ventas del Mes");
        mostrarMensaje("Reporte", "Ventas del Mes - En desarrollo");
    }

    @FXML
    private void reporteIVATrimestre() {
        actualizarEstado("Generando reporte: IVA por Trimestre");
        mostrarMensaje("Reporte", "IVA por Trimestre - En desarrollo");
    }

    @FXML
    private void reporteInventarioCategoria() {
        actualizarEstado("Generando reporte: Inventario por Categoría");
        mostrarMensaje("Reporte", "Inventario por Categoría - En desarrollo");
    }

    @FXML
    private void reporteClientesMorosos() {
        actualizarEstado("Generando reporte: Clientes Morosos");
        mostrarMensaje("Reporte", "Clientes Morosos - En desarrollo");
    }

    // ==================== UTILIDADES ====================

    @FXML
    private void abrirCalculadora() {
        actualizarEstado("Abriendo calculadora...");
        mostrarMensaje("Utilidad", "Calculadora - En desarrollo");
    }

    @FXML
    private void abrirCalendario() {
        actualizarEstado("Abriendo calendario...");
        mostrarMensaje("Utilidad", "Calendario - En desarrollo");
    }

    @FXML
    private void abrirBitacora() {
        if (!SessionManager.tienePermiso("BITACORA")) {
            mostrarError("No tiene permisos para acceder a la bitácora");
            return;
        }
        actualizarEstado("Abriendo bitácora de auditoría...");
        mostrarMensaje("Auditoría", "Bitácora - En desarrollo");
    }

    // ==================== BÚSQUEDAS ====================

    @FXML
    private void buscarCliente() {
        actualizarEstado("Buscando cliente...");
        mostrarMensaje("Búsqueda", "Buscar Cliente - En desarrollo");
    }

    @FXML
    private void buscarProducto() {
        actualizarEstado("Buscando producto...");
        mostrarMensaje("Búsqueda", "Buscar Producto - En desarrollo");
    }

    // ==================== AYUDA ====================

    @FXML
    private void abrirAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Sistema de Gestión de Electrodomésticos");
        alert.setContentText(
            "Versión: 1.0\n" +
            "Desarrollado por: Grupo de Bases de Datos I\n" +
            "Universidad del Quindío\n" +
            "Año: 2025\n\n" +
            "Sistema de gestión para tienda de electrodomésticos\n" +
            "con soporte para ventas a crédito y de contado."
        );
        alert.showAndWait();
    }

    @FXML
    private void abrirManual() {
        mostrarMensaje("Manual", "Manual de Usuario - En desarrollo");
    }

    // ==================== CERRAR SESIÓN ====================

    @FXML
    private void cerrarSesion() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar Sesión");
        confirmacion.setHeaderText("¿Está seguro que desea cerrar sesión?");
        confirmacion.setContentText("Se cerrará su sesión actual");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Registrar el cierre de sesión en auditoría
            registrarCierreSesion();
            
            // Cerrar la sesión
            SessionManager.cerrarSesion();
            
            // Detener el reloj
            if (relojTimeline != null) {
                relojTimeline.stop();
            }
            
            // Volver a la pantalla de login
            volverALogin();
        }
    }

    /**
     * Registra el cierre de sesión en la auditoría
     */
    private void registrarCierreSesion() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            Auditoria auditoria = new Auditoria(
                usuarioActual.getIdUsuario(),
                "LOGOUT",
                "Usuario",
                "Cierre de sesión de " + usuarioActual.getUsername(),
                ip
            );
            auditoria.setNombreUsuario(usuarioActual.getNombreCompleto());
            auditoriaDAO.agregar(auditoria);
        } catch (Exception e) {
            System.err.println("Error al registrar cierre de sesión: " + e.getMessage());
        }
    }

    /**
     * Vuelve a la pantalla de login
     */
    private void volverALogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Sistema de Electrodomésticos - Login");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Cerrar ventana actual
            Stage currentStage = (Stage) lblUsuarioActual.getScene().getWindow();
            currentStage.close();
            
        } catch (IOException e) {
            mostrarError("Error al volver al login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Muestra un mensaje de información
     */
    private void mostrarMensaje(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error de permisos");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
