package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.dao.VentaDAO;
import com.taller.proyecto_bd.dao.CuotaDAO;
import com.taller.proyecto_bd.models.Cliente;
import com.taller.proyecto_bd.models.Cuota;
import com.taller.proyecto_bd.models.Venta;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la lógica de créditos.
 * Maneja operaciones relacionadas con ventas a crédito y sus cuotas.
 *
 * NOTA IMPORTANTE:
 * En este sistema, los créditos están manejados directamente por las ventas.
 * Una venta con esCredito=true ES un crédito, y las cuotas están asociadas a esa venta.
 * No existe una tabla "Creditos" separada.
 *
 * Reglas de negocio:
 * - Cuota inicial: 30% del total
 * - Saldo financiado: 70% del total
 * - Interés: 5% sobre el saldo financiado
 * - Plazos: 12, 18 o 24 meses
 * - Un cliente solo puede tener UNA venta a crédito activa
 *
 * @author Sistema
 * @version 2.0 - Adaptado al nuevo diseño sin tabla Creditos
 */
public class CreditoController {
    private final CuotaDAO cuotaDAO = CuotaDAO.getInstance();
    private final VentaDAO ventaDAO = VentaDAO.getInstance();
    private final ClienteDAO clienteDAO = ClienteDAO.getInstance();

    private static final String ESTADO_REGISTRADA = "REGISTRADA";
    private static final String ESTADO_PAGADA = "PAGADA";

    /**
     * Pagar una cuota específica de una venta a crédito
     *
     * @param idVenta ID de la venta a crédito
     * @param numeroCuota Número de la cuota a pagar (1, 2, 3...)
     * @param monto Monto pagado
     * @return true si el pago fue exitoso
     */
    public boolean pagarCuota(int idVenta, int numeroCuota, double monto) {
        // Validar que la venta existe y es a crédito
        Venta venta = ventaDAO.obtenerPorId(idVenta);
        if (venta == null || !venta.isEsCredito()) {
            return false;
        }

        // Buscar la cuota específica
        Optional<Cuota> optCuota = cuotaDAO.obtenerPorVenta(idVenta)
                .stream()
                .filter(c -> c.getNumeroCuota() == numeroCuota)
                .findFirst();

        if (optCuota.isEmpty()) {
            return false;
        }

        Cuota cuota = optCuota.get();

        // Validar que la cuota no esté ya pagada y el monto sea suficiente
        if (cuota.isPagada() || monto < cuota.getValor()) {
            return false;
        }

        // Registrar el pago de la cuota
        boolean pagoExitoso = cuotaDAO.registrarPago(cuota.getIdCuota(), new Date());

        if (pagoExitoso) {
            // Verificar si todas las cuotas están pagadas
            boolean todasPagadas = cuotaDAO.obtenerPendientesPorVenta(idVenta).isEmpty();

            if (todasPagadas) {
                // Marcar la venta como PAGADA
                venta.setEstado(ESTADO_PAGADA);
                ventaDAO.actualizar(venta);

                // Actualizar saldo del cliente a 0
                Cliente cliente = clienteDAO.obtenerPorId(venta.getIdCliente());
                if (cliente != null) {
                    double montoFinanciado = venta.getMontoFinanciado();
                    double nuevoSaldo = cliente.getSaldoPendiente() - montoFinanciado;
                    cliente.setSaldoPendiente(Math.max(nuevoSaldo, 0));
                    clienteDAO.actualizar(cliente);
                }
            }
        }

        return pagoExitoso;
    }

    /**
     * Obtener todas las cuotas pendientes de un cliente
     *
     * @param idCliente ID del cliente
     * @return Lista de cuotas pendientes
     */
    public List<Cuota> obtenerCuotasPendientesCliente(int idCliente) {
        // Obtener todas las ventas a crédito del cliente
        List<Venta> ventasCredito = ventaDAO.obtenerPorCliente(idCliente)
                .stream()
                .filter(Venta::isEsCredito)
                .filter(v -> ESTADO_REGISTRADA.equals(v.getEstado()))
                .toList();

        // Obtener todas las cuotas pendientes de esas ventas
        return ventasCredito.stream()
                .flatMap(v -> cuotaDAO.obtenerPendientesPorVenta(v.getIdVenta()).stream())
                .toList();
    }

    /**
     * Obtener cuotas vencidas (morosas) de un cliente
     *
     * @param idCliente ID del cliente
     * @return Lista de cuotas vencidas
     */
    public List<Cuota> obtenerCuotasVencidasCliente(int idCliente) {
        return obtenerCuotasPendientesCliente(idCliente)
                .stream()
                .filter(Cuota::estaVencida)
                .toList();
    }

    /**
     * Verificar si un cliente tiene un crédito activo
     * (usado para validar si puede solicitar otro crédito)
     *
     * @param idCliente ID del cliente
     * @return true si el cliente tiene un crédito activo
     */
    public boolean clienteTieneCreditoActivo(int idCliente) {
        List<Venta> ventasCredito = ventaDAO.obtenerPorCliente(idCliente)
                .stream()
                .filter(Venta::isEsCredito)
                .filter(v -> ESTADO_REGISTRADA.equals(v.getEstado()))
                .toList();

        // Verificar si alguna de esas ventas tiene cuotas pendientes
        return ventasCredito.stream()
                .anyMatch(v -> !cuotaDAO.obtenerPendientesPorVenta(v.getIdVenta()).isEmpty());
    }

    /**
     * Obtener el saldo total pendiente de un cliente
     * (suma de todas las cuotas pendientes)
     *
     * @param idCliente ID del cliente
     * @return Saldo total pendiente
     */
    public double obtenerSaldoPendienteCliente(int idCliente) {
        return obtenerCuotasPendientesCliente(idCliente)
                .stream()
                .mapToDouble(Cuota::getValor)
                .sum();
    }

    /**
     * Obtener todas las ventas a crédito activas de un cliente
     *
     * @param idCliente ID del cliente
     * @return Lista de ventas a crédito activas
     */
    public List<Venta> obtenerCreditosActivosCliente(int idCliente) {
        return ventaDAO.obtenerPorCliente(idCliente)
                .stream()
                .filter(Venta::isEsCredito)
                .filter(v -> ESTADO_REGISTRADA.equals(v.getEstado()))
                .toList();
    }

    /**
     * Obtener el progreso de pago de una venta a crédito
     *
     * @param idVenta ID de la venta
     * @return Porcentaje de cuotas pagadas (0-100)
     */
    public double obtenerProgresoPago(int idVenta) {
        List<Cuota> todasCuotas = cuotaDAO.obtenerPorVenta(idVenta);
        if (todasCuotas.isEmpty()) {
            return 0.0;
        }

        long cuotasPagadas = todasCuotas.stream()
                .filter(Cuota::isPagada)
                .count();

        return (cuotasPagadas * 100.0) / todasCuotas.size();
    }

    /**
     * Verifica si un cliente está moroso (tiene cuotas vencidas)
     *
     * @param idCliente ID del cliente
     * @return true si el cliente tiene cuotas vencidas
     */
    public boolean clienteEsMoroso(int idCliente) {
        return !obtenerCuotasVencidasCliente(idCliente).isEmpty();
    }

    /**
     * Obtener el total de días de atraso acumulados de un cliente
     *
     * @param idCliente ID del cliente
     * @return Total de días de atraso
     */
    public long obtenerDiasAtrasoCliente(int idCliente) {
        return obtenerCuotasVencidasCliente(idCliente)
                .stream()
                .mapToLong(Cuota::diasAtraso)
                .sum();
    }
}
