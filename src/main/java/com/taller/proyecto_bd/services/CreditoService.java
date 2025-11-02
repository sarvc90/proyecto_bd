package com.taller.proyecto_bd.services;

import com.taller.proyecto_bd.dao.*;
import com.taller.proyecto_bd.models.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión integral de Créditos (Ventas a Crédito).
 * Maneja registro, pagos, cancelaciones y estados.
 *
 * NOTA IMPORTANTE:
 * En este sistema, los créditos están manejados directamente por las ventas.
 * Una venta con esCredito=true ES un crédito.
 * Las cuotas están asociadas directamente a las ventas mediante idVenta.
 *
 * @author Sistema
 * @version 2.0 - Adaptado al nuevo diseño sin tabla Creditos
 */
public class CreditoService {
    // ==================== DEPENDENCIAS ====================
    private VentaDAO ventaDAO = VentaDAO.getInstance();
    private CreditoDAO creditoDAO = CreditoDAO.getInstance();
    private CuotaDAO cuotaDAO = CuotaDAO.getInstance();
    private ClienteDAO clienteDAO = ClienteDAO.getInstance();
    private AuditoriaDAO auditoriaDAO = AuditoriaDAO.getInstance();

    private static final String ESTADO_REGISTRADA = "REGISTRADA";
    private static final String ESTADO_PAGADA = "PAGADA";
    private static final String ESTADO_ANULADA = "ANULADA";

    // ==================== PROCESOS PRINCIPALES ====================

    /**
     * Registrar el pago de una cuota específica.
     *
     * @param idCuota ID de la cuota a pagar
     * @param usuario Usuario que registra el pago
     * @return true si el pago fue exitoso
     */
    public boolean pagarCuota(int idCuota, Usuario usuario) {
        Cuota cuota = cuotaDAO.obtenerPorId(idCuota);
        if (cuota == null || cuota.isPagada()) {
            return false;
        }

        // Obtener el crédito asociado
        Credito credito = creditoDAO.obtenerPorId(cuota.getIdCredito());
        if (credito == null) {
            return false;
        }

        // Obtener la venta asociada
        Venta venta = ventaDAO.obtenerPorId(credito.getIdVenta());
        if (venta == null || !venta.isEsCredito()) {
            return false;
        }

        // Marcar cuota como pagada
        boolean pagoExitoso = cuotaDAO.registrarPago(idCuota, new Date());

        if (pagoExitoso) {
            // Verificar si todas las cuotas están pagadas
            boolean todasPagadas = cuotaDAO.obtenerPendientesPorCredito(credito.getIdCredito()).isEmpty();

            if (todasPagadas) {
                // Marcar la venta como PAGADA
                venta.setEstado(ESTADO_PAGADA);
                ventaDAO.actualizar(venta);

                // Actualizar saldo del cliente
                Cliente cliente = clienteDAO.obtenerPorId(venta.getIdCliente());
                if (cliente != null) {
                    double montoFinanciado = venta.getMontoFinanciado();
                    double nuevoSaldo = cliente.getSaldoPendiente() - montoFinanciado;
                    cliente.setSaldoPendiente(Math.max(nuevoSaldo, 0));
                    clienteDAO.actualizar(cliente);
                }
            }

            // Registrar auditoría
            auditoriaDAO.agregar(new Auditoria(
                usuario.getIdUsuario(),
                "PAGO_CUOTA",
                "Cuota",
                String.format("Pago de cuota #%d de venta %s por $%.2f",
                    cuota.getNumeroCuota(), venta.getCodigo(), cuota.getValor()),
                "127.0.0.1"
            ));

            return true;
        }

        return false;
    }

    /**
     * Anular una venta a crédito (ej: si se anuló la venta).
     *
     * @param idVenta ID de la venta a anular
     * @param usuario Usuario que anula
     * @return true si se anuló correctamente
     */
    public boolean anularVentaCredito(int idVenta, Usuario usuario) {
        Venta venta = ventaDAO.obtenerPorId(idVenta);
        if (venta == null || !venta.isEsCredito()) {
            return false;
        }

        // Cambiar estado de la venta
        venta.setEstado(ESTADO_ANULADA);
        ventaDAO.actualizar(venta);

        // Actualizar saldo del cliente (restar el monto financiado)
        Cliente cliente = clienteDAO.obtenerPorId(venta.getIdCliente());
        if (cliente != null) {
            double montoFinanciado = venta.getMontoFinanciado();
            double nuevoSaldo = cliente.getSaldoPendiente() - montoFinanciado;
            cliente.setSaldoPendiente(Math.max(nuevoSaldo, 0));
            clienteDAO.actualizar(cliente);
        }

        // Registrar auditoría
        auditoriaDAO.agregar(new Auditoria(
            usuario.getIdUsuario(),
            "ANULAR_CREDITO",
            "Venta",
            String.format("Venta a crédito anulada: %s", venta.getCodigo()),
            "127.0.0.1"
        ));

        return true;
    }

    /**
     * Verificar morosidad de una venta a crédito.
     * Una venta es morosa si tiene cuotas vencidas no pagadas.
     *
     * @param idVenta ID de la venta
     * @return true si la venta tiene cuotas vencidas
     */
    public boolean verificarMorosidad(int idVenta) {
        Venta venta = ventaDAO.obtenerPorId(idVenta);
        if (venta == null || !venta.isEsCredito()) {
            return false;
        }

        // No verificar si ya está pagada o anulada
        if (ESTADO_PAGADA.equals(venta.getEstado()) || ESTADO_ANULADA.equals(venta.getEstado())) {
            return false;
        }

        // Obtener el crédito asociado a la venta
        Credito credito = creditoDAO.obtenerPorVenta(idVenta);
        if (credito == null) {
            return false;
        }

        // Verificar si tiene cuotas vencidas
        List<Cuota> cuotasVencidas = cuotaDAO.obtenerPorCredito(credito.getIdCredito())
                .stream()
                .filter(cuota -> !cuota.isPagada() && cuota.estaVencida())
                .collect(Collectors.toList());

        return !cuotasVencidas.isEmpty();
    }

    /**
     * Obtener el estado de morosidad de un cliente.
     *
     * @param idCliente ID del cliente
     * @return true si el cliente tiene alguna venta morosa
     */
    public boolean clienteEsMoroso(int idCliente) {
        List<Venta> ventasCredito = listarVentasCreditoActivas(idCliente);

        return ventasCredito.stream()
                .anyMatch(v -> verificarMorosidad(v.getIdVenta()));
    }

    /**
     * Obtener todas las cuotas vencidas de un cliente.
     *
     * @param idCliente ID del cliente
     * @return Lista de cuotas vencidas
     */
    public List<Cuota> obtenerCuotasVencidasCliente(int idCliente) {
        List<Venta> ventasCredito = listarVentasCreditoActivas(idCliente);

        return ventasCredito.stream()
                .flatMap(v -> {
                    Credito credito = creditoDAO.obtenerPorVenta(v.getIdVenta());
                    if (credito == null) {
                        return List.<Cuota>of().stream();
                    }
                    return cuotaDAO.obtenerPorCredito(credito.getIdCredito()).stream();
                })
                .filter(cuota -> !cuota.isPagada() && cuota.estaVencida())
                .collect(Collectors.toList());
    }

    // ==================== REPORTES ====================

    /**
     * Listar todas las ventas a crédito activas (estado REGISTRADA).
     *
     * @return Lista de ventas a crédito activas
     */
    public List<Venta> listarCreditosActivos() {
        return ventaDAO.obtenerPorCredito(true)
                .stream()
                .filter(v -> ESTADO_REGISTRADA.equals(v.getEstado()))
                .collect(Collectors.toList());
    }

    /**
     * Listar ventas a crédito morosas (tienen cuotas vencidas).
     *
     * @return Lista de ventas morosas
     */
    public List<Venta> listarCreditosMorosos() {
        return listarCreditosActivos()
                .stream()
                .filter(v -> verificarMorosidad(v.getIdVenta()))
                .collect(Collectors.toList());
    }

    /**
     * Listar ventas a crédito ya pagadas completamente.
     *
     * @return Lista de ventas pagadas
     */
    public List<Venta> listarCreditosPagados() {
        return ventaDAO.obtenerPorCredito(true)
                .stream()
                .filter(v -> ESTADO_PAGADA.equals(v.getEstado()))
                .collect(Collectors.toList());
    }

    /**
     * Listar ventas a crédito anuladas.
     *
     * @return Lista de ventas anuladas
     */
    public List<Venta> listarCreditosAnulados() {
        return ventaDAO.obtenerPorCredito(true)
                .stream()
                .filter(v -> ESTADO_ANULADA.equals(v.getEstado()))
                .collect(Collectors.toList());
    }

    /**
     * Listar ventas a crédito activas de un cliente específico.
     *
     * @param idCliente ID del cliente
     * @return Lista de ventas a crédito activas del cliente
     */
    public List<Venta> listarVentasCreditoActivas(int idCliente) {
        return ventaDAO.obtenerPorCliente(idCliente)
                .stream()
                .filter(Venta::isEsCredito)
                .filter(v -> ESTADO_REGISTRADA.equals(v.getEstado()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener resumen de crédito de un cliente.
     *
     * @param idCliente ID del cliente
     * @return Resumen con información del crédito
     */
    public ResumenCredito obtenerResumenCliente(int idCliente) {
        List<Venta> ventasCredito = listarVentasCreditoActivas(idCliente);

        double totalFinanciado = ventasCredito.stream()
                .mapToDouble(Venta::getMontoFinanciado)
                .sum();

        List<Cuota> cuotasPendientes = ventasCredito.stream()
                .flatMap(v -> {
                    Credito credito = creditoDAO.obtenerPorVenta(v.getIdVenta());
                    if (credito == null) {
                        return List.<Cuota>of().stream();
                    }
                    return cuotaDAO.obtenerPendientesPorCredito(credito.getIdCredito()).stream();
                })
                .collect(Collectors.toList());

        double montoPendiente = cuotasPendientes.stream()
                .mapToDouble(Cuota::getValor)
                .sum();

        List<Cuota> cuotasVencidas = cuotasPendientes.stream()
                .filter(Cuota::estaVencida)
                .collect(Collectors.toList());

        return new ResumenCredito(
            ventasCredito.size(),
            totalFinanciado,
            montoPendiente,
            cuotasPendientes.size(),
            cuotasVencidas.size()
        );
    }

    // ==================== CLASE INTERNA PARA RESUMEN ====================

    /**
     * Clase interna para representar el resumen de crédito de un cliente.
     */
    public static class ResumenCredito {
        private int cantidadCreditos;
        private double montoTotalFinanciado;
        private double montoPendiente;
        private int cuotasPendientes;
        private int cuotasVencidas;

        public ResumenCredito(int cantidadCreditos, double montoTotalFinanciado,
                            double montoPendiente, int cuotasPendientes, int cuotasVencidas) {
            this.cantidadCreditos = cantidadCreditos;
            this.montoTotalFinanciado = montoTotalFinanciado;
            this.montoPendiente = montoPendiente;
            this.cuotasPendientes = cuotasPendientes;
            this.cuotasVencidas = cuotasVencidas;
        }

        // Getters
        public int getCantidadCreditos() { return cantidadCreditos; }
        public double getMontoTotalFinanciado() { return montoTotalFinanciado; }
        public double getMontoPendiente() { return montoPendiente; }
        public int getCuotasPendientes() { return cuotasPendientes; }
        public int getCuotasVencidas() { return cuotasVencidas; }
        public boolean esMoroso() { return cuotasVencidas > 0; }
    }
}
