package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.models.*;

import java.util.List;

/**
 * Servicio de validaciones de negocio.
 * Encapsula reglas y chequeos usados en diferentes procesos.
 *
 * @author Sistema
 * @version 1.0
 */
public class ValidacionService {

    // ==================== USUARIO ====================

    /** Verifica que el usuario esté activo */
    public boolean usuarioActivo(Usuario usuario) {
        return usuario != null && usuario.isActivo();
    }

    /** Verifica credenciales mínimas */
    public boolean credencialesValidas(String username, String password) {
        return username != null && !username.trim().isEmpty()
                && password != null && password.length() >= 4;
    }

    // ==================== PRODUCTO ====================

    /** Verifica que el producto esté disponible para la venta */
    public boolean productoValido(Producto producto) {
        return producto != null && producto.getPrecioVenta() > 0;
    }

    /** Verifica que haya stock suficiente */
    public boolean stockSuficiente(Inventario inventario, int cantidad) {
        return inventario != null && cantidad > 0 && inventario.getCantidadActual() >= cantidad;
    }

    // ==================== VENTA ====================

    /** Verifica que una lista de productos no esté vacía */
    public boolean productosParaVenta(List<Producto> productos) {
        return productos != null && !productos.isEmpty();
    }

    /** Verifica que un cliente es válido */
    public boolean clienteValido(Cliente cliente) {
        return cliente != null && cliente.getIdCliente() > 0;
    }

    // ==================== CRÉDITO ====================

    /** Verifica que los datos de un crédito sean correctos */
    public boolean creditoValido(Credito credito) {
        return credito != null
                && credito.getMontoTotal() > 0
                && credito.getPlazoMeses() > 0
                && credito.getInteres() >= 0;
    }

    /** Verifica que la cuota inicial sea menor al monto total */
    public boolean cuotaInicialCorrecta(double montoTotal, double cuotaInicial) {
        return cuotaInicial >= 0 && cuotaInicial < montoTotal;
    }

    // ==================== INVENTARIO ====================

    /** Verifica que el stock esté dentro de los rangos establecidos */
    public boolean stockEnRango(Inventario inventario) {
        if (inventario == null) return false;
        return inventario.getCantidadActual() >= 0
                && inventario.getCantidadActual() <= inventario.getStockMaximo();
    }
}
