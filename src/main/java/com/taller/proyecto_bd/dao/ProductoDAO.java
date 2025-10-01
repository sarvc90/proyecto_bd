package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Producto.
 * Maneja operaciones CRUD sobre productos (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.2 - Fix de validaciones nulas y manejo seguro de IDs
 */
public class ProductoDAO {
    // ==================== ATRIBUTOS ====================
    private final List<Producto> productos;
    private static ProductoDAO instance; // Singleton
    private static int idCounter = 1;

    // ==================== CONSTRUCTOR ====================
    private ProductoDAO() {
        this.productos = new ArrayList<>();
        cargarDatosPrueba();
    }

    public static ProductoDAO getInstance() {
        if (instance == null) {
            instance = new ProductoDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================
    public boolean agregar(Producto producto) {
        if (producto != null && producto.validarDatosObligatorios() && producto.validarStock()) {
            if (producto.getIdProducto() <= 0) {
                producto.setIdProducto(idCounter++);
            }
            return productos.add(producto);
        }
        return false;
    }

    public List<Producto> obtenerTodos() {
        return new ArrayList<>(productos);
    }

    public Producto obtenerPorId(int id) {
        return productos.stream()
                .filter(p -> p.getIdProducto() == id)
                .findFirst()
                .orElse(null);
    }

    public Producto obtenerPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        return productos.stream()
                .filter(p -> codigo.equalsIgnoreCase(p.getCodigo()))
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Producto producto) {
        if (producto == null || producto.getIdProducto() <= 0) return false;

        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getIdProducto() == producto.getIdProducto()) {
                productos.set(i, producto);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int id) {
        return productos.removeIf(p -> p.getIdProducto() == id);
    }

    // ==================== MÉTODOS EXTRA ====================
    private void cargarDatosPrueba() {
        Producto p1 = new Producto("P001", "Refrigerador", "LG", "X200",
                1, 1200, 1500, 10, 2);
        p1.setDescripcion("Refrigerador grande");
        p1.setStockMaximo(20);
        p1.setUnidadMedida("UNIDAD");
        p1.setActivo(true);
        p1.setGarantiaMeses(24);
        p1.setUbicacionAlmacen("Bodega A");
        agregar(p1);

        Producto p2 = new Producto("P002", "Televisor", "Samsung", "QLED55",
                2, 800, 1200, 5, 1);
        p2.setDescripcion("Televisor 55 pulgadas");
        p2.setStockMaximo(15);
        p2.setUnidadMedida("UNIDAD");
        p2.setActivo(true);
        p2.setGarantiaMeses(12);
        p2.setUbicacionAlmacen("Bodega B");
        agregar(p2);
    }

    public List<Producto> obtenerActivos() {
        List<Producto> activos = new ArrayList<>();
        for (Producto p : productos) {
            if (p.isActivo()) {
                activos.add(p);
            }
        }
        return activos;
    }

    public List<Producto> buscarPorNombreOMarca(String texto) {
        List<Producto> resultado = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) return resultado;

        String query = texto.toLowerCase();
        for (Producto p : productos) {
            if ((p.getNombre() != null && p.getNombre().toLowerCase().contains(query)) ||
                    (p.getMarca() != null && p.getMarca().toLowerCase().contains(query))) {
                resultado.add(p);
            }
        }
        return resultado;
    }

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
