package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Consejo;

@Repository
public interface ConsejoRepository extends JpaRepository<Consejo, Long> {

    List<Consejo> findAllByOrderByIdDesc();

    List<Consejo> findByTituloContainingIgnoreCaseOrderByIdDesc(
            String titulo
    );
}
