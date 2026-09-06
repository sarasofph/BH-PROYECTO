package com.backhome.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.EstadoCustodia;

@Repository
public interface EstadoCustodiaRepository
        extends JpaRepository<EstadoCustodia, Integer> {
}