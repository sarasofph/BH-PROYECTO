package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Refugio;
import com.backhome.demo.model.Refugio.Estado;

@Repository
public interface RefugioRepository extends JpaRepository<Refugio, Integer> {

    List<Refugio> findAllByOrderByIdRefugioDesc();

    List<Refugio> findByEstadoOrderByIdRefugioDesc(Estado estado);

    List<Refugio> findByNombreContainingIgnoreCaseOrderByIdRefugioDesc(
            String nombre
    );

    List<Refugio> findByLocalidad_IdLocalidadOrderByIdRefugioDesc(
            Integer idLocalidad
    );

    long countByEstado(Estado estado);
}
