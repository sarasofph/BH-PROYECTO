package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.ActualizacionSeguimiento;

@Repository
public interface ActualizacionSeguimientoRepository
        extends JpaRepository<ActualizacionSeguimiento, Integer> {

    List<ActualizacionSeguimiento> findBySeguimiento_IdSeguimientoOrderByCreatedAtDesc(
            Integer idSeguimiento
    );
}
