package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.AnimalExotico;

@Repository
public interface AnimalExoticoRepository
        extends JpaRepository<AnimalExotico, Integer> {

    List<AnimalExotico> findAllByOrderByIdAnimalEDesc();

    Optional<AnimalExotico> findByAnimal_IdAnimal(
            Integer idAnimal
    );

    List<AnimalExotico> findByEspecieContainingIgnoreCase(
            String especie
    );

    boolean existsByAnimal_IdAnimal(
            Integer idAnimal
    );
}
