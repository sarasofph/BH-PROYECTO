package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Coincidencia;
import com.backhome.demo.model.Coincidencia.Estado;

@Repository
public interface CoincidenciaRepository
        extends JpaRepository<Coincidencia, Integer> {

    List<Coincidencia> findAllByOrderByIdCoincidenciaDesc();

    List<Coincidencia> findByEstadoOrderByIdCoincidenciaDesc(
            Estado estado
    );

    List<Coincidencia> findBySeguimientoPerdido_IdSeguimiento(
            Integer idSeguimiento
    );

    List<Coincidencia> findBySeguimientoEncontrado_IdSeguimiento(
            Integer idSeguimiento
    );

    boolean existsBySeguimientoPerdido_IdSeguimientoAndSeguimientoEncontrado_IdSeguimiento(
            Integer idPerdido,
            Integer idEncontrado
    );
}
