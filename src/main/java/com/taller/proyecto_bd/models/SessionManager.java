package com.taller.proyecto_bd.models;



import com.taller.proyecto_bd.models.Usuario;
import java.util.Date;

/**
 * Clase para gestionar la sesión del usuario actual
 * Patrón Singleton
 */
public class SessionManager {
    
    private static Usuario usuarioActual;
    private static SessionManager instance;
    private static Date inicioSesion;
    
    private SessionManager() {
        // Constructor privado para patrón singleton
    }
    
    /**
     * Obtiene la instancia única de SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Establece el usuario actual de la sesión
     */
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
        inicioSesion = new Date();
        if (usuario != null) {
            usuario.setUltimoAcceso(inicioSesion);
        }
    }
    
    /**
     * Obtiene el usuario actual de la sesión
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }
    
    /**
     * Cierra la sesión actual
     */
    public static void cerrarSesion() {
        usuarioActual = null;
        inicioSesion = null;
    }
    
    /**
     * Obtiene la fecha de inicio de la sesión
     */
    public static Date getInicioSesion() {
        return inicioSesion;
    }
    
    /**
     * Verifica si el usuario actual es administrador (Nivel 1)
     */
    public static boolean esAdministrador() {
        if (usuarioActual == null) return false;
        String rol = usuarioActual.getRol();
        return "ADMIN".equalsIgnoreCase(rol) || 
               "ADMINISTRADOR".equalsIgnoreCase(rol);
    }
    
    /**
     * Verifica si el usuario actual es vendedor (Nivel 2 - Paramétrico)
     */
    public static boolean esVendedor() {
        if (usuarioActual == null) return false;
        String rol = usuarioActual.getRol();
        return "VENDEDOR".equalsIgnoreCase(rol);
    }
    
    /**
     * Verifica si el usuario actual es gerente (Nivel 3 - Solo consultas)
     */
    public static boolean esGerente() {
        if (usuarioActual == null) return false;
        String rol = usuarioActual.getRol();
        return "GERENTE".equalsIgnoreCase(rol);
    }

    /**
     * Verifica si el usuario actual es cliente (Nivel 4 - Solo compras)
     */
    public static boolean esCliente() {
        if (usuarioActual == null) return false;
        String rol = usuarioActual.getRol();
        return "CLIENTE".equalsIgnoreCase(rol);
    }
    
    /**
     * Alias para compatibilidad - Nivel 2
     */
    public static boolean esParametrico() {
        return esVendedor();
    }
    
    /**
     * Alias para compatibilidad - Nivel 3
     */
    public static boolean esEsporadico() {
        return esGerente();
    }
    
    /**
     * Obtiene el nivel del usuario (1, 2, 3)
     */
    public static int getNivelUsuario() {
        if (esAdministrador()) return 1;
        if (esParametrico()) return 2;
        if (esEsporadico()) return 3;
        return 0;
    }
    
    /**
     * Obtiene el nombre del usuario actual
     */
    public static String getNombreUsuarioActual() {
        return usuarioActual != null ? usuarioActual.getNombreCompleto() : "Desconocido";
    }
    
    /**
     * Obtiene el username del usuario actual
     */
    public static String getUsernameActual() {
        return usuarioActual != null ? usuarioActual.getUsername() : "";
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    public static int getIdUsuarioActual() {
        return usuarioActual != null ? usuarioActual.getIdUsuario() : 0;
    }
    
    /**
     * Obtiene el rol del usuario actual
     */
    public static String getRolUsuarioActual() {
        return usuarioActual != null ? usuarioActual.getRol() : "NINGUNO";
    }
    
    /**
     * Obtiene el email del usuario actual
     */
    public static String getEmailUsuarioActual() {
        return usuarioActual != null ? usuarioActual.getEmail() : "";
    }
    
    /**
     * Verifica si el usuario tiene permiso para acceder a una funcionalidad
     * 
     * Niveles de acceso:
     * - ADMINISTRADOR (Nivel 1): Acceso total a todo
     * - PARAMETRICO (Nivel 2): No puede acceder a Usuarios ni Bitácora
     * - ESPORADICO (Nivel 3): Solo acceso a Consultas
     */
    public static boolean tienePermiso(String funcionalidad) {
        if (!haySesionActiva()) {
            return false;
        }
        
        // Administrador tiene acceso a todo
        if (esAdministrador()) {
            return true;
        }
        
        // Normalizar la funcionalidad a mayúsculas
        String func = funcionalidad.toUpperCase();
        
        // Paramétrico no puede acceder a usuarios ni bitácora
        if (esParametrico()) {
            return !func.equals("USUARIOS") && 
                   !func.equals("BITACORA") &&
                   !func.equals("AUDITORIA");
        }
        
        // Esporádico solo tiene acceso a consultas
        if (esEsporadico()) {
            return func.equals("CONSULTAS") || 
                   func.equals("REPORTES") ||
                   func.equals("CONSULTAS_REPORTES");
        }
        
        return false;
    }
    
    /**
     * Verifica si el usuario puede realizar operaciones CRUD
     */
    public static boolean puedeCrear() {
        return esAdministrador() || esParametrico();
    }
    
    public static boolean puedeEditar() {
        return esAdministrador() || esParametrico();
    }
    
    public static boolean puedeEliminar() {
        return esAdministrador(); // Solo el admin puede eliminar
    }
    
    public static boolean puedeLeer() {
        return haySesionActiva(); // Todos pueden leer
    }
    
    /**
     * Obtiene una descripción del nivel de acceso
     */
    public static String getDescripcionNivel() {
        if (esAdministrador()) {
            return "Nivel 1 - Administrador (Acceso Total)";
        } else if (esParametrico()) {
            return "Nivel 2 - Paramétrico (Sin acceso a Usuarios/Bitácora)";
        } else if (esEsporadico()) {
            return "Nivel 3 - Esporádico (Solo Consultas)";
        }
        return "Sin nivel asignado";
    }
    
    /**
     * Valida que la sesión esté activa, lanza excepción si no lo está
     */
    public static void validarSesionActiva() throws Exception {
        if (!haySesionActiva()) {
            throw new Exception("No hay una sesión activa. Por favor inicie sesión.");
        }
    }
    
    /**
     * Valida que el usuario tenga el permiso especificado
     */
    public static void validarPermiso(String funcionalidad) throws Exception {
        validarSesionActiva();
        if (!tienePermiso(funcionalidad)) {
            throw new Exception("No tiene permisos para acceder a: " + funcionalidad);
        }
    }
    
    /**
     * Obtiene información completa de la sesión en formato String
     */
    public static String getInfoSesion() {
        if (!haySesionActiva()) {
            return "Sin sesión activa";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Usuario: ").append(getNombreUsuarioActual()).append("\n");
        info.append("Username: ").append(getUsernameActual()).append("\n");
        info.append("Rol: ").append(getRolUsuarioActual()).append("\n");
        info.append("Nivel: ").append(getDescripcionNivel()).append("\n");
        info.append("Inicio sesión: ").append(inicioSesion).append("\n");
        
        return info.toString();
    }
}