package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.CreditoDAO;
import com.taller.proyecto_bd.dao.VentaDAO;
import com.taller.proyecto_bd.dao.CuotaDAO;
import com.taller.proyecto_bd.models.Credito;
import com.taller.proyecto_bd.models.Cuota;
import com.taller.proyecto_bd.models.Venta;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la lógica de créditos.
 * Aplica reglas de negocio: cuotas iniciales, intereses, morosidad.
 *
 * @author Sistema
 * @version 1.6 - Validaciones reforzadas y mejoras en métodos
 */
public class CreditoController {
    private final CreditoDAO creditoDAO = CreditoDAO.getInstance();
    private final CuotaDAO cuotaDAO = CuotaDAO.getInstance();
    private final VentaDAO ventaDAO = VentaDAO.getInstance();

    /**
     * Crear un crédito a partir de una venta
     */
    public Credito generarCredito(Venta venta, double cuotaInicial, int plazoMeses, double interes) {
        if (venta == null) throw new IllegalArgumentException("La venta no puede ser nula");
        if (plazoMeses <= 0) throw new IllegalArgumentException("El plazo debe ser mayor a 0");
        if (interes < 0) throw new IllegalArgumentException("El interés no puede ser negativo");
        if (cuotaInicial < 0 || cuotaInicial > venta.getTotal()) {
            throw new IllegalArgumentException("La cuota inicial no es válida");
        }

        if (clienteTieneCreditoActivo(venta.getIdCliente())) {
            throw new IllegalStateException("El cliente ya tiene un crédito activo.");
        }

        // Crear crédito
        Credito credito = new Credito(
                venta.getIdVenta(),
                venta.getIdCliente(),
                venta.getTotal(),
                cuotaInicial,
                plazoMeses,
                interes
        );
        credito.setEstado("ACTIVO");

        // Generar cuotas y persistir
        credito.generarCuotas();
        creditoDAO.agregar(credito);

        for (Cuota cuota : credito.getCuotas()) {
            cuota.setIdCredito(credito.getIdCredito());
            cuotaDAO.agregar(cuota);
        }

        return credito;
    }

    /**
     * Pagar una cuota específica
     */
    public boolean pagarCuota(int idCredito, int numeroCuota, double monto) {
        Credito credito = creditoDAO.obtenerPorId(idCredito);
        if (credito == null) return false;

        Optional<Cuota> optCuota = cuotaDAO.obtenerPorCredito(idCredito)
                .stream()
                .filter(c -> c.getNumeroCuota() == numeroCuota)
                .findFirst();

        if (optCuota.isEmpty()) return false;
        Cuota cuota = optCuota.get();

        if (cuota.isPagada() || monto < cuota.getValor()) return false;

        // Marcar pagada
        cuota.pagarCuota(new Date());
        cuotaDAO.actualizar(cuota);

        // Actualizar saldo
        double nuevoSaldo = credito.getSaldoPendiente() - cuota.getValor();
        credito.setSaldoPendiente(Math.max(nuevoSaldo, 0));

        // Verificar estado
        boolean todasPagadas = cuotaDAO.obtenerPorCredito(idCredito)
                .stream()
                .allMatch(Cuota::isPagada);

        credito.setEstado(todasPagadas ? "CANCELADO" : "ACTIVO");
        if (todasPagadas) credito.setSaldoPendiente(0);

        return creditoDAO.actualizar(credito);
    }

    /**
     * Obtener créditos morosos de un cliente
     */
    public List<Credito> obtenerMorososCliente(int idCliente) {
        return creditoDAO.obtenerPorCliente(idCliente)
                .stream()
                .filter(Credito::esMoroso)
                .toList();
    }

    /**
     * Cancelar un crédito cuando ya no tiene saldo pendiente
     */
    public boolean cancelarCredito(int idCredito) {
        Credito credito = creditoDAO.obtenerPorId(idCredito);
        if (credito == null) return false;

        boolean todasPagadas = cuotaDAO.obtenerPorCredito(idCredito)
                .stream()
                .allMatch(Cuota::isPagada);

        if (credito.tieneSaldoPendiente() || !todasPagadas) {
            return false;
        }

        credito.setEstado("CANCELADO");
        credito.setSaldoPendiente(0);
        return creditoDAO.actualizar(credito);
    }

    // ==== Métodos privados auxiliares ====

    private boolean clienteTieneCreditoActivo(int idCliente) {
        return creditoDAO.obtenerPorCliente(idCliente)
                .stream()
                .anyMatch(c -> c.tieneSaldoPendiente() && "ACTIVO".equalsIgnoreCase(c.getEstado()));
    }
}
