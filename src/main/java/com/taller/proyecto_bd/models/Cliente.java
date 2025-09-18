package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa la entidad Cliente en el sistema de electrodomésticos
 * @author Sistema de Electrodomésticos
 * @version 1.0
 */
public class Cliente {
    // ==================== ATRIBUTOS ====================
    private int idCliente;
    private String cedula;
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String email;
    private Date fechaRegistro;
    private boolean activo;
    private double limiteCredito;
    private double saldoPendiente;

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor por defecto
     */
    public Cliente() {
        this.fechaRegistro = new Date();
        this.activo = true;
        this.limiteCredito = 0.0;
        this.saldoPendiente = 0.0;
    }

    /**
     * Constructor con parámetros básicos (sin ID - para nuevos clientes)
     */
    public Cliente(String cedula, String nombre, String apellido,
                   String direccion, String telefono, String email) {
        this();  // Llama al constructor por defecto
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
    }

    /**
     * Constructor completo (con ID - para clientes existentes)
     */
    public Cliente(int idCliente, String cedula, String nombre, String apellido,
                   String direccion, String telefono, String email,
                   Date fechaRegistro, boolean activo, double limiteCredito,
                   double saldoPendiente) {
        this.idCliente = idCliente;
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
        this.limiteCredito = limiteCredito;
        this.saldoPendiente = saldoPendiente;
    }

    // ==================== GETTERS ====================

    public int getIdCliente() {
        return idCliente;
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public double getSaldoPendiente() {
        return saldoPendiente;
    }

    // ==================== SETTERS ====================

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public void setCedula(String cedula) {
        if (cedula != null && !cedula.trim().isEmpty()) {
            this.cedula = cedula.trim();
        }
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
        }
    }

    public void setApellido(String apellido) {
        if (apellido != null && !apellido.trim().isEmpty()) {
            this.apellido = apellido.trim();
        }
    }

    public void setDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            this.direccion = direccion.trim();
        }
    }

    public void setTelefono(String telefono) {
        if (telefono != null && !telefono.trim().isEmpty()) {
            this.telefono = telefono.trim();
        }
    }

    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            this.email = email.trim().toLowerCase();
        }
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setLimiteCredito(double limiteCredito) {
        if (limiteCredito >= 0) {
            this.limiteCredito = limiteCredito;
        }
    }

    public void setSaldoPendiente(double saldoPendiente) {
        if (saldoPendiente >= 0) {
            this.saldoPendiente = saldoPendiente;
        }
    }

    // ==================== MÉTODOS ÚTILES ====================

    /**
     * Obtiene el nombre completo del cliente
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Verifica si el cliente tiene crédito disponible
     */
    public boolean tieneCreditoDisponible() {
        return (limiteCredito - saldoPendiente) > 0;
    }

    /**
     * Obtiene el monto de crédito disponible
     */
    public double getCreditoDisponible() {
        double disponible = limiteCredito - saldoPendiente;
        return disponible > 0 ? disponible : 0;
    }

    /**
     * Verifica si el cliente puede realizar una compra a crédito
     */
    public boolean puedeComprarACredito(double montoCompra) {
        return activo && (getCreditoDisponible() >= montoCompra);
    }

    /**
     * Actualiza el saldo pendiente del cliente
     */
    public boolean actualizarSaldo(double monto, boolean esAbono) {
        if (esAbono) {
            // Si es un abono, resta del saldo pendiente
            if (monto <= saldoPendiente) {
                saldoPendiente -= monto;
                return true;
            }
        } else {
            // Si es una nueva compra, suma al saldo pendiente
            if (puedeComprarACredito(monto)) {
                saldoPendiente += monto;
                return true;
            }
        }
        return false;
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que todos los campos obligatorios estén completos
     */
    public boolean validarDatosObligatorios() {
        return cedula != null && !cedula.trim().isEmpty() &&
                nombre != null && !nombre.trim().isEmpty() &&
                apellido != null && !apellido.trim().isEmpty() &&
                telefono != null && !telefono.trim().isEmpty();
    }

    /**
     * Valida el formato básico del email
     */
    public boolean validarEmail() {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email es opcional
        }
        return email.contains("@") && email.contains(".");
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "Cliente{" +
                "ID=" + idCliente +
                ", Cédula='" + cedula + '\'' +
                ", Nombre='" + getNombreCompleto() + '\'' +
                ", Teléfono='" + telefono + '\'' +
                ", Email='" + email + '\'' +
                ", Activo=" + activo +
                ", Límite Crédito=$" + String.format("%.2f", limiteCredito) +
                ", Saldo Pendiente=$" + String.format("%.2f", saldoPendiente) +
                ", Crédito Disponible=$" + String.format("%.2f", getCreditoDisponible()) +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Cliente cliente = (Cliente) obj;

        // Dos clientes son iguales si tienen la misma cédula
        return cedula != null && cedula.equals(cliente.cedula);
    }

    @Override
    public int hashCode() {
        return cedula != null ? cedula.hashCode() : 0;
    }
}