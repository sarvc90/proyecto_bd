package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Categoria;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Categoria.
 * Maneja operaciones CRUD y jerarquía de categorías.
 *
 * Implementado como Singleton para mantener consistencia en toda la app.
 *
 * @author Sistema
 * @version 1.2
 */
public class CategoriaDAO {
    // ==================== ATRIBUTOS ====================
    private static CategoriaDAO instance; // instancia única

    // ==================== CONSTRUCTOR ====================
    private CategoriaDAO() {
    }

    // ==================== SINGLETON ====================
    public static synchronized CategoriaDAO getInstance() {
        if (instance == null) {
            instance = new CategoriaDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar una nueva categoría
     */
    public boolean agregar(Categoria categoria) {
        if (categoria == null || !categoria.validarDatosObligatorios() || !categoria.validarJerarquia()) {
            System.err.println("Error: Categoría nula o datos obligatorios/jerarquía inválidos");
            return false;
        }

        String sql = "INSERT INTO Categorias (codigo, nombre, descripcion, activo, nivel, idCategoriaPadre, rutaCompleta, porcentajeIVA, porcentajeUtilidad) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, categoria.getCodigo());
                stmt.setString(2, categoria.getNombre());

                // Manejar descripción que puede ser NULL
                if (categoria.getDescripcion() != null && !categoria.getDescripcion().trim().isEmpty()) {
                    stmt.setString(3, categoria.getDescripcion());
                } else {
                    stmt.setNull(3, java.sql.Types.VARCHAR);
                }

                stmt.setBoolean(4, categoria.isActivo());
                stmt.setInt(5, categoria.getNivel());

                // Manejar idCategoriaPadre que puede ser NULL
                if (categoria.getIdCategoriaPadre() != null && categoria.getIdCategoriaPadre() > 0) {
                    stmt.setInt(6, categoria.getIdCategoriaPadre());
                } else {
                    stmt.setNull(6, java.sql.Types.INTEGER);
                }

                // Manejar rutaCompleta que puede ser NULL
                if (categoria.getRutaCompleta() != null && !categoria.getRutaCompleta().trim().isEmpty()) {
                    stmt.setString(7, categoria.getRutaCompleta());
                } else {
                    stmt.setNull(7, java.sql.Types.VARCHAR);
                }

                stmt.setDouble(8, categoria.getPorcentajeIVA());
                stmt.setDouble(9, categoria.getPorcentajeUtilidad());

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            categoria.setIdCategoria(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener todas las categorías
     */
    public List<Categoria> obtenerTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT idCategoria, codigo, nombre, descripcion, activo, fechaRegistro, fechaUltimaActualizacion, " +
                     "nivel, idCategoriaPadre, rutaCompleta, porcentajeIVA, porcentajeUtilidad " +
                     "FROM Categorias ORDER BY nivel, nombre";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCategoria(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar categoría por ID
     */
    public Categoria obtenerPorId(int id) {
        String sql = "SELECT idCategoria, codigo, nombre, descripcion, activo, fechaRegistro, fechaUltimaActualizacion, " +
                     "nivel, idCategoriaPadre, rutaCompleta, porcentajeIVA, porcentajeUtilidad " +
                     "FROM Categorias WHERE idCategoria = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearCategoria(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar categoría por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Buscar categoría por código
     */
    public Categoria obtenerPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT idCategoria, codigo, nombre, descripcion, activo, fechaRegistro, fechaUltimaActualizacion, " +
                     "nivel, idCategoriaPadre, rutaCompleta, porcentajeIVA, porcentajeUtilidad " +
                     "FROM Categorias WHERE codigo = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, codigo.trim().toUpperCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearCategoria(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar categoría por código: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar una categoría existente
     */
    public boolean actualizar(Categoria categoria) {
        if (categoria == null || !categoria.validarDatosObligatorios()) {
            return false;
        }

        String sql = "UPDATE Categorias SET codigo = ?, nombre = ?, descripcion = ?, activo = ?, " +
                     "nivel = ?, idCategoriaPadre = ?, rutaCompleta = ?, porcentajeIVA = ?, porcentajeUtilidad = ? " +
                     "WHERE idCategoria = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoria.getCodigo());
                stmt.setString(2, categoria.getNombre());

                // Manejar descripción que puede ser NULL
                if (categoria.getDescripcion() != null && !categoria.getDescripcion().trim().isEmpty()) {
                    stmt.setString(3, categoria.getDescripcion());
                } else {
                    stmt.setNull(3, java.sql.Types.VARCHAR);
                }

                stmt.setBoolean(4, categoria.isActivo());
                stmt.setInt(5, categoria.getNivel());

                // Manejar idCategoriaPadre que puede ser NULL
                if (categoria.getIdCategoriaPadre() != null && categoria.getIdCategoriaPadre() > 0) {
                    stmt.setInt(6, categoria.getIdCategoriaPadre());
                } else {
                    stmt.setNull(6, java.sql.Types.INTEGER);
                }

                // Manejar rutaCompleta que puede ser NULL
                if (categoria.getRutaCompleta() != null && !categoria.getRutaCompleta().trim().isEmpty()) {
                    stmt.setString(7, categoria.getRutaCompleta());
                } else {
                    stmt.setNull(7, java.sql.Types.VARCHAR);
                }

                stmt.setDouble(8, categoria.getPorcentajeIVA());
                stmt.setDouble(9, categoria.getPorcentajeUtilidad());
                stmt.setInt(10, categoria.getIdCategoria());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar una categoría por ID
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Categorias WHERE idCategoria = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Obtener categorías principales (sin padre)
     */
    public List<Categoria> obtenerPrincipales() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT idCategoria, codigo, nombre, descripcion, activo, fechaRegistro, fechaUltimaActualizacion, " +
                     "nivel, idCategoriaPadre, rutaCompleta, porcentajeIVA, porcentajeUtilidad " +
                     "FROM Categorias WHERE idCategoriaPadre IS NULL OR nivel = 1 ORDER BY nombre";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCategoria(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías principales: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtener subcategorías de una categoría padre
     */
    public List<Categoria> obtenerSubcategorias(int idCategoriaPadre) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT idCategoria, codigo, nombre, descripcion, activo, fechaRegistro, fechaUltimaActualizacion, " +
                     "nivel, idCategoriaPadre, rutaCompleta, porcentajeIVA, porcentajeUtilidad " +
                     "FROM Categorias WHERE idCategoriaPadre = ? ORDER BY nombre";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idCategoriaPadre);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearCategoria(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener subcategorías: " + e.getMessage());
        }
        return lista;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Mapea un ResultSet a un objeto Categoria
     */
    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Timestamp fechaRegistro = rs.getTimestamp("fechaRegistro");
        Timestamp fechaActualizacion = rs.getTimestamp("fechaUltimaActualizacion");

        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("idCategoria"));
        categoria.setCodigo(rs.getString("codigo"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setActivo(rs.getBoolean("activo"));
        categoria.setFechaRegistro(fechaRegistro != null ? new Date(fechaRegistro.getTime()) : null);
        categoria.setFechaUltimaActualizacion(fechaActualizacion != null ? new Date(fechaActualizacion.getTime()) : null);
        categoria.setNivel(rs.getInt("nivel"));

        // Manejar idCategoriaPadre que puede ser NULL
        int idPadre = rs.getInt("idCategoriaPadre");
        if (!rs.wasNull()) {
            categoria.setIdCategoriaPadre(idPadre);
        } else {
            categoria.setIdCategoriaPadre(null);
        }

        categoria.setRutaCompleta(rs.getString("rutaCompleta"));
        categoria.setPorcentajeIVA(rs.getDouble("porcentajeIVA"));
        categoria.setPorcentajeUtilidad(rs.getDouble("porcentajeUtilidad"));

        return categoria;
    }
}
