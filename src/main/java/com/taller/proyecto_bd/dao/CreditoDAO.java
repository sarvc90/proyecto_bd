package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Credito;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Credito.
 * Maneja operaciones CRUD sobre créditos en la base de datos.
 *
 * @author Sistema
 * @version 2.0
 */
public class CreditoDAO {
    private static CreditoDAO instance;

    private CreditoDAO() {
    }

    public static synchronized CreditoDAO getInstance() {
        if (instance == null) {
            instance = new CreditoDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar un nuevo crédito
     */
    public boolean agregar(Credito credito) {
        if (credito == null || credito.getMontoTotal() < 0) {
            System.err.println("Error: Crédito nulo o monto inválido");
            return false;
        }

        String sql = "INSERT INTO Creditos (idVenta, idCliente, montoTotal, interes, plazoMeses, cuotaInicial, saldoPendiente, estado, fechaRegistro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, credito.getIdVenta());
                stmt.setInt(2, credito.getIdCliente());
                stmt.setDouble(3, credito.getMontoTotal());
                stmt.setDouble(4, credito.getInteres());
                stmt.setInt(5, credito.getPlazoMeses());
                stmt.setDouble(6, credito.getCuotaInicial());
                stmt.setDouble(7, credito.getSaldoPendiente());
                stmt.setString(8, credito.getEstado());
                stmt.setTimestamp(9, new Timestamp(credito.getFechaRegistro().getTime()));

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            credito.setIdCredito(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar crédito: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener todos los créditos
     */
    public List<Credito> obtenerTodos() {
        List<Credito> lista = new ArrayList<>();
        String sql = "SELECT idCredito, idVenta, idCliente, montoTotal, interes, plazoMeses, cuotaInicial, saldoPendiente, estado, fechaRegistro " +
                     "FROM Creditos ORDER BY fechaRegistro DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCredito(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener créditos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar crédito por ID
     */
    public Credito obtenerPorId(int idCredito) {
        String sql = "SELECT idCredito, idVenta, idCliente, montoTotal, interes, plazoMeses, cuotaInicial, saldoPendiente, estado, fechaRegistro " +
                     "FROM Creditos WHERE idCredito = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCredito);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearCredito(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar crédito por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Buscar crédito por ID de venta
     */
    public Credito obtenerPorVenta(int idVenta) {
        String sql = "SELECT idCredito, idVenta, idCliente, montoTotal, interes, plazoMeses, cuotaInicial, saldoPendiente, estado, fechaRegistro " +
                     "FROM Creditos WHERE idVenta = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idVenta);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearCredito(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar crédito por venta: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar un crédito existente
     */
    public boolean actualizar(Credito credito) {
        if (credito == null || credito.getMontoTotal() < 0) {
            return false;
        }

        String sql = "UPDATE Creditos SET idVenta = ?, idCliente = ?, montoTotal = ?, interes = ?, " +
                     "plazoMeses = ?, cuotaInicial = ?, saldoPendiente = ?, estado = ? WHERE idCredito = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, credito.getIdVenta());
                stmt.setInt(2, credito.getIdCliente());
                stmt.setDouble(3, credito.getMontoTotal());
                stmt.setDouble(4, credito.getInteres());
                stmt.setInt(5, credito.getPlazoMeses());
                stmt.setDouble(6, credito.getCuotaInicial());
                stmt.setDouble(7, credito.getSaldoPendiente());
                stmt.setString(8, credito.getEstado());
                stmt.setInt(9, credito.getIdCredito());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar crédito: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar crédito por ID
     */
    public boolean eliminar(int idCredito) {
        String sql = "DELETE FROM Creditos WHERE idCredito = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCredito);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar crédito: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Obtener créditos por cliente
     */
    public List<Credito> obtenerPorCliente(int idCliente) {
        List<Credito> lista = new ArrayList<>();
        String sql = "SELECT idCredito, idVenta, idCliente, montoTotal, interes, plazoMeses, cuotaInicial, saldoPendiente, estado, fechaRegistro " +
                     "FROM Creditos WHERE idCliente = ? ORDER BY fechaRegistro DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCliente);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearCredito(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener créditos por cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener créditos por estado
     */
    public List<Credito> obtenerPorEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Credito> lista = new ArrayList<>();
        String sql = "SELECT idCredito, idVenta, idCliente, montoTotal, interes, plazoMeses, cuotaInicial, saldoPendiente, estado, fechaRegistro " +
                     "FROM Creditos WHERE estado = ? ORDER BY fechaRegistro DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, estado.trim().toUpperCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearCredito(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener créditos por estado: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener créditos activos
     */
    public List<Credito> obtenerActivos() {
        return obtenerPorEstado("ACTIVO");
    }

    /**
     * Obtener créditos cancelados
     */
    public List<Credito> obtenerCancelados() {
        return obtenerPorEstado("CANCELADO");
    }

    /**
     * Obtener créditos morosos (con cuotas vencidas)
     */
    public List<Credito> obtenerMorosos() {
        List<Credito> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT c.idCredito, c.idVenta, c.idCliente, c.montoTotal, c.interes, c.plazoMeses, " +
                     "c.cuotaInicial, c.saldoPendiente, c.estado, c.fechaRegistro " +
                     "FROM Creditos c " +
                     "INNER JOIN Cuotas cu ON c.idCredito = cu.idCredito " +
                     "WHERE c.estado = 'ACTIVO' AND cu.pagada = 0 AND cu.fechaVencimiento < GETDATE() " +
                     "ORDER BY c.fechaRegistro DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCredito(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener créditos morosos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Verificar si un cliente tiene créditos activos pendientes
     */
    public boolean clienteTieneCreditoActivo(int idCliente) {
        String sql = "SELECT COUNT(*) FROM Creditos WHERE idCliente = ? AND estado = 'ACTIVO' AND saldoPendiente > 0";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCliente);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar crédito activo del cliente: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Mapea un ResultSet a un objeto Credito
     */
    private Credito mapearCredito(ResultSet rs) throws SQLException {
        Timestamp fechaRegistro = rs.getTimestamp("fechaRegistro");

        return new Credito(
                rs.getInt("idCredito"),
                rs.getInt("idVenta"),
                rs.getInt("idCliente"),
                rs.getDouble("montoTotal"),
                rs.getDouble("interes"),
                rs.getInt("plazoMeses"),
                rs.getDouble("cuotaInicial"),
                rs.getDouble("saldoPendiente"),
                rs.getString("estado"),
                fechaRegistro != null ? new Date(fechaRegistro.getTime()) : null
        );
    }
}
