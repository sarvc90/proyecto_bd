package com.taller.proyecto_bd.dao;

import com.taller.proyecto_bd.models.Cliente;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Cliente.
 * Maneja operaciones CRUD sobre clientes (en memoria por ahora).
 *
 * @author Sistema
 * @version 1.0
 */
public class ClienteDAO {
    // ==================== ATRIBUTOS ====================
    private List<Cliente> clientes;

    // ==================== CONSTRUCTOR ====================

    public ClienteDAO() {
        this.clientes = new ArrayList<>();
        cargarDatosPrueba();
    }

    // ==================== CRUD ====================

    /**
     * Agregar un nuevo cliente
     */
    public boolean agregar(Cliente cliente) {
        if (cliente != null && cliente.validarDatosObligatorios()) {
            return clientes.add(cliente);
        }
        return false;
    }

    /**
     * Obtener todos los clientes
     */
    public List<Cliente> obtenerTodos() {
        return new ArrayList<>(clientes);
    }

    /**
     * Buscar cliente por ID
     */
    public Cliente obtenerPorId(int id) {
        return clientes.stream()
                .filter(c -> c.getIdCliente() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Buscar cliente por cédula
     */
    public Cliente obtenerPorCedula(String cedula) {
        return clientes.stream()
                .filter(c -> c.getCedula().equalsIgnoreCase(cedula))
                .findFirst()
                .orElse(null);
    }

    /**
     * Actualizar un cliente existente
     */
    public boolean actualizar(Cliente cliente) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getIdCliente() == cliente.getIdCliente()) {
                clientes.set(i, cliente);
                return true;
            }
        }
        return false;
    }

    /**
     * Eliminar cliente por ID
     */
    public boolean eliminar(int id) {
        return clientes.removeIf(c -> c.getIdCliente() == id);
    }

    // ==================== MÉTODOS EXTRA ====================

    /**
     * Cargar datos de prueba para testeo
     */
    private void cargarDatosPrueba() {
        clientes.add(new Cliente(1, "1234567890", "Carlos", "Pérez",
                "Calle 123", "3101112233", "carlos@mail.com",
                null, true, 1000, 200));
        clientes.add(new Cliente(2, "1098765432", "Ana", "Gómez",
                "Carrera 45", "3204445566", "ana@mail.com",
                null, true, 2000, 0));
    }

    /**
     * Buscar clientes activos
     */
    public List<Cliente> obtenerActivos() {
        List<Cliente> activos = new ArrayList<>();
        for (Cliente c : clientes) {
            if (c.isActivo()) {
                activos.add(c);
            }
        }
        return activos;
    }

    /**
     * Buscar clientes por nombre o apellido
     */
    public List<Cliente> buscarPorNombre(String texto) {
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : clientes) {
            if (c.getNombreCompleto().toLowerCase().contains(texto.toLowerCase())) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
