package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.models.Usuario;

import java.util.List;

/**
 * Controlador para la lógica de usuarios.
 * Maneja login, roles, activación/bloqueo y coordinación con DAO.
 *
 * @author Sistema
 * @version 1.2 - Corregido manejo de usuarios bloqueados
 */
public class UsuarioController {
    private UsuarioDAO usuarioDAO = UsuarioDAO.getInstance();

    /**
     * Intentar login de un usuario
     * Retorna null si las credenciales son incorrectas o si el usuario está bloqueado
     */
    public Usuario login(String username, String password) {
        Usuario user = usuarioDAO.login(username, password);

        // Si no existe el usuario o credenciales incorrectas
        if (user == null) {
            return null;
        }

        // Si el usuario existe pero está bloqueado
        if (!user.isActivo()) {
            return null; // CORREGIDO: retornar null en lugar de lanzar excepción
        }

        return user;
    }

    /**
     * Registrar un nuevo usuario
     */
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getUsername() == null || usuario.getPassword() == null) {
            return false;
        }
        return usuarioDAO.agregar(usuario);
    }

    /**
     * Bloquear un usuario (desactivar su cuenta)
     */
    public boolean bloquearUsuario(int idUsuario) {
        try {
            Usuario u = usuarioDAO.obtenerPorId(idUsuario);
            if (u == null) {
                System.out.println("Usuario no encontrado ID: " + idUsuario);
                return false;
            }

            u.setActivo(false);
            boolean resultado = usuarioDAO.actualizar(u);

            if (resultado) {
                System.out.println("✓ Usuario " + u.getUsername() + " bloqueado exitosamente");
            } else {
                System.out.println("✗ Error al bloquear usuario " + u.getUsername());
            }

            return resultado;
        } catch (Exception e) {
            System.out.println("Error en bloquearUsuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activar un usuario (reactivar su cuenta)
     */
    public boolean activarUsuario(int idUsuario) {
        Usuario u = usuarioDAO.obtenerPorId(idUsuario);
        if (u == null) {
            return false;
        }

        // CORREGIDO: activar sin importar el estado actual
        u.setActivo(true);
        return usuarioDAO.actualizar(u);
    }

    /**
     * Consultar usuarios activos
     */
    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioDAO.obtenerActivos();
    }

    /**
     * Consultar usuarios inactivos
     */
    public List<Usuario> obtenerUsuariosInactivos() {
        return usuarioDAO.obtenerInactivos();
    }

    /**
     * Consultar usuarios por rol
     */
    public List<Usuario> obtenerUsuariosPorRol(String rol) {
        return usuarioDAO.obtenerPorRol(rol);
    }

    /**
     * Obtener todos los usuarios
     */
    public List<Usuario> obtenerTodos() {
        return usuarioDAO.obtenerTodos();
    }
}