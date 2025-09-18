package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Inventario;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para la entidad Inventario.
 * Maneja operaciones CRUD y consultas específicas sobre stock (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class InventarioDAO {
    // ==================== ATRIBUTOS ====================
    private List<Inventario> inventarios;

    // ==================== CONSTRUCTOR ====================

    public InventarioDAO() {
        this.inventarios = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    /** Agregar nuevo registro de inventario */
    public boolean agregar(Inventario inventario) {
        if (inventario != null && inventario.getIdProducto() > 0) {
            return inventarios.add(inventario);
        }
        return false;
    }

    /** Obtener todos los registros */
    public List<Inventario> obtenerTodos() {
        return new ArrayList<>(inventarios);
    }

    /** Buscar inventario por ID */
    public Inventario obtenerPorId(int idInventario) {
        return inventarios.stream()
                .filter(i -> i.getIdInventario() == idInventario)
                .findFirst()
                .orElse(null);
    }

    /** Buscar inventario por ID de producto */
    public Inventario obtenerPorProducto(int idProducto) {
        return inventarios.stream()
                .filter(i -> i.getIdProducto() == idProducto)
                .findFirst()
                .orElse(null);
    }

    /** Actualizar registro */
    public boolean actualizar(Inventario inventario) {
        for (int i = 0; i < inventarios.size(); i++) {
            if (inventarios.get(i).getIdInventario() == inventario.getIdInventario()) {
                inventarios.set(i, inventario);
                return true;
            }
        }
        return false;
    }

    /** Eliminar registro por ID */
    public boolean eliminar(int idInventario) {
        return inventarios.removeIf(i -> i.getIdInventario() == idInventario);
    }

    // ==================== MÉTODOS EXTRA ====================

    /** Obtener productos con bajo stock */
    public List<Inventario> obtenerBajoStock() {
        List<Inventario> resultado = new ArrayList<>();
        for (Inventario i : inventarios) {
            if (i.necesitaReposicion()) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    /** Obtener productos en sobre stock */
    public List<Inventario> obtenerSobreStock() {
        List<Inventario> resultado = new ArrayList<>();
        for (Inventario i : inventarios) {
            if (i.sobreStock()) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    /** Registrar entrada (aumentar stock) */
    public boolean registrarEntrada(int idProducto, int cantidad) {
        Inventario inv = obtenerPorProducto(idProducto);
        if (inv != null) {
            inv.registrarEntrada(cantidad);
            return true;
        }
        return false;
    }

    /** Registrar salida (disminuir stock) */
    public boolean registrarSalida(int idProducto, int cantidad) {
        Inventario inv = obtenerPorProducto(idProducto);
        if (inv != null) {
            return inv.registrarSalida(cantidad);
        }
        return false;
    }

    // ==================== DATOS DE PRUEBA ====================

    private void cargarDatosPrueba() {
        Inventario i1 = new Inventario(1, 1, 50, 5, 100, new Date());
        i1.setNombreProducto("Lavadora");
        i1.setCategoria("Electrodomésticos");

        Inventario i2 = new Inventario(2, 2, 10, 3, 50, new Date());
        i2.setNombreProducto("Televisor");
        i2.setCategoria("Electrónica");

        Inventario i3 = new Inventario(3, 3, 3, 5, 20, new Date());
        i3.setNombreProducto("Microondas");
        i3.setCategoria("Electrodomésticos");

        inventarios.add(i1);
        inventarios.add(i2);
        inventarios.add(i3);
    }
}
