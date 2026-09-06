package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.SeguimientoEncontrado;

@Repository
public interface SeguimientoEncontradoRepository
        extends JpaRepository<SeguimientoEncontrado, Integer> {

    List<SeguimientoEncontrado> findAllByOrderByIdEncontradoDesc();

    Optional<SeguimientoEncontrado> findBySeguimiento_IdSeguimiento(
            Integer idSeguimiento
    );

    boolean existsBySeguimiento_IdSeguimiento(Integer idSeguimiento);
}