package com.taller.proyecto_bd.controllers;

import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.models.Usuario;

import java.util.List;

/**
 * Controlador para la lógica de usuarios.
 * Maneja login, roles, activación/bloqueo y coordinación con DAO.
 *
 * @author Sistema
 * @version 1.0
 */
public class UsuarioController {
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Intentar login de un usuario
     */
    public Usuario login(String username, String password) {
        Usuario user = usuarioDAO.login(username, password);
        if (user != null) {
            if (!user.isActivo()) {
                throw new IllegalStateException("La cuenta está bloqueada.");
            }
            return user;
        }
        return null;
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
        Usuario u = usuarioDAO.obtenerPorId(idUsuario);
        if (u != null && u.isActivo()) {
            u.bloquear();
            return usuarioDAO.actualizar(u);
        }
        return false;
    }

    /**
     * Activar un usuario (reactivar su cuenta)
     */
    public boolean activarUsuario(int idUsuario) {
        Usuario u = usuarioDAO.obtenerPorId(idUsuario);
        if (u != null && !u.isActivo()) {
            u.activar();
            return usuarioDAO.actualizar(u);
        }
        return false;
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
