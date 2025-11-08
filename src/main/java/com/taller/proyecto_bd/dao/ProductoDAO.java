package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Producto;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Producto.
 * Maneja operaciones CRUD sobre productos con SQL Server.
 *
 * @author Sistema
 * @version 2.0 - Conectado a SQL Server
 */
public class ProductoDAO {
    // ==================== SINGLETON ====================
    private static ProductoDAO instance;

    private ProductoDAO() {
    }

    public static ProductoDAO getInstance() {
        if (instance == null) {
            instance = new ProductoDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar un nuevo producto a la base de datos
     */
    public boolean agregar(Producto producto) {
        String sql = "INSERT INTO Productos (codigo, nombre, descripcion, marca, modelo, idCategoria, " +
                     "precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, " +
                     "activo, garantiaMeses, ubicacionAlmacen) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, producto.getCodigo());
                stmt.setString(2, producto.getNombre());
                stmt.setString(3, producto.getDescripcion());
                stmt.setString(4, producto.getMarca());
                stmt.setString(5, producto.getModelo());
                stmt.setInt(6, producto.getIdCategoria());
                stmt.setDouble(7, producto.getPrecioCompra());
                stmt.setDouble(8, producto.getPrecioVenta());
                stmt.setInt(9, producto.getStockActual());
                stmt.setInt(10, producto.getStockMinimo());
                stmt.setInt(11, producto.getStockMaximo());
                stmt.setString(12, producto.getUnidadMedida());
                stmt.setBoolean(13, producto.isActivo());
                stmt.setInt(14, producto.getGarantiaMeses());
                stmt.setString(15, producto.getUbicacionAlmacen());

                int filasAfectadas = stmt.executeUpdate();

                if (filasAfectadas > 0) {
                    // Obtener el ID generado
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            producto.setIdProducto(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener todos los productos
     */
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT idProducto, codigo, nombre, descripcion, marca, modelo, idCategoria, " +
                     "precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, " +
                     "activo, fechaRegistro, fechaUltimaActualizacion, garantiaMeses, ubicacionAlmacen " +
                     "FROM Productos ORDER BY nombre";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtener producto por ID
     */
    public Producto obtenerPorId(int id) {
        String sql = "SELECT idProducto, codigo, nombre, descripcion, marca, modelo, idCategoria, " +
                     "precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, " +
                     "activo, fechaRegistro, fechaUltimaActualizacion, garantiaMeses, ubicacionAlmacen " +
                     "FROM Productos WHERE idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearProducto(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtener producto por código
     */
    public Producto obtenerPorCodigo(String codigo) {
        String sql = "SELECT idProducto, codigo, nombre, descripcion, marca, modelo, idCategoria, " +
                     "precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, " +
                     "activo, fechaRegistro, fechaUltimaActualizacion, garantiaMeses, ubicacionAlmacen " +
                     "FROM Productos WHERE codigo = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, codigo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearProducto(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por código: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar un producto existente
     */
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE Productos SET codigo = ?, nombre = ?, descripcion = ?, marca = ?, " +
                     "modelo = ?, idCategoria = ?, precioCompra = ?, precioVenta = ?, stockActual = ?, " +
                     "stockMinimo = ?, stockMaximo = ?, unidadMedida = ?, activo = ?, garantiaMeses = ?, " +
                     "ubicacionAlmacen = ? WHERE idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, producto.getCodigo());
                stmt.setString(2, producto.getNombre());
                stmt.setString(3, producto.getDescripcion());
                stmt.setString(4, producto.getMarca());
                stmt.setString(5, producto.getModelo());
                stmt.setInt(6, producto.getIdCategoria());
                stmt.setDouble(7, producto.getPrecioCompra());
                stmt.setDouble(8, producto.getPrecioVenta());
                stmt.setInt(9, producto.getStockActual());
                stmt.setInt(10, producto.getStockMinimo());
                stmt.setInt(11, producto.getStockMaximo());
                stmt.setString(12, producto.getUnidadMedida());
                stmt.setBoolean(13, producto.isActivo());
                stmt.setInt(14, producto.getGarantiaMeses());
                stmt.setString(15, producto.getUbicacionAlmacen());
                stmt.setInt(16, producto.getIdProducto());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Eliminar producto por ID
     * IMPORTANTE: Solo se puede eliminar si el producto NO tiene ventas asociadas.
     * Si tiene ventas, se debe marcar como inactivo en su lugar.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Productos WHERE idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("========== ERROR AL ELIMINAR PRODUCTO ==========");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());

            // Detectar error de FK constraint (producto tiene ventas)
            if (e.getMessage().contains("FK_DetalleVentas_Productos") ||
                e.getMessage().contains("REFERENCE constraint") ||
                e.getErrorCode() == 547) {
                System.err.println("ERROR: No se puede eliminar el producto porque tiene ventas asociadas.");
                System.err.println("SOLUCIÓN: Marque el producto como inactivo en su lugar.");
            }

            e.printStackTrace();
            System.err.println("===============================================");
        }
        return false;
    }

    /**
     * Verifica si un producto tiene ventas asociadas
     * @param id ID del producto
     * @return true si el producto tiene ventas, false si no
     */
    public boolean tieneVentasAsociadas(int id) {
        String sql = "SELECT COUNT(*) AS total FROM DetalleVentas WHERE idProducto = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("total") > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar ventas del producto: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Obtener solo productos activos
     */
    public List<Producto> obtenerActivos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT idProducto, codigo, nombre, descripcion, marca, modelo, idCategoria, " +
                     "precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, " +
                     "activo, fechaRegistro, fechaUltimaActualizacion, garantiaMeses, ubicacionAlmacen " +
                     "FROM Productos WHERE activo = 1 ORDER BY nombre";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos activos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar productos por nombre o marca
     */
    public List<Producto> buscarPorNombreOMarca(String texto) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT idProducto, codigo, nombre, descripcion, marca, modelo, idCategoria, " +
                     "precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, " +
                     "activo, fechaRegistro, fechaUltimaActualizacion, garantiaMeses, ubicacionAlmacen " +
                     "FROM Productos WHERE nombre LIKE ? OR marca LIKE ? OR codigo LIKE ? ORDER BY nombre";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String busqueda = "%" + texto + "%";
                stmt.setString(1, busqueda);
                stmt.setString(2, busqueda);
                stmt.setString(3, busqueda);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearProducto(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener productos con stock bajo
     */
    public List<Producto> obtenerStockBajo() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT idProducto, codigo, nombre, descripcion, marca, modelo, idCategoria, " +
                     "precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, " +
                     "activo, fechaRegistro, fechaUltimaActualizacion, garantiaMeses, ubicacionAlmacen " +
                     "FROM Productos WHERE stockActual <= stockMinimo AND activo = 1 ORDER BY stockActual";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
        }
        return lista;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Mapear ResultSet a objeto Producto
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("idProducto"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setMarca(rs.getString("marca"));
        p.setModelo(rs.getString("modelo"));
        p.setIdCategoria(rs.getInt("idCategoria"));
        p.setPrecioCompra(rs.getDouble("precioCompra"));
        p.setPrecioVenta(rs.getDouble("precioVenta"));
        p.setStockActual(rs.getInt("stockActual"));
        p.setStockMinimo(rs.getInt("stockMinimo"));
        p.setStockMaximo(rs.getInt("stockMaximo"));
        p.setUnidadMedida(rs.getString("unidadMedida"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaRegistro(rs.getTimestamp("fechaRegistro"));
        p.setFechaUltimaActualizacion(rs.getTimestamp("fechaUltimaActualizacion"));
        p.setGarantiaMeses(rs.getInt("garantiaMeses"));
        p.setUbicacionAlmacen(rs.getString("ubicacionAlmacen"));
        return p;
    }
}
