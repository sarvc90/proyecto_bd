package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.DetalleVenta;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad DetalleVenta.
 * Maneja operaciones CRUD sobre detalles de ventas en la base de datos.
 *
 * @author Sistema
 * @version 2.0
 */
public class DetalleVentaDAO {
    private static DetalleVentaDAO instance;

    private DetalleVentaDAO() {
    }

    public static synchronized DetalleVentaDAO getInstance() {
        if (instance == null) {
            instance = new DetalleVentaDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar un nuevo detalle de venta
     */
    public boolean agregar(DetalleVenta detalle) {
        if (detalle == null || !detalle.validarDetalle()) {
            System.err.println("Error: Detalle nulo o datos inválidos");
            return false;
        }

        String sql = "INSERT INTO DetalleVentas (idVenta, idProducto, cantidad, precioUnitario, subtotal, montoIVA, total) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, detalle.getIdVenta());
                stmt.setInt(2, detalle.getIdProducto());
                stmt.setInt(3, detalle.getCantidad());
                stmt.setDouble(4, detalle.getPrecioUnitario());
                stmt.setDouble(5, detalle.getSubtotal());
                stmt.setDouble(6, detalle.getMontoIVA());
                stmt.setDouble(7, detalle.getTotal());

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            detalle.setIdDetalle(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar detalle de venta: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener todos los detalles
     */
    public List<DetalleVenta> obtenerTodos() {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT idDetalle, idVenta, idProducto, cantidad, precioUnitario, subtotal, montoIVA, total, fechaRegistro " +
                     "FROM DetalleVentas";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDetalle(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar detalle por ID
     */
    public DetalleVenta obtenerPorId(int idDetalle) {
        String sql = "SELECT idDetalle, idVenta, idProducto, cantidad, precioUnitario, subtotal, montoIVA, total, fechaRegistro " +
                     "FROM DetalleVentas WHERE idDetalle = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idDetalle);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearDetalle(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar detalle por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar un detalle existente
     */
    public boolean actualizar(DetalleVenta detalle) {
        if (detalle == null || !detalle.validarDetalle()) {
            return false;
        }

        String sql = "UPDATE DetalleVentas SET idVenta = ?, idProducto = ?, cantidad = ?, precioUnitario = ?, " +
                     "subtotal = ?, montoIVA = ?, total = ? WHERE idDetalle = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, detalle.getIdVenta());
                stmt.setInt(2, detalle.getIdProducto());
                stmt.setInt(3, detalle.getCantidad());
                stmt.setDouble(4, detalle.getPrecioUnitario());
                stmt.setDouble(5, detalle.getSubtotal());
                stmt.setDouble(6, detalle.getMontoIVA());
                stmt.setDouble(7, detalle.getTotal());
                stmt.setInt(8, detalle.getIdDetalle());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar detalle: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar detalle por ID
     */
    public boolean eliminar(int idDetalle) {
        String sql = "DELETE FROM DetalleVentas WHERE idDetalle = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idDetalle);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar detalle: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Obtener todos los detalles de una venta específica
     */
    public List<DetalleVenta> obtenerPorVenta(int idVenta) {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT idDetalle, idVenta, idProducto, cantidad, precioUnitario, subtotal, montoIVA, total, fechaRegistro " +
                     "FROM DetalleVentas WHERE idVenta = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idVenta);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearDetalle(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles por venta: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Calcular el subtotal de una venta
     */
    public double calcularSubtotalVenta(int idVenta) {
        String sql = "SELECT SUM(subtotal) AS total FROM DetalleVentas WHERE idVenta = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return 0.0;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idVenta);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular subtotal de venta: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Eliminar todos los detalles de una venta (útil para anular una venta)
     */
    public boolean eliminarPorVenta(int idVenta) {
        String sql = "DELETE FROM DetalleVentas WHERE idVenta = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idVenta);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar detalles por venta: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Mapea un ResultSet a un objeto DetalleVenta
     */
    private DetalleVenta mapearDetalle(ResultSet rs) throws SQLException {
        Timestamp fechaRegistro = rs.getTimestamp("fechaRegistro");

        return new DetalleVenta(
                rs.getInt("idDetalle"),
                rs.getInt("idVenta"),
                rs.getInt("idProducto"),
                rs.getInt("cantidad"),
                rs.getDouble("precioUnitario"),
                rs.getDouble("subtotal"),
                rs.getDouble("montoIVA"),
                rs.getDouble("total"),
                fechaRegistro != null ? new Date(fechaRegistro.getTime()) : null
        );
    }
}
