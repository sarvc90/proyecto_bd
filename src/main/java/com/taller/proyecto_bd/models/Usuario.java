package com.taller.proyecto_bd.models;

import com.taller.proyecto_bd.utils.Encriptacion; // Importar la clase de encriptación
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

    // Constructor modificado para hashear la contraseña al crear el objeto
    public Usuario(String nombreCompleto, String username, String passwordPlana, String rol) {
        this();
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        // Hashear la contraseña al momento de la creación del objeto
        this.password = Encriptacion.encriptarSHA256(passwordPlana);
        this.rol = rol;
    }

    // Constructor completo (con ID - para usuarios existentes)
    // Asume que la contraseña ya viene hasheada si se carga de BD
    public Usuario(int idUsuario, String nombreCompleto, String username, String passwordHash,
                   String rol, String email, String telefono, boolean activo,
                   Date fechaRegistro, Date ultimoAcceso) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        this.password = passwordHash; // Aquí se espera el hash, no la contraseña plana
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
    public String getPassword() { return password; } // Retorna el hash
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
    // Setter para la contraseña: debería recibir la contraseña plana y hashearla
    public void setPassword(String passwordPlana) { this.password = Encriptacion.encriptarSHA256(passwordPlana); }
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
     * NOTA: Este método en el modelo no debería hacer la validación de contraseña,
     * eso es responsabilidad del DAO/Controller. Aquí solo se actualiza el acceso.
     */
    public boolean login(String user, String pass) {
        // Este método en el modelo no es el lugar ideal para la lógica de login completa.
        // La validación de credenciales y estado activo se hace en UsuarioDAO y UsuarioController.
        // Aquí solo se actualiza el último acceso si ya se validó externamente.
        if (username.equals(user) && password.equals(pass)) { // Esta comparación es con el hash
            this.ultimoAcceso = new Date();
            return true;
        }
        return false;
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

    public void setNombre(String administradorActualizado) {
        this.nombreCompleto = administradorActualizado;
    }
    public String getNombre() {
        return nombreCompleto;
    }
}
