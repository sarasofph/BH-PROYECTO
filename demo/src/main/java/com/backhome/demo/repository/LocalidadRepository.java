package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Localidad;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Integer> {

    List<Localidad> findAllByOrderByNombreAsc();

    List<Localidad> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

}
