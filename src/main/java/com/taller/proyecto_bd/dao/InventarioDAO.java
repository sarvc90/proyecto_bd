package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Inventario;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Inventario.
 * Maneja operaciones CRUD sobre inventarios en la base de datos SQL Server.
 *
 * @author Sistema
 * @version 2.0 - Conectado a SQL Server
 */
public class InventarioDAO {
    private static InventarioDAO instance;

    private InventarioDAO() {
    }

    public static synchronized InventarioDAO getInstance() {
        if (instance == null) {
            instance = new InventarioDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar nuevo registro de inventario
     */
    public boolean agregar(Inventario inventario) {
        if (inventario == null || inventario.getIdProducto() <= 0) {
            System.err.println("Error: Inventario nulo o ID de producto inválido");
            return false;
        }

        // Verificar si ya existe inventario para este producto
        if (obtenerPorProducto(inventario.getIdProducto()) != null) {
            System.err.println("Error: Ya existe inventario para este producto");
            return false;
        }

        String sql = "INSERT INTO Inventarios (idProducto, cantidadActual, stockMinimo, stockMaximo) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, inventario.getIdProducto());
                stmt.setInt(2, inventario.getCantidadActual());
                stmt.setInt(3, inventario.getStockMinimo());
                stmt.setInt(4, inventario.getStockMaximo());

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            inventario.setIdInventario(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar inventario: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener todos los inventarios con información del producto
     */
    public List<Inventario> obtenerTodos() {
        List<Inventario> lista = new ArrayList<>();
        String sql = "SELECT i.idInventario, i.idProducto, i.cantidadActual, i.stockMinimo, i.stockMaximo, " +
                     "i.ultimaActualizacion, p.nombre AS nombreProducto, p.codigo, c.nombre AS categoria " +
                     "FROM Inventarios i " +
                     "INNER JOIN Productos p ON i.idProducto = p.idProducto " +
                     "LEFT JOIN Categorias c ON p.idCategoria = c.idCategoria " +
                     "ORDER BY p.nombre";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearInventario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener inventarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar inventario por ID
     */
    public Inventario obtenerPorId(int idInventario) {
        String sql = "SELECT i.idInventario, i.idProducto, i.cantidadActual, i.stockMinimo, i.stockMaximo, " +
                     "i.ultimaActualizacion, p.nombre AS nombreProducto, p.codigo, c.nombre AS categoria " +
                     "FROM Inventarios i " +
                     "INNER JOIN Productos p ON i.idProducto = p.idProducto " +
                     "LEFT JOIN Categorias c ON p.idCategoria = c.idCategoria " +
                     "WHERE i.idInventario = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idInventario);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearInventario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar inventario por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Buscar inventario por ID de producto
     */
    public Inventario obtenerPorProducto(int idProducto) {
        String sql = "SELECT i.idInventario, i.idProducto, i.cantidadActual, i.stockMinimo, i.stockMaximo, " +
                     "i.ultimaActualizacion, p.nombre AS nombreProducto, p.codigo, c.nombre AS categoria " +
                     "FROM Inventarios i " +
                     "INNER JOIN Productos p ON i.idProducto = p.idProducto " +
                     "LEFT JOIN Categorias c ON p.idCategoria = c.idCategoria " +
                     "WHERE i.idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idProducto);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearInventario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar inventario por producto: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar un inventario existente
     */
    public boolean actualizar(Inventario inventario) {
        if (inventario == null || inventario.getIdInventario() <= 0) {
            return false;
        }

        String sql = "UPDATE Inventarios SET idProducto = ?, cantidadActual = ?, stockMinimo = ?, " +
                     "stockMaximo = ?, ultimaActualizacion = SYSDATETIME() WHERE idInventario = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, inventario.getIdProducto());
                stmt.setInt(2, inventario.getCantidadActual());
                stmt.setInt(3, inventario.getStockMinimo());
                stmt.setInt(4, inventario.getStockMaximo());
                stmt.setInt(5, inventario.getIdInventario());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar inventario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar inventario por ID
     */
    public boolean eliminar(int idInventario) {
        String sql = "DELETE FROM Inventarios WHERE idInventario = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idInventario);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar inventario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar inventario por ID de producto
     */
    public boolean eliminarPorProducto(int idProducto) {
        String sql = "DELETE FROM Inventarios WHERE idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idProducto);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar inventario por producto: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Obtener inventarios con stock bajo (cantidad <= stockMinimo)
     */
    public List<Inventario> obtenerBajoStock() {
        List<Inventario> lista = new ArrayList<>();
        String sql = "SELECT i.idInventario, i.idProducto, i.cantidadActual, i.stockMinimo, i.stockMaximo, " +
                     "i.ultimaActualizacion, p.nombre AS nombreProducto, p.codigo, c.nombre AS categoria " +
                     "FROM Inventarios i " +
                     "INNER JOIN Productos p ON i.idProducto = p.idProducto " +
                     "LEFT JOIN Categorias c ON p.idCategoria = c.idCategoria " +
                     "WHERE i.cantidadActual <= i.stockMinimo " +
                     "ORDER BY i.cantidadActual ASC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearInventario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener inventarios con bajo stock: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener inventarios con sobre stock (cantidad > stockMaximo)
     */
    public List<Inventario> obtenerSobreStock() {
        List<Inventario> lista = new ArrayList<>();
        String sql = "SELECT i.idInventario, i.idProducto, i.cantidadActual, i.stockMinimo, i.stockMaximo, " +
                     "i.ultimaActualizacion, p.nombre AS nombreProducto, p.codigo, c.nombre AS categoria " +
                     "FROM Inventarios i " +
                     "INNER JOIN Productos p ON i.idProducto = p.idProducto " +
                     "LEFT JOIN Categorias c ON p.idCategoria = c.idCategoria " +
                     "WHERE i.cantidadActual > i.stockMaximo " +
                     "ORDER BY i.cantidadActual DESC";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearInventario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener inventarios con sobre stock: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registrar entrada de productos al inventario
     */
    public boolean registrarEntrada(int idProducto, int cantidad) {
        if (cantidad <= 0) {
            return false;
        }

        String sql = "UPDATE Inventarios SET cantidadActual = cantidadActual + ?, " +
                     "ultimaActualizacion = SYSDATETIME() WHERE idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, cantidad);
                stmt.setInt(2, idProducto);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar entrada: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registrar salida de productos del inventario
     */
    public boolean registrarSalida(int idProducto, int cantidad) {
        if (cantidad <= 0) {
            return false;
        }

        // Verificar que hay suficiente stock
        Inventario inv = obtenerPorProducto(idProducto);
        if (inv == null || inv.getCantidadActual() < cantidad) {
            System.err.println("Error: Stock insuficiente para registrar salida");
            return false;
        }

        String sql = "UPDATE Inventarios SET cantidadActual = cantidadActual - ?, " +
                     "ultimaActualizacion = SYSDATETIME() WHERE idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, cantidad);
                stmt.setInt(2, idProducto);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar salida: " + e.getMessage());
        }
        return false;
    }

    /**
     * Crear o actualizar inventario para un producto
     */
    public boolean crearOActualizar(int idProducto, int stock) {
        if (idProducto <= 0) {
            return false;
        }

        Inventario inv = obtenerPorProducto(idProducto);
        if (inv != null) {
            inv.setCantidadActual(stock);
            return actualizar(inv);
        } else {
            Inventario nuevo = new Inventario(idProducto, stock, 5, 100);
            return agregar(nuevo);
        }
    }

    /**
     * Ajustar stock de un producto (puede ser positivo o negativo)
     */
    public boolean ajustarStock(int idProducto, int ajuste, String motivo) {
        if (ajuste == 0) {
            return false;
        }

        Inventario inv = obtenerPorProducto(idProducto);
        if (inv == null) {
            System.err.println("Error: No existe inventario para el producto");
            return false;
        }

        int nuevoStock = inv.getCantidadActual() + ajuste;
        if (nuevoStock < 0) {
            System.err.println("Error: El ajuste resultaría en stock negativo");
            return false;
        }

        inv.setCantidadActual(nuevoStock);
        return actualizar(inv);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Mapea un ResultSet a un objeto Inventario
     */
    private Inventario mapearInventario(ResultSet rs) throws SQLException {
        Timestamp ultimaActualizacion = rs.getTimestamp("ultimaActualizacion");

        Inventario inv = new Inventario(
                rs.getInt("idInventario"),
                rs.getInt("idProducto"),
                rs.getInt("cantidadActual"),
                rs.getInt("stockMinimo"),
                rs.getInt("stockMaximo"),
                ultimaActualizacion != null ? new Date(ultimaActualizacion.getTime()) : null
        );

        inv.setNombreProducto(rs.getString("nombreProducto"));
        inv.setCategoria(rs.getString("categoria"));

        return inv;
    }
}