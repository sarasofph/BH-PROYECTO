package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.IngresoRefugio;
import com.backhome.demo.model.IngresoRefugio.Estado;

@Repository
public interface IngresoRefugioRepository
        extends JpaRepository<IngresoRefugio, Integer> {

    List<IngresoRefugio> findAllByOrderByIdIngresoDesc();

    List<IngresoRefugio> findByEstadoOrderByIdIngresoDesc(
            Estado estado
    );

    List<IngresoRefugio> findByRefugio_IdRefugioOrderByIdIngresoDesc(
            Integer idRefugio
    );

    List<IngresoRefugio> findBySeguimiento_IdSeguimientoOrderByIdIngresoDesc(
            Integer idSeguimiento
    );

    long countByEstado(Estado estado);

    @Query("""
        SELECT i
        FROM IngresoRefugio i
        JOIN FETCH i.seguimiento s
        JOIN FETCH s.animal
        WHERE i.refugio.idRefugio = :idRefugio
        ORDER BY i.idIngreso DESC
    """)
    List<IngresoRefugio> findIngresosConAnimal(
            @Param("idRefugio") Integer idRefugio
    );
}