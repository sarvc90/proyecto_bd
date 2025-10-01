package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;
import com.taller.proyecto_bd.utils.Constantes; // Importar Constantes

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Servicio para gestionar el proceso completo de una Venta.
 * Coordina DAOs, controladores y lógica de negocio asociada.
 *
 * @author Sistema
 * @version 1.1 - Corregidos problemas de IDs y validaciones
 */
public class VentaService {
    // ==================== DEPENDENCIAS ====================
    private VentaDAO ventaDAO = VentaDAO.getInstance();
    private DetalleVentaDAO detalleVentaDAO = DetalleVentaDAO.getInstance();
    private ProductoDAO productoDAO = ProductoDAO.getInstance();
    private InventarioDAO inventarioDAO = InventarioDAO.getInstance();
    private CreditoDAO creditoDAO = CreditoDAO.getInstance();
    private CuotaDAO cuotaDAO = CuotaDAO.getInstance();
    private AuditoriaDAO auditoriaDAO = AuditoriaDAO.getInstance();


    // ==================== PROCESOS PRINCIPALES ====================

    /**
     * Realiza una venta en el sistema (contado o crédito).
     */
    public boolean realizarVenta(Cliente cliente, Usuario vendedor,
                                 List<DetalleVenta> detalles, boolean esCredito,
                                 double cuotaInicial, int plazoMeses, double interes) {

        // 1. Validar datos básicos
        if (cliente == null || vendedor == null || detalles == null || detalles.isEmpty()) {
            System.out.println(Constantes.MSG_ERROR_GENERAL + ": Datos básicos inválidos para la venta.");
            return false;
        }

        // Dentro de realizarVenta(), reemplaza el bloque de validación de stock:

// 2. Validar stock
        for (DetalleVenta d : detalles) {
            Producto producto = productoDAO.obtenerPorId(d.getIdProducto());
            if (producto == null) {
                System.out.println(Constantes.MSG_ERROR_GENERAL + ": Producto no encontrado ID: " + d.getIdProducto());
                return false;
            }

            Inventario inv = inventarioDAO.obtenerPorProducto(d.getIdProducto());
            if (inv == null || inv.getCantidadActual() < d.getCantidad()) {
                System.out.println(Constantes.MSG_ERROR_GENERAL + ": Stock insuficiente para producto: " + producto.getNombre());
                return false;
            }
        }


        // 3. Calcular totales
        double subtotal = 0;
        double ivaTotal = 0;

        for (DetalleVenta d : detalles) {
            // Usar la constante de IVA
            d.calcularTotales(Constantes.IVA_DEFAULT);
            subtotal += d.getSubtotal();
            ivaTotal += d.getMontoIVA();
        }

        double total = subtotal + ivaTotal;

        // 4. Validar crédito si aplica
        if (esCredito) {
            if (cuotaInicial < 0 || cuotaInicial > total) {
                System.out.println(Constantes.MSG_ERROR_GENERAL + ": Cuota inicial inválida.");
                return false;
            }
            if (plazoMeses <= 0) {
                System.out.println(Constantes.MSG_ERROR_GENERAL + ": Plazo de meses inválido.");
                return false;
            }
        }

        // 5. Crear objeto Venta
        // El ID se generará en el DAO, el código puede ser temporal
        Venta venta = new Venta();
        venta.setCodigo("V-" + System.currentTimeMillis()); // Código temporal, el DAO asignará el ID
        venta.setIdCliente(cliente.getIdCliente());
        venta.setIdUsuario(vendedor.getIdUsuario());
        venta.setFechaVenta(new Date());
        venta.setEsCredito(esCredito);
        venta.setDetalles(new ArrayList<>(detalles)); // Asignar detalles para calcular totales
        venta.setSubtotal(subtotal);
        venta.setIvaTotal(ivaTotal);
        venta.setTotal(total);
        venta.setCuotaInicial(esCredito ? cuotaInicial : 0);
        venta.setPlazoMeses(esCredito ? plazoMeses : 0);
        venta.setEstado(Constantes.VENTA_REGISTRADA);

        // 6. Guardar venta (el DAO asigna el ID automáticamente)
        if (!ventaDAO.agregar(venta)) {
            System.out.println(Constantes.MSG_ERROR_GENERAL + ": Error al guardar la venta.");
            return false;
        }

        // VERIFICAR que se asignó el ID (importante para los detalles y el crédito)
        if (venta.getIdVenta() == 0) {
            System.out.println(Constantes.MSG_ERROR_GENERAL + ": No se asignó ID a la venta después de agregarla.");
            return false;
        }

        // 7. Guardar detalles de venta y actualizar inventario
        for (DetalleVenta d : detalles) {
            d.setIdVenta(venta.getIdVenta()); // Asignar el ID de la venta recién creada
            if (!detalleVentaDAO.agregar(d)) {
                System.out.println(Constantes.MSG_ERROR_GENERAL + ": Error al guardar detalle de venta.");
                // Considerar revertir la venta si esto falla (requiere transacciones)
                return false;
            }

            // Actualizar stock en Producto
            Producto producto = productoDAO.obtenerPorId(d.getIdProducto());
            if (producto != null) {
                producto.setStockActual(producto.getStockActual() - d.getCantidad());
                productoDAO.actualizar(producto);
            }

            // Actualizar stock en Inventario
            Inventario inv = inventarioDAO.obtenerPorProducto(d.getIdProducto());
            if (inv != null) {
                inv.registrarSalida(d.getCantidad()); // El DAO ya actualiza el objeto en su lista
                // inventarioDAO.actualizar(inv); // No es necesario llamar explícitamente si registrarSalida ya actualiza el objeto en la lista
            }
        }

        // 8. Si es crédito -> generar crédito + cuotas
        if (esCredito) {
            try {
                double montoFinanciado = total - cuotaInicial;
                Credito credito = new Credito(venta.getIdVenta(), cliente.getIdCliente(),
                        montoFinanciado, cuotaInicial, plazoMeses, interes);
                credito.generarCuotas();

                if (!creditoDAO.agregar(credito)) {
                    System.out.println(Constantes.MSG_ERROR_GENERAL + ": Error al guardar crédito.");
                    // Considerar revertir la venta si esto falla (requiere transacciones)
                    return false;
                }

                // Guardar cuotas
                for (Cuota c : credito.getCuotas()) {
                    c.setIdCredito(credito.getIdCredito()); // Asignar el ID del crédito recién creado
                    cuotaDAO.agregar(c);
                }
            } catch (Exception e) {
                System.out.println(Constantes.MSG_ERROR_GENERAL + ": Error al generar crédito: " + e.getMessage());
                e.printStackTrace();
                // Considerar revertir la venta si esto falla (requiere transacciones)
                return false;
            }
        }

        // 9. Registrar en auditoría
        auditoriaDAO.agregar(new Auditoria(vendedor.getIdUsuario(), "CREAR_VENTA",
                "Venta", "Venta registrada ID=" + venta.getIdVenta(), "127.0.0.1"));

        System.out.println("✓ Venta registrada con éxito: " + venta.getResumen());
        return true;
    }

    /**
     * Anular una venta (y su crédito si aplica).
     */
    public boolean anularVenta(int idVenta, Usuario usuario) {
        Venta venta = ventaDAO.obtenerPorId(idVenta);
        if (venta == null) {
            System.out.println(Constantes.MSG_VENTA_NO_ENCONTRADA);
            return false;
        }
        if (Constantes.VENTA_ANULADA.equals(venta.getEstado())) {
            System.out.println("⚠ La venta " + venta.getCodigo() + " ya está anulada.");
            return false;
        }

        // Devolver inventario
        List<DetalleVenta> detalles = detalleVentaDAO.obtenerPorVenta(idVenta);
        for (DetalleVenta d : detalles) {
            // Actualizar stock en Producto
            Producto producto = productoDAO.obtenerPorId(d.getIdProducto());
            if (producto != null) {
                producto.setStockActual(producto.getStockActual() + d.getCantidad());
                productoDAO.actualizar(producto);
            }

            // Actualizar stock en Inventario
            Inventario inv = inventarioDAO.obtenerPorProducto(d.getIdProducto());
            if (inv != null) {
                inv.registrarEntrada(d.getCantidad()); // El DAO ya actualiza el objeto en su lista
                // inventarioDAO.actualizar(inv); // No es necesario llamar explícitamente si registrarEntrada ya actualiza el objeto en la lista
            }
        }

        // Anular crédito asociado
        Credito credito = creditoDAO.obtenerPorVenta(idVenta);
        if (credito != null) {
            credito.setEstado(Constantes.CREDITO_CANCELADO); // O "ANULADO" si se define ese estado
            creditoDAO.actualizar(credito);
        }

        // Cambiar estado de venta
        venta.setEstado(Constantes.VENTA_ANULADA);
        boolean resultado = ventaDAO.actualizar(venta);

        // Registrar auditoría
        if (resultado) {
            auditoriaDAO.agregar(new Auditoria(usuario.getIdUsuario(), "ANULAR_VENTA",
                    "Venta", "Venta anulada ID=" + idVenta, "127.0.0.1"));
            System.out.println("✓ Venta " + venta.getCodigo() + " anulada con éxito.");
        } else {
            System.out.println(Constantes.MSG_ERROR_GENERAL + ": Error al actualizar el estado de la venta.");
        }

        return resultado;
    }
}