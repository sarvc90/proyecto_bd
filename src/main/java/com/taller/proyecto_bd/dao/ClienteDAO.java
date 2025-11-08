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

        String sql = "INSERT INTO Clientes (cedula, nombre, apellido, direccion, telefono, email, activo, limiteCredito, saldoPendiente, passwordHash) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión a la base de datos");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Debug: imprimir valores
                System.out.println("DEBUG ClienteDAO - Insertando cliente:");
                System.out.println("  Cedula: " + cliente.getCedula());
                System.out.println("  Nombre: " + cliente.getNombre());
                System.out.println("  LimiteCredito: " + cliente.getLimiteCredito());
                System.out.println("  SaldoPendiente: " + cliente.getSaldoPendiente());

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
                // Usar BigDecimal con escala de 2 decimales para NUMERIC(10,2)
                java.math.BigDecimal limiteCredito = java.math.BigDecimal.valueOf(cliente.getLimiteCredito()).setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal saldoPendiente = java.math.BigDecimal.valueOf(cliente.getSaldoPendiente()).setScale(2, java.math.RoundingMode.HALF_UP);

                System.out.println("  BigDecimal LimiteCredito: " + limiteCredito);
                System.out.println("  BigDecimal SaldoPendiente: " + saldoPendiente);

                stmt.setBigDecimal(8, limiteCredito);
                stmt.setBigDecimal(9, saldoPendiente);

                // passwordHash (puede ser NULL)
                if (cliente.getPasswordHash() != null && !cliente.getPasswordHash().trim().isEmpty()) {
                    stmt.setString(10, cliente.getPasswordHash());
                } else {
                    stmt.setNull(10, java.sql.Types.VARCHAR);
                }

                System.out.println("DEBUG ClienteDAO - Ejecutando SQL INSERT...");
                int filas = stmt.executeUpdate();
                System.out.println("DEBUG ClienteDAO - Filas insertadas: " + filas);

                if (filas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            cliente.setIdCliente(rs.getInt(1));
                            System.out.println("DEBUG ClienteDAO - ID generado: " + cliente.getIdCliente());
                        }
                    }
                    System.out.println("DEBUG ClienteDAO - Cliente insertado exitosamente!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("========== ERROR SQL EN ClienteDAO.agregar() ==========");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());

            // Detectar error de email duplicado
            if (e.getMessage().contains("UX_Clientes_Email")) {
                System.err.println("ERROR: Ya existe un cliente con el email: " + cliente.getEmail());
                System.err.println("Por favor use un email diferente.");
            }
            // Detectar error de cédula duplicada
            else if (e.getMessage().contains("UX_Clientes_Cedula")) {
                System.err.println("ERROR: Ya existe un cliente con la cédula: " + cliente.getCedula());
                System.err.println("Por favor verifique la cédula.");
            }
            // Detectar error de columna inexistente
            else if (e.getMessage().contains("Invalid column name") || e.getMessage().contains("passwordHash")) {
                System.err.println("======================================================");
                System.err.println("ERROR CRÍTICO: La columna 'passwordHash' NO EXISTE en la tabla Clientes");
                System.err.println("SOLUCIÓN: Ejecuta el script 'db/alter_clientes_add_password.sql' en tu base de datos");
                System.err.println("======================================================");
            }
            else {
                System.err.println("Error al insertar cliente: " + e.getMessage());
            }

            e.printStackTrace(); // Imprimir stack trace completo para debugging
            System.err.println("======================================================");
        }
        return false;
    }

    /**
     * Obtener todos los clientes
     */
    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente, passwordHash " +
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
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente, passwordHash " +
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

        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente, passwordHash " +
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
                     "activo = ?, limiteCredito = ?, saldoPendiente = ?, passwordHash = ? WHERE idCliente = ?";

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
                // Usar BigDecimal con escala de 2 decimales para NUMERIC(10,2)
                stmt.setBigDecimal(8, java.math.BigDecimal.valueOf(cliente.getLimiteCredito()).setScale(2, java.math.RoundingMode.HALF_UP));
                stmt.setBigDecimal(9, java.math.BigDecimal.valueOf(cliente.getSaldoPendiente()).setScale(2, java.math.RoundingMode.HALF_UP));

                // passwordHash (puede ser NULL)
                if (cliente.getPasswordHash() != null && !cliente.getPasswordHash().trim().isEmpty()) {
                    stmt.setString(10, cliente.getPasswordHash());
                } else {
                    stmt.setNull(10, java.sql.Types.VARCHAR);
                }

                stmt.setInt(11, cliente.getIdCliente());

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
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente, passwordHash " +
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
        String sql = "SELECT idCliente, cedula, nombre, apellido, direccion, telefono, email, fechaRegistro, activo, limiteCredito, saldoPendiente, passwordHash " +
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

    /**
     * Obtener clientes que tienen créditos morosos (con cuotas vencidas no pagadas)
     * Este método es útil para generar reportes de morosidad
     */
    public List<Cliente> obtenerClientesMorosos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT c.idCliente, c.cedula, c.nombre, c.apellido, c.direccion, c.telefono, c.email, " +
                     "c.fechaRegistro, c.activo, c.limiteCredito, c.saldoPendiente, c.passwordHash " +
                     "FROM Clientes c " +
                     "INNER JOIN Creditos cr ON c.idCliente = cr.idCliente " +
                     "INNER JOIN Cuotas cu ON cr.idVenta = cu.idVenta " +
                     "WHERE cr.estado = 'ACTIVO' AND cu.pagada = 0 AND cu.fechaVencimiento < GETDATE() " +
                     "ORDER BY c.nombre, c.apellido";

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
            System.err.println("Error al obtener clientes morosos: " + e.getMessage());
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
                rs.getDouble("saldoPendiente"),
                rs.getString("passwordHash")  // Puede ser NULL
        );
        return cliente;
    }
}
