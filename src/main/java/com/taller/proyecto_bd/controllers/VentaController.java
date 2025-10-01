package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;
import com.taller.proyecto_bd.services.VentaService;
import com.taller.proyecto_bd.utils.Constantes;

import java.util.List;

/**
 * Controlador de la lógica de negocio para las ventas.
 * Orquesta la interacción entre Venta, Producto, Cliente e Inventario.
 */
public class VentaController {

    private VentaDAO ventaDAO = VentaDAO.getInstance();
    private ProductoDAO productoDAO = ProductoDAO.getInstance();
    private ClienteDAO clienteDAO = ClienteDAO.getInstance();
    private CreditoDAO creditoDAO = CreditoDAO.getInstance();
    private DetalleVentaDAO detalleDAO = DetalleVentaDAO.getInstance();
    private UsuarioDAO usuarioDAO = UsuarioDAO.getInstance();

    private VentaService ventaService = new VentaService();

    /**
     * Realizar una venta
     *
     * @param idCliente   cliente que compra
     * @param idUsuario   vendedor que registra
     * @param detalles    lista de productos con cantidades
     * @param esCredito   true si es a crédito, false si es contado
     * @param cuotaInicial monto de cuota inicial si es crédito
     * @param plazoMeses  número de meses si es crédito
     */
    public boolean realizarVenta(int idCliente, int idUsuario,
                                 List<DetalleVenta> detalles,
                                 boolean esCredito, double cuotaInicial, int plazoMeses) {

        Cliente cliente = clienteDAO.obtenerPorId(idCliente);
        Usuario vendedor = usuarioDAO.obtenerPorId(idUsuario);

        if (cliente == null || vendedor == null || detalles == null || detalles.isEmpty()) {
            System.out.println("⚠ Datos inválidos para realizar la venta.");
            return false;
        }

        // Delegar toda la lógica al servicio
        return ventaService.realizarVenta(cliente, vendedor, detalles, esCredito,
                cuotaInicial, plazoMeses, Constantes.INTERES_DEFAULT);
    }

    /** Obtener todas las ventas */
    public List<Venta> obtenerVentas() {
        return ventaDAO.obtenerTodas();
    }

    /** Obtener ventas de un cliente */
    public List<Venta> obtenerVentasPorCliente(int idCliente) {
        return ventaDAO.obtenerPorCliente(idCliente);
    }

    /** Anular una venta */
    public boolean anularVenta(int idVenta) {
        // Asumimos usuario admin con ID 1 por defecto
        Usuario usuarioAnulador = usuarioDAO.obtenerPorId(1);

        if (usuarioAnulador == null) {
            System.out.println("⚠ No se pudo obtener el usuario para la auditoría de anulación.");
            return false;
        }

        return ventaService.anularVenta(idVenta, usuarioAnulador);
    }
}
