package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.HistorialEstadoSeguimiento;

@Repository
public interface HistorialEstadoSeguimientoRepository
        extends JpaRepository<HistorialEstadoSeguimiento, Integer> {

    List<HistorialEstadoSeguimiento> findBySeguimiento_IdSeguimientoOrderByFechaCambioDesc(
            Integer idSeguimiento
    );
}
