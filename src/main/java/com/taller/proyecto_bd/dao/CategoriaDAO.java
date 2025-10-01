package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Categoria;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Categoria.
 * Maneja operaciones CRUD y jerarquía de categorías (en memoria por ahora).
 *
 * Implementado como Singleton para mantener consistencia en toda la app.
 *
 * @author Sistema
 * @version 1.1
 */
public class CategoriaDAO {
    // ==================== ATRIBUTOS ====================
    private List<Categoria> categorias;
    private static int idCounter = 1; // Contador para IDs en memoria
    private static CategoriaDAO instance; // instancia única

    // ==================== CONSTRUCTOR ====================
    private CategoriaDAO() {
        this.categorias = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== SINGLETON ====================
    public static synchronized CategoriaDAO getInstance() {
        if (instance == null) {
            instance = new CategoriaDAO();
        }
        return instance;
    }

    // ==================== CRUD ====================

    /**
     * Agregar una nueva categoría
     */
    public boolean agregar(Categoria categoria) {
        if (categoria != null && categoria.validarDatosObligatorios() && categoria.validarJerarquia()) {
            // Asignar ID si no tiene
            if (categoria.getIdCategoria() == 0) {
                categoria.setIdCategoria(idCounter++);
            }
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
        // Los IDs se asignan automáticamente al agregar
        Categoria c1 = new Categoria("CAT001", "Electrodomésticos",
                "Categoría principal de electrodomésticos", null, 1);
        c1.setActivo(true);
        agregar(c1);

        Categoria c2 = new Categoria("CAT002", "Refrigeradores",
                "Subcategoría de refrigeradores", c1.getIdCategoria(), 2);
        c2.setActivo(true);
        agregar(c2);

        Categoria c3 = new Categoria("CAT003", "Televisores",
                "Subcategoría de televisores", c1.getIdCategoria(), 2);
        c3.setActivo(true);
        agregar(c3);
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
