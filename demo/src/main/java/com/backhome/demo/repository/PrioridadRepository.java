package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Prioridad;
import com.backhome.demo.model.Prioridad.Estado;

@Repository
public interface PrioridadRepository extends JpaRepository<Prioridad, Integer> {

    List<Prioridad> findAllByOrderByNivelAsc();

    List<Prioridad> findByEstadoOrderByNivelAsc(Estado estado);

    Optional<Prioridad> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdPrioridadNot(
            String nombre,
            Integer idPrioridad
    );
}
