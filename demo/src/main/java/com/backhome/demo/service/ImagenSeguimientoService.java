package com.backhome.demo.service;

import com.backhome.demo.model.ImagenSeguimiento;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.repository.ImagenSeguimientoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagenSeguimientoService {

    private final ImagenSeguimientoRepository imagenSeguimientoRepository;

    private final Path directorio =
            Paths.get("uploads/seguimientos");

    public ImagenSeguimientoService(
            ImagenSeguimientoRepository imagenSeguimientoRepository) {

        this.imagenSeguimientoRepository =
                imagenSeguimientoRepository;
    }

    public void guardarImagenes(
            Seguimiento seguimiento,
            MultipartFile[] imagenes) {

        if (imagenes == null || imagenes.length == 0) {
            return;
        }

        try {

            Files.createDirectories(directorio);

            boolean esPrimeraImagen =
                    imagenSeguimientoRepository
                            .findBySeguimiento_IdSeguimiento(
                                    seguimiento.getIdSeguimiento()
                            )
                            .isEmpty();

            for (MultipartFile archivo : imagenes) {

                if (archivo == null || archivo.isEmpty()) {
                    continue;
                }

                String tipo = archivo.getContentType();

                if (tipo == null || !tipo.startsWith("image/")) {
                    continue;
                }

                String nombreOriginal =
                        archivo.getOriginalFilename();

                String extension = "";

                if (nombreOriginal != null
                        && nombreOriginal.contains(".")) {

                    extension =
                            nombreOriginal.substring(
                                    nombreOriginal.lastIndexOf(".")
                            );
                }

                String nombreArchivo =
                        UUID.randomUUID()
                                + extension.toLowerCase();

                Path destino =
                        directorio.resolve(nombreArchivo);

                Files.copy(
                        archivo.getInputStream(),
                        destino,
                        StandardCopyOption.REPLACE_EXISTING
                );

                ImagenSeguimiento imagen =
                        new ImagenSeguimiento();

                imagen.setSeguimiento(seguimiento);

                imagen.setRutaImagen(
                        "/uploads/seguimientos/"
                                + nombreArchivo
                );

                imagen.setImagenPrincipal(
                        esPrimeraImagen
                );

                imagenSeguimientoRepository.save(imagen);

                esPrimeraImagen = false;
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudieron guardar las imágenes.",
                    e
            );
        }
    }
}