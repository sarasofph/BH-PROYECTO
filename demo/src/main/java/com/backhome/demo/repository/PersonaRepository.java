package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    Optional<Persona> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);

    long countByEstado(EstadoPersona estado);

    List<Persona> findAllByOrderByIdPersonaDesc();

    List<Persona> findByEstadoOrderByIdPersonaDesc(
            EstadoPersona estado
    );

    @Query("""
        SELECT p
        FROM Persona p
        WHERE
            LOWER(p.primerNombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.segundoNombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.primerApellido) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.segundoApellido) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.email) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.numeroTel) LIKE LOWER(CONCAT('%', :texto, '%'))
        ORDER BY p.idPersona DESC
    """)
    List<Persona> buscarPorTexto(
            @Param("texto") String texto
    );

    @Query("""
        SELECT p
        FROM Persona p
        WHERE
            p.estado = :estado
            AND (
                LOWER(p.primerNombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.segundoNombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.primerApellido) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.segundoApellido) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.email) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.numeroTel) LIKE LOWER(CONCAT('%', :texto, '%'))
            )
        ORDER BY p.idPersona DESC
    """)
    List<Persona> buscarPorTextoYEstado(
            @Param("texto") String texto,
            @Param("estado") EstadoPersona estado
    );
}