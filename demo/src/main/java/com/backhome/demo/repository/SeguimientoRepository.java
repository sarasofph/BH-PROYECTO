package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.TipoSeguimiento;
import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.EstadoSeguimiento;

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

    @Query("""
        SELECT s
        FROM Seguimiento s
        WHERE s.estadoModeracion = :estadoModeracion
          AND (:titulo IS NULL OR :titulo = ''
               OR LOWER(s.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
          AND (:tipoSeguimiento IS NULL
               OR s.tipoSeguimiento = :tipoSeguimiento)
          AND (:estadoSeguimiento IS NULL
               OR s.estadoSeguimiento = :estadoSeguimiento)
          AND (:localidadId IS NULL
               OR s.lugar.localidad.idLocalidad = :localidadId)
          AND (:prioridadId IS NULL
               OR s.prioridad.idPrioridad = :prioridadId)
        ORDER BY s.idSeguimiento DESC
    """)
    List<Seguimiento> buscarSeguimientosPublicos(
            @Param("estadoModeracion")
            EstadoModeracion estadoModeracion,

            @Param("titulo")
            String titulo,

            @Param("tipoSeguimiento")
            TipoSeguimiento tipoSeguimiento,

            @Param("estadoSeguimiento")
            EstadoSeguimiento estadoSeguimiento,

            @Param("localidadId")
            Integer localidadId,

            @Param("prioridadId")
            Integer prioridadId
    );
}