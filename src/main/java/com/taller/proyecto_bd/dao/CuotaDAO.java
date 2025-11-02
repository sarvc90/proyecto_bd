package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Cuota;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Cuota.
 * Maneja operaciones CRUD sobre cuotas de crédito en la base de datos.
 *
 * NOTA: Según requisitos, los clientes a crédito:
 * - Dan cuota inicial del 30%
 * - El 70% restante se paga en 12, 18 o 24 cuotas mensuales
 * - Este 70% se incrementa en 5% por concepto de intereses
 *
 * @author Sistema
 * @version 2.0
 */
public class CuotaDAO {
    private static CuotaDAO instance;

    private CuotaDAO() {
    }

    public static synchronized CuotaDAO getInstance() {
        if (instance == null) {
            instance = new CuotaDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar una nueva cuota
     */
    public boolean agregar(Cuota cuota) {
        if (cuota == null || cuota.getValor() <= 0) {
            System.err.println("Error: Cuota nula o valor inválido");
            return false;
        }

        String sql = "INSERT INTO Cuotas (numeroCuota, idCredito, valor, fechaVencimiento, fechaPago, pagada) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, cuota.getNumeroCuota());
                stmt.setInt(2, cuota.getIdCredito());
                stmt.setDouble(3, cuota.getValor());
                stmt.setTimestamp(4, new Timestamp(cuota.getFechaVencimiento().getTime()));

                if (cuota.getFechaPago() != null) {
                    stmt.setTimestamp(5, new Timestamp(cuota.getFechaPago().getTime()));
                } else {
                    stmt.setNull(5, java.sql.Types.TIMESTAMP);
                }

                stmt.setBoolean(6, cuota.isPagada());

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            cuota.setIdCuota(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar cuota: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener todas las cuotas
     */
    public List<Cuota> obtenerTodas() {
        List<Cuota> lista = new ArrayList<>();
        String sql = "SELECT idCuota, numeroCuota, idCredito, valor, fechaVencimiento, fechaPago, pagada " +
                     "FROM Cuotas ORDER BY idCredito, numeroCuota";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCuota(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar cuota por ID
     */
    public Cuota obtenerPorId(int idCuota) {
        String sql = "SELECT idCuota, numeroCuota, idCredito, valor, fechaVencimiento, fechaPago, pagada " +
                     "FROM Cuotas WHERE idCuota = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCuota);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearCuota(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cuota por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar una cuota existente
     */
    public boolean actualizar(Cuota cuota) {
        if (cuota == null || cuota.getValor() <= 0) {
            return false;
        }

        String sql = "UPDATE Cuotas SET numeroCuota = ?, idCredito = ?, valor = ?, fechaVencimiento = ?, " +
                     "fechaPago = ?, pagada = ? WHERE idCuota = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, cuota.getNumeroCuota());
                stmt.setInt(2, cuota.getIdCredito());
                stmt.setDouble(3, cuota.getValor());
                stmt.setTimestamp(4, new Timestamp(cuota.getFechaVencimiento().getTime()));

                if (cuota.getFechaPago() != null) {
                    stmt.setTimestamp(5, new Timestamp(cuota.getFechaPago().getTime()));
                } else {
                    stmt.setNull(5, java.sql.Types.TIMESTAMP);
                }

                stmt.setBoolean(6, cuota.isPagada());
                stmt.setInt(7, cuota.getIdCuota());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar cuota: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar cuota por ID
     */
    public boolean eliminar(int idCuota) {
        String sql = "DELETE FROM Cuotas WHERE idCuota = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCuota);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar cuota: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Obtener todas las cuotas de un crédito específico
     */
    public List<Cuota> obtenerPorCredito(int idCredito) {
        List<Cuota> lista = new ArrayList<>();
        String sql = "SELECT idCuota, numeroCuota, idCredito, valor, fechaVencimiento, fechaPago, pagada " +
                     "FROM Cuotas WHERE idCredito = ? ORDER BY numeroCuota";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCredito);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearCuota(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas por crédito: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener cuotas pendientes de pago de un crédito
     */
    public List<Cuota> obtenerPendientesPorCredito(int idCredito) {
        List<Cuota> lista = new ArrayList<>();
        String sql = "SELECT idCuota, numeroCuota, idCredito, valor, fechaVencimiento, fechaPago, pagada " +
                     "FROM Cuotas WHERE idCredito = ? AND pagada = 0 ORDER BY numeroCuota";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCredito);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearCuota(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas pendientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener cuotas vencidas (morosas)
     */
    public List<Cuota> obtenerVencidas() {
        List<Cuota> lista = new ArrayList<>();
        String sql = "SELECT idCuota, numeroCuota, idCredito, valor, fechaVencimiento, fechaPago, pagada " +
                     "FROM Cuotas WHERE pagada = 0 AND fechaVencimiento < GETDATE() ORDER BY fechaVencimiento";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCuota(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas vencidas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registrar el pago de una cuota
     */
    public boolean registrarPago(int idCuota, Date fechaPago) {
        String sql = "UPDATE Cuotas SET fechaPago = ?, pagada = 1 WHERE idCuota = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, new Timestamp(fechaPago != null ? fechaPago.getTime() : new Date().getTime()));
                stmt.setInt(2, idCuota);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar pago de cuota: " + e.getMessage());
        }
        return false;
    }

    /**
     * Generar cuotas para un crédito
     * Según requisitos:
     * - Cuota inicial 30% (ya pagada al momento de la venta)
     * - 70% restante + 5% de interés = 73.5% del total
     * - Dividido en 12, 18 o 24 meses
     */
    public boolean generarCuotas(int idCredito, double totalVenta, int plazoMeses, Date fechaVenta) {
        if (plazoMeses != 12 && plazoMeses != 18 && plazoMeses != 24) {
            System.err.println("Error: Plazo debe ser 12, 18 o 24 meses");
            return false;
        }

        // Calcular montos según requisitos
        double cuotaInicial = totalVenta * 0.30; // 30%
        double saldo = totalVenta - cuotaInicial; // 70%
        double saldoConIntereses = saldo * 1.05; // 70% + 5% de interés = 73.5% del total
        double valorCuota = saldoConIntereses / plazoMeses;

        // Generar las cuotas mensuales
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaVenta);

        for (int i = 1; i <= plazoMeses; i++) {
            calendar.add(Calendar.MONTH, 1); // Siguiente mes

            Cuota cuota = new Cuota();
            cuota.setNumeroCuota(i);
            cuota.setIdCredito(idCredito);
            cuota.setValor(valorCuota);
            cuota.setFechaVencimiento(calendar.getTime());
            cuota.setPagada(false);

            if (!agregar(cuota)) {
                System.err.println("Error al generar cuota " + i + " de " + plazoMeses);
                return false;
            }
        }

        return true;
    }

    /**
     * Eliminar todas las cuotas de un crédito (útil para anular un crédito)
     */
    public boolean eliminarPorCredito(int idCredito) {
        String sql = "DELETE FROM Cuotas WHERE idCredito = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCredito);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar cuotas por crédito: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Mapea un ResultSet a un objeto Cuota
     */
    private Cuota mapearCuota(ResultSet rs) throws SQLException {
        Timestamp fechaVencimiento = rs.getTimestamp("fechaVencimiento");
        Timestamp fechaPago = rs.getTimestamp("fechaPago");

        return new Cuota(
                rs.getInt("idCuota"),
                rs.getInt("numeroCuota"),
                rs.getInt("idCredito"),
                rs.getDouble("valor"),
                fechaVencimiento != null ? new Date(fechaVencimiento.getTime()) : null,
                fechaPago != null ? new Date(fechaPago.getTime()) : null,
                rs.getBoolean("pagada")
        );
    }
}
