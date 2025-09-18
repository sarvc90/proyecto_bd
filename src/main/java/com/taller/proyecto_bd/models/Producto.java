package com.taller.proyecto_bd.models;

import java.util.Date;

/**
 * Clase que representa la entidad Producto en el sistema de electrodomésticos
 * @author Sistema de Electrodomésticos
 * @version 1.0
 */
public class Producto {
    // ==================== ATRIBUTOS ====================
    private int idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String marca;
    private String modelo;
    private int idCategoria;
    private double precioCompra;
    private double precioVenta;
    private int stockActual;
    private int stockMinimo;
    private int stockMaximo;
    private String unidadMedida;
    private boolean activo;
    private Date fechaRegistro;
    private Date fechaUltimaActualizacion;
    private int garantiaMeses;
    private String ubicacionAlmacen;

    // Atributos calculados o auxiliares
    private String nombreCategoria; // Para joins con categoria

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor por defecto
     */
    public Producto() {
        this.fechaRegistro = new Date();
        this.fechaUltimaActualizacion = new Date();
        this.activo = true;
        this.stockActual = 0;
        this.stockMinimo = 0;
        this.stockMaximo = 100;
        this.unidadMedida = "UNIDAD";
        this.garantiaMeses = 12; // Garantía default de 1 año
    }

    /**
     * Constructor con parámetros básicos (sin ID - para nuevos productos)
     */
    public Producto(String codigo, String nombre, String marca, String modelo,
                    int idCategoria, double precioCompra, double precioVenta,
                    int stockActual, int stockMinimo) {
        this();  // Llama al constructor por defecto
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.modelo = modelo;
        this.idCategoria = idCategoria;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    /**
     * Constructor completo (con ID - para productos existentes)
     */
    public Producto(int idProducto, String codigo, String nombre, String descripcion,
                    String marca, String modelo, int idCategoria,
                    double precioCompra, double precioVenta,
                    int stockActual, int stockMinimo, int stockMaximo,
                    String unidadMedida, boolean activo, Date fechaRegistro,
                    int garantiaMeses, String ubicacionAlmacen) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.marca = marca;
        this.modelo = modelo;
        this.idCategoria = idCategoria;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.unidadMedida = unidadMedida;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.fechaUltimaActualizacion = new Date();
        this.garantiaMeses = garantiaMeses;
        this.ubicacionAlmacen = ubicacionAlmacen;
    }

    // ==================== GETTERS ====================

    public int getIdProducto() {
        return idProducto;
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

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public int getStockActual() {
        return stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public int getStockMaximo() {
        return stockMaximo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
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

    public int getGarantiaMeses() {
        return garantiaMeses;
    }

    public String getUbicacionAlmacen() {
        return ubicacionAlmacen;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    // ==================== SETTERS ====================

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
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

    public void setMarca(String marca) {
        if (marca != null && !marca.trim().isEmpty()) {
            this.marca = marca.trim();
            actualizarFechaModificacion();
        }
    }

    public void setModelo(String modelo) {
        if (modelo != null && !modelo.trim().isEmpty()) {
            this.modelo = modelo.trim();
            actualizarFechaModificacion();
        }
    }

    public void setIdCategoria(int idCategoria) {
        if (idCategoria > 0) {
            this.idCategoria = idCategoria;
            actualizarFechaModificacion();
        }
    }

    public void setPrecioCompra(double precioCompra) {
        if (precioCompra >= 0) {
            this.precioCompra = precioCompra;
            actualizarFechaModificacion();
        }
    }

    public void setPrecioVenta(double precioVenta) {
        if (precioVenta >= 0) {
            this.precioVenta = precioVenta;
            actualizarFechaModificacion();
        }
    }

    public void setStockActual(int stockActual) {
        if (stockActual >= 0) {
            this.stockActual = stockActual;
            actualizarFechaModificacion();
        }
    }

    public void setStockMinimo(int stockMinimo) {
        if (stockMinimo >= 0) {
            this.stockMinimo = stockMinimo;
            actualizarFechaModificacion();
        }
    }

    public void setStockMaximo(int stockMaximo) {
        if (stockMaximo >= 0) {
            this.stockMaximo = stockMaximo;
            actualizarFechaModificacion();
        }
    }

    public void setUnidadMedida(String unidadMedida) {
        if (unidadMedida != null && !unidadMedida.trim().isEmpty()) {
            this.unidadMedida = unidadMedida.trim().toUpperCase();
            actualizarFechaModificacion();
        }
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

    public void setGarantiaMeses(int garantiaMeses) {
        if (garantiaMeses >= 0) {
            this.garantiaMeses = garantiaMeses;
            actualizarFechaModificacion();
        }
    }

    public void setUbicacionAlmacen(String ubicacionAlmacen) {
        this.ubicacionAlmacen = ubicacionAlmacen != null ? ubicacionAlmacen.trim() : "";
        actualizarFechaModificacion();
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Calcula la utilidad del producto
     */
    public double getUtilidad() {
        return precioVenta - precioCompra;
    }

    /**
     * Calcula el margen de utilidad en porcentaje
     */
    public double getMargenUtilidad() {
        if (precioCompra > 0) {
            return ((precioVenta - precioCompra) / precioCompra) * 100;
        }
        return 0;
    }

    /**
     * Calcula el valor total del inventario actual
     */
    public double getValorInventario() {
        return stockActual * precioCompra;
    }

    /**
     * Calcula el valor potencial de venta del inventario
     */
    public double getValorVentaInventario() {
        return stockActual * precioVenta;
    }

    /**
     * Verifica si hay stock disponible
     */
    public boolean hayStock() {
        return activo && stockActual > 0;
    }

    /**
     * Verifica si hay suficiente stock para una cantidad
     */
    public boolean hayStockSuficiente(int cantidad) {
        return activo && stockActual >= cantidad;
    }

    /**
     * Verifica si el stock está por debajo del mínimo
     */
    public boolean necesitaReabastecimiento() {
        return stockActual <= stockMinimo;
    }

    /**
     * Verifica si el stock está al máximo
     */
    public boolean stockAlMaximo() {
        return stockActual >= stockMaximo;
    }

    /**
     * Realiza una venta descontando del stock
     */
    public boolean vender(int cantidad) {
        if (hayStockSuficiente(cantidad)) {
            stockActual -= cantidad;
            actualizarFechaModificacion();
            return true;
        }
        return false;
    }

    /**
     * Agrega stock (compra o devolución)
     */
    public boolean agregarStock(int cantidad) {
        if (cantidad > 0 && (stockActual + cantidad) <= stockMaximo) {
            stockActual += cantidad;
            actualizarFechaModificacion();
            return true;
        }
        return false;
    }

    /**
     * Ajusta el stock a un valor específico (inventario físico)
     */
    public void ajustarStock(int nuevoStock) {
        if (nuevoStock >= 0) {
            stockActual = nuevoStock;
            actualizarFechaModificacion();
        }
    }

    /**
     * Obtiene el nombre completo del producto
     */
    public String getNombreCompleto() {
        return marca + " " + modelo + " - " + nombre;
    }

    /**
     * Obtiene la descripción de la garantía
     */
    public String getDescripcionGarantia() {
        if (garantiaMeses == 0) {
            return "Sin garantía";
        } else if (garantiaMeses < 12) {
            return garantiaMeses + " meses";
        } else if (garantiaMeses == 12) {
            return "1 año";
        } else {
            int anos = garantiaMeses / 12;
            int meses = garantiaMeses % 12;
            if (meses == 0) {
                return anos + " años";
            } else {
                return anos + " años y " + meses + " meses";
            }
        }
    }

    /**
     * Calcula el precio con IVA (12% Ecuador)
     */
    public double getPrecioConIVA() {
        return precioVenta * 1.12;
    }

    /**
     * Calcula el IVA del precio de venta
     */
    public double getMontoIVA() {
        return precioVenta * 0.12;
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que todos los campos obligatorios estén completos
     */
    public boolean validarDatosObligatorios() {
        return codigo != null && !codigo.trim().isEmpty() &&
                nombre != null && !nombre.trim().isEmpty() &&
                marca != null && !marca.trim().isEmpty() &&
                idCategoria > 0 &&
                precioCompra >= 0 &&
                precioVenta >= 0 &&
                precioVenta >= precioCompra;
    }

    /**
     * Valida la coherencia del stock
     */
    public boolean validarStock() {
        return stockActual >= 0 &&
                stockMinimo >= 0 &&
                stockMaximo > 0 &&
                stockMaximo >= stockMinimo;
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
        return "Producto{" +
                "ID=" + idProducto +
                ", Código='" + codigo + '\'' +
                ", Nombre='" + getNombreCompleto() + '\'' +
                ", Stock=" + stockActual + "/" + stockMaximo +
                ", Precio Compra=$" + String.format("%.2f", precioCompra) +
                ", Precio Venta=$" + String.format("%.2f", precioVenta) +
                ", Utilidad=$" + String.format("%.2f", getUtilidad()) +
                " (" + String.format("%.1f", getMargenUtilidad()) + "%)" +
                ", Garantía=" + getDescripcionGarantia() +
                ", Activo=" + activo +
                (necesitaReabastecimiento() ? " [REABASTECER]" : "") +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Producto producto = (Producto) obj;

        // Dos productos son iguales si tienen el mismo código
        return codigo != null && codigo.equals(producto.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }
}