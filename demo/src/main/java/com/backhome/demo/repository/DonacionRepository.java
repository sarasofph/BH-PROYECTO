package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Donacion;

@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Integer> {

    List<Donacion> findAllByOrderByIdDonacionDesc();

    List<Donacion> findByCliente_IdClienteOrderByIdDonacionDesc(
            Integer idCliente
    );
}