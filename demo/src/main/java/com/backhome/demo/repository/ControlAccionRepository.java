package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.ControlAccion;

@Repository
public interface ControlAccionRepository
        extends JpaRepository<ControlAccion, Integer> {

    List<ControlAccion> findAllByOrderByCreatedAtDesc();
}
