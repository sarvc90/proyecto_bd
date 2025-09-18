package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa la entidad Categoría en el sistema de electrodomésticos
 * @author Sistema de Electrodomésticos
 * @version 1.0
 */
public class Categoria {
    // ==================== ATRIBUTOS ====================
    private int idCategoria;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private Date fechaRegistro;
    private Date fechaUltimaActualizacion;
    private int nivel; // 1: Principal, 2: Subcategoría, etc.
    private Integer idCategoriaPadre; // Para categorías jerárquicas
    private String rutaCompleta; // Ruta jerárquica completa
    private int cantidadProductos; // Contador de productos (para joins)

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor por defecto
     */
    public Categoria() {
        this.fechaRegistro = new Date();
        this.fechaUltimaActualizacion = new Date();
        this.activo = true;
        this.nivel = 1;
        this.cantidadProductos = 0;
    }

    /**
     * Constructor con parámetros básicos (sin ID - para nuevas categorías)
     */
    public Categoria(String codigo, String nombre, String descripcion) {
        this();  // Llama al constructor por defecto
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Constructor para categorías con padre
     */
    public Categoria(String codigo, String nombre, String descripcion,
                     Integer idCategoriaPadre, int nivel) {
        this(codigo, nombre, descripcion);
        this.idCategoriaPadre = idCategoriaPadre;
        this.nivel = nivel;
    }

    /**
     * Constructor completo (con ID - para categorías existentes)
     */
    public Categoria(int idCategoria, String codigo, String nombre,
                     String descripcion, boolean activo, Date fechaRegistro,
                     int nivel, Integer idCategoriaPadre, String rutaCompleta,
                     int cantidadProductos) {
        this.idCategoria = idCategoria;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.fechaUltimaActualizacion = new Date();
        this.nivel = nivel;
        this.idCategoriaPadre = idCategoriaPadre;
        this.rutaCompleta = rutaCompleta;
        this.cantidadProductos = cantidadProductos;
    }

    // ==================== GETTERS ====================

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public Date getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public int getNivel() {
        return nivel;
    }

    public Integer getIdCategoriaPadre() {
        return idCategoriaPadre;
    }

    public String getRutaCompleta() {
        return rutaCompleta;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    // ==================== SETTERS ====================

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public void setCodigo(String codigo) {
        if (codigo != null && !codigo.trim().isEmpty()) {
            this.codigo = codigo.trim().toUpperCase();
            actualizarFechaModificacion();
        }
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
            actualizarFechaModificacion();
        }
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        actualizarFechaModificacion();
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
        actualizarFechaModificacion();
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setFechaUltimaActualizacion(Date fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public void setNivel(int nivel) {
        if (nivel >= 1) {
            this.nivel = nivel;
            actualizarFechaModificacion();
        }
    }

    public void setIdCategoriaPadre(Integer idCategoriaPadre) {
        this.idCategoriaPadre = idCategoriaPadre;
        actualizarFechaModificacion();
    }

    public void setRutaCompleta(String rutaCompleta) {
        this.rutaCompleta = rutaCompleta;
    }

    public void setCantidadProductos(int cantidadProductos) {
        if (cantidadProductos >= 0) {
            this.cantidadProductos = cantidadProductos;
        }
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Verifica si la categoría es una categoría principal (sin padre)
     */
    public boolean esCategoriaPrincipal() {
        return idCategoriaPadre == null || idCategoriaPadre == 0;
    }

    /**
     * Verifica si la categoría es una subcategoría
     */
    public boolean esSubcategoria() {
        return !esCategoriaPrincipal();
    }

    /**
     * Verifica si la categoría tiene productos asociados
     */
    public boolean tieneProductos() {
        return cantidadProductos > 0;
    }

    /**
     * Verifica si la categoría puede ser eliminada (sin productos)
     */
    public boolean puedeEliminarse() {
        return !tieneProductos();
    }

    /**
     * Incrementa el contador de productos
     */
    public void agregarProducto() {
        cantidadProductos++;
    }

    /**
     * Decrementa el contador de productos
     */
    public void removerProducto() {
        if (cantidadProductos > 0) {
            cantidadProductos--;
        }
    }

    /**
     * Obtiene el nombre completo con código
     */
    public String getNombreCompleto() {
        return "[" + codigo + "] " + nombre;
    }

    /**
     * Obtiene información del nivel jerárquico
     */
    public String getDescripcionNivel() {
        switch (nivel) {
            case 1: return "Categoría Principal";
            case 2: return "Subcategoría Nivel 1";
            case 3: return "Subcategoría Nivel 2";
            default: return "Nivel " + nivel;
        }
    }

    /**
     * Genera una ruta jerárquica básica
     */
    public String generarRutaBasica() {
        if (rutaCompleta != null && !rutaCompleta.isEmpty()) {
            return rutaCompleta;
        }
        return nombre;
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que todos los campos obligatorios estén completos
     */
    public boolean validarDatosObligatorios() {
        return codigo != null && !codigo.trim().isEmpty() &&
                nombre != null && !nombre.trim().isEmpty();
    }

    /**
     * Valida la coherencia jerárquica
     */
    public boolean validarJerarquia() {
        // Si tiene padre, no puede ser nivel 1
        if (idCategoriaPadre != null && idCategoriaPadre > 0 && nivel == 1) {
            return false;
        }
        // Si no tiene padre, debe ser nivel 1
        if ((idCategoriaPadre == null || idCategoriaPadre == 0) && nivel != 1) {
            return false;
        }
        return nivel >= 1 && nivel <= 5; // Máximo 5 niveles de profundidad
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Actualiza la fecha de última modificación
     */
    private void actualizarFechaModificacion() {
        this.fechaUltimaActualizacion = new Date();
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Categoria{" +
                "ID=" + idCategoria +
                ", Código='" + codigo + '\'' +
                ", Nombre='" + nombre + '\'' +
                ", Nivel=" + nivel + " (" + getDescripcionNivel() + ")" +
                ", Productos=" + cantidadProductos +
                ", Activo=" + activo +
                (esSubcategoria() ? ", PadreID=" + idCategoriaPadre : "") +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Categoria categoria = (Categoria) obj;

        // Dos categorías son iguales si tienen el mismo código
        return codigo != null && codigo.equals(categoria.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }
}