package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Usuario;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Usuario.
 * Maneja operaciones CRUD sobre usuarios (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class UsuarioDAO {
    // ==================== ATRIBUTOS ====================
    private List<Usuario> usuarios;

    // ==================== CONSTRUCTOR ====================

    public UsuarioDAO() {
        this.usuarios = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    public boolean agregar(Usuario usuario) {
        if (usuario != null && usuario.getUsername() != null) {
            return usuarios.add(usuario);
        }
        return false;
    }

    public List<Usuario> obtenerTodos() {
        return new ArrayList<>(usuarios);
    }

    public Usuario obtenerPorId(int idUsuario) {
        return usuarios.stream()
                .filter(u -> u.getIdUsuario() == idUsuario)
                .findFirst()
                .orElse(null);
    }

    public Usuario obtenerPorUsername(String username) {
        return usuarios.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Usuario usuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getIdUsuario() == usuario.getIdUsuario()) {
                usuarios.set(i, usuario);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int idUsuario) {
        return usuarios.removeIf(u -> u.getIdUsuario() == idUsuario);
    }

    // ==================== MÉTODOS EXTRA ====================

    public List<Usuario> obtenerPorRol(String rol) {
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u.getRol() != null && u.getRol().equalsIgnoreCase(rol)) {
                resultado.add(u);
            }
        }
        return resultado;
    }

    public List<Usuario> obtenerActivos() {
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u.isActivo()) {
                resultado.add(u);
            }
        }
        return resultado;
    }

    public List<Usuario> obtenerInactivos() {
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (!u.isActivo()) {
                resultado.add(u);
            }
        }
        return resultado;
    }

    /**
     * Valida el login contra los usuarios almacenados
     */
    public Usuario login(String username, String password) {
        for (Usuario u : usuarios) {
            if (u.login(username, password)) {
                return u;
            }
        }
        return null;
    }

    // ==================== DATOS DE PRUEBA ====================

    private void cargarDatosPrueba() {
        usuarios.add(new Usuario(1, "Admin General", "admin", "1234",
                "ADMIN", "admin@sistema.com", "3001112233", true,
                new Date(), null));

        usuarios.add(new Usuario(2, "Carlos Pérez", "cperez", "ventas123",
                "VENDEDOR", "carlos@sistema.com", "3014445566", true,
                new Date(), null));

        usuarios.add(new Usuario(3, "Laura Gómez", "lgomez", "clave789",
                "GERENTE", "laura@sistema.com", "3029998877", false,
                new Date(), null));
    }
}
