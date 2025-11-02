package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Venta;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Venta.
 * Maneja operaciones CRUD sobre ventas en la base de datos.
 *
 * @author Sistema
 * @version 2.0
 */
public class VentaDAO {
    private static VentaDAO instance;

    private VentaDAO() {
    }

    public static VentaDAO getInstance() {
        if (instance == null) {
            instance = new VentaDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar una nueva venta
     */
    public boolean agregar(Venta venta) {
        if (venta == null || !venta.validarVenta()) {
            System.err.println("Error: Venta nula o datos inválidos");
            return false;
        }

        String sql = "INSERT INTO Ventas (codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, venta.getCodigo());
                stmt.setInt(2, venta.getIdCliente());
                stmt.setInt(3, venta.getIdUsuario());
                stmt.setTimestamp(4, new Timestamp(venta.getFechaVenta().getTime()));
                stmt.setBoolean(5, venta.isEsCredito());
                stmt.setDouble(6, venta.getSubtotal());
                stmt.setDouble(7, venta.getIvaTotal());
                stmt.setDouble(8, venta.getTotal());
                stmt.setDouble(9, venta.getCuotaInicial());
                stmt.setInt(10, venta.getPlazoMeses());
                stmt.setString(11, venta.getEstado());

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            venta.setIdVenta(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar venta: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener todas las ventas
     */
    public List<Venta> obtenerTodas() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT idVenta, codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado " +
                     "FROM Ventas ORDER BY fechaVenta DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVenta(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar venta por ID
     */
    public Venta obtenerPorId(int id) {
        String sql = "SELECT idVenta, codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado " +
                     "FROM Ventas WHERE idVenta = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearVenta(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar venta por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Buscar venta por código
     */
    public Venta obtenerPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT idVenta, codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado " +
                     "FROM Ventas WHERE codigo = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, codigo.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearVenta(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar venta por código: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar una venta existente
     */
    public boolean actualizar(Venta venta) {
        if (venta == null || !venta.validarVenta()) {
            return false;
        }

        String sql = "UPDATE Ventas SET codigo = ?, idCliente = ?, idUsuario = ?, fechaVenta = ?, esCredito = ?, " +
                     "subtotal = ?, ivaTotal = ?, total = ?, cuotaInicial = ?, plazoMeses = ?, estado = ? " +
                     "WHERE idVenta = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, venta.getCodigo());
                stmt.setInt(2, venta.getIdCliente());
                stmt.setInt(3, venta.getIdUsuario());
                stmt.setTimestamp(4, new Timestamp(venta.getFechaVenta().getTime()));
                stmt.setBoolean(5, venta.isEsCredito());
                stmt.setDouble(6, venta.getSubtotal());
                stmt.setDouble(7, venta.getIvaTotal());
                stmt.setDouble(8, venta.getTotal());
                stmt.setDouble(9, venta.getCuotaInicial());
                stmt.setInt(10, venta.getPlazoMeses());
                stmt.setString(11, venta.getEstado());
                stmt.setInt(12, venta.getIdVenta());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar venta: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar venta por ID
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Ventas WHERE idVenta = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar venta: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Obtener ventas por cliente
     */
    public List<Venta> obtenerPorCliente(int idCliente) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT idVenta, codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado " +
                     "FROM Ventas WHERE idCliente = ? ORDER BY fechaVenta DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCliente);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearVenta(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener ventas por usuario (vendedor)
     */
    public List<Venta> obtenerPorUsuario(int idUsuario) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT idVenta, codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado " +
                     "FROM Ventas WHERE idUsuario = ? ORDER BY fechaVenta DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUsuario);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearVenta(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por usuario: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener ventas por tipo (crédito o contado)
     */
    public List<Venta> obtenerPorCredito(boolean esCredito) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT idVenta, codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado " +
                     "FROM Ventas WHERE esCredito = ? ORDER BY fechaVenta DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, esCredito);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearVenta(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por tipo de crédito: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener ventas por estado
     */
    public List<Venta> obtenerPorEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT idVenta, codigo, idCliente, idUsuario, fechaVenta, esCredito, subtotal, ivaTotal, total, cuotaInicial, plazoMeses, estado " +
                     "FROM Ventas WHERE estado = ? ORDER BY fechaVenta DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, estado.trim().toUpperCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearVenta(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por estado: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener ventas activas (estado REGISTRADA)
     */
    public List<Venta> obtenerActivas() {
        return obtenerPorEstado("REGISTRADA");
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Mapea un ResultSet a un objeto Venta
     */
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Timestamp fechaVenta = rs.getTimestamp("fechaVenta");

        return new Venta(
                rs.getInt("idVenta"),
                rs.getString("codigo"),
                rs.getInt("idCliente"),
                rs.getInt("idUsuario"),
                fechaVenta != null ? new Date(fechaVenta.getTime()) : null,
                rs.getBoolean("esCredito"),
                rs.getDouble("subtotal"),
                rs.getDouble("ivaTotal"),
                rs.getDouble("total"),
                rs.getDouble("cuotaInicial"),
                rs.getInt("plazoMeses"),
                rs.getString("estado")
        );
    }
}
