package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.SeguimientoPerdido;

@Repository
public interface SeguimientoPerdidoRepository
        extends JpaRepository<SeguimientoPerdido, Integer> {

    List<SeguimientoPerdido> findAllByOrderByIdSeguimientoPerdidoDesc();

    Optional<SeguimientoPerdido> findBySeguimiento_IdSeguimiento(
            Integer idSeguimiento
    );

    boolean existsBySeguimiento_IdSeguimiento(Integer idSeguimiento);
}
