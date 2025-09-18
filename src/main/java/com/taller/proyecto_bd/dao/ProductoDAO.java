package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Producto.
 * Maneja operaciones CRUD sobre productos (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class ProductoDAO {
    // ==================== ATRIBUTOS ====================
    private List<Producto> productos;

    // ==================== CONSTRUCTOR ====================

    public ProductoDAO() {
        this.productos = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    /**
     * Agregar un nuevo producto
     */
    public boolean agregar(Producto producto) {
        if (producto != null && producto.validarDatosObligatorios() && producto.validarStock()) {
            return productos.add(producto);
        }
        return false;
    }

    /**
     * Obtener todos los productos
     */
    public List<Producto> obtenerTodos() {
        return new ArrayList<>(productos);
    }

    /**
     * Buscar producto por ID
     */
    public Producto obtenerPorId(int id) {
        return productos.stream()
                .filter(p -> p.getIdProducto() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Buscar producto por código
     */
    public Producto obtenerPorCodigo(String codigo) {
        return productos.stream()
                .filter(p -> p.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    /**
     * Actualizar un producto existente
     */
    public boolean actualizar(Producto producto) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getIdProducto() == producto.getIdProducto()) {
                productos.set(i, producto);
                return true;
            }
        }
        return false;
    }

    /**
     * Eliminar producto por ID
     */
    public boolean eliminar(int id) {
        return productos.removeIf(p -> p.getIdProducto() == id);
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Cargar datos de prueba
     */
    private void cargarDatosPrueba() {
        productos.add(new Producto(1, "P001", "Refrigerador", "Refrigerador grande",
                "LG", "X200", 1, 1200, 1500,
                10, 2, 20, "UNIDAD", true, null,
                24, "Bodega A"));
        productos.add(new Producto(2, "P002", "Televisor", "Televisor 55 pulgadas",
                "Samsung", "QLED55", 2, 800, 1200,
                5, 1, 15, "UNIDAD", true, null,
                12, "Bodega B"));
    }

    /**
     * Buscar productos activos
     */
    public List<Producto> obtenerActivos() {
        List<Producto> activos = new ArrayList<>();
        for (Producto p : productos) {
            if (p.isActivo()) {
                activos.add(p);
            }
        }
        return activos;
    }

    /**
     * Buscar productos por nombre o marca
     */
    public List<Producto> buscarPorNombreOMarca(String texto) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                    p.getMarca().toLowerCase().contains(texto.toLowerCase())) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Obtener productos con stock bajo
     */
    public List<Producto> obtenerStockBajo() {
        List<Producto> stockBajo = new ArrayList<>();
        for (Producto p : productos) {
            if (p.necesitaReabastecimiento()) {
                stockBajo.add(p);
            }
        }
        return stockBajo;
    }
}
