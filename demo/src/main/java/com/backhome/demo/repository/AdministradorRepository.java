package com.backhome.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.backhome.demo.model.Administrador;

public interface AdministradorRepository
        extends JpaRepository<Administrador, Integer> {

    boolean existsByPersona_IdPersona(Integer idPersona);

    Optional<Administrador> findByPersona_IdPersona(Integer idPersona);
}