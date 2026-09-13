package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.HistorialEstadoCustodia;

@Repository
public interface HistorialEstadoCustodiaRepository
        extends JpaRepository<HistorialEstadoCustodia, Integer> {

    List<HistorialEstadoCustodia> findBySeguimientoEncontrado_IdEncontradoOrderByFechaCambioDesc(
            Integer idEncontrado
    );
}