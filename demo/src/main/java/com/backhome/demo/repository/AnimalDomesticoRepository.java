package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.AnimalDomestico;

@Repository
public interface AnimalDomesticoRepository
        extends JpaRepository<AnimalDomestico, Integer> {

    List<AnimalDomestico> findAllByOrderByIdAnimalDDesc();

    Optional<AnimalDomestico> findByAnimal_IdAnimal(
            Integer idAnimal
    );

    List<AnimalDomestico> findByEspecieContainingIgnoreCase(
            String especie
    );

    List<AnimalDomestico> findByRazaContainingIgnoreCase(
            String raza
    );

    boolean existsByAnimal_IdAnimal(
            Integer idAnimal
    );
}