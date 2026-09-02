package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Animal;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Integer> {

    List<Animal> findAllByOrderByIdAnimalDesc();

    List<Animal> findByNombreContainingIgnoreCaseOrderByIdAnimalDesc(
            String nombre
    );

    List<Animal> findBySexoOrderByIdAnimalDesc(
            Animal.Sexo sexo
    );

    List<Animal> findByTamanoOrderByIdAnimalDesc(
            Animal.Tamano tamano
    );
}
