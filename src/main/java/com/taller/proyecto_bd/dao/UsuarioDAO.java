package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.utils.ConexionBD;
import com.taller.proyecto_bd.utils.Encriptacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Usuario.
 * Maneja operaciones CRUD sobre usuarios (en memoria por ahora).
 */
public class UsuarioDAO {

    // ==================== ATRIBUTOS ====================
    private static UsuarioDAO instance;

    // ==================== SINGLETON ====================
    private UsuarioDAO() {
    }

    public static UsuarioDAO getInstance() {
        if (instance == null) {
            instance = new UsuarioDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    public boolean agregar(Usuario usuario) {
        if (usuario == null || usuario.getUsername() == null) {
            System.err.println("Error: Usuario nulo o username faltante");
            return false;
        }

        String sql = "INSERT INTO Usuarios (nombreCompleto, username, passwordHash, rol, email, telefono, activo, fechaRegistro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATETIME())";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, usuario.getNombreCompleto());
                stmt.setString(2, usuario.getUsername());
                stmt.setString(3, usuario.getPassword());
                stmt.setString(4, usuario.getRol());

                // Manejar campos que pueden ser NULL
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    stmt.setString(5, usuario.getEmail());
                } else {
                    stmt.setNull(5, java.sql.Types.VARCHAR);
                }

                if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty()) {
                    stmt.setString(6, usuario.getTelefono());
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                }

                stmt.setBoolean(7, usuario.isActivo());

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            usuario.setIdUsuario(rs.getInt(1));
                        }
                    }
                    usuario.setFechaRegistro(new Date());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            e.printStackTrace(); // Imprimir stack trace completo para debugging
        }
        return false;
    }

    public List<Usuario> obtenerTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT idUsuario, nombreCompleto, username, passwordHash, rol, email, telefono, activo, fechaRegistro, ultimoAcceso " +
                     "FROM Usuarios ORDER BY nombreCompleto";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }
        return lista;
    }

    public Usuario obtenerPorId(int idUsuario) {
        String sql = "SELECT idUsuario, nombreCompleto, username, passwordHash, rol, email, telefono, activo, fechaRegistro, ultimoAcceso " +
                     "FROM Usuarios WHERE idUsuario = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUsuario);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearUsuario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
        }
        return null;
    }

    public Usuario obtenerPorUsername(String username) {
        if (username == null) return null;

        String sql = "SELECT idUsuario, nombreCompleto, username, passwordHash, rol, email, telefono, activo, fechaRegistro, ultimoAcceso " +
                     "FROM Usuarios WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearUsuario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por username: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizar(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() <= 0) return false;

        String sql = "UPDATE Usuarios SET nombreCompleto = ?, username = ?, passwordHash = ?, rol = ?, email = ?, telefono = ?, activo = ? " +
                     "WHERE idUsuario = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, usuario.getNombreCompleto());
                stmt.setString(2, usuario.getUsername());
                stmt.setString(3, usuario.getPassword());
                stmt.setString(4, usuario.getRol());
                stmt.setString(5, usuario.getEmail());
                stmt.setString(6, usuario.getTelefono());
                stmt.setBoolean(7, usuario.isActivo());
                stmt.setInt(8, usuario.getIdUsuario());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM Usuarios WHERE idUsuario = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUsuario);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    public List<Usuario> obtenerPorRol(String rol) {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT idUsuario, nombreCompleto, username, passwordHash, rol, email, telefono, activo, fechaRegistro, ultimoAcceso " +
                     "FROM Usuarios WHERE rol = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, rol);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearUsuario(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios por rol: " + e.getMessage());
        }
        return lista;
    }

    public List<Usuario> obtenerActivos() {
        return obtenerPorEstado(true);
    }

    public List<Usuario> obtenerInactivos() {
        return obtenerPorEstado(false);
    }

    /**
     * Valida login con hash SHA-256.
     * No valida si el usuario está activo, eso se debe hacer en el controlador.
     */
    public Usuario login(String username, String passwordPlana) {
        if (username == null || passwordPlana == null) return null;

        String hashedPassword = Encriptacion.encriptarSHA256(passwordPlana);

        String sql = "SELECT idUsuario, nombreCompleto, username, passwordHash, rol, email, telefono, activo, fechaRegistro, ultimoAcceso " +
                     "FROM Usuarios WHERE LOWER(username) = LOWER(?) AND passwordHash = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username.trim());
                stmt.setString(2, hashedPassword);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Usuario usuario = mapearUsuario(rs);
                        actualizarUltimoAcceso(usuario.getIdUsuario());
                        usuario.setUltimoAcceso(new Date());
                        return usuario;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
        }
        return null;
    }

    private List<Usuario> obtenerPorEstado(boolean activo) {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT idUsuario, nombreCompleto, username, passwordHash, rol, email, telefono, activo, fechaRegistro, ultimoAcceso " +
                     "FROM Usuarios WHERE activo = ? ORDER BY nombreCompleto";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, activo);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearUsuario(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios por estado: " + e.getMessage());
        }
        return lista;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Timestamp fechaRegistro = rs.getTimestamp("fechaRegistro");
        Timestamp ultimoAcceso = rs.getTimestamp("ultimoAcceso");

        return new Usuario(
                rs.getInt("idUsuario"),
                rs.getString("nombreCompleto"),
                rs.getString("username"),
                rs.getString("passwordHash"),
                rs.getString("rol"),
                rs.getString("email"),
                rs.getString("telefono"),
                rs.getBoolean("activo"),
                fechaRegistro != null ? new Date(fechaRegistro.getTime()) : null,
                ultimoAcceso != null ? new Date(ultimoAcceso.getTime()) : null
        );
    }

    private void actualizarUltimoAcceso(int idUsuario) {
        String sql = "UPDATE Usuarios SET ultimoAcceso = SYSDATETIME() WHERE idUsuario = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUsuario);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar último acceso: " + e.getMessage());
        }
    }
}
