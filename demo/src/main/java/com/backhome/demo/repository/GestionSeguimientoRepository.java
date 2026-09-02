package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.GestionSeguimiento;

@Repository
public interface GestionSeguimientoRepository
        extends JpaRepository<GestionSeguimiento, Integer> {

    List<GestionSeguimiento> findBySeguimiento_IdSeguimientoOrderByFechaSeguimientoDesc(
            Integer idSeguimiento
    );

    List<GestionSeguimiento> findByAdministrador_IdAdminOrderByFechaSeguimientoDesc(
            Integer idAdmin
    );
}
