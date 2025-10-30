package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Cliente;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Cliente.
 * Maneja operaciones CRUD sobre clientes (en memoria por ahora).
 *
 * Implementado como Singleton para mantener consistencia en toda la aplicación.
 *
 * @author Sistema
 * @version 1.1
 */
public class ClienteDAO {
    private static ClienteDAO instance; // instancia única

    // ==================== CONSTRUCTOR ====================
    private ClienteDAO() {
    }

    // ==================== SINGLETON ====================
    public static synchronized ClienteDAO getInstance() {
        if (instance == null) {
            instance = new ClienteDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar un nuevo cliente
     */
    public boolean agregar(Cliente cliente) {
        if (cliente == null || !cliente.validarDatosObligatorios()) {
            System.err.println("Error: Cliente nulo o datos obligatorios faltantes");
            return false;
        }

        String sql = "INSERT INTO Clientes (cedula, nombre, apellido, direccion, telefono, email, activo, limiteCredito, saldoPendiente) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, cliente.getCedula());
                stmt.setString(2, cliente.getNombre());
                stmt.setString(3, cliente.getApellido());

                // Manejar campos que pueden ser NULL
                if (cliente.getDireccion() != null && !cliente.getDireccion().trim().isEmpty()) {
                    stmt.setString(4, cliente.getDireccion());
                } else {
                    stmt.setNull(4, java.sql.Types.VARCHAR);
                }

                if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
                    stmt.setString(5, cliente.getTelefono());
                } else {
                    stmt.setNull(5, java.sql.Types.VARCHAR);
                }

                if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
                    stmt.setString(6, cliente.getEmail());
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                }

                stmt.setBoolean(7, cliente.isActivo());
                stmt.setDouble(8, cliente.getLimiteCredito());
                stmt.setDouble(9, cliente.getSaldoPendiente());

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            cliente.setIdCliente(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
            e.printStackTrace(); // Imprimir stack trace completo para debugging
        }
        return false;
    }

    /**
     * Obtener todos los clientes
     */
    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente " +
                     "FROM Clientes ORDER BY nombre, apellido";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar cliente por ID
     */
    public Cliente obtenerPorId(int id) {
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente " +
                     "FROM Clientes WHERE idCliente = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearCliente(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Buscar cliente por cédula
     */
    public Cliente obtenerPorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente " +
                     "FROM Clientes WHERE cedula = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cedula.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearCliente(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por cédula: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualizar un cliente existente
     */
    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE Clientes SET cedula = ?, nombre = ?, apellido = ?, direccion = ?, telefono = ?, email = ?, " +
                     "activo = ?, limiteCredito = ?, saldoPendiente = ? WHERE idCliente = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cliente.getCedula());
                stmt.setString(2, cliente.getNombre());
                stmt.setString(3, cliente.getApellido());
                stmt.setString(4, cliente.getDireccion());
                stmt.setString(5, cliente.getTelefono());
                stmt.setString(6, cliente.getEmail());
                stmt.setBoolean(7, cliente.isActivo());
                stmt.setDouble(8, cliente.getLimiteCredito());
                stmt.setDouble(9, cliente.getSaldoPendiente());
                stmt.setInt(10, cliente.getIdCliente());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
        }
        return false;
    }

    /**
     * Eliminar cliente por ID
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Clientes WHERE idCliente = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
        }
        return false;
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Cargar datos de prueba para testeo
     */
    /**
     * Buscar clientes activos
     */
    public List<Cliente> obtenerActivos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente " +
                     "FROM Clientes WHERE activo = 1 ORDER BY nombre, apellido";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes activos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Buscar clientes por nombre, apellido o cédula
     */
    public List<Cliente> buscarPorNombre(String texto) {
        List<Cliente> lista = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return lista;
        }

        String criterio = "%" + texto.trim().toLowerCase() + "%";
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente " +
                     "FROM Clientes WHERE LOWER(nombre) LIKE ? OR LOWER(apellido) LIKE ? OR LOWER(cedula) LIKE ? ORDER BY nombre, apellido";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                return lista;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, criterio);
                stmt.setString(2, criterio);
                stmt.setString(3, criterio);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearCliente(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
        }
        return lista;
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Timestamp fechaRegistro = rs.getTimestamp("fechaRegistro");

        Cliente cliente = new Cliente(
                rs.getInt("idCliente"),
                rs.getString("cedula"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("direccion"),
                rs.getString("telefono"),
                rs.getString("email"),
                fechaRegistro != null ? new Date(fechaRegistro.getTime()) : null,
                rs.getBoolean("activo"),
                rs.getDouble("limiteCredito"),
                rs.getDouble("saldoPendiente")
        );
        return cliente;
    }
}
