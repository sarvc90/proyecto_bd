package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa un Usuario del sistema de electrodomésticos
 * Puede ser administrador, vendedor u otro rol del sistema.
 * Maneja login, roles y estado de la cuenta.
 *
 * @author Sistema
 * @version 1.0
 */
public class Usuario {
    // ==================== ATRIBUTOS ====================
    private int idUsuario;
    private String nombreCompleto;
    private String username;         // nombre de usuario para login
    private String password;         // contraseña (idealmente cifrada)
    private String rol;              // "ADMIN", "VENDEDOR", "GERENTE"
    private String email;
    private String telefono;
    private boolean activo;          // true = activo, false = bloqueado
    private Date fechaRegistro;
    private Date ultimoAcceso;

    // ==================== CONSTRUCTORES ====================

    public Usuario() {
        this.fechaRegistro = new Date();
        this.activo = true;
    }

    public Usuario(String nombreCompleto, String username, String password, String rol) {
        this();
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public Usuario(int idUsuario, String nombreCompleto, String username, String password,
                   String rol, String email, String telefono, boolean activo,
                   Date fechaRegistro, Date ultimoAcceso) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.email = email;
        this.telefono = telefono;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.ultimoAcceso = ultimoAcceso;
    }

    // ==================== GETTERS ====================

    public int getIdUsuario() { return idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public boolean isActivo() { return activo; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public Date getUltimoAcceso() { return ultimoAcceso; }

    // ==================== SETTERS ====================

    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRol(String rol) { this.rol = rol; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setUltimoAcceso(Date ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Verifica si el usuario tiene un rol específico
     */
    public boolean tieneRol(String rolBuscado) {
        return rol != null && rol.equalsIgnoreCase(rolBuscado);
    }

    /**
     * Simula el inicio de sesión verificando credenciales
     */
    public boolean login(String user, String pass) {
        if (!activo) return false;
        boolean ok = username.equals(user) && password.equals(pass);
        if (ok) {
            this.ultimoAcceso = new Date();
        }
        return ok;
    }

    /**
     * Bloquea la cuenta del usuario
     */
    public void bloquear() {
        this.activo = false;
    }

    /**
     * Activa la cuenta del usuario
     */
    public void activar() {
        this.activo = true;
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Usuario{" +
                "ID=" + idUsuario +
                ", Nombre='" + nombreCompleto + '\'' +
                ", Username='" + username + '\'' +
                ", Rol='" + rol + '\'' +
                ", Activo=" + activo +
                ", Último acceso=" + ultimoAcceso +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Usuario usuario = (Usuario) obj;
        return username != null && username.equals(usuario.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
