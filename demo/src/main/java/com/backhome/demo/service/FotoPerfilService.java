package com.backhome.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FotoPerfilService {

    private final Path directorio =
            Paths.get("uploads/perfiles");

    public String guardarFoto(
            MultipartFile archivo,
            String fotoAnterior) {

        if (archivo == null || archivo.isEmpty()) {
            return fotoAnterior;
        }

        String tipo = archivo.getContentType();

        if (tipo == null || !tipo.startsWith("image/")) {
            throw new RuntimeException(
                    "El archivo seleccionado no es una imagen."
            );
        }

        try {

            Files.createDirectories(directorio);

            /*
             * Primero eliminamos la foto anterior,
             * si existe.
             */
            eliminarFoto(fotoAnterior);

            String nombreOriginal =
                    archivo.getOriginalFilename();

            String extension = "";

            if (nombreOriginal != null
                    && nombreOriginal.contains(".")) {

                extension = nombreOriginal.substring(
                        nombreOriginal.lastIndexOf(".")
                ).toLowerCase();
            }

            String nombreArchivo =
                    UUID.randomUUID() + extension;

            Path destino =
                    directorio.resolve(nombreArchivo);

            Files.copy(
                    archivo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/perfiles/" + nombreArchivo;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo guardar la foto de perfil.",
                    e
            );
        }
    }

    public void eliminarFoto(String rutaFoto) {

        if (rutaFoto == null
                || rutaFoto.isBlank()) {
            return;
        }

        try {

            /*
             * Solo trabajamos con rutas de nuestro
             * directorio de perfiles.
             */
            if (!rutaFoto.startsWith("/uploads/perfiles/")) {
                return;
            }

            String nombreArchivo =
                    rutaFoto.substring(
                            "/uploads/perfiles/".length()
                    );

            Path archivo =
                    directorio.resolve(nombreArchivo);

            Files.deleteIfExists(archivo);

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo eliminar la foto anterior.",
                    e
            );
        }
    }
}