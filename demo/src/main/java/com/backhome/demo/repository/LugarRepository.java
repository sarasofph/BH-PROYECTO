package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Lugar;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Integer> {

    List<Lugar> findAllByOrderByIdLugarDesc();

    List<Lugar> findByDireccionContainingIgnoreCaseOrderByIdLugarDesc(
            String direccion
    );

    List<Lugar> findByLocalidad_IdLocalidadOrderByIdLugarDesc(
            Integer idLocalidad
    );

    boolean existsByDireccionIgnoreCaseAndLocalidad_IdLocalidad(
            String direccion,
            Integer idLocalidad
    );
}