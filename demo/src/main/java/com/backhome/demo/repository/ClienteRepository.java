package com.backhome.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backhome.demo.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    boolean existsByPersona_IdPersona(Integer idPersona);

    Optional<Cliente> findByPersona_IdPersona(Integer idPersona);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.persona.email) = LOWER(:email)")
    Optional<Cliente> findByPersonaEmailIgnoreCase(@Param("email") String email);
}