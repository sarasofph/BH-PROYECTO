package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.HistorialRefugio;

@Repository
public interface HistorialRefugioRepository
        extends JpaRepository<HistorialRefugio, Integer> {

    List<HistorialRefugio> findByIngreso_IdIngresoOrderByFechaCambioDesc(
            Integer idIngreso
    );
}
