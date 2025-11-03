package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Auditoria;
import com.taller.proyecto_bd.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Auditoria.
 * Maneja operaciones CRUD sobre registros de auditoría en SQL Server.
 *
 * Implementado como Singleton.
 *
 * @author Sistema
 * @version 2.0
 */
public class AuditoriaDAO {
    // ==================== SINGLETON ====================
    private static AuditoriaDAO instance;

    private AuditoriaDAO() {}

    public static synchronized AuditoriaDAO getInstance() {
        if (instance == null) {
            instance = new AuditoriaDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Registra una nueva auditoría en la base de datos
     */
    public boolean agregar(Auditoria auditoria) {
        String sql = "INSERT INTO Auditorias (idUsuario, accion, tablaAfectada, descripcion, ip) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, auditoria.getIdUsuario());
            stmt.setString(2, auditoria.getAccion());
            stmt.setString(3, auditoria.getTablaAfectada());
            stmt.setString(4, auditoria.getDescripcion());
            stmt.setString(5, auditoria.getIp());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    auditoria.setIdAuditoria(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al agregar auditoría: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene todas las auditorías con información del usuario (JOIN)
     */
    public List<Auditoria> obtenerTodas() {
        List<Auditoria> auditorias = new ArrayList<>();
        String sql = "SELECT a.idAuditoria, a.idUsuario, a.accion, a.tablaAfectada, " +
                     "a.descripcion, a.ip, a.fechaAccion, u.nombreCompleto AS nombreUsuario " +
                     "FROM Auditorias a " +
                     "INNER JOIN Usuarios u ON a.idUsuario = u.idUsuario " +
                     "ORDER BY a.fechaAccion DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Auditoria a = new Auditoria();
                a.setIdAuditoria(rs.getInt("idAuditoria"));
                a.setIdUsuario(rs.getInt("idUsuario"));
                a.setAccion(rs.getString("accion"));
                a.setTablaAfectada(rs.getString("tablaAfectada"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setIp(rs.getString("ip"));
                a.setFechaAccion(rs.getTimestamp("fechaAccion"));
                a.setNombreUsuario(rs.getString("nombreUsuario"));
                auditorias.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener auditorías: " + e.getMessage());
            e.printStackTrace();
        }
        return auditorias;
    }

    /**
     * Obtiene una auditoría por su ID
     */
    public Auditoria obtenerPorId(int idAuditoria) {
        String sql = "SELECT a.idAuditoria, a.idUsuario, a.accion, a.tablaAfectada, " +
                     "a.descripcion, a.ip, a.fechaAccion, u.nombreCompleto AS nombreUsuario " +
                     "FROM Auditorias a " +
                     "INNER JOIN Usuarios u ON a.idUsuario = u.idUsuario " +
                     "WHERE a.idAuditoria = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAuditoria);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Auditoria a = new Auditoria();
                a.setIdAuditoria(rs.getInt("idAuditoria"));
                a.setIdUsuario(rs.getInt("idUsuario"));
                a.setAccion(rs.getString("accion"));
                a.setTablaAfectada(rs.getString("tablaAfectada"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setIp(rs.getString("ip"));
                a.setFechaAccion(rs.getTimestamp("fechaAccion"));
                a.setNombreUsuario(rs.getString("nombreUsuario"));
                return a;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener auditoría por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Nota: Las auditorías generalmente no se actualizan ni eliminan (inmutables)
    // Se mantienen los métodos por completitud pero no se recomienda usarlos

    /**
     * Obtiene auditorías de un usuario específico
     */
    public List<Auditoria> obtenerPorUsuario(int idUsuario) {
        List<Auditoria> auditorias = new ArrayList<>();
        String sql = "SELECT a.idAuditoria, a.idUsuario, a.accion, a.tablaAfectada, " +
                     "a.descripcion, a.ip, a.fechaAccion, u.nombreCompleto AS nombreUsuario " +
                     "FROM Auditorias a " +
                     "INNER JOIN Usuarios u ON a.idUsuario = u.idUsuario " +
                     "WHERE a.idUsuario = ? " +
                     "ORDER BY a.fechaAccion DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Auditoria a = new Auditoria();
                a.setIdAuditoria(rs.getInt("idAuditoria"));
                a.setIdUsuario(rs.getInt("idUsuario"));
                a.setAccion(rs.getString("accion"));
                a.setTablaAfectada(rs.getString("tablaAfectada"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setIp(rs.getString("ip"));
                a.setFechaAccion(rs.getTimestamp("fechaAccion"));
                a.setNombreUsuario(rs.getString("nombreUsuario"));
                auditorias.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener auditorías por usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return auditorias;
    }

    /**
     * Obtiene auditorías por tipo de acción
     */
    public List<Auditoria> obtenerPorAccion(String accion) {
        List<Auditoria> auditorias = new ArrayList<>();
        String sql = "SELECT a.idAuditoria, a.idUsuario, a.accion, a.tablaAfectada, " +
                     "a.descripcion, a.ip, a.fechaAccion, u.nombreCompleto AS nombreUsuario " +
                     "FROM Auditorias a " +
                     "INNER JOIN Usuarios u ON a.idUsuario = u.idUsuario " +
                     "WHERE a.accion = ? " +
                     "ORDER BY a.fechaAccion DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accion);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Auditoria a = new Auditoria();
                a.setIdAuditoria(rs.getInt("idAuditoria"));
                a.setIdUsuario(rs.getInt("idUsuario"));
                a.setAccion(rs.getString("accion"));
                a.setTablaAfectada(rs.getString("tablaAfectada"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setIp(rs.getString("ip"));
                a.setFechaAccion(rs.getTimestamp("fechaAccion"));
                a.setNombreUsuario(rs.getString("nombreUsuario"));
                auditorias.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener auditorías por acción: " + e.getMessage());
            e.printStackTrace();
        }
        return auditorias;
    }

    /**
     * Obtiene auditorías críticas (LOGIN_FALLIDO, ELIMINAR, ANULAR)
     */
    public List<Auditoria> obtenerCriticas() {
        List<Auditoria> auditorias = new ArrayList<>();
        String sql = "SELECT a.idAuditoria, a.idUsuario, a.accion, a.tablaAfectada, " +
                     "a.descripcion, a.ip, a.fechaAccion, u.nombreCompleto AS nombreUsuario " +
                     "FROM Auditorias a " +
                     "INNER JOIN Usuarios u ON a.idUsuario = u.idUsuario " +
                     "WHERE a.accion IN ('ELIMINAR', 'ANULAR', 'LOGIN_FALLIDO') " +
                     "ORDER BY a.fechaAccion DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Auditoria a = new Auditoria();
                a.setIdAuditoria(rs.getInt("idAuditoria"));
                a.setIdUsuario(rs.getInt("idUsuario"));
                a.setAccion(rs.getString("accion"));
                a.setTablaAfectada(rs.getString("tablaAfectada"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setIp(rs.getString("ip"));
                a.setFechaAccion(rs.getTimestamp("fechaAccion"));
                a.setNombreUsuario(rs.getString("nombreUsuario"));
                auditorias.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener auditorías críticas: " + e.getMessage());
            e.printStackTrace();
        }
        return auditorias;
    }

    /**
     * Obtiene auditorías de las últimas N horas
     */
    public List<Auditoria> obtenerRecientes(int horas) {
        List<Auditoria> auditorias = new ArrayList<>();
        String sql = "SELECT a.idAuditoria, a.idUsuario, a.accion, a.tablaAfectada, " +
                     "a.descripcion, a.ip, a.fechaAccion, u.nombreCompleto AS nombreUsuario " +
                     "FROM Auditorias a " +
                     "INNER JOIN Usuarios u ON a.idUsuario = u.idUsuario " +
                     "WHERE a.fechaAccion >= DATEADD(HOUR, ?, SYSDATETIME()) " +
                     "ORDER BY a.fechaAccion DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, -horas);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Auditoria a = new Auditoria();
                a.setIdAuditoria(rs.getInt("idAuditoria"));
                a.setIdUsuario(rs.getInt("idUsuario"));
                a.setAccion(rs.getString("accion"));
                a.setTablaAfectada(rs.getString("tablaAfectada"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setIp(rs.getString("ip"));
                a.setFechaAccion(rs.getTimestamp("fechaAccion"));
                a.setNombreUsuario(rs.getString("nombreUsuario"));
                auditorias.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener auditorías recientes: " + e.getMessage());
            e.printStackTrace();
        }
        return auditorias;
    }
}
