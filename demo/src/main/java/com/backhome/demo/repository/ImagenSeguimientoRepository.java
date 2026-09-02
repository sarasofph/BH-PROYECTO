package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.ImagenSeguimiento;

@Repository
public interface ImagenSeguimientoRepository
        extends JpaRepository<ImagenSeguimiento, Integer> {

    List<ImagenSeguimiento> findBySeguimiento_IdSeguimiento(
            Integer idSeguimiento
    );

    Optional<ImagenSeguimiento> findBySeguimiento_IdSeguimientoAndImagenPrincipalTrue(
            Integer idSeguimiento
    );
}
