package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Usuario;
import com.taller.proyecto_bd.utils.Encriptacion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Usuario.
 * Maneja operaciones CRUD sobre usuarios (en memoria por ahora).
 */
public class UsuarioDAO {

    // ==================== ATRIBUTOS ====================
    private List<Usuario> usuarios;
    private static int idCounter = 100; // IDs únicos en memoria

    // ==================== SINGLETON ====================
    private static UsuarioDAO instance;

    private UsuarioDAO() {
        this.usuarios = new ArrayList<>();
        cargarDatosPrueba();
    }

    public static UsuarioDAO getInstance() {
        if (instance == null) {
            instance = new UsuarioDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    public boolean agregar(Usuario usuario) {
        if (usuario != null && usuario.getUsername() != null) {
            if (usuario.getIdUsuario() == 0) {
                usuario.setIdUsuario(idCounter++);
            }
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
        if (username == null) return null;
        return usuarios.stream()
                .filter(u -> u.getUsername() != null &&
                        u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() <= 0) return false;
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
     * Valida login con hash SHA-256.
     * No valida si el usuario está activo, eso se debe hacer en el controlador.
     */
    public Usuario login(String username, String passwordPlana) {
        if (username == null || passwordPlana == null) return null;

        String hashedPassword = Encriptacion.encriptarSHA256(passwordPlana);

        for (Usuario u : usuarios) {
            if (u.getUsername() != null &&
                    u.getUsername().equalsIgnoreCase(username) &&
                    u.getPassword() != null &&
                    u.getPassword().equals(hashedPassword)) {

                u.setUltimoAcceso(new Date());
                return u;
            }
        }
        return null;
    }

    // ==================== DATOS DE PRUEBA ====================
    private void cargarDatosPrueba() {
        usuarios.add(new Usuario(idCounter++, "Admin General", "admin",
                Encriptacion.encriptarSHA256("1234"),
                "ADMIN", "admin@sistema.com", "3001112233", true,
                new Date(), null));

        usuarios.add(new Usuario(idCounter++, "Carlos Pérez", "cperez",
                Encriptacion.encriptarSHA256("ventas123"),
                "VENDEDOR", "carlos@sistema.com", "3014445566", true,
                new Date(), null));

        usuarios.add(new Usuario(idCounter++, "Laura Gómez", "lgomez",
                Encriptacion.encriptarSHA256("clave789"),
                "GERENTE", "laura@sistema.com", "3029998877", false,
                new Date(), null));
    }
}
