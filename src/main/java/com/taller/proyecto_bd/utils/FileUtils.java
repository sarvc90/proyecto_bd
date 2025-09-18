package com.taller.proyecto_bd.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utilidades para manejo de archivos.
 * Permite leer, escribir y eliminar archivos de texto o binarios.
 *
 * @author Sistema
 * @version 1.0
 */
public class FileUtils {

    /**
     * Escribe texto en un archivo (sobrescribe si ya existe).
     */
    public static void escribirArchivo(String ruta, String contenido) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            writer.write(contenido);
        }
    }

    /**
     * Escribe varias líneas en un archivo (sobrescribe si ya existe).
     */
    public static void escribirArchivo(String ruta, List<String> lineas) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (String linea : lineas) {
                writer.write(linea);
                writer.newLine();
            }
        }
    }

    /**
     * Lee todas las líneas de un archivo de texto.
     */
    public static List<String> leerArchivo(String ruta) throws IOException {
        return Files.readAllLines(Paths.get(ruta));
    }

    /**
     * Elimina un archivo si existe.
     */
    public static boolean eliminarArchivo(String ruta) {
        File archivo = new File(ruta);
        return archivo.exists() && archivo.delete();
    }

    /**
     * Verifica si un archivo existe.
     */
    public static boolean existeArchivo(String ruta) {
        return Files.exists(Paths.get(ruta));
    }

    /**
     * Copia un archivo de origen a destino.
     */
    public static void copiarArchivo(String origen, String destino) throws IOException {
        Files.copy(Paths.get(origen), Paths.get(destino));
    }

    /**
     * Añade contenido al final de un archivo (modo append).
     */
    public static void anexarArchivo(String ruta, String contenido) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) {
            writer.write(contenido);
            writer.newLine();
        }
    }
}
