package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Categoria;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Categoria.
 * Maneja operaciones CRUD y jerarquía de categorías (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class CategoriaDAO {
    // ==================== ATRIBUTOS ====================
    private List<Categoria> categorias;

    // ==================== CONSTRUCTOR ====================

    public CategoriaDAO() {
        this.categorias = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    /**
     * Agregar una nueva categoría
     */
    public boolean agregar(Categoria categoria) {
        if (categoria != null && categoria.validarDatosObligatorios() && categoria.validarJerarquia()) {
            return categorias.add(categoria);
        }
        return false;
    }

    /**
     * Obtener todas las categorías
     */
    public List<Categoria> obtenerTodas() {
        return new ArrayList<>(categorias);
    }

    /**
     * Buscar categoría por ID
     */
    public Categoria obtenerPorId(int id) {
        return categorias.stream()
                .filter(c -> c.getIdCategoria() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Buscar categoría por código
     */
    public Categoria obtenerPorCodigo(String codigo) {
        return categorias.stream()
                .filter(c -> c.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    /**
     * Actualizar una categoría existente
     */
    public boolean actualizar(Categoria categoria) {
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getIdCategoria() == categoria.getIdCategoria()) {
                categorias.set(i, categoria);
                return true;
            }
        }
        return false;
    }

    /**
     * Eliminar una categoría por ID
     */
    public boolean eliminar(int id) {
        Categoria categoria = obtenerPorId(id);
        if (categoria != null && categoria.puedeEliminarse()) {
            return categorias.remove(categoria);
        }
        return false; // no se puede eliminar si tiene productos
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Cargar datos de prueba
     */
    private void cargarDatosPrueba() {
        categorias.add(new Categoria(1, "CAT001", "Electrodomésticos",
                "Categoría principal de electrodomésticos", true, null,
                1, null, "Electrodomésticos", 0));

        categorias.add(new Categoria(2, "CAT002", "Refrigeradores",
                "Subcategoría de refrigeradores", true, null,
                2, 1, "Electrodomésticos > Refrigeradores", 0));

        categorias.add(new Categoria(3, "CAT003", "Televisores",
                "Subcategoría de televisores", true, null,
                2, 1, "Electrodomésticos > Televisores", 0));
    }

    /**
     * Obtener categorías principales
     */
    public List<Categoria> obtenerPrincipales() {
        List<Categoria> principales = new ArrayList<>();
        for (Categoria c : categorias) {
            if (c.esCategoriaPrincipal()) {
                principales.add(c);
            }
        }
        return principales;
    }

    /**
     * Obtener subcategorías de una categoría padre
     */
    public List<Categoria> obtenerSubcategorias(int idCategoriaPadre) {
        List<Categoria> subs = new ArrayList<>();
        for (Categoria c : categorias) {
            if (c.getIdCategoriaPadre() != null && c.getIdCategoriaPadre() == idCategoriaPadre) {
                subs.add(c);
            }
        }
        return subs;
    }
}
