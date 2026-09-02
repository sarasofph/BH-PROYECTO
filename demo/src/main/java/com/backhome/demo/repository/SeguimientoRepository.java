package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.Seguimiento.EstadoModeracion;
import com.backhome.demo.model.Seguimiento.EstadoSeguimiento;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {

    List<Seguimiento> findAllByOrderByIdSeguimientoDesc();

    List<Seguimiento> findByTituloContainingIgnoreCaseOrderByIdSeguimientoDesc(
            String titulo
    );

    List<Seguimiento> findByEstadoSeguimientoOrderByIdSeguimientoDesc(
            EstadoSeguimiento estadoSeguimiento
    );

    List<Seguimiento> findByEstadoModeracionOrderByIdSeguimientoDesc(
            EstadoModeracion estadoModeracion
    );

    List<Seguimiento> findByCliente_IdClienteOrderByIdSeguimientoDesc(
            Integer idCliente
    );

    List<Seguimiento> findByAnimal_IdAnimalOrderByIdSeguimientoDesc(
            Integer idAnimal
    );

    List<Seguimiento> findByPrioridad_IdPrioridadOrderByIdSeguimientoDesc(
            Integer idPrioridad
    );

    long countByEstadoSeguimiento(
            EstadoSeguimiento estadoSeguimiento
    );

    long countByEstadoModeracion(
            EstadoModeracion estadoModeracion
    );

    long countByCliente_IdCliente(
            Integer idCliente
    );

    long countByPrioridad_IdPrioridad(
            Integer idPrioridad
    );
}