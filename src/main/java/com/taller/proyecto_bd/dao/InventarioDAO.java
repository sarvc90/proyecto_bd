package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Inventario;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Inventario.
 * Maneja operaciones CRUD y consultas específicas sobre stock (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.3 - Correcciones mínimas en manejo de IDs y seguridad null
 */
public class InventarioDAO {
    // ==================== ATRIBUTOS ====================
    private final List<Inventario> inventarios;
    private static InventarioDAO instance; // Singleton
    private static int idCounter = 1; // Contador para IDs en memoria

    // ==================== CONSTRUCTOR ====================
    private InventarioDAO() {
        this.inventarios = new ArrayList<>();
        cargarDatosPrueba();
    }

    // Patrón Singleton
    public static InventarioDAO getInstance() {
        if (instance == null) {
            instance = new InventarioDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /** Agregar nuevo registro de inventario */
    public boolean agregar(Inventario inventario) {
        if (inventario != null && inventario.getIdProducto() > 0) {
            if (obtenerPorProducto(inventario.getIdProducto()) != null) {
                return false; // ya existe ese producto
            }
            // Asignar ID único si no tiene
            if (inventario.getIdInventario() <= 0) {
                inventario.setIdInventario(idCounter++);
            }
            return inventarios.add(inventario);
        }
        return false;
    }

    public List<Inventario> obtenerTodos() {
        return new ArrayList<>(inventarios);
    }

    public Inventario obtenerPorId(int idInventario) {
        return inventarios.stream()
                .filter(i -> i.getIdInventario() == idInventario)
                .findFirst()
                .orElse(null);
    }

    public Inventario obtenerPorProducto(int idProducto) {
        return inventarios.stream()
                .filter(i -> i.getIdProducto() == idProducto)
                .findFirst()
                .orElse(null);
    }

    public boolean actualizar(Inventario inventario) {
        if (inventario == null || inventario.getIdInventario() <= 0) return false;

        for (int i = 0; i < inventarios.size(); i++) {
            if (inventarios.get(i).getIdInventario() == inventario.getIdInventario()) {
                inventarios.set(i, inventario);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int idInventario) {
        return inventarios.removeIf(i -> i.getIdInventario() == idInventario);
    }

    public boolean eliminarPorProducto(int idProducto) {
        return inventarios.removeIf(i -> i.getIdProducto() == idProducto);
    }

    // ==================== MÉTODOS EXTRA ====================

    public List<Inventario> obtenerBajoStock() {
        List<Inventario> resultado = new ArrayList<>();
        for (Inventario i : inventarios) {
            if (i.necesitaReposicion()) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    public List<Inventario> obtenerSobreStock() {
        List<Inventario> resultado = new ArrayList<>();
        for (Inventario i : inventarios) {
            if (i.sobreStock()) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    public boolean registrarEntrada(int idProducto, int cantidad) {
        Inventario inv = obtenerPorProducto(idProducto);
        if (inv != null && cantidad > 0) {
            inv.registrarEntrada(cantidad);
            return actualizar(inv);
        }
        return false;
    }

    public boolean registrarSalida(int idProducto, int cantidad) {
        Inventario inv = obtenerPorProducto(idProducto);
        if (inv != null && cantidad > 0) {
            if (inv.registrarSalida(cantidad)) {
                return actualizar(inv);
            }
        }
        return false;
    }

    public boolean crearOActualizar(int idProducto, int stock) {
        if (idProducto <= 0) return false;

        Inventario inv = obtenerPorProducto(idProducto);
        if (inv != null) {
            inv.setCantidadActual(stock);
            return actualizar(inv);
        } else {
            Inventario nuevo = new Inventario(idProducto, stock, 5, 100);
            return agregar(nuevo);
        }
    }

    // ==================== DATOS DE PRUEBA ====================
    private void cargarDatosPrueba() {
        Inventario i1 = new Inventario(1, 50, 5, 100);
        i1.setNombreProducto("Lavadora");
        i1.setCategoria("Electrodomésticos");
        agregar(i1);

        Inventario i2 = new Inventario(2, 10, 3, 50);
        i2.setNombreProducto("Televisor");
        i2.setCategoria("Electrónica");
        agregar(i2);

        Inventario i3 = new Inventario(3, 3, 5, 20);
        i3.setNombreProducto("Microondas");
        i3.setCategoria("Electrodomésticos");
        agregar(i3);

        Inventario i4 = new Inventario(10, 20, 5, 50);
        i4.setNombreProducto("ProductoPrueba10");
        i4.setCategoria("Pruebas");
        agregar(i4);

        Inventario i5 = new Inventario(13, 25, 5, 50);
        i5.setNombreProducto("ProductoPrueba13");
        i5.setCategoria("Pruebas");
        agregar(i5);
    }
}
