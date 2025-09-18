package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.CreditoDAO;
import com.taller.proyecto_bd.dao.VentaDAO;
import com.taller.proyecto_bd.dao.CuotaDAO;
import com.taller.proyecto_bd.models.Credito;
import com.taller.proyecto_bd.models.Cuota;
import com.taller.proyecto_bd.models.Venta;

import java.util.Date;
import java.util.List;

/**
 * Controlador para la lógica de créditos.
 * Aplica reglas de negocio: cuotas iniciales, intereses, morosidad.
 *
 * @author Sistema
 * @version 1.2
 */
public class CreditoController {
    private CreditoDAO creditoDAO = new CreditoDAO();
    private CuotaDAO cuotaDAO = new CuotaDAO();
    private VentaDAO ventaDAO = new VentaDAO();

    /**
     * Crear un crédito a partir de una venta
     */
    public Credito generarCredito(Venta venta, double cuotaInicial, int plazoMeses, double interes) {
        // Validación: el cliente no debe tener otro crédito pendiente
        List<Credito> creditosCliente = creditoDAO.obtenerPorCliente(venta.getIdCliente());
        for (Credito c : creditosCliente) {
            if (c.tieneSaldoPendiente()) {
                throw new IllegalStateException("El cliente ya tiene un crédito activo.");
            }
        }

        // Crear crédito con el constructor correcto
        Credito credito = new Credito(
                venta.getIdVenta(),
                venta.getIdCliente(),
                venta.getTotal(),
                cuotaInicial,
                plazoMeses,
                interes
        );

        // Generar cuotas
        credito.generarCuotas();
        creditoDAO.agregar(credito);

        // Guardar cuotas en el DAO
        for (Cuota cuota : credito.getCuotas()) {
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

        // Buscar la cuota dentro de las del crédito
        Cuota cuota = cuotaDAO.obtenerPorCredito(idCredito)
                .stream()
                .filter(c -> c.getNumeroCuota() == numeroCuota)
                .findFirst()
                .orElse(null);

        if (cuota == null || cuota.isPagada()) return false;

        // Marcar la cuota como pagada
        cuota.pagarCuota(new Date());
        cuotaDAO.actualizar(cuota);

        // Reducir el saldo pendiente del crédito
        credito.pagarCuota(monto);
        creditoDAO.actualizar(credito);

        return true;
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
        if (credito == null || credito.tieneSaldoPendiente()) return false;

        credito.setEstado("CANCELADO");
        return creditoDAO.actualizar(credito);
    }
}
